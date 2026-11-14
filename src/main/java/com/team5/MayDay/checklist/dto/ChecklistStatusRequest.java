package com.team5.MayDay.checklist.dto;

import com.team5.MayDay.checklist.ChecklistStatus;

public record ChecklistStatusRequest(
        ChecklistStatus status
) {
}