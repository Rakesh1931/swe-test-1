package com.ecocode.dto;

import com.ecocode.model.StaticIssue;

import java.util.List;

public class ComparisonDTO {
    private Long runAId;
    private Long runBId;
    private String runABranch;
    private String runBBranch;
    private Integer scoreA;
    private Integer scoreB;
    private Integer scoreChange;
    private List<MetricComparison> metrics;
    private List<StaticIssue> resolvedIssues;
    private List<StaticIssue> newIssues;
    private List<StaticIssue> unchangedIssues;

    public ComparisonDTO() {}

    public Long getRunAId() { return runAId; }
    public void setRunAId(Long runAId) { this.runAId = runAId; }

    public Long getRunBId() { return runBId; }
    public void setRunBId(Long runBId) { this.runBId = runBId; }

    public String getRunABranch() { return runABranch; }
    public void setRunABranch(String runABranch) { this.runABranch = runABranch; }

    public String getRunBBranch() { return runBBranch; }
    public void setRunBBranch(String runBBranch) { this.runBBranch = runBBranch; }

    public Integer getScoreA() { return scoreA; }
    public void setScoreA(Integer scoreA) { this.scoreA = scoreA; }

    public Integer getScoreB() { return scoreB; }
    public void setScoreB(Integer scoreB) { this.scoreB = scoreB; }

    public Integer getScoreChange() { return scoreChange; }
    public void setScoreChange(Integer scoreChange) { this.scoreChange = scoreChange; }

    public List<MetricComparison> getMetrics() { return metrics; }
    public void setMetrics(List<MetricComparison> metrics) { this.metrics = metrics; }

    public List<StaticIssue> getResolvedIssues() { return resolvedIssues; }
    public void setResolvedIssues(List<StaticIssue> resolvedIssues) { this.resolvedIssues = resolvedIssues; }

    public List<StaticIssue> getNewIssues() { return newIssues; }
    public void setNewIssues(List<StaticIssue> newIssues) { this.newIssues = newIssues; }

    public List<StaticIssue> getUnchangedIssues() { return unchangedIssues; }
    public void setUnchangedIssues(List<StaticIssue> unchangedIssues) { this.unchangedIssues = unchangedIssues; }

    public static class MetricComparison {
        private String metricName;
        private double valueA;
        private double valueB;
        private double percentChange;

        public MetricComparison() {}

        public MetricComparison(String metricName, double valueA, double valueB, double percentChange) {
            this.metricName = metricName;
            this.valueA = valueA;
            this.valueB = valueB;
            this.percentChange = percentChange;
        }

        public String getMetricName() { return metricName; }
        public void setMetricName(String metricName) { this.metricName = metricName; }

        public double getValueA() { return valueA; }
        public void setValueA(double valueA) { this.valueA = valueA; }

        public double getValueB() { return valueB; }
        public void setValueB(double valueB) { this.valueB = valueB; }

        public double getPercentChange() { return percentChange; }
        public void setPercentChange(double percentChange) { this.percentChange = percentChange; }
    }
}
