package com.team5.MayDay.checklist;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "checklist_items")
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long experienceId;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private int sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChecklistStatus status;

    private LocalDateTime updatedAt;

    protected ChecklistItem() {
    }

    public ChecklistItem(
            Long experienceId,
            String content,
            int sortOrder) {

        this.experienceId = experienceId;
        this.content = content;
        this.sortOrder = sortOrder;
        this.status = ChecklistStatus.NOT_STARTED;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(ChecklistStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getExperienceId() { return experienceId; }
    public String getContent() { return content; }
    public int getSortOrder() { return sortOrder; }
    public ChecklistStatus getStatus() { return status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}