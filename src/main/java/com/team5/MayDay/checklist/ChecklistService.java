package com.team5.MayDay.checklist;

import com.team5.MayDay.checklist.dto.*;
import com.team5.MayDay.experience.ExperienceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class ChecklistService {

    private final ChecklistRepository checklistRepository;
    private final ExperienceRepository experienceRepository;

    public ChecklistService(
            ChecklistRepository checklistRepository,
            ExperienceRepository experienceRepository) {

        this.checklistRepository = checklistRepository;
        this.experienceRepository = experienceRepository;
    }

    @Transactional
    public ChecklistResponse create(
            Long experienceId,
            ChecklistCreateRequest request) {

        if (!experienceRepository.existsById(experienceId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "사건이 없습니다.");
        }

        if (request.content() == null
                || request.content().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "내용은 필수입니다.");
        }

        ChecklistItem item = new ChecklistItem(
                experienceId,
                request.content(),
                request.sortOrder()
        );

        return toResponse(checklistRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<ChecklistResponse> findByExperienceId(
            Long experienceId) {

        return checklistRepository
                .findByExperienceIdOrderBySortOrderAscIdAsc(
                        experienceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ChecklistResponse changeStatus(
            Long itemId,
            ChecklistStatusRequest request) {

        if (request.status() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "상태는 필수입니다.");
        }

        ChecklistItem item = checklistRepository
                .findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "체크리스트 항목이 없습니다."));

        item.changeStatus(request.status());

        return toResponse(item);
    }

    private ChecklistResponse toResponse(ChecklistItem item) {
        return new ChecklistResponse(
                item.getId(),
                item.getExperienceId(),
                item.getContent(),
                item.getSortOrder(),
                item.getStatus(),
                item.getUpdatedAt()
        );
    }
}