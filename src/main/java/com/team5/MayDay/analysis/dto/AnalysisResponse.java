package com.team5.MayDay.analysis.dto;

import java.util.List;

/**
 * POST /api/analysis 응답.
 * 감정 트랙(항상 표시)과 할 일 트랙의 수습 체크리스트, 재발 방지 후보를 한 번에 반환한다.
 * 블랙박스 보고서와 상사용 메일은 사용자가 토글을 켤 때 별도 API 로 생성한다.
 * evidence 는 사용자 원문에서 근거가 된 문장(원문 근거 칩)이다.
 */
public record AnalysisResponse(
        String summary,
        EmotionTrack emotionTrack,
        TaskTrack taskTrack,
        List<PreventionItem> preventionChecklist
) {

    /** 감정 트랙: 사실 / 내 해석(확인되지 않은 추측) / 감정 이름 붙이기 / 감정 다스리기(공감) */
    public record EmotionTrack(
            List<Fact> facts,
            List<Interpretation> interpretations,
            List<Emotion> emotions,
            String comfortMessage,
            /** 공개 모욕, 위협 등 상처를 준 행동이 기록된 경우 true */
            boolean harmfulBehaviorDetected
    ) {
    }

    public record Fact(String content, String evidence) {
    }

    public record Interpretation(String content, String evidence) {
    }

    public record Emotion(String name, String evidence) {
    }

    /** 할 일 트랙: 지금 당장 할 일 체크리스트 */
    public record TaskTrack(
            List<ChecklistItem> checklist,
            /** 아직 확인되지 않은 사실 (예: 다른 표에도 같은 문제가 있는지) */
            List<String> unknowns,
            /** 수습 순서를 정하는 데 필요한 확인 질문 (최대 3개) */
            List<String> followUpQuestions
    ) {
    }

    public record ChecklistItem(int order, Phase phase, String action) {
    }

    public enum Phase {
        /** 지금 */
        NOW,
        /** 그다음 */
        NEXT,
        /** 마무리 */
        FINISH
    }

    public record PreventionItem(Timing timing, String item) {
    }

    public enum Timing {
        /** 업무 시작 전 */
        BEFORE_START,
        /** 작업 중 */
        DURING,
        /** 제출 전 */
        BEFORE_SUBMIT
    }
}
