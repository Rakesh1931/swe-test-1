package com.ecocode.service;

import com.ecocode.dto.ComparisonDTO;
import com.ecocode.model.AnalysisRun;
import com.ecocode.model.EcoScore;
import com.ecocode.model.RuntimeMetric;
import com.ecocode.model.StaticIssue;
import com.ecocode.repository.AnalysisRunRepository;
import com.ecocode.repository.EcoScoreRepository;
import com.ecocode.repository.RuntimeMetricRepository;
import com.ecocode.repository.StaticIssueRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VersionComparisonService {

    private final AnalysisRunRepository runRepository;
    private final StaticIssueRepository issueRepository;
    private final RuntimeMetricRepository metricRepository;
    private final EcoScoreRepository scoreRepository;

    public VersionComparisonService(AnalysisRunRepository runRepository,
                                     StaticIssueRepository issueRepository,
                                     RuntimeMetricRepository metricRepository,
                                     EcoScoreRepository scoreRepository) {
        this.runRepository = runRepository;
        this.issueRepository = issueRepository;
        this.metricRepository = metricRepository;
        this.scoreRepository = scoreRepository;
    }

    public ComparisonDTO compareRuns(Long runAId, Long runBId) {
        AnalysisRun runA = runRepository.findById(runAId).orElseThrow(() -> new IllegalArgumentException("Run A not found: " + runAId));
        AnalysisRun runB = runRepository.findById(runBId).orElseThrow(() -> new IllegalArgumentException("Run B not found: " + runBId));

        EcoScore scoreA = scoreRepository.findByAnalysisRunId(runAId).orElse(null);
        EcoScore scoreB = scoreRepository.findByAnalysisRunId(runBId).orElse(null);

        List<StaticIssue> issuesA = issueRepository.findByAnalysisRunId(runAId);
        List<StaticIssue> issuesB = issueRepository.findByAnalysisRunId(runBId);

        List<RuntimeMetric> metricsA = metricRepository.findByAnalysisRunId(runAId);
        List<RuntimeMetric> metricsB = metricRepository.findByAnalysisRunId(runBId);

        // Build metric comparisons
        Map<String, Double> mapA = buildMetricMap(metricsA);
        Map<String, Double> mapB = buildMetricMap(metricsB);

        List<ComparisonDTO.MetricComparison> metricDiffs = new ArrayList<>();
        Set<String> allMetricKeys = new HashSet<>(mapA.keySet());
        allMetricKeys.addAll(mapB.keySet());

        for (String key : allMetricKeys) {
            double valA = mapA.getOrDefault(key, 0.0);
            double valB = mapB.getOrDefault(key, 0.0);
            double pctChange = valA != 0.0 ? ((valB - valA) / valA) * 100.0 : 0.0;
            metricDiffs.add(new ComparisonDTO.MetricComparison(key, valA, valB, pctChange));
        }

        // Issue Diffs
        List<StaticIssue> resolvedIssues = new ArrayList<>();
        List<StaticIssue> newIssues = new ArrayList<>();
        List<StaticIssue> unchangedIssues = new ArrayList<>();

        Set<String> keysB = new HashSet<>();
        for (StaticIssue b : issuesB) {
            keysB.add(makeIssueKey(b));
        }

        Set<String> keysA = new HashSet<>();
        for (StaticIssue a : issuesA) {
            String key = makeIssueKey(a);
            keysA.add(key);
            if (keysB.contains(key)) {
                unchangedIssues.add(a);
            } else {
                resolvedIssues.add(a);
            }
        }

        for (StaticIssue b : issuesB) {
            if (!keysA.contains(makeIssueKey(b))) {
                newIssues.add(b);
            }
        }

        ComparisonDTO dto = new ComparisonDTO();
        dto.setRunAId(runAId);
        dto.setRunBId(runBId);
        dto.setRunABranch(runA.getBranch());
        dto.setRunBBranch(runB.getBranch());

        if (scoreA != null && scoreB != null) {
            dto.setScoreA(scoreA.getTotalScore());
            dto.setScoreB(scoreB.getTotalScore());
            dto.setScoreChange(scoreB.getTotalScore() - scoreA.getTotalScore());
        }

        dto.setMetrics(metricDiffs);
        dto.setResolvedIssues(resolvedIssues);
        dto.setNewIssues(newIssues);
        dto.setUnchangedIssues(unchangedIssues);

        return dto;
    }

    private Map<String, Double> buildMetricMap(List<RuntimeMetric> metrics) {
        Map<String, Double> map = new HashMap<>();
        for (RuntimeMetric m : metrics) {
            map.put(m.getMetricName(), m.getMetricValue());
        }
        return map;
    }

    private String makeIssueKey(StaticIssue issue) {
        return issue.getRuleId() + ":" + issue.getClassName() + ":" + issue.getMethodName() + ":" + issue.getLineNumber();
    }
}
