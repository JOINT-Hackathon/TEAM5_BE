package com.team5.MayDay.prevention;

import com.team5.MayDay.prevention.dto.PreventionRequest;
import com.team5.MayDay.prevention.dto.PreventionResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PreventionController {

    private final PreventionService preventionService;

    public PreventionController(PreventionService preventionService) {
        this.preventionService = preventionService;
    }

    @PostMapping("/api/experiences/{id}/preventions")
    @ResponseStatus(HttpStatus.CREATED)
    public PreventionResponse save(
            @PathVariable Long id,
            @RequestBody PreventionRequest request) {

        return preventionService.save(id, request);
    }

    /** category 를 빼면 전체 항목 */
    @GetMapping("/api/preventions")
    public List<PreventionResponse> findByCategory(
            @RequestParam(required = false) String category) {

        return preventionService.findByCategory(category);
    }

    @GetMapping("/api/experiences/{id}/preventions")
    public List<PreventionResponse> findByExperience(
            @PathVariable Long id) {

        return preventionService.findByExperienceId(id);
    }
}