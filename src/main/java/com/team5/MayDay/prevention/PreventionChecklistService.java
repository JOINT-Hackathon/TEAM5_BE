package com.team5.MayDay.prevention;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.team5.MayDay.experience.Experience;
import com.team5.MayDay.experience.ExperienceRepository;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.ManualRequest;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.SaveSelectedResponse;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.Skipped;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.StreakResponse;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.TagResponse;
import com.team5.MayDay.prevention.dto.PreventionResponse;

/**
 * 상단 [재발 방지 체크리스트] 팝업 기능.
 * 저장은 사용자가 [재발 방지 체크리스트에 저장] 버튼을 눌렀을 때만 일어난다.
 * 기존 항목은 고치지 않고, 의미가 겹치지 않는 새 항목만 추가한다.
 */
@Service
public class PreventionChecklistService {

    private final PreventionRepository preventionRepository;
    private final PreventionStreakRepository streakRepository;
    private final ExperienceRepository experienceRepository;
    private final PreventionRefiner refiner;

    public PreventionChecklistService(PreventionRepository preventionRepository,
                                      PreventionStreakRepository streakRepository,
                                      ExperienceRepository experienceRepository,
                                      PreventionRefiner refiner) {
        this.preventionRepository = preventionRepository;
        this.streakRepository = streakRepository;
        this.experienceRepository = experienceRepository;
        this.refiner = refiner;
    }

    @Transactional
    public SaveSelectedResponse saveSelected(Long experienceId, List<String> items) {
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 사건이 존재하지 않습니다."));

        List<String> candidates = items.stream().map(String::strip).filter(s -> !s.isEmpty()).distinct().toList();
        List<PreventionRule> existingRules = preventionRepository.findTop50ByOrderByCreatedAtDesc();
        Map<Long, PreventionRule> existingById = new LinkedHashMap<>();
        existingRules.forEach(rule -> existingById.put(rule.getId(), rule));

        PreventionRefiner.Result refined = refiner.refine(
                experience.getContent(), candidates, existingCategories(), existingRules);

        List<PreventionResponse> saved = new ArrayList<>();
        List<Skipped> skipped = new ArrayList<>();
        Set<String> seenInBatch = new HashSet<>();
        Set<String> touchedCategories = new LinkedHashSet<>();

        for (PreventionRefiner.Item item : refined.items()) {
            String category = normalizeCategory(item.category());
            String question = blankToNull(item.question());
            String content = blankToNull(item.content()) != null ? item.content().strip() : item.candidate();
            if (category == null || content == null) {
                continue;
            }
            touchedCategories.add(category);

            // 1) AI 가 같은 의미라고 판단한 기존 항목
            PreventionRule duplicate = item.duplicateOfId() != null ? existingById.get(item.duplicateOfId()) : null;
            // 2) AI 가 놓친 완전히 같은 문장
            if (duplicate == null) {
                duplicate = existingRules.stream()
                        .filter(rule -> sameText(rule.getContent(), content) || sameText(rule.getQuestion(), question))
                        .findFirst().orElse(null);
            }
            if (duplicate != null) {
                skipped.add(new Skipped(item.candidate(), PreventionResponse.from(duplicate)));
                continue;
            }
            // 3) 이번에 같이 보낸 후보끼리 겹치는 경우
            if (!seenInBatch.add(normalizeText(question != null ? question : content))) {
                continue;
            }

            PreventionRule rule = new PreventionRule(experience.getId(), category, content,
                    triggerLabel(item.triggerPoint()), question, blankToNull(refined.sourceTitle()));
            saved.add(PreventionResponse.from(preventionRepository.save(rule)));
        }

        // 같은 업무에서 새 사건이 생겼으니 "연속으로 막았어요"는 처음부터 다시 센다.
        touchedCategories.forEach(category ->
                streakRepository.findByCategory(category).ifPresent(PreventionStreak::reset));

        return new SaveSelectedResponse(saved, skipped);
    }

    @Transactional
    public PreventionResponse addManual(ManualRequest request) {
        String category = normalizeCategory(request.category());
        String question = request.question().strip();
        boolean exists = preventionRepository.findByCategoryOrderByCreatedAtDesc(category).stream()
                .anyMatch(rule -> sameText(rule.getQuestion(), question) || sameText(rule.getContent(), question));
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 같은 항목이 있어요.");
        }
        PreventionRule rule = new PreventionRule(null, category, question, "직접 추가", question, null);
        return PreventionResponse.from(preventionRepository.save(rule));
    }

    @Transactional(readOnly = true)
    public List<TagResponse> tags() {
        Map<String, Long> counts = new LinkedHashMap<>();
        preventionRepository.findAllByOrderByCreatedAtDesc()
                .forEach(rule -> counts.merge(rule.getCategory(), 1L, Long::sum));
        return counts.entrySet().stream().map(e -> new TagResponse(e.getKey(), e.getValue())).toList();
    }

    @Transactional
    public StreakResponse check(String rawCategory) {
        String category = normalizeCategory(rawCategory);
        PreventionStreak streak = streakRepository.findByCategory(category)
                .orElseGet(() -> streakRepository.save(new PreventionStreak(category)));
        streak.check();
        return toStreakResponse(category, streak);
    }

    @Transactional(readOnly = true)
    public StreakResponse streak(String rawCategory) {
        String category = normalizeCategory(rawCategory);
        return streakRepository.findByCategory(category)
                .map(streak -> toStreakResponse(category, streak))
                .orElse(new StreakResponse(category, 0, 0, null));
    }

    private static StreakResponse toStreakResponse(String category, PreventionStreak streak) {
        String message = null;
        if (streak.getStreak() == 1) {
            message = "#" + category + ", 1번 막았어요";
        } else if (streak.getStreak() > 1) {
            message = "#" + category + ", " + streak.getStreak() + "번 연속으로 막았어요";
        }
        return new StreakResponse(category, streak.getStreak(), streak.getTotalChecks(), message);
    }

    private List<String> existingCategories() {
        return tags().stream().map(TagResponse::category).toList();
    }

    private static String triggerLabel(String triggerPoint) {
        if (triggerPoint == null) {
            return "업무 시작 전";
        }
        return switch (triggerPoint) {
            case "DURING" -> "작업 중";
            case "BEFORE_SUBMIT" -> "제출 전";
            default -> "업무 시작 전";
        };
    }

    /** "#외부 메일발송 " → "외부메일발송" */
    static String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }
        String normalized = category.replace("#", "").replaceAll("\\s+", "");
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean sameText(String a, String b) {
        return a != null && b != null && normalizeText(a).equals(normalizeText(b));
    }

    private static String normalizeText(String text) {
        return text.replaceAll("[\\s.?!~'\"‘’“”]", "");
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
