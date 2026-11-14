package com.team5.MayDay.analysis.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 프론트가 보내는 체크리스트 항목과 현재 상태.
 * 상태는 사용자가 표시한 값이며 AI 가 바꾸지 않는다.
 * GET /api/experiences/{id}/checklist 응답(content, sortOrder)을 그대로 넘겨도 된다.
 */
public record ChecklistState(
        @JsonAlias("sortOrder") int order,
        @JsonAlias("content") @NotBlank String action,
        @NotNull Status status
) {
    /** checklist.ChecklistStatus(DB 저장 값)와 같은 이름을 쓴다. 프론트가 조회한 값을 그대로 넘기면 된다. */
    public enum Status {
        /** 미착수 */
        NOT_STARTED,
        /** 진행 중 */
        IN_PROGRESS,
        /** 완료 */
        COMPLETED,
        /** 해당 없음 */
        NOT_APPLICABLE
    }
}
