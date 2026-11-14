package com.team5.MayDay.experience.dto;

import java.time.LocalDateTime;

public record RecentExperienceResponse(
        Long id,
        String title,
        String status,
        LocalDateTime createdAt
) {
}