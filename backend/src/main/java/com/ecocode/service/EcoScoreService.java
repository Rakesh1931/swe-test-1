package com.ecocode.service;

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
        // 1. Static Score
        long highCount = issues.stream().filter(i -> "HIGH".equalsIgnoreCase(i.getSeverity())).count();
        long medCount = issues.stream().filter(i -> "MEDIUM".equalsIgnoreCase(i.getSeverity())).count();
        long lowCount = issues.stream().filter(i -> "LOW".equalsIgnoreCase(i.getSeverity())).count();

        int staticScore = (int) Math.max(0, 100 - (highCount * 15 + medCount * 7 + lowCount * 2));

        // 2. Metrics lookup
        Map<String, Double> metricMap = metrics.stream()
                .collect(Collectors.toMap(RuntimeMetric::getMetricName, RuntimeMetric::getMetricValue, (v1, v2) -> v1));

        double avgCpu = metricMap.getOrDefault("avg_cpu_pct", 25.0);
        double peakMem = metricMap.getOrDefault("peak_memory_mb", 120.0);
        double dbQueries = metricMap.getOrDefault("db_queries_per_req", 2.0);
        double payloadKb = metricMap.getOrDefault("payload_kb", 50.0);

        // 3. Component Scores
        // cpuScore: 100 if avg_cpu < 30%, linear to 0 at 100%
        int cpuScore = avgCpu < 30.0 ? 100 : (int) Math.max(0, 100 - ((avgCpu - 30.0) / 70.0) * 100);

        // memoryScore: 100 if peak_mem < 100MB, linear to 0 at 500MB
        int memoryScore = peakMem < 100.0 ? 100 : (int) Math.max(0, 100 - ((peakMem - 100.0) / 400.0) * 100);

        // dbScore: 100 if db_queries_per_req <= 3, linear to 0 at 100
        int dbScore = dbQueries <= 3.0 ? 100 : (int) Math.max(0, 100 - ((dbQueries - 3.0) / 97.0) * 100);

        // networkScore: 100 if payload_kb <= 100, linear to 0 at 5000 KB
        int networkScore = payloadKb <= 100.0 ? 100 : (int) Math.max(0, 100 - ((payloadKb - 100.0) / 4900.0) * 100);

        // 4. Overall Weighted Score
        double weighted = (0.25 * cpuScore) + (0.20 * memoryScore) + (0.20 * networkScore) + (0.20 * dbScore) + (0.15 * staticScore);
        int totalScore = (int) Math.round(weighted);

        return new EcoScore(run, totalScore, cpuScore, memoryScore, dbScore, networkScore, staticScore);
    }
}
