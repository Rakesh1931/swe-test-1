package com.ecocode.controller;

import com.ecocode.dto.AnalysisResultDTO;
import com.ecocode.dto.ComparisonDTO;
import com.ecocode.dto.StartAnalysisRequest;
import com.ecocode.model.*;
import com.ecocode.repository.*;
import com.ecocode.service.AnalysisService;
import com.ecocode.service.VersionComparisonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final VersionComparisonService comparisonService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AnalysisRunRepository runRepository;
    private final StaticIssueRepository issueRepository;
    private final RuntimeMetricRepository metricRepository;
    private final EcoScoreRepository scoreRepository;
    private final ReportRepository reportRepository;

    public AnalysisController(AnalysisService analysisService,
                               VersionComparisonService comparisonService,
                               ProjectRepository projectRepository,
                               UserRepository userRepository,
                               AnalysisRunRepository runRepository,
                               StaticIssueRepository issueRepository,
                               RuntimeMetricRepository metricRepository,
                               EcoScoreRepository scoreRepository,
                               ReportRepository reportRepository) {
        this.analysisService = analysisService;
        this.comparisonService = comparisonService;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.runRepository = runRepository;
        this.issueRepository = issueRepository;
        this.metricRepository = metricRepository;
        this.scoreRepository = scoreRepository;
        this.reportRepository = reportRepository;
    }

    private User getOrCreateDefaultUser(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return userRepository.findByEmail("dev@ecocode.io").orElseGet(() ->
                userRepository.save(new User("dev_user", "dev@ecocode.io", "password123", "DEVELOPER"))
        );
    }

    @PostMapping("/start")
    public ResponseEntity<?> startAnalysis(@RequestBody StartAnalysisRequest req, Authentication auth) {
        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Provide either sourcePath or repoUrl"));
        }

        boolean hasSource = req.getSourcePath() != null && !req.getSourcePath().isBlank();
        boolean hasRepo   = req.getRepoUrl()    != null && !req.getRepoUrl().isBlank();

        if (!hasSource && !hasRepo) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Provide either sourcePath or repoUrl"));
        }

        Optional<Project> projOpt = projectRepository.findById(req.getProjectId());
        if (projOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Project not found with id: " + req.getProjectId()));
        }

        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            return ResponseEntity.status(401).body(Map.of("error", "You must be signed in to start an analysis."));
        }
        User user = (User) auth.getPrincipal();
        Project project = projOpt.get();

        if (project.getOwner() != null && !project.getOwner().getId().equals(user.getId()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied: you do not own this project."));
        }

        AnalysisRun run = analysisService.createRun(project, user, req.getSourcePath(), req.getRepoUrl(), req.getBranch());

        analysisService.executeAnalysisAsync(run.getId(), req.getRepoUrl(), req.getSourcePath());

        return ResponseEntity.ok(Map.of("analysisRunId", run.getId(), "status", run.getStatus()));
    }

    @GetMapping("/test-path")
    public ResponseEntity<Map<String, Object>> testPath(@RequestParam String path) {
        try {
            Path targetRoot = Paths.get(path);
            if (!Files.exists(targetRoot)) {
                Path parentRelative = Paths.get("..", path);
                if (Files.exists(parentRelative)) {
                    targetRoot = parentRelative;
                } else {
                    return ResponseEntity.ok(Map.of("exists", false, "error", "Path not found"));
                }
            }
            final Path finalRoot = targetRoot;
            List<String> javaFiles = Files.walk(finalRoot)
                    .filter(p -> p.toString().endsWith(".java"))
                    .map(p -> finalRoot.relativize(p).toString())
                    .limit(10)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "absolutePath", finalRoot.toAbsolutePath().toString(),
                    "javaFileCount", javaFiles.size(),
                    "sampleFiles", javaFiles
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{runId}/status")
    public ResponseEntity<?> getStatus(@PathVariable Long runId) {
        Optional<AnalysisRun> runOpt = runRepository.findById(runId);
        if (runOpt.isEmpty()) return ResponseEntity.notFound().build();

        AnalysisRun run = runOpt.get();
        int progress = "COMPLETED".equals(run.getStatus()) ? 100 : ("RUNNING".equals(run.getStatus()) ? 50 : 0);
        return ResponseEntity.ok(Map.of("runId", run.getId(), "status", run.getStatus(), "progress", progress));
    }

    @GetMapping("/{runId}/result")
    public ResponseEntity<?> getResult(@PathVariable Long runId) {
        Optional<AnalysisRun> runOpt = runRepository.findById(runId);
        if (runOpt.isEmpty()) return ResponseEntity.notFound().build();

        AnalysisRun run = runOpt.get();
        EcoScore score = scoreRepository.findByAnalysisRunId(runId).orElse(null);
        List<StaticIssue> issues = issueRepository.findByAnalysisRunId(runId);
        List<RuntimeMetric> metrics = metricRepository.findByAnalysisRunId(runId);

        AnalysisResultDTO dto = new AnalysisResultDTO();
        dto.setRunId(run.getId());
        dto.setProjectId(run.getProject().getId());
        dto.setStatus(run.getStatus());
        dto.setEcoScore(score);
        dto.setIssues(issues);
        dto.setMetrics(metrics);
        dto.setStartedAt(run.getStartedAt());
        dto.setFinishedAt(run.getFinishedAt());

        if ("FAILED".equals(run.getStatus())) {
            reportRepository.findByAnalysisRunId(runId).ifPresent(r -> {
                String content = r.getContent();
                if (content != null && content.contains("\"error\":\"")) {
                    int start = content.indexOf("\"error\":\"") + 9;
                    int end = content.indexOf("\"", start);
                    if (end > start) {
                        dto.setError(content.substring(start, end));
                    }
                }
            });
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/compare")
    public ResponseEntity<?> compareRuns(@RequestParam Long runA, @RequestParam Long runB) {
        try {
            ComparisonDTO comparison = comparisonService.compareRuns(runA, runB);
            return ResponseEntity.ok(comparison);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
