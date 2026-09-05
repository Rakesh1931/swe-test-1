package com.ecocode.controller;

import com.ecocode.model.Report;
import com.ecocode.repository.ReportRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportRepository reportRepository;

    public ReportController(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @GetMapping("/{runId}")
    public ResponseEntity<?> getReport(@PathVariable Long runId, @RequestParam(defaultValue = "JSON") String format) {
        Optional<Report> reportOpt = reportRepository.findByAnalysisRunId(runId);
        if (reportOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Report report = reportOpt.get();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ecocode-report-" + runId + ".json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(report.getContent());
    }
}
