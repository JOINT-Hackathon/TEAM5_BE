package com.team5.MayDay.checklist.dto;

public record ChecklistCreateRequest(
        String content,
        int sortOrder
) {
}