package com.team5.MayDay.prevention;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PreventionStreakRepository extends JpaRepository<PreventionStreak, Long> {

    Optional<PreventionStreak> findByCategory(String category);
}
