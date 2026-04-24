package com.skillforge.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String thinkingLevel = "L1"; // L1, L2, L3, L4

    @Column(nullable = false)
    private Double accuracyPercentage = 0.0;

    @Column(nullable = false)
    private Double avgResponseTimeMs = 0.0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getThinkingLevel() { return thinkingLevel; }
    public void setThinkingLevel(String thinkingLevel) { this.thinkingLevel = thinkingLevel; }

    public Double getAccuracyPercentage() { return accuracyPercentage; }
    public void setAccuracyPercentage(Double accuracyPercentage) { this.accuracyPercentage = accuracyPercentage; }

    public Double getAvgResponseTimeMs() { return avgResponseTimeMs; }
    public void setAvgResponseTimeMs(Double avgResponseTimeMs) { this.avgResponseTimeMs = avgResponseTimeMs; }
}
