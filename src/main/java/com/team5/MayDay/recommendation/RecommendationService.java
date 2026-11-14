package com.team5.MayDay.recommendation;

import com.team5.MayDay.prevention.PreventionRepository;
import com.team5.MayDay.recommendation.dto.RecommendationRequest;
import com.team5.MayDay.recommendation.dto.RecommendationResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class RecommendationService {

    private final PreventionRepository preventionRepository;

    public RecommendationService(
            PreventionRepository preventionRepository) {
        this.preventionRepository = preventionRepository;
    }

    @Transactional(readOnly = true)
    public RecommendationResponse recommend(
            RecommendationRequest request) {

        if (request == null
                || request.task() == null
                || request.task().isBlank()
                || request.category() == null
                || request.category().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "task와 category는 필수입니다."
            );
        }

        String category = request.category()
                .trim()
                .toUpperCase(Locale.ROOT);

        var rules = preventionRepository
                .findByCategoryOrderByCreatedAtDesc(category)
                .stream()
                .limit(3)
                .toList();

        List<RecommendationResponse.RecommendedItem> items =
                rules.stream()
                        .map(rule ->
                                new RecommendationResponse.RecommendedItem(
                                        rule.getId(),
                                        rule.getExperienceId(),
                                        rule.getContent(),
                                        rule.getTriggerPoint()
                                )
                        )
                        .toList();

        String reason = items.isEmpty()
                ? "아직 이 업무에 저장된 재발 방지 규칙이 없습니다."
                : "이전에 저장한 " + category
                + " 업무의 재발 방지 규칙을 추천합니다.";

        return new RecommendationResponse(
                request.task(),
                category,
                reason,
                items
        );
    }
}