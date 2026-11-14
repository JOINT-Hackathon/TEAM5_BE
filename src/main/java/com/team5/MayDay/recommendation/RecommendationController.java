package com.team5.MayDay.recommendation;

import com.team5.MayDay.recommendation.dto.RecommendationRequest;
import com.team5.MayDay.recommendation.dto.RecommendationResponse;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(
            RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public RecommendationResponse recommend(
            @RequestBody RecommendationRequest request) {

        return recommendationService.recommend(request);
    }
}