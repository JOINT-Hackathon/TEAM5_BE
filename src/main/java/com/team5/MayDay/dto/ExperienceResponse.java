package com.team5.MayDay.experience.dto;

import java.time.LocalDateTime;

public record ExperienceResponse(
        Long id,
        String type,
        String content,
        String category,
        LocalDateTime createdAt
) {
}