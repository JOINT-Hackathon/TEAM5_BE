package com.team5.MayDay.prevention;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prevention_rules")
public class PreventionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long experienceId;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private String triggerPoint;

    private LocalDateTime createdAt;

    protected PreventionRule() {
    }

    public PreventionRule(
            Long experienceId,
            String category,
            String content,
            String triggerPoint) {

        this.experienceId = experienceId;
        this.category = category;
        this.content = content;
        this.triggerPoint = triggerPoint;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getExperienceId() { return experienceId; }
    public String getCategory() { return category; }
    public String getContent() { return content; }
    public String getTriggerPoint() { return triggerPoint; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}