package com.team5.MayDay.analysis.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * POST /api/analysis/blackbox
 *
 * 1차 요청: answers 없이 보낸다 → 정보가 부족하면 NEED_MORE_INFO + questions 응답
 * 2차 요청: questions 에 대한 답을 answers 에 담아 다시 보낸다
 * skipQuestions=true 면 더 묻지 않고 모르는 부분은 "확인 필요"로 표시해 생성한다.
 */
public record BlackboxRequest(
        @NotNull @Valid AnalysisRequest incident,
        @Valid List<ChecklistState> checklist,
        List<QuestionAnswer> answers,
        /** 안 보내면 false */
        Boolean skipQuestions
) {
    public boolean skip() {
        return Boolean.TRUE.equals(skipQuestions);
    }
}
