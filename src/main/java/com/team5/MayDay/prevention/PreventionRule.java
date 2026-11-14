package com.team5.MayDay.prevention;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prevention_rules")
public class PreventionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 출처 사건. 사용자가 직접 추가한 항목은 null */
    private Long experienceId;

    /** 업무 태그 (예: 외부메일발송). 화면에서는 앞에 # 를 붙여 보여준다. */
    @Column(nullable = false)
    private String category;

    /** 행동형 문장 (예: 외부 발송 전 첨부파일 직접 열어보기) */
    @Column(nullable = false, length = 1000)
    private String content;

    /** 확인 시점 (예: 제출 전) */
    @Column(nullable = false)
    private String triggerPoint;

    /** 체크리스트에 보여줄 질문형 문장 (예: 첨부파일을 직접 열어서 확인했나요?) */
    @Column(length = 1000)
    private String question;

    /** 출처 사건의 짧은 이름 (예: 견적서 실수). "11/12 견적서 실수에서 저장" 표시에 쓴다. */
    private String sourceTitle;

    private LocalDateTime createdAt;

    protected PreventionRule() {
    }

    public PreventionRule(
            Long experienceId,
            String category,
            String content,
            String triggerPoint) {

        this(experienceId, category, content, triggerPoint, null, null);
    }

    public PreventionRule(
            Long experienceId,
            String category,
            String content,
            String triggerPoint,
            String question,
            String sourceTitle) {

        this.experienceId = experienceId;
        this.category = category;
        this.content = content;
        this.triggerPoint = triggerPoint;
        this.question = question;
        this.sourceTitle = sourceTitle;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getExperienceId() { return experienceId; }
    public String getCategory() { return category; }
    public String getContent() { return content; }
    public String getTriggerPoint() { return triggerPoint; }
    public String getQuestion() { return question; }
    public String getSourceTitle() { return sourceTitle; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
