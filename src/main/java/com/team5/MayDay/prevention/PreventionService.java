package com.team5.MayDay.prevention;

import com.team5.MayDay.experience.ExperienceRepository;
import com.team5.MayDay.prevention.dto.PreventionRequest;
import com.team5.MayDay.prevention.dto.PreventionResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PreventionService {

    private final PreventionRepository preventionRepository;
    private final ExperienceRepository experienceRepository;

    public PreventionService(
            PreventionRepository preventionRepository,
            ExperienceRepository experienceRepository) {

        this.preventionRepository = preventionRepository;
        this.experienceRepository = experienceRepository;
    }

    @Transactional
    public PreventionResponse save(
            Long experienceId,
            PreventionRequest request) {

        if (!experienceRepository.existsById(experienceId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "해당 사건이 존재하지 않습니다."
            );
        }

        if (request.category() == null
                || request.category().isBlank()
                || request.content() == null
                || request.content().isBlank()
                || request.triggerPoint() == null
                || request.triggerPoint().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "category, content, triggerPoint는 필수입니다."
            );
        }

        PreventionRule rule = new PreventionRule(
                experienceId,
                request.category(),
                request.content(),
                request.triggerPoint()
        );

        return toResponse(preventionRepository.save(rule));
    }

    @Transactional(readOnly = true)
    public List<PreventionResponse> findByCategory(String category) {

        return preventionRepository
                .findByCategoryOrderByCreatedAtDesc(category)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PreventionResponse> findByExperienceId(
            Long experienceId) {

        return preventionRepository
                .findByExperienceId(experienceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PreventionResponse toResponse(PreventionRule rule) {

        return new PreventionResponse(
                rule.getId(),
                rule.getExperienceId(),
                rule.getCategory(),
                rule.getContent(),
                rule.getTriggerPoint(),
                rule.getCreatedAt()
        );
    }
}