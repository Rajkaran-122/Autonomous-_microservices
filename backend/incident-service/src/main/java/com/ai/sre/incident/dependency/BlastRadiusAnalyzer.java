package com.ai.sre.incident.dependency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Blast Radius Analyzer — performs BFS traversal on the service dependency graph
 * to determine which services are affected when a given service fails.
 *
 * Impact scoring formula:
 *   impactScore = criticalityWeight × trafficVolume × (1/depth)
 *
 * Criticality weights: CRITICAL=1.0, HIGH=0.7, MEDIUM=0.4, LOW=0.1
 *
 * This is a pure domain service with no framework dependencies — 
 * follows hexagonal architecture principles.
 */
@Service
public class BlastRadiusAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(BlastRadiusAnalyzer.class);

    private static final Map<String, Double> CRITICALITY_WEIGHTS = Map.of(
            "CRITICAL", 1.0,
            "HIGH", 0.7,
            "MEDIUM", 0.4,
            "LOW", 0.1
    );

    /**
     * Analyzes the blast radius starting from a failed service.
     *
     * @param failedServiceId  The service that has failed
     * @param adjacencyList    Map<ServiceId, List<Edge>> — downstream dependencies
     * @param serviceNames     Map<ServiceId, ServiceName> — for display purposes
     * @return BlastRadiusResult with all affected services, depth, and impact scores
     */
    public BlastRadiusResult analyze(UUID failedServiceId,
                                     Map<UUID, List<DependencyEdge>> adjacencyList,
                                     Map<UUID, String> serviceNames) {

        Set<UUID> visited = new HashSet<>();
        Queue<BfsNode> queue = new LinkedList<>();
        List<AffectedService> affectedServices = new ArrayList<>();

        visited.add(failedServiceId);
        queue.add(new BfsNode(failedServiceId, 0, List.of(serviceNames.getOrDefault(failedServiceId, "unknown"))));

        int maxDepth = 0;
        double totalImpactScore = 0;

        while (!queue.isEmpty()) {
            BfsNode current = queue.poll();

            // Get downstream dependents (services that depend ON the current service)
            List<DependencyEdge> dependents = adjacencyList.getOrDefault(current.serviceId(), List.of());

            for (DependencyEdge edge : dependents) {
                if (visited.contains(edge.targetServiceId())) continue;
                visited.add(edge.targetServiceId());

                int depth = current.depth() + 1;
                maxDepth = Math.max(maxDepth, depth);

                // Build impact path
                List<String> path = new ArrayList<>(current.path());
                path.add(serviceNames.getOrDefault(edge.targetServiceId(), "unknown"));

                // Calculate impact score
                double critWeight = CRITICALITY_WEIGHTS.getOrDefault(edge.criticality(), 0.4);
                double trafficFactor = Math.min(edge.avgRequestsPerMinute() / 100.0, 1.0);
                double depthPenalty = 1.0 / depth;
                double impactScore = critWeight * trafficFactor * depthPenalty * 100;

                totalImpactScore += impactScore;

                AffectedService affected = new AffectedService(
                        edge.targetServiceId(),
                        serviceNames.getOrDefault(edge.targetServiceId(), "unknown"),
                        depth,
                        path,
                        Math.round(impactScore * 100.0) / 100.0,
                        edge.dependencyType()
                );
                affectedServices.add(affected);

                queue.add(new BfsNode(edge.targetServiceId(), depth, path));
            }
        }

        // Sort by impact score descending
        affectedServices.sort(Comparator.comparingDouble(AffectedService::impactScore).reversed());

        BlastRadiusResult result = new BlastRadiusResult(
                failedServiceId,
                serviceNames.getOrDefault(failedServiceId, "unknown"),
                affectedServices,
                affectedServices.size(),
                maxDepth,
                Math.round(totalImpactScore * 100.0) / 100.0
        );

        log.info("Blast radius for {}: {} affected services, max depth={}, total impact={}",
                result.sourceServiceName(), result.totalAffectedCount(), maxDepth, result.totalImpactScore());

        return result;
    }

    // ==================== Domain Records ====================

    public record BlastRadiusResult(
            UUID sourceServiceId,
            String sourceServiceName,
            List<AffectedService> affectedServices,
            int totalAffectedCount,
            int maxDepth,
            double totalImpactScore
    ) {}

    public record AffectedService(
            UUID serviceId,
            String serviceName,
            int depth,
            List<String> impactPath,
            double impactScore,
            String dependencyType
    ) {}

    public record DependencyEdge(
            UUID targetServiceId,
            String dependencyType,
            String criticality,
            double avgRequestsPerMinute
    ) {}

    record BfsNode(UUID serviceId, int depth, List<String> path) {}
}
