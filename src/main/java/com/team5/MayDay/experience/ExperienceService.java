package com.team5.MayDay.experience;

import com.team5.MayDay.experience.dto.ExperienceRequest;
import com.team5.MayDay.experience.dto.ExperienceResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team5.MayDay.experience.dto.RecentExperienceResponse;
import java.util.List;

@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;

    public ExperienceService(ExperienceRepository experienceRepository) {
        this.experienceRepository = experienceRepository;
    }

    @Transactional
    public ExperienceResponse save(ExperienceRequest request) {

        if (request.type() == null || request.type().isBlank()
                || request.content() == null || request.content().isBlank()
                || request.category() == null || request.category().isBlank()) {
            throw new IllegalArgumentException(
                    "type, content, category는 필수입니다."
            );
        }

        Experience experience = new Experience(
                request.type(),
                request.content(),
                request.category()
        );

        Experience saved = experienceRepository.save(experience);

        return new ExperienceResponse(
                saved.getId(),
                saved.getType(),
                saved.getContent(),
                saved.getCategory(),
                saved.getCreatedAt()
        );
    }

    public List<RecentExperienceResponse> findRecent() {

        return experienceRepository
                .findTop3ByOrderByCreatedAtDescIdDesc()
                .stream()
                .map(experience -> new RecentExperienceResponse(
                        experience.getId(),
                        makeTitle(experience.getContent()),
                        "기록 저장됨",
                        experience.getCreatedAt()
                ))
                .toList();
    }

    private String makeTitle(String content) {
        if (content == null || content.isBlank()) {
            return "제목 없는 기록";
        }

        return content.length() > 20
                ? content.substring(0, 20) + "..."
                : content;
    }
}