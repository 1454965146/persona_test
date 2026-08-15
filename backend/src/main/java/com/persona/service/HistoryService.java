package com.persona.service;

import com.persona.model.Comparison;
import com.persona.model.Report;
import com.persona.model.ShareLink;
import com.persona.model.User;
import com.persona.repository.ComparisonRepository;
import com.persona.repository.ReportRepository;
import com.persona.repository.ShareLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HistoryService {
    private final ReportRepository reportRepository;
    private final ShareLinkRepository shareLinkRepository;
    private final ComparisonRepository comparisonRepository;

    public HistoryService(ReportRepository reportRepository, ShareLinkRepository shareLinkRepository,
                          ComparisonRepository comparisonRepository) {
        this.reportRepository = reportRepository;
        this.shareLinkRepository = shareLinkRepository;
        this.comparisonRepository = comparisonRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getHistory(User user) {
        List<Report> reports = reportRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (reports.isEmpty()) return new ArrayList<>();

        List<Long> reportIds = reports.stream().map(Report::getId).collect(Collectors.toList());
        List<ShareLink> inviterLinks = shareLinkRepository.findByInviterReportIdIn(reportIds);
        List<ShareLink> inviteeLinks = shareLinkRepository.findByInviteeReportIdIn(reportIds);

        Map<Long, List<Map<String, Object>>> linkMap = new HashMap<>();
        for (ShareLink link : inviterLinks) {
            linkMap.computeIfAbsent(link.getInviterReport().getId(), k -> new ArrayList<>())
                    .add(linkView(link, "inviter"));
        }
        for (ShareLink link : inviteeLinks) {
            linkMap.computeIfAbsent(link.getInviteeReport().getId(), k -> new ArrayList<>())
                    .add(linkView(link, "invitee"));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Report report : reports) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("reportCode", report.getReportCode());
            item.put("nickname", report.getNickname());
            item.put("personalityType", report.getPersonalityType());
            item.put("createdAt", report.getCreatedAt() == null ? null : report.getCreatedAt().toString());
            item.put("links", linkMap.getOrDefault(report.getId(), new ArrayList<>()));
            result.add(item);
        }
        return result;
    }

    private Map<String, Object> linkView(ShareLink link, String role) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("shareCode", link.getShareCode());
        item.put("myRole", role);
        item.put("inviterName", link.getInviterName());
        item.put("inviterType", link.getInviterReport().getPersonalityType());
        item.put("status", link.getStatus());
        if ("inviter".equals(role)) {
            item.put("relationshipType", link.getRelationshipType());
            item.put("relationshipLabel", relationshipLabel(link.getRelationshipType()));
            item.put("allowInviteeView", Boolean.TRUE.equals(link.getVisibleToInvitee()));
        }
        if (link.getInviteeReport() != null) {
            item.put("inviteeName", link.getInviteeReport().getNickname());
            item.put("inviteeType", link.getInviteeReport().getPersonalityType());
            if ("inviter".equals(role) || Boolean.TRUE.equals(link.getVisibleToInvitee())) {
                Comparison comparison = findComparison(link);
                if (comparison != null) {
                    item.put("comparisonId", comparison.getId());
                    item.put("comparisonStatus", comparison.getStatus());
                }
            }
        }
        return item;
    }

    private Comparison findComparison(ShareLink link) {
        List<Comparison> comparisons = comparisonRepository.findByReportAIdOrReportBId(
                link.getInviterReport().getId(), link.getInviteeReport().getId());
        return comparisons.isEmpty() ? null : comparisons.get(0);
    }

    private String relationshipLabel(String type) {
        switch (type) {
            case "BROTHER": return "兄弟默契度";
            case "COUPLE": return "情侣契合度";
            case "FRIEND": return "朋友合拍度";
            case "COLLEAGUE": return "同事协作度";
            case "FAMILY": return "亲子亲密度";
            default: return "关系分析";
        }
    }
}
