package com.team5.MayDay.experience.dto;

public record ExperienceRequest(
        String type,
        String content,
        String category
) {
}