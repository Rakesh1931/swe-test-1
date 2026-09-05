package com.ecocode.dto;

import com.ecocode.model.EcoScore;
import com.ecocode.model.RuntimeMetric;
import com.ecocode.model.StaticIssue;

import java.time.LocalDateTime;
import java.util.List;

public class AnalysisResultDTO {
    private Long runId;
    private Long projectId;
    private String status;
    private String error;
    private EcoScore ecoScore;
    private List<StaticIssue> issues;
    private List<RuntimeMetric> metrics;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public AnalysisResultDTO() {}

    public Long getRunId() { return runId; }
    public void setRunId(Long runId) { this.runId = runId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public EcoScore getEcoScore() { return ecoScore; }
    public void setEcoScore(EcoScore ecoScore) { this.ecoScore = ecoScore; }

    public List<StaticIssue> getIssues() { return issues; }
    public void setIssues(List<StaticIssue> issues) { this.issues = issues; }

    public List<RuntimeMetric> getMetrics() { return metrics; }
    public void setMetrics(List<RuntimeMetric> metrics) { this.metrics = metrics; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
}
