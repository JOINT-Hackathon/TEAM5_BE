package com.team5.MayDay.recommendation.dto;

import java.util.List;

public record RecommendationResponse(
        String task,
        String category,
        String reason,
        List<RecommendedItem> recommendations
) {
    public record RecommendedItem(
            Long preventionRuleId,
            Long experienceId,
            String content,
            String triggerPoint
    ) {
    }
}