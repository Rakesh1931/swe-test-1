package com.ecocode.dto;

import java.time.LocalDateTime;

public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private String repoUrl;
    private String language;
    private LocalDateTime createdAt;
    private Integer lastEcoScore;
    private int highIssues;
    private int mediumIssues;
    private int lowIssues;
    private int totalRuns;

    public ProjectDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getLastEcoScore() { return lastEcoScore; }
    public void setLastEcoScore(Integer lastEcoScore) { this.lastEcoScore = lastEcoScore; }

    public int getHighIssues() { return highIssues; }
    public void setHighIssues(int highIssues) { this.highIssues = highIssues; }

    public int getMediumIssues() { return mediumIssues; }
    public void setMediumIssues(int mediumIssues) { this.mediumIssues = mediumIssues; }

    public int getLowIssues() { return lowIssues; }
    public void setLowIssues(int lowIssues) { this.lowIssues = lowIssues; }

    public int getTotalRuns() { return totalRuns; }
    public void setTotalRuns(int totalRuns) { this.totalRuns = totalRuns; }
}
