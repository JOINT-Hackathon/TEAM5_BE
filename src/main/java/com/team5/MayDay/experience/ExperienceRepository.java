package com.team5.MayDay.experience;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExperienceRepository
        extends JpaRepository<Experience, Long> {

    List<Experience> findTop3ByOrderByCreatedAtDescIdDesc();
}