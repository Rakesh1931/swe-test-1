package com.ecocode.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eco_scores")
public class EcoScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_run_id", nullable = false, unique = true)
    private AnalysisRun analysisRun;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "cpu_score")
    private Integer cpuScore;

    @Column(name = "memory_score")
    private Integer memoryScore;

    @Column(name = "database_score")
    private Integer databaseScore;

    @Column(name = "network_score")
    private Integer networkScore;

    @Column(name = "static_score")
    private Integer staticScore;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt = LocalDateTime.now();

    public EcoScore() {}

    public EcoScore(AnalysisRun analysisRun, Integer totalScore, Integer cpuScore, Integer memoryScore,
                    Integer databaseScore, Integer networkScore, Integer staticScore) {
        this.analysisRun = analysisRun;
        this.totalScore = totalScore;
        this.cpuScore = cpuScore;
        this.memoryScore = memoryScore;
        this.databaseScore = databaseScore;
        this.networkScore = networkScore;
        this.staticScore = staticScore;
        this.computedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AnalysisRun getAnalysisRun() { return analysisRun; }
    public void setAnalysisRun(AnalysisRun analysisRun) { this.analysisRun = analysisRun; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public Integer getCpuScore() { return cpuScore; }
    public void setCpuScore(Integer cpuScore) { this.cpuScore = cpuScore; }

    public Integer getMemoryScore() { return memoryScore; }
    public void setMemoryScore(Integer memoryScore) { this.memoryScore = memoryScore; }

    public Integer getDatabaseScore() { return databaseScore; }
    public void setDatabaseScore(Integer databaseScore) { this.databaseScore = databaseScore; }

    public Integer getNetworkScore() { return networkScore; }
    public void setNetworkScore(Integer networkScore) { this.networkScore = networkScore; }

    public Integer getStaticScore() { return staticScore; }
    public void setStaticScore(Integer staticScore) { this.staticScore = staticScore; }

    public LocalDateTime getComputedAt() { return computedAt; }
    public void setComputedAt(LocalDateTime computedAt) { this.computedAt = computedAt; }
}
