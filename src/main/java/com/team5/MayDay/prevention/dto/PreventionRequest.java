package com.team5.MayDay.prevention.dto;

public record PreventionRequest(
        String category,
        String content,
        String triggerPoint
) {
}