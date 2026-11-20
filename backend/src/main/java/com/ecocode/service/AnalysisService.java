package com.ecocode.service;

import com.ecocode.model.*;
import com.ecocode.repository.*;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
public class AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    private final AnalysisRunRepository runRepository;
    private final StaticIssueRepository issueRepository;
    private final RuntimeMetricRepository metricRepository;
    private final EcoScoreRepository scoreRepository;
    private final ReportRepository reportRepository;
    private final StaticAnalyzerService staticAnalyzerService;
    private final RuntimeProfilerService runtimeProfilerService;
    private final EcoScoreService ecoScoreService;

    public AnalysisService(AnalysisRunRepository runRepository,
                           StaticIssueRepository issueRepository,
                           RuntimeMetricRepository metricRepository,
                           EcoScoreRepository scoreRepository,
                           ReportRepository reportRepository,
                           StaticAnalyzerService staticAnalyzerService,
                           RuntimeProfilerService runtimeProfilerService,
                           EcoScoreService ecoScoreService) {
        this.runRepository = runRepository;
        this.issueRepository = issueRepository;
        this.metricRepository = metricRepository;
        this.scoreRepository = scoreRepository;
        this.reportRepository = reportRepository;
        this.staticAnalyzerService = staticAnalyzerService;
        this.runtimeProfilerService = runtimeProfilerService;
        this.ecoScoreService = ecoScoreService;
    }

    public AnalysisRun createRun(Project project, User user, String sourcePath, String repoUrl, String branch) {
        AnalysisRun run = new AnalysisRun(project, user, sourcePath, branch);
        if (repoUrl != null && !repoUrl.isEmpty()) {
            project.setRepoUrl(repoUrl);
        }
        return runRepository.save(run);
    }

    @Async
    public void executeAnalysisAsync(Long runId, String repoUrl, String sourcePath) {
        AnalysisRun run = runRepository.findById(runId).orElse(null);
        if (run == null) return;

        run.setStatus("RUNNING");
        run.setStartedAt(LocalDateTime.now());
        runRepository.save(run);

        String workingPath = sourcePath;

        // Validation & Source Resolution: NEVER fall back to sample project
        if (workingPath == null || workingPath.isBlank()) {
            if (repoUrl != null && !repoUrl.isBlank()) {
                File tempDir = new File(System.getProperty("java.io.tmpdir"), "ecocode-sandbox" + File.separator + "git-" + runId);
                try {
                    if (tempDir.exists()) {
                        deleteRecursively(tempDir);
                    }
                    tempDir.getParentFile().mkdirs();

                    String requestedBranch = (run.getBranch() != null && !run.getBranch().isBlank()) ? run.getBranch().trim() : null;
                    log.info("Cloning repository {} (branch: {}) into {}", repoUrl, requestedBranch != null ? requestedBranch : "default", tempDir.getAbsolutePath());

                    try {
                        var cloneCmd = Git.cloneRepository()
                                .setURI(repoUrl)
                                .setDirectory(tempDir);
                        if (requestedBranch != null) {
                            cloneCmd.setBranch(requestedBranch);
                        }
                        try (Git git = cloneCmd.call()) {
                            workingPath = tempDir.getAbsolutePath();
                        }
                    } catch (Exception branchEx) {
                        // If requested branch failed (e.g. main vs master), retry with default branch
                        if (tempDir.exists()) {
                            deleteRecursively(tempDir);
                        }
                        log.warn("Cloning specific branch failed ({}), retrying with default repository branch...", branchEx.getMessage());
                        try (Git git = Git.cloneRepository()
                                .setURI(repoUrl)
                                .setDirectory(tempDir)
                                .call()) {
                            workingPath = tempDir.getAbsolutePath();
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to clone git repository {}: {}", repoUrl, e.getMessage());
                    markFailed(run, "Cannot clone repository: " + e.getMessage());
                    return;
                }
            } else {
                markFailed(run, "No source path or repository URL provided.");
                return;
            }
        }

        // Verify the path actually exists
        Path projectRoot = Paths.get(workingPath);
        if (!Files.exists(projectRoot)) {
            // Check if relative to parent workspace
            Path parentRelative = Paths.get("..", workingPath);
            if (Files.exists(parentRelative)) {
                projectRoot = parentRelative;
                workingPath = projectRoot.toString();
            } else {
                markFailed(run, "Source path does not exist: " + workingPath);
                return;
            }
        }

        // Verify the directory contains .java files
        try (Stream<Path> stream = Files.walk(projectRoot)) {
            boolean hasJavaFiles = stream.anyMatch(p -> p.toString().endsWith(".java"));
            if (!hasJavaFiles) {
                markFailed(run, "No Java source files found in: " + workingPath);
                return;
            }
        } catch (Exception e) {
            markFailed(run, "Error accessing path: " + e.getMessage());
            return;
        }

        try {
            log.info("Starting static analysis for run {} on path: {}", runId, workingPath);

            // Step 4-7: Static Analysis
            StaticAnalyzerService.AnalysisResult analysisResult = staticAnalyzerService.analyze(workingPath);
            List<StaticIssue> issues = analysisResult.getIssues();
            for (StaticIssue issue : issues) {
                issue.setAnalysisRun(run);
                issueRepository.save(issue);
            }

            // Step 9-10: Runtime Profiling
            List<RuntimeMetric> metrics = runtimeProfilerService.profile(workingPath, run, analysisResult.getModel(), issues);
            for (RuntimeMetric metric : metrics) {
                metric.setAnalysisRun(run);
                metricRepository.save(metric);
            }

            // Step 11-12: EcoScore Computation
            EcoScore score = ecoScoreService.compute(run, issues, metrics, analysisResult.getModel());
            scoreRepository.save(score);

            // Step 13: Report JSON Generation
            String reportJson = String.format(
                    "{\"runId\":%d,\"ecoScore\":%d,\"issuesCount\":%d,\"metricsCount\":%d,\"status\":\"COMPLETED\"}",
                    runId, score.getTotalScore(), issues.size(), metrics.size()
            );
            Report report = new Report(run, "JSON", reportJson);
            reportRepository.save(report);

            run.setStatus("COMPLETED");
            run.setFinishedAt(LocalDateTime.now());
            runRepository.save(run);
            log.info("Analysis run {} completed successfully with score {}", runId, score.getTotalScore());

        } catch (Exception e) {
            log.error("Analysis run {} failed with exception: {}", runId, e.getMessage(), e);
            markFailed(run, "Analysis execution failed: " + e.getMessage());
        }
    }

    private void deleteRecursively(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    private void markFailed(AnalysisRun run, String reason) {
        log.warn("Marking analysis run {} as FAILED: {}", run.getId(), reason);
        run.setStatus("FAILED");
        run.setFinishedAt(LocalDateTime.now());
        runRepository.save(run);

        String json = String.format("{\"runId\":%d,\"status\":\"FAILED\",\"error\":\"%s\"}",
                run.getId(), reason.replace("\"", "\\\"").replace("\n", " "));
        Report report = new Report(run, "JSON", json);
        reportRepository.save(report);
    }
}
