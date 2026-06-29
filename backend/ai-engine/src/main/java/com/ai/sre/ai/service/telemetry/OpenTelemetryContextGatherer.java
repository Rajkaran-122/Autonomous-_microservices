package com.ai.sre.ai.service.telemetry;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Primary
public class OpenTelemetryContextGatherer implements TelemetryContextGatherer {

    private static final Logger log = LoggerFactory.getLogger(OpenTelemetryContextGatherer.class);

    private final RestClient restClient;
    private final String prometheusUrl;
    private final String jaegerUrl;
    private final String githubUrl;
    private final String githubToken;

    public OpenTelemetryContextGatherer(
            RestClient.Builder restClientBuilder,
            @Value("${sre.telemetry.prometheus.url:http://prometheus:9090}") String prometheusUrl,
            @Value("${sre.telemetry.jaeger.url:http://jaeger:16686}") String jaegerUrl,
            @Value("${sre.telemetry.github.url:https://api.github.com}") String githubUrl,
            @Value("${sre.telemetry.github.token:}") String githubToken) {
        
        this.restClient = restClientBuilder.build();
        this.prometheusUrl = prometheusUrl;
        this.jaegerUrl = jaegerUrl;
        this.githubUrl = githubUrl;
        this.githubToken = githubToken;
    }

    @Override
    public String gatherStackTraces(String serviceName) {
        log.info("Fetching recent traces for service: {} from Jaeger", serviceName);
        long lookback = Instant.now().minus(15, ChronoUnit.MINUTES).toEpochMilli() * 1000; // microseconds
        
        String url = String.format("%s/api/traces?service=%s&tags={\"error\":\"true\"}&limit=5&start=%d", 
                jaegerUrl, serviceName, lookback);
        
        try {
            JsonNode response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("data") && response.get("data").isArray() && !response.get("data").isEmpty()) {
                // Simplified trace extraction for the LLM context
                StringBuilder traces = new StringBuilder("Found recent error traces in Jaeger:\n");
                for (JsonNode trace : response.get("data")) {
                    String traceId = trace.path("traceID").asText();
                    traces.append("- Trace ID: ").append(traceId).append("\n");
                    // In a full implementation, we would extract the specific "exception.stacktrace" tag from the spans.
                }
                return traces.toString();
            }
            return "No recent error traces found in Jaeger for " + serviceName + ".";
            
        } catch (RestClientException e) {
            log.warn("Failed to fetch traces from Jaeger: {}", e.getMessage());
            return "Unable to fetch stack traces: Jaeger connection failed.";
        }
    }

    @Override
    public String gatherRecentCommits(String serviceName) {
        if (githubToken.isEmpty()) {
            return "GitHub integration not configured. Skipping recent commit gathering.";
        }

        log.info("Fetching recent commits for repository matching service: {}", serviceName);
        // Assuming repo name matches service name for this example
        String url = String.format("%s/repos/internal-org/%s/commits?per_page=3", githubUrl, serviceName);

        try {
            JsonNode response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + githubToken)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.isArray() && !response.isEmpty()) {
                StringBuilder commits = new StringBuilder("Recent Commits:\n");
                for (JsonNode commitNode : response) {
                    String sha = commitNode.path("sha").asText().substring(0, 7);
                    String message = commitNode.path("commit").path("message").asText().split("\n")[0];
                    String author = commitNode.path("commit").path("author").path("name").asText();
                    commits.append(String.format("- %s: \"%s\" (Author: %s)\n", sha, message, author));
                }
                return commits.toString();
            }
            return "No recent commits found.";

        } catch (RestClientException e) {
            log.warn("Failed to fetch commits from GitHub: {}", e.getMessage());
            return "Unable to fetch recent commits: GitHub API connection failed.";
        }
    }

    @Override
    public String gatherMetrics(String serviceName) {
        log.info("Fetching anomalous metrics for service: {} from Prometheus", serviceName);
        
        // Fetching CPU usage as an example query
        String query = String.format("sum(rate(container_cpu_usage_seconds_total{container=\"%s\"}[5m])) by (container)", serviceName);
        String url = String.format("%s/api/v1/query?query=%s", prometheusUrl, query);

        try {
            JsonNode response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && "success".equals(response.path("status").asText())) {
                JsonNode results = response.path("data").path("result");
                if (results.isArray() && !results.isEmpty()) {
                    String value = results.get(0).path("value").get(1).asText();
                    return String.format("Prometheus Metrics: CPU Usage rate is %s", value);
                }
            }
            return "No anomalous metrics detected for " + serviceName + ".";
            
        } catch (RestClientException e) {
            log.warn("Failed to fetch metrics from Prometheus: {}", e.getMessage());
            return "Unable to fetch metrics: Prometheus connection failed.";
        }
    }
}
