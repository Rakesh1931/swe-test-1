package com.ecocode.service;

import com.ecocode.analyzer.ProjectModel;
import com.ecocode.model.AnalysisRun;
import com.ecocode.model.EcoScore;
import com.ecocode.model.RuntimeMetric;
import com.ecocode.model.StaticIssue;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EcoScoreService {

    public EcoScore compute(AnalysisRun run, List<StaticIssue> issues, List<RuntimeMetric> metrics) {
        return compute(run, issues, metrics, null);
    }

    public EcoScore compute(AnalysisRun run, List<StaticIssue> issues, List<RuntimeMetric> metrics, ProjectModel model) {
        // 1. Static AST Score calculation (normalized against project size so it never flatlines to 0)
        long highCount = issues.stream().filter(i -> "HIGH".equalsIgnoreCase(i.getSeverity())).count();
        long medCount = issues.stream().filter(i -> "MEDIUM".equalsIgnoreCase(i.getSeverity())).count();
        long lowCount = issues.stream().filter(i -> "LOW".equalsIgnoreCase(i.getSeverity())).count();

        double penaltyPoints = (highCount * 3.8) + (medCount * 1.6) + (lowCount * 0.4);

        int totalUnits;
        if (model != null && !model.getClasses().isEmpty()) {
            int totalMethods = model.getClasses().stream().mapToInt(c -> c.getMethods().size()).sum();
            totalUnits = Math.max(12, totalMethods);
        } else {
            totalUnits = Math.max(15, (int)(issues.size() * 1.5));
        }

        double defectRatio = penaltyPoints / totalUnits;
        int staticScore;
        if (penaltyPoints == 0) {
            staticScore = 100;
        } else {
            staticScore = (int) Math.round(100.0 / (1.0 + 1.8 * defectRatio));
            staticScore = Math.max(5, Math.min(100, staticScore));
        }

        // 2. Metrics lookup
        Map<String, Double> metricMap = metrics.stream()
                .collect(Collectors.toMap(RuntimeMetric::getMetricName, RuntimeMetric::getMetricValue, (v1, v2) -> v1));

        double avgCpu = metricMap.getOrDefault("avg_cpu_pct", 20.0);
        double peakMem = metricMap.getOrDefault("peak_memory_mb", 95.0);
        double dbQueries = metricMap.getOrDefault("db_queries_per_req", 2.0);
        double payloadKb = metricMap.getOrDefault("payload_kb", 45.0);

        // 3. Component Scores (graded against industry green-software benchmarks)
        // cpuScore: 100 if avg_cpu <= 20%, linear to 0 at 95%
        int cpuScore = avgCpu <= 20.0 ? 100 : (int) Math.max(0, 100 - ((avgCpu - 20.0) / 75.0) * 100);

        // memoryScore: 100 if peak_mem <= 80MB, linear to 0 at 600MB
        int memoryScore = peakMem <= 80.0 ? 100 : (int) Math.max(0, 100 - ((peakMem - 80.0) / 520.0) * 100);

        // dbScore: 100 if db_queries_per_req <= 2.5, linear to 0 at 40
        int dbScore = dbQueries <= 2.5 ? 100 : (int) Math.max(0, 100 - ((dbQueries - 2.5) / 37.5) * 100);

        // networkScore: 100 if payload_kb <= 50, linear to 0 at 1000 KB
        int networkScore = payloadKb <= 50.0 ? 100 : (int) Math.max(0, 100 - ((payloadKb - 50.0) / 950.0) * 100);

        // 4. Overall Weighted Score: CPU (25%) + Memory (20%) + Network (20%) + Database (20%) + Static Code AST (15%)
        double weighted = (0.25 * cpuScore) + (0.20 * memoryScore) + (0.20 * networkScore) + (0.20 * dbScore) + (0.15 * staticScore);
        int totalScore = (int) Math.round(weighted);

        return new EcoScore(run, totalScore, cpuScore, memoryScore, dbScore, networkScore, staticScore);
    }
}
