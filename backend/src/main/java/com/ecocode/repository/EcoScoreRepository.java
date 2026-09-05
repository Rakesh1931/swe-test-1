package com.ecocode.repository;

import com.ecocode.model.EcoScore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EcoScoreRepository extends JpaRepository<EcoScore, Long> {
    Optional<EcoScore> findByAnalysisRunId(Long analysisRunId);
}
