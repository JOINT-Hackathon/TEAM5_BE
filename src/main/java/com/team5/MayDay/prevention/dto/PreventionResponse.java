package com.team5.MayDay.prevention.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.team5.MayDay.prevention.PreventionRule;

public record PreventionResponse(
        Long id,
        Long experienceId,
        String category,
        String content,
        String triggerPoint,
        LocalDateTime createdAt,
        /** 체크리스트에 보여줄 질문형 문장. 없으면 content */
        String question,
        /** 출처 표시 (예: "11/12 견적서 실수에서 저장", "직접 추가") */
        String sourceLabel
) {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MM/dd");

    public static PreventionResponse from(PreventionRule rule) {
        String question = rule.getQuestion() != null ? rule.getQuestion() : rule.getContent();
        return new PreventionResponse(
                rule.getId(),
                rule.getExperienceId(),
                rule.getCategory(),
                rule.getContent(),
                rule.getTriggerPoint(),
                rule.getCreatedAt(),
                question,
                sourceLabel(rule));
    }

    private static String sourceLabel(PreventionRule rule) {
        if (rule.getExperienceId() == null) {
            return "직접 추가";
        }
        String date = rule.getCreatedAt() != null ? rule.getCreatedAt().format(DATE) + " " : "";
        String title = rule.getSourceTitle() != null ? rule.getSourceTitle() : "기록";
        return date + title + "에서 저장";
    }
}
