package com.persona.repository;

import com.persona.model.Comparison;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComparisonRepository extends JpaRepository<Comparison, Long> {
    List<Comparison> findByReportAIdOrReportBId(Long reportAId, Long reportBId);
    Optional<Comparison> findFirstByReportAIdAndReportBIdAndRelationshipType(Long reportAId, Long reportBId, String relationshipType);
}
