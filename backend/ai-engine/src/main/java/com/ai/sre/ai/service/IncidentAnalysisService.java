package com.ai.sre.ai.service;

import com.ai.sre.ai.guardrails.PolicyEngineService;
import com.ai.sre.ai.model.AiAnalysis;
import com.ai.sre.ai.repository.AiAnalysisRepository;
import com.ai.sre.common.config.KafkaTopics;
import com.ai.sre.common.event.AnalysisEvent;
import com.ai.sre.common.event.IncidentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzes incidents using LLM to identify root causes and recommend actions.
 */
@Service
public class IncidentAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(IncidentAnalysisService.class);

    private final ChatLanguageModel chatLanguageModel;
    private final AiAnalysisRepository analysisRepository;
    private final PolicyEngineService policyEngineService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // A prompt template guiding the LLM to output a specific JSON format
    private static final String PROMPT_TEMPLATE = """
            You are an expert Site Reliability Engineer (SRE).
            Analyze the following incident and provide root cause analysis and remediation recommendations.
            
            Incident Details:
            Title: {{title}}
            Description: {{description}}
            Severity: {{severity}}
            Service: {{serviceName}}
            
            Based on your knowledge of microservice architectures and common failure modes, provide an analysis in the exact JSON format below. Do not include markdown formatting like ```json. Just raw JSON.
            
            {
              "rootCause": "A detailed explanation of the likely root cause.",
              "summary": "A one sentence summary.",
              "confidenceScore": 85,
              "affectedServices": ["service-a", "service-b"],
              "recommendations": [
                {
                  "actionType": "POD_RESTART",
                  "target": "payment-service",
                  "description": "Restart the pods to clear connections",
                  "confidenceScore": 90,
                  "parameters": {
                    "namespace": "production"
                  }
                }
              ]
            }
            """;

    public IncidentAnalysisService(ChatLanguageModel chatLanguageModel,
                                   AiAnalysisRepository analysisRepository,
                                   PolicyEngineService policyEngineService,
                                   KafkaTemplate<String, Object> kafkaTemplate,
                                   ObjectMapper objectMapper) {
        this.chatLanguageModel = chatLanguageModel;
        this.analysisRepository = analysisRepository;
        this.policyEngineService = policyEngineService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaTopics.INCIDENT_EVENTS,
            groupId = KafkaTopics.GROUP_AI_ENGINE,
            concurrency = "2"
    )
    public void analyzeIncident(IncidentEvent event) {
        log.info("Starting AI analysis for incident: {}", event.incidentId());
        long startTime = System.currentTimeMillis();

        try {
            PromptTemplate promptTemplate = PromptTemplate.from(PROMPT_TEMPLATE);
            Prompt prompt = promptTemplate.apply(Map.of(
                    "title", event.title(),
                    "description", event.description() != null ? event.description() : "No description",
                    "severity", event.severity(),
                    "serviceName", event.serviceName()
            ));

            String responseText = chatLanguageModel.generate(prompt.text());
            String jsonOutput = extractJson(responseText);
            
            // Parse LLM output
            LLMOutput output = objectMapper.readValue(jsonOutput, LLMOutput.class);

            // Record DB
            AiAnalysis analysis = AiAnalysis.builder()
                    .incidentId(event.incidentId())
                    .rootCause(output.rootCause)
                    .summary(output.summary)
                    .affectedServices(output.affectedServices != null ? output.affectedServices.toArray(new String[0]) : new String[0])
                    .recommendations(objectMapper.writeValueAsString(output.recommendations))
                    .confidenceScore(output.confidenceScore)
                    .modelUsed("gpt-4o")
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .rawResponse(jsonOutput)
                    .build();

            analysisRepository.save(analysis);
            log.info("AI Analysis completed with {}% confidence", analysis.getConfidenceScore());

            // Build recommendations for event
            List<AnalysisEvent.Recommendation> recs = new ArrayList<>();
            if (output.recommendations != null) {
                for (LLMRecommendation lr : output.recommendations) {
                    recs.add(new AnalysisEvent.Recommendation(
                            lr.actionType, lr.target, lr.description, lr.confidenceScore, lr.parameters
                    ));
                }
            }

            AnalysisEvent analysisEvent = new AnalysisEvent(
                    UUID.randomUUID(),
                    analysis.getId(),
                    event.incidentId(),
                    null, // serviceId not strictly needed here
                    event.serviceName(),
                    output.rootCause,
                    output.summary,
                    output.affectedServices != null ? output.affectedServices : List.of(),
                    recs,
                    output.confidenceScore,
                    "gpt-4o",
                    0,
                    System.currentTimeMillis() - startTime,
                    false,
                    0,
                    Instant.now()
            );

            // Publish analysis results
            kafkaTemplate.send(KafkaTopics.AI_ANALYSIS_RESULTS, event.serviceName(), analysisEvent);
            
            // Send to Policy Engine
            policyEngineService.evaluateRecommendations(analysisEvent);

        } catch (Exception e) {
            log.error("AI Analysis failed for incident {}: {}", event.incidentId(), e.getMessage(), e);
        }
    }

    private String extractJson(String text) {
        // Simple extraction in case LLM adds markdown wrappers despite instructions
        Matcher matcher = Pattern.compile("\\{.*\\}", Pattern.DOTALL).matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return text;
    }

    // DTOs for JSON mapping
    private static class LLMOutput {
        public String rootCause;
        public String summary;
        public int confidenceScore;
        public List<String> affectedServices;
        public List<LLMRecommendation> recommendations;
    }

    private static class LLMRecommendation {
        public String actionType;
        public String target;
        public String description;
        public int confidenceScore;
        public Map<String, Object> parameters;
    }
}
