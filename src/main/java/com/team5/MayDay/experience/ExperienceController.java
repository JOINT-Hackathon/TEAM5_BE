package com.team5.MayDay.experience;

import com.team5.MayDay.experience.dto.ExperienceRequest;
import com.team5.MayDay.experience.dto.ExperienceResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.team5.MayDay.experience.dto.RecentExperienceResponse;
import java.util.List;

@RestController
@RequestMapping("/api/experiences")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExperienceResponse save(
            @RequestBody ExperienceRequest request) {

        return experienceService.save(request);
    }

    @GetMapping("/recent")
    public List<RecentExperienceResponse> recent() {
        return experienceService.findRecent();
    }
}