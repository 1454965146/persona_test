package com.persona.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persona.ai.AiService;
import com.persona.ai.FallbackReportService;
import com.persona.event.ComparisonRetryEvent;
import com.persona.model.Comparison;
import com.persona.model.Report;
import com.persona.model.ShareLink;
import com.persona.model.User;
import com.persona.repository.ComparisonRepository;
import com.persona.repository.ReportRepository;
import com.persona.repository.ShareLinkRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Service
public class CompareService {
    private static final Logger log = LoggerFactory.getLogger(CompareService.class);
    private final ReportRepository reportRepository;
    private final ComparisonRepository comparisonRepository;
    private final ShareLinkRepository shareLinkRepository;
    private final AiService aiService;
    private final FallbackReportService fallback;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public CompareService(ReportRepository reportRepository, ComparisonRepository comparisonRepository,
                          ShareLinkRepository shareLinkRepository, AiService aiService,
                          FallbackReportService fallback, ObjectMapper objectMapper,
                          ApplicationEventPublisher eventPublisher) {
        this.reportRepository = reportRepository;
        this.comparisonRepository = comparisonRepository;
        this.shareLinkRepository = shareLinkRepository;
        this.aiService = aiService;
        this.fallback = fallback;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Map<String, Object> generateComparison(String reportCodeA, String reportCodeB, String relationshipType, User currentUser) {
        Report reportA = reportRepository.findByReportCode(reportCodeA)
                .orElseThrow(() -> new RuntimeException("报告A不存在"));
        Report reportB = reportRepository.findByReportCode(reportCodeB)
                .orElseThrow(() -> new RuntimeException("报告B不存在"));
        if (reportA.getUser() == null || !reportA.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权操作该报告");
        }

        Map<String, Double> scoresA = parseScores(reportA.getDimensionScoresJson());
        Map<String, Double> scoresB = parseScores(reportB.getDimensionScoresJson());

        return doGenerate(reportA, reportB, scoresA, scoresB, relationshipType, currentUser);
    }

    @Transactional
    public void completeComparison(Long comparisonId) {
        Comparison comparison = comparisonRepository.findById(comparisonId)
                .orElseThrow(() -> new RuntimeException("对比记录不存在"));
        if ("COMPLETED".equals(comparison.getStatus())) return;

        comparison.setStatus("PROCESSING");
        comparison.setErrorMessage(null);
        comparison.setUpdatedAt(LocalDateTime.now());
        comparisonRepository.save(comparison);

        Report reportA = comparison.getReportA();
        Report reportB = comparison.getReportB();
        Map<String, Double> scoresA = parseScores(reportA.getDimensionScoresJson());
        Map<String, Double> scoresB = parseScores(reportB.getDimensionScoresJson());

        try {
            String aiContent = aiService.generateComparison(
                    scoresA, reportA.getPersonalityType(), reportA.getNickname(),
                    scoresB, reportB.getPersonalityType(), reportB.getNickname(),
                    comparison.getRelationshipType());
            comparison.setAnalysisContent(aiContent);
            comparison.setStatus("COMPLETED");
            comparison.setErrorMessage(null);
            comparison.setUpdatedAt(LocalDateTime.now());
        } catch (Exception e) {
            log.warn("AI对比生成失败，使用本地模板降级: {}", e.getMessage());
            try {
                String fallbackContent = fallback.generateComparison(
                        scoresA, reportA.getPersonalityType(), reportA.getNickname(),
                        scoresB, reportB.getPersonalityType(), reportB.getNickname(),
                        comparison.getRelationshipType());
                comparison.setAnalysisContent(fallbackContent);
                comparison.setStatus("COMPLETED");
                comparison.setErrorMessage(null);
            } catch (Exception fallbackError) {
                comparison.setStatus("FAILED");
                comparison.setErrorMessage("AI 分析暂时失败，请稍后重试");
            }
            comparison.setUpdatedAt(LocalDateTime.now());
        }
        comparisonRepository.save(comparison);
    }

    private Map<String, Object> doGenerate(Report reportA, Report reportB,
            Map<String, Double> scoresA, Map<String, Double> scoresB, String relationshipType, User owner) {
        Comparison existing = comparisonRepository
                .findFirstByReportAIdAndReportBIdAndRelationshipType(reportA.getId(), reportB.getId(), relationshipType)
                .orElse(null);
        if (existing != null) {
            if (!"COMPLETED".equals(existing.getStatus())) {
                existing.setStatus("PENDING");
                comparisonRepository.save(existing);
                eventPublisher.publishEvent(new ComparisonRetryEvent(this, existing.getId()));
            }
            return comparisonView(existing);
        }

        Comparison c = new Comparison();
        c.setReportA(reportA);
        c.setReportB(reportB);
        c.setNameA(reportA.getNickname());
        c.setNameB(reportB.getNickname());
        c.setRelationshipType(relationshipType);
        c.setOwnerUser(owner);
        c.setStatus("PENDING");
        c = comparisonRepository.save(c);
        eventPublisher.publishEvent(new ComparisonRetryEvent(this, c.getId()));

        Map<String, Object> result = comparisonView(c);
        result.put("nameA", c.getNameA());
        result.put("nameB", c.getNameB());
        result.put("typeA", reportA.getPersonalityType());
        result.put("typeB", reportB.getPersonalityType());
        return result;
    }

    private Map<String, Object> comparisonView(Comparison c) {
        return comparisonView(c, true);
    }

    private Map<String, Object> comparisonView(Comparison c, boolean exposeRelationshipType) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("comparisonId", c.getId());
        result.put("nameA", c.getNameA());
        result.put("nameB", c.getNameB());
        result.put("typeA", c.getReportA().getPersonalityType());
        result.put("typeB", c.getReportB().getPersonalityType());
        if (exposeRelationshipType) {
            result.put("relationshipType", c.getRelationshipType());
        }
        result.put("reportCodeA", c.getReportA().getReportCode());
        result.put("reportCodeB", c.getReportB().getReportCode());
        result.put("status", c.getStatus());
        result.put("errorMessage", c.getErrorMessage());
        result.put("content", c.getAnalysisContent());
        result.put("scoresA", parseScores(c.getReportA().getDimensionScoresJson()));
        result.put("scoresB", parseScores(c.getReportB().getDimensionScoresJson()));
        return result;
    }

    public Map<String, Object> getComparison(Long comparisonId, User currentUser) {
        Comparison c = comparisonRepository.findById(comparisonId)
                .orElseThrow(() -> new RuntimeException("对比记录不存在"));
        boolean isOwner = c.getOwnerUser() != null
                && c.getOwnerUser().getId().equals(currentUser.getId());
        boolean isInvitee = c.getReportB() != null
                && c.getReportB().getUser() != null
                && c.getReportB().getUser().getId().equals(currentUser.getId());
        if (!isOwner && isInvitee) {
            ShareLink link = shareLinkRepository
                    .findFirstByInviterReportIdAndInviteeReportIdAndRelationshipType(
                            c.getReportA().getId(),
                            c.getReportB().getId(),
                            c.getRelationshipType())
                    .orElse(null);
            if (link == null || !Boolean.TRUE.equals(link.getVisibleToInvitee())) {
                throw new RuntimeException("无权访问该对比结果");
            }
        } else if (!isOwner) {
            throw new RuntimeException("无权访问该对比结果");
        }
        return comparisonView(c, isOwner);
    }

    @Transactional
    public void retryComparison(Long comparisonId, User currentUser) {
        Comparison comparison = comparisonRepository.findById(comparisonId)
                .orElseThrow(() -> new RuntimeException("对比记录不存在"));
        if (comparison.getOwnerUser() == null || !comparison.getOwnerUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权操作该对比结果");
        }
        if ("COMPLETED".equals(comparison.getStatus())) return;
        comparison.setStatus("PENDING");
        comparison.setErrorMessage(null);
        comparison.setUpdatedAt(LocalDateTime.now());
        comparisonRepository.save(comparison);
        eventPublisher.publishEvent(new ComparisonRetryEvent(this, comparisonId));
    }

    private Map<String, Double> parseScores(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }
}
