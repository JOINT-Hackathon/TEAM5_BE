package com.team5.MayDay.analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 프론트가 보내는 체크리스트 항목과 현재 상태.
 * 상태는 사용자가 표시한 값이며 AI 가 바꾸지 않는다.
 */
public record ChecklistState(
        int order,
        @NotBlank String action,
        @NotNull Status status
) {
    public enum Status {
        /** 미착수 */
        TODO,
        /** 진행 중 */
        IN_PROGRESS,
        /** 완료 */
        DONE,
        /** 해당 없음 */
        NOT_APPLICABLE
    }
}
