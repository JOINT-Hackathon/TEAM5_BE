package com.team5.MayDay.checklist.dto;

import com.team5.MayDay.checklist.ChecklistStatus;
import java.time.LocalDateTime;

public record ChecklistResponse(
        Long id,
        Long experienceId,
        String content,
        int sortOrder,
        ChecklistStatus status,
        LocalDateTime updatedAt
) {
}