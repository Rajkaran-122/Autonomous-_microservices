package com.ai.sre.ai.service;

import com.ai.sre.common.config.KafkaTopics;
import com.ai.sre.common.event.IncidentEvent;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Listens for resolved incidents and adds their context to the Vector Store
 * for future RAG queries.
 */
@Service
public class IncidentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(IncidentIngestionService.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public IncidentIngestionService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    @KafkaListener(
            topics = KafkaTopics.INCIDENT_EVENTS,
            groupId = KafkaTopics.GROUP_AI_ENGINE + "_ingestion"
    )
    public void ingestResolvedIncident(IncidentEvent event) {
        // Only ingest closed/resolved incidents
        if (!"RESOLVED".equals(event.status()) && !"CLOSED".equals(event.status())) {
            return;
        }

        log.info("Ingesting resolved incident into vector store for RAG memory: {}", event.incidentId());

        try {
            String historicalKnowledge = String.format(
                    "Service: %s\nIssue: %s\nDescription: %s\nSeverity: %s\n" +
                    "This incident was resolved successfully.",
                    event.serviceName(),
                    event.title(),
                    event.description() != null ? event.description() : "N/A",
                    event.severity()
            );

            TextSegment segment = TextSegment.from(historicalKnowledge, Metadata.from("incidentId", event.incidentId().toString()));
            dev.langchain4j.data.embedding.Embedding embedding = embeddingModel.embed(segment).content();
            
            embeddingStore.add(embedding, segment);
            
            log.info("Successfully ingested incident {} into vector memory.", event.incidentId());
        } catch (Exception e) {
            log.error("Failed to ingest incident {} into vector store: {}", event.incidentId(), e.getMessage(), e);
        }
    }
}
