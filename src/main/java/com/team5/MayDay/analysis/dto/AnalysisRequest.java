package com.team5.MayDay.analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 사건 입력. content 외에는 사용자가 아는 만큼만 채운다.
 */
public record AnalysisRequest(
        @NotNull(message = "상황 표시(MISTAKE_FOUND / CRITICIZED)를 선택해주세요.")
        SituationType situationType,

        @NotBlank(message = "무슨 일이 있었는지 입력해주세요.")
        @Size(max = 3000, message = "3000자 이내로 입력해주세요.")
        String content,

        /** 사용자가 알고 있는 기한 (예: "오늘 오후 3시") */
        String deadline,

        /** 현재 공유 범위 (예: "팀장님만 알고 계심") */
        String sharedScope,

        /** 이미 한 조치 */
        String actionsTaken
) {
    public enum SituationType {
        /** 실수를 발견했어요 */
        MISTAKE_FOUND,
        /** 지적받았어요 */
        CRITICIZED
    }
}
