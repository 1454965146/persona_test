package com.persona.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comparison")
public class Comparison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id_a", nullable = false)
    private Report reportA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id_b", nullable = false)
    private Report reportB;

    @Column(name = "name_a", length = 50)
    private String nameA;

    @Column(name = "name_b", length = 50)
    private String nameB;

    @Column(name = "relationship_type", length = 20)
    private String relationshipType;

    /** AI 生成的对比分析全文 */
    @Column(name = "analysis_content", columnDefinition = "MEDIUMTEXT")
    private String analysisContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User ownerUser;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Report getReportA() { return reportA; }
    public void setReportA(Report reportA) { this.reportA = reportA; }
    public Report getReportB() { return reportB; }
    public void setReportB(Report reportB) { this.reportB = reportB; }
    public String getNameA() { return nameA; }
    public void setNameA(String nameA) { this.nameA = nameA; }
    public String getNameB() { return nameB; }
    public void setNameB(String nameB) { this.nameB = nameB; }
    public String getRelationshipType() { return relationshipType; }
    public void setRelationshipType(String relationshipType) { this.relationshipType = relationshipType; }
    public String getAnalysisContent() { return analysisContent; }
    public void setAnalysisContent(String analysisContent) { this.analysisContent = analysisContent; }
    public User getOwnerUser() { return ownerUser; }
    public void setOwnerUser(User ownerUser) { this.ownerUser = ownerUser; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
