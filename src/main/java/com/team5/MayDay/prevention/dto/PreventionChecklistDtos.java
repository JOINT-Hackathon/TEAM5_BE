package com.team5.MayDay.prevention.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/** 상단 [재발 방지 체크리스트] 팝업용 요청/응답 */
public final class PreventionChecklistDtos {

    private PreventionChecklistDtos() {
    }

    /**
     * [재발 방지 체크리스트에 저장] 버튼.
     * items 에는 사용자가 체크한 재발 방지 후보 문장을 그대로 담는다. (분석/블랙박스 결과의 prevention 항목)
     */
    public record SaveSelectedRequest(
            @NotEmpty(message = "저장할 항목을 하나 이상 선택해주세요.")
            @Size(max = 10)
            List<@NotBlank String> items
    ) {
    }

    public record SaveSelectedResponse(
            /** 새로 추가된 항목 */
            List<PreventionResponse> saved,
            /** 이미 있는 항목과 같아서 추가하지 않은 것 */
            List<Skipped> skipped
    ) {
    }

    public record Skipped(String item, PreventionResponse duplicateOf) {
    }

    /** [+ 항목 추가] 버튼 (AI 없이 사용자가 직접) */
    public record ManualRequest(
            @NotBlank(message = "태그를 입력해주세요.") String category,
            @NotBlank(message = "확인할 내용을 입력해주세요.") @Size(max = 200) String question
    ) {
    }

    /** [확인 완료] 버튼 */
    public record CheckRequest(
            @NotBlank(message = "태그를 입력해주세요.") String category
    ) {
    }

    public record StreakResponse(
            String category,
            int streak,
            int totalChecks,
            /** 배너 문구 (예: "#외부메일발송, 3번 연속으로 막았어요"). 0번이면 null */
            String message
    ) {
    }

    /** 상단 태그 칩 */
    public record TagResponse(String category, long count) {
    }
}
