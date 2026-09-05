package com.ecocode.controller;

import com.ecocode.dto.ProjectDTO;
import com.ecocode.model.*;
import com.ecocode.repository.*;
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

    private User getOrCreateDefaultUser(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return userRepository.findByEmail("dev@ecocode.io").orElseGet(() ->
                userRepository.save(new User("dev_user", "dev@ecocode.io", "password123", "DEVELOPER"))
        );
    }

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getProjects(Authentication auth) {
        User user = getOrCreateDefaultUser(auth);
        List<Project> projects = projectRepository.findAll();

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

            if (!runs.isEmpty()) {
                AnalysisRun latest = runs.get(0);
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
    public ResponseEntity<ProjectDTO> createProject(@RequestBody Project projectReq, Authentication auth) {
        User owner = getOrCreateDefaultUser(auth);
        projectReq.setOwner(owner);
        if (projectReq.getLanguage() == null) projectReq.setLanguage("Java");
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
    public ResponseEntity<?> getProjectDetails(@PathVariable Long id) {
        Optional<Project> projOpt = projectRepository.findById(id);
        if (projOpt.isEmpty()) return ResponseEntity.notFound().build();

        Project p = projOpt.get();
        List<AnalysisRun> runs = runRepository.findByProjectIdOrderByCreatedAtDesc(id);

        Map<String, Object> resp = new HashMap<>();
        resp.put("project", p);
        resp.put("runs", runs);

        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
