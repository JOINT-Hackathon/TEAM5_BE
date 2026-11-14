package com.team5.MayDay.checklist;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChecklistRepository
        extends JpaRepository<ChecklistItem, Long> {

    List<ChecklistItem>
    findByExperienceIdOrderBySortOrderAscIdAsc(
            Long experienceId);
}