package com.ecocode.repository;

import com.ecocode.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByAnalysisRunId(Long analysisRunId);
}
