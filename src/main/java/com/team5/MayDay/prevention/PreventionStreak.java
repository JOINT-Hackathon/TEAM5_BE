package com.team5.MayDay.prevention;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 태그별 "N번 연속으로 막았어요" 기록.
 * [확인 완료]를 누르면 1 증가하고, 같은 태그로 새 사건의 재발 방지가 저장되면 0으로 돌아간다.
 */
@Entity
@Table(name = "prevention_streaks")
public class PreventionStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String category;

    /** 연속으로 막은 횟수 */
    @Column(nullable = false)
    private int streak;

    /** 누적 확인 횟수 (리셋되지 않음) */
    @Column(nullable = false)
    private int totalChecks;

    private LocalDateTime lastCheckedAt;

    protected PreventionStreak() {
    }

    public PreventionStreak(String category) {
        this.category = category;
    }

    public void check() {
        this.streak++;
        this.totalChecks++;
        this.lastCheckedAt = LocalDateTime.now();
    }

    public void reset() {
        this.streak = 0;
    }

    public String getCategory() { return category; }
    public int getStreak() { return streak; }
    public int getTotalChecks() { return totalChecks; }
    public LocalDateTime getLastCheckedAt() { return lastCheckedAt; }
}
