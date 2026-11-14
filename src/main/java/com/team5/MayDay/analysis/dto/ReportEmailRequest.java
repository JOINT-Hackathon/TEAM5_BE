package com.team5.MayDay.analysis.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * POST /api/analysis/report-email
 *
 * 체크리스트 상태가 바뀔 때마다 다시 호출하면 [현재 조치]가 갱신된다.
 * answers / skipQuestions 사용법은 BlackboxRequest 와 같다.
 */
public record ReportEmailRequest(
        @NotNull @Valid AnalysisRequest incident,
        @Valid List<ChecklistState> checklist,
        /** 격식 / 캐주얼. 없으면 격식 */
        Tone tone,
        /** 받는 사람 호칭. 없으면 "팀장님" */
        String recipient,
        List<QuestionAnswer> answers,
        /** 안 보내면 false */
        Boolean skipQuestions
) {
    public boolean skip() {
        return Boolean.TRUE.equals(skipQuestions);
    }

    public enum Tone {
        FORMAL,
        CASUAL
    }
}
