package com.persona.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persona.ai.AiService;
import com.persona.ai.FallbackReportService;
import com.persona.model.Report;
import com.persona.model.TestSession;
import com.persona.model.User;
import com.persona.repository.ReportRepository;
import com.persona.repository.TestSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private final TestSessionRepository sessionRepository;
    private final ReportRepository reportRepository;
    private final AiService aiService;
    private final FallbackReportService fallback;
    private final ObjectMapper objectMapper;

    public ReportService(TestSessionRepository sessionRepository, ReportRepository reportRepository,
                         AiService aiService, FallbackReportService fallback, ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.reportRepository = reportRepository;
        this.aiService = aiService;
        this.fallback = fallback;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Map<String, Object> generateReport(String sessionCode, String nickname, User currentUser) {
        if (nickname == null || nickname.trim().isEmpty())
            throw new RuntimeException("昵称不能为空");
        String safeName = DimensionUtil.sanitize(nickname.trim());
        if (safeName.length() > 20)
            throw new RuntimeException("昵称最长20个字符");

        TestSession session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("测试会话不存在"));
        if (session.getUser() == null || !session.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权访问该测试会话");
        }
        if (reportRepository.findBySessionId(session.getId()).isPresent())
            throw new RuntimeException("该测试已生成过报告，请勿重复提交");

        Map<String, Double> scores = parseScores(session.getDimensionScoresJson());
        String personalityType = DimensionUtil.computeType(scores);

        Report report = new Report();
        report.setReportCode(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        report.setSession(session);
        report.setUser(currentUser);
        report.setNickname(safeName);
        report.setPersonalityType(personalityType);
        report.setDimensionScoresJson(session.getDimensionScoresJson());
        report.setPremiumUnlocked(false);
        report = reportRepository.save(report);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reportCode", report.getReportCode());
        result.put("nickname", safeName);
        result.put("personalityType", personalityType);
        result.put("dimensionScores", scores);
        result.put("premiumUnlocked", false);
        result.put("previewContent", buildPreview(safeName, personalityType, scores));
        return result;
    }

    public Map<String, Object> getReport(String reportCode, User currentUser) {
        Report report = reportRepository.findByReportCode(reportCode)
                .orElseThrow(() -> new RuntimeException("报告不存在"));
        if (report.getUser() == null || !report.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权访问该报告");
        }
        Map<String, Double> scores = parseScores(report.getDimensionScoresJson());
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("reportCode", report.getReportCode()); r.put("nickname", report.getNickname());
        r.put("personalityType", report.getPersonalityType());
        r.put("dimensionScores", scores);
        r.put("premiumUnlocked", Boolean.TRUE.equals(report.getPremiumUnlocked()));
        r.put("previewContent", buildPreview(report.getNickname(), report.getPersonalityType(), scores));
        if (Boolean.TRUE.equals(report.getPremiumUnlocked())) {
            r.put("content", report.getReportContent());
        } else {
            r.put("content", null);
        }
        return r;
    }

    @Transactional
    public void generatePremiumReport(Report report) {
        Map<String, Double> scores = parseScores(report.getDimensionScoresJson());
        String aiContent;
        try {
            aiContent = aiService.generateReport(scores, report.getNickname());
        } catch (Exception e) {
            log.warn("AI生成失败，使用本地模板降级: {}", e.getMessage());
            aiContent = fallback.generateReport(scores, report.getNickname());
        }
        report.setReportContent(aiContent);
        report.setPremiumUnlocked(true);
        reportRepository.save(report);
    }

    private String buildPreview(String nickname, String personalityType, Map<String, Double> scores) {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("EI", "外向/内向");
        labels.put("SN", "感觉/直觉");
        labels.put("TF", "思考/情感");
        labels.put("JP", "判断/感知");
        labels.put("EXTRA", "开放度");

        StringBuilder sb = new StringBuilder();
        sb.append("你当前的人格类型为 ").append(personalityType).append("。");
        sb.append("五个核心维度的表现分别为：");
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            Double value = scores.get(entry.getKey());
            sb.append(entry.getValue()).append(" ").append(value == null ? "3.0" : value).append(" 分；");
        }
        sb.append("解锁完整报告后，可获得 AI 深度解读、优势盲区、成长建议和社交建议。");
        return sb.toString();
    }

    private Map<String, Double> parseScores(String json) {
        try { return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {}); }
        catch (Exception e) { return new LinkedHashMap<>(); }
    }
}
