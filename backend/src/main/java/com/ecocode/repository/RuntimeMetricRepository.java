package com.ecocode.repository;

import com.ecocode.model.RuntimeMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RuntimeMetricRepository extends JpaRepository<RuntimeMetric, Long> {
    List<RuntimeMetric> findByAnalysisRunId(Long analysisRunId);
}
