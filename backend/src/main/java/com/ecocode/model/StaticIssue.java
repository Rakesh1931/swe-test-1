package com.ecocode.model;

import jakarta.persistence.*;

@Entity
@Table(name = "static_issues")
public class StaticIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_run_id", nullable = false)
    private AnalysisRun analysisRun;

    @Column(name = "rule_id", nullable = false, length = 10)
    private String ruleId;

    @Column(nullable = false, length = 20)
    private String severity; // HIGH, MEDIUM, LOW

    @Column(nullable = false, length = 20)
    private String confidence = "MEDIUM";

    @Column(name = "file_name", length = 500)
    private String fileName;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "class_name", length = 255)
    private String className;

    @Column(name = "method_name", length = 255)
    private String methodName;

    @Column(name = "call_path", columnDefinition = "TEXT")
    private String callPath;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "trade_offs", columnDefinition = "TEXT")
    private String tradeOffs;

    public StaticIssue() {}

    public StaticIssue(AnalysisRun analysisRun, String ruleId, String severity, String confidence,
                       String fileName, Integer lineNumber, String className, String methodName,
                       String callPath, String explanation, String recommendation, String tradeOffs) {
        this.analysisRun = analysisRun;
        this.ruleId = ruleId;
        this.severity = severity;
        this.confidence = confidence != null ? confidence : "MEDIUM";
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.className = className;
        this.methodName = methodName;
        this.callPath = callPath;
        this.explanation = explanation;
        this.recommendation = recommendation;
        this.tradeOffs = tradeOffs;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AnalysisRun getAnalysisRun() { return analysisRun; }
    public void setAnalysisRun(AnalysisRun analysisRun) { this.analysisRun = analysisRun; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public String getCallPath() { return callPath; }
    public void setCallPath(String callPath) { this.callPath = callPath; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getTradeOffs() { return tradeOffs; }
    public void setTradeOffs(String tradeOffs) { this.tradeOffs = tradeOffs; }
}
