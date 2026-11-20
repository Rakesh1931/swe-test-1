package com.ecocode.controller;

import com.ecocode.dto.ProjectDTO;
import com.ecocode.model.*;
import com.ecocode.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AnalysisRunRepository runRepository;
    private final EcoScoreRepository scoreRepository;
    private final StaticIssueRepository issueRepository;

    public ProjectController(ProjectRepository projectRepository,
                             UserRepository userRepository,
                             AnalysisRunRepository runRepository,
                             EcoScoreRepository scoreRepository,
                             StaticIssueRepository issueRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.runRepository = runRepository;
        this.scoreRepository = scoreRepository;
        this.issueRepository = issueRepository;
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getProjects(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        if (user == null) {
            // Unauthenticated requests receive an empty list to prevent data exposure
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Only return projects belonging to the logged-in user
        List<Project> projects = projectRepository.findByOwnerId(user.getId());

        List<ProjectDTO> dtos = projects.stream().map(p -> {
            ProjectDTO dto = new ProjectDTO();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setDescription(p.getDescription());
            dto.setRepoUrl(p.getRepoUrl());
            dto.setLanguage(p.getLanguage());
            dto.setCreatedAt(p.getCreatedAt());

            List<AnalysisRun> runs = runRepository.findByProjectIdOrderByCreatedAtDesc(p.getId());
            dto.setTotalRuns(runs.size());

            // Only report scores from completed runs
            Optional<AnalysisRun> latestCompleted = runs.stream()
                    .filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus()))
                    .findFirst();

            if (latestCompleted.isPresent()) {
                AnalysisRun latest = latestCompleted.get();
                scoreRepository.findByAnalysisRunId(latest.getId())
                        .ifPresent(s -> dto.setLastEcoScore(s.getTotalScore()));

                List<StaticIssue> issues = issueRepository.findByAnalysisRunId(latest.getId());
                dto.setHighIssues((int) issues.stream().filter(i -> "HIGH".equalsIgnoreCase(i.getSeverity())).count());
                dto.setMediumIssues((int) issues.stream().filter(i -> "MEDIUM".equalsIgnoreCase(i.getSeverity())).count());
                dto.setLowIssues((int) issues.stream().filter(i -> "LOW".equalsIgnoreCase(i.getSeverity())).count());
            }

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody Project projectReq, Authentication auth) {
        User owner = getAuthenticatedUser(auth);
        if (owner == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "You must be signed in to create a new project."));
        }

        if (projectReq.getName() == null || projectReq.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Project name is required."));
        }

        projectReq.setOwner(owner);
        if (projectReq.getLanguage() == null || projectReq.getLanguage().isBlank()) {
            projectReq.setLanguage("Java");
        }
        Project saved = projectRepository.save(projectReq);

        ProjectDTO dto = new ProjectDTO();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setDescription(saved.getDescription());
        dto.setRepoUrl(saved.getRepoUrl());
        dto.setLanguage(saved.getLanguage());
        dto.setCreatedAt(saved.getCreatedAt());
        dto.setTotalRuns(0);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectDetails(@PathVariable Long id, Authentication auth) {
        Optional<Project> projOpt = projectRepository.findById(id);
        if (projOpt.isEmpty()) return ResponseEntity.notFound().build();

        Project p = projOpt.get();
        User user = getAuthenticatedUser(auth);
        // Verify ownership
        if (user != null && p.getOwner() != null && !p.getOwner().getId().equals(user.getId()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: you do not own this project."));
        }

        List<AnalysisRun> runs = runRepository.findByProjectIdOrderByCreatedAtDesc(id);

        Map<String, Object> resp = new HashMap<>();
        resp.put("project", p);
        resp.put("runs", runs);

        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id, Authentication auth) {
        Optional<Project> projOpt = projectRepository.findById(id);
        if (projOpt.isEmpty()) return ResponseEntity.notFound().build();

        Project p = projOpt.get();
        User user = getAuthenticatedUser(auth);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required."));
        }
        if (p.getOwner() != null && !p.getOwner().getId().equals(user.getId()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied: cannot delete another user's project."));
        }

        projectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
