package com.persona.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_session")
public class TestSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_code", unique = true, nullable = false, length = 32)
    private String sessionCode;

    /** JSON: {"q1": 4, "q2": 2, ...} */
    @Column(name = "answers_json", columnDefinition = "TEXT")
    private String answersJson;

    /** JSON: {"EI": 3.2, "SN": 4.1, ...} */
    @Column(name = "dimension_scores_json", columnDefinition = "TEXT")
    private String dimensionScoresJson;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "IN_PROGRESS";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }
    public String getAnswersJson() { return answersJson; }
    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }
    public String getDimensionScoresJson() { return dimensionScoresJson; }
    public void setDimensionScoresJson(String dimensionScoresJson) { this.dimensionScoresJson = dimensionScoresJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
