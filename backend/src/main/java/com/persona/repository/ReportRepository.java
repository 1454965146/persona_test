package com.persona.repository;

import com.persona.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByReportCode(String reportCode);
    Optional<Report> findBySessionId(Long sessionId);
    List<Report> findByUserIdOrderByCreatedAtDesc(Long userId);
}
