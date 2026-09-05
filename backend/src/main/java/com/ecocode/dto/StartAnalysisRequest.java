package com.ecocode.dto;

public class StartAnalysisRequest {
    private Long projectId;
    private String sourcePath;
    private String repoUrl;
    private String branch;

    public StartAnalysisRequest() {}

    public StartAnalysisRequest(Long projectId, String sourcePath, String repoUrl, String branch) {
        this.projectId = projectId;
        this.sourcePath = sourcePath;
        this.repoUrl = repoUrl;
        this.branch = branch;
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    public String getBranch() {
        return (branch == null || branch.isBlank()) ? "main" : branch;
    }
    public void setBranch(String branch) { this.branch = branch; }
}
