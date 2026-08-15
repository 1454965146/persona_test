package com.persona.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "share_link")
public class ShareLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "share_code", unique = true, nullable = false, length = 10)
    private String shareCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_report_id", nullable = false)
    private Report inviterReport;

    @Column(name = "inviter_name", length = 50)
    private String inviterName;

    /** 兄弟/情侣/朋友/同事/亲子 */
    @Column(name = "relationship_type", length = 20)
    private String relationshipType;

    /** 被邀请人的 report，完测后回填 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_report_id")
    private Report inviteeReport;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getShareCode() { return shareCode; }
    public void setShareCode(String shareCode) { this.shareCode = shareCode; }
    public Report getInviterReport() { return inviterReport; }
    public void setInviterReport(Report inviterReport) { this.inviterReport = inviterReport; }
    public String getInviterName() { return inviterName; }
    public void setInviterName(String inviterName) { this.inviterName = inviterName; }
    public String getRelationshipType() { return relationshipType; }
    public void setRelationshipType(String relationshipType) { this.relationshipType = relationshipType; }
    public Report getInviteeReport() { return inviteeReport; }
    public void setInviteeReport(Report inviteeReport) { this.inviteeReport = inviteeReport; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
