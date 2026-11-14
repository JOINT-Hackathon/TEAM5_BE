package com.team5.MayDay.checklist;

import com.team5.MayDay.checklist.dto.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ChecklistController {

    private final ChecklistService checklistService;

    public ChecklistController(
            ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @PostMapping("/api/experiences/{id}/checklist")
    @ResponseStatus(HttpStatus.CREATED)
    public ChecklistResponse create(
            @PathVariable Long id,
            @RequestBody ChecklistCreateRequest request) {

        return checklistService.create(id, request);
    }

    @GetMapping("/api/experiences/{id}/checklist")
    public List<ChecklistResponse> findAll(
            @PathVariable Long id) {

        return checklistService.findByExperienceId(id);
    }

    @PatchMapping("/api/checklist/{itemId}/status")
    public ChecklistResponse changeStatus(
            @PathVariable Long itemId,
            @RequestBody ChecklistStatusRequest request) {

        return checklistService.changeStatus(itemId, request);
    }
}