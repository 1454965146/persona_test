package com.persona.service;

import com.persona.event.ShareCompletedEvent;
import com.persona.model.Comparison;
import com.persona.model.Report;
import com.persona.model.ShareLink;
import com.persona.model.User;
import com.persona.repository.ComparisonRepository;
import com.persona.repository.ReportRepository;
import com.persona.repository.ShareLinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ShareService {
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();
    private final ShareLinkRepository shareLinkRepository;
    private final ReportRepository reportRepository;
    private final ComparisonRepository comparisonRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${share.expire-days:7}") private int expireDays;
    @Value("${share.code-length:6}") private int codeLength;

    public ShareService(ShareLinkRepository shareLinkRepository, ReportRepository reportRepository,
                        ComparisonRepository comparisonRepository, ApplicationEventPublisher eventPublisher) {
        this.shareLinkRepository = shareLinkRepository;
        this.reportRepository = reportRepository;
        this.comparisonRepository = comparisonRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Map<String, Object> createShare(String reportCode, String relationshipType, User currentUser) {
        Report report = reportRepository.findByReportCode(reportCode)
                .orElseThrow(() -> new RuntimeException("报告不存在"));
        if (report.getUser() == null || !report.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权操作该报告");
        }
        String shareCode = generateCode();
        ShareLink link = new ShareLink();
        link.setShareCode(shareCode);
        link.setInviterReport(report);
        link.setInviterName(report.getNickname());
        link.setRelationshipType(relationshipType);
        link.setStatus("ACTIVE");
        link.setExpiresAt(LocalDateTime.now().plusDays(expireDays));
        link = shareLinkRepository.save(link);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("shareCode", link.getShareCode());
        result.put("inviterName", link.getInviterName());
        result.put("relationshipType", relationshipType);
        result.put("relationshipLabel", relLabel(relationshipType));
        result.put("expiresAt", link.getExpiresAt().toString());
        return result;
    }

    public Map<String, Object> getShareInfo(String shareCode) {
        ShareLink link = shareLinkRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new RuntimeException("分享链接不存在"));
        if (!"ACTIVE".equals(link.getStatus())) throw new RuntimeException("分享链接已失效");
        if (LocalDateTime.now().isAfter(link.getExpiresAt())) { link.setStatus("EXPIRED"); shareLinkRepository.save(link); throw new RuntimeException("分享链接已过期"); }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("shareCode", link.getShareCode()); r.put("inviterName", link.getInviterName());
        return r;
    }

    @Transactional
    public Map<String, Object> bindInviteeReport(String shareCode, String inviteeReportCode, User currentUser) {
        ShareLink link = shareLinkRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new RuntimeException("分享链接不存在"));
        if (!"ACTIVE".equals(link.getStatus())) throw new RuntimeException("分享链接已失效");
        if (LocalDateTime.now().isAfter(link.getExpiresAt())) {
            link.setStatus("EXPIRED");
            shareLinkRepository.save(link);
            throw new RuntimeException("分享链接已过期");
        }
        Report inviteeReport = reportRepository.findByReportCode(inviteeReportCode)
                .orElseThrow(() -> new RuntimeException("报告不存在"));
        if (inviteeReport.getUser() == null || !inviteeReport.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权绑定该报告");
        }
        if (link.getInviteeReport() != null) {
            if (!link.getInviteeReport().getId().equals(inviteeReport.getId())) {
                throw new RuntimeException("该邀请码已完成绑定");
            }
            return bindResult(link, inviteeReport);
        }

        link.setInviteeReport(inviteeReport);
        link.setStatus("COMPLETED");
        shareLinkRepository.save(link);

        Comparison comparison = comparisonRepository
                .findFirstByReportAIdAndReportBIdAndRelationshipType(
                        link.getInviterReport().getId(), inviteeReport.getId(), link.getRelationshipType())
                .orElseGet(() -> {
                    Comparison c = new Comparison();
                    c.setReportA(link.getInviterReport());
                    c.setReportB(inviteeReport);
                    c.setNameA(link.getInviterReport().getNickname());
                    c.setNameB(inviteeReport.getNickname());
                    c.setRelationshipType(link.getRelationshipType());
                    c.setOwnerUser(link.getInviterReport().getUser());
                    c.setStatus("PENDING");
                    return comparisonRepository.save(c);
                });

        // 自动触发异步对比分析
        eventPublisher.publishEvent(new ShareCompletedEvent(
                this, comparison.getId(), shareCode));

        return bindResult(link, inviteeReport);
    }

    private Map<String, Object> bindResult(ShareLink link, Report inviteeReport) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("shareCode", link.getShareCode());
        result.put("inviterReportCode", link.getInviterReport().getReportCode());
        result.put("inviteeReportCode", inviteeReport.getReportCode());
        result.put("relationshipType", link.getRelationshipType());
        return result;
    }

    public List<Map<String, Object>> getLinksByReport(String reportCode, User currentUser) {
        Report report = reportRepository.findByReportCode(reportCode).orElseThrow(() -> new RuntimeException("报告不存在"));
        if (report.getUser() == null || !report.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权访问该报告");
        }
        List<ShareLink> asInviter = shareLinkRepository.findByInviterReportId(report.getId());
        List<ShareLink> asInvitee = shareLinkRepository.findByInviteeReportId(report.getId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (ShareLink l : asInviter) result.add(buildItem(l, "inviter"));
        for (ShareLink l : asInvitee) result.add(buildItem(l, "invitee"));
        return result;
    }

    private Map<String, Object> buildItem(ShareLink l, String role) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("shareCode", l.getShareCode()); item.put("myRole", role);
        // 测试方向仅对邀请者可见；被邀请者（invitee）视角不返回，留作后续开放模块
        if ("inviter".equals(role)) {
            item.put("relationshipType", l.getRelationshipType());
            item.put("relationshipLabel", relLabel(l.getRelationshipType()));
        }
        item.put("inviterName", l.getInviterName()); item.put("inviterType", l.getInviterReport().getPersonalityType());
        item.put("inviterReportCode", l.getInviterReport().getReportCode()); item.put("status", l.getStatus());
        if (l.getInviteeReport() != null) {
            item.put("inviteeName", l.getInviteeReport().getNickname());
            item.put("inviteeType", l.getInviteeReport().getPersonalityType());
            item.put("inviteeReportCode", l.getInviteeReport().getReportCode());
            List<Comparison> comps = comparisonRepository.findByReportAIdOrReportBId(l.getInviterReport().getId(), l.getInviteeReport().getId());
            if (!comps.isEmpty()) item.put("comparisonId", comps.get(0).getId());
            if (!comps.isEmpty()) item.put("comparisonStatus", comps.get(0).getStatus());
        }
        return item;
    }

    private String generateCode() { StringBuilder sb = new StringBuilder(codeLength); for (int i=0;i<codeLength;i++) sb.append(CHARS.charAt(random.nextInt(CHARS.length()))); return sb.toString(); }
    private String relLabel(String t) { switch(t){case"BROTHER":return"兄弟默契度";case"COUPLE":return"情侣契合度";case"FRIEND":return"朋友合拍度";case"COLLEAGUE":return"同事协作度";case"FAMILY":return"亲子亲密度";default:return"关系分析";} }
}
