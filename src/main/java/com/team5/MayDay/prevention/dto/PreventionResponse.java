package com.team5.MayDay.prevention.dto;

import java.time.LocalDateTime;

public record PreventionResponse(
        Long id,
        Long experienceId,
        String category,
        String content,
        String triggerPoint,
        LocalDateTime createdAt
) {
}