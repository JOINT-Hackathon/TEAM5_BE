package com.team5.MayDay.analysis.dto;

import java.util.List;

/**
 * 상사용 수습 보고 메일.
 * status=NEED_MORE_INFO 이면 email 은 null 이고 questions 를 사용자에게 보여준다.
 */
public record ReportEmailResponse(
        GenerationStatus status,
        List<String> questions,
        Email email
) {

    public record Email(
            String subject,
            /** 복사하기 버튼으로 그대로 복사할 전체 본문 */
            String body,
            /** [현재 조치]에 체크리스트에서 자동 반영된 항목 (화면에서 강조 표시용) */
            List<AppliedAction> appliedActions
    ) {
    }

    public record AppliedAction(int order, String action, ChecklistState.Status status, String sentence) {
    }
}
