package com.team5.MayDay.prevention;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PreventionRepository
        extends JpaRepository<PreventionRule, Long> {

    List<PreventionRule> findByCategoryOrderByCreatedAtDesc(
            String category
    );

    List<PreventionRule> findByExperienceId(Long experienceId);

    List<PreventionRule> findAllByOrderByCreatedAtDesc();

    /** AI 중복 검사에 넘길 최근 항목 */
    List<PreventionRule> findTop50ByOrderByCreatedAtDesc();
}
