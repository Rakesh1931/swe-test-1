package com.ecocode.repository;

import com.ecocode.model.StaticIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StaticIssueRepository extends JpaRepository<StaticIssue, Long> {
    List<StaticIssue> findByAnalysisRunId(Long analysisRunId);
}
