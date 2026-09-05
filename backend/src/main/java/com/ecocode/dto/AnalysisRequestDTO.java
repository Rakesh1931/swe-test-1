package com.ecocode.dto;

public class AnalysisRequestDTO extends StartAnalysisRequest {
    public AnalysisRequestDTO() {
        super();
    }

    public AnalysisRequestDTO(Long projectId, String sourcePath, String repoUrl, String branch) {
        super(projectId, sourcePath, repoUrl, branch);
    }
}
