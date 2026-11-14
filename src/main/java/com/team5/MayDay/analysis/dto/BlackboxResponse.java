package com.team5.MayDay.analysis.dto;

import java.util.List;

/**
 * 블랙박스 보고서: 무슨 일 → 왜 → 영향 → 재발 방지.
 * status=NEED_MORE_INFO 이면 report 는 null 이고 questions 를 사용자에게 보여준다.
 */
public record BlackboxResponse(
        GenerationStatus status,
        List<String> questions,
        Report report
) {

    public record Report(
            /** 언제, 어디서(누구에게), 무엇이 있었는지 */
            String what,
            Why why,
            /**
             * 다른 사람에게서 들은 모욕, 비하, 위협 표현 기록. 없으면 null.
             * 업무 수정과 별개로 보존하며, 상사용 메일에는 넣지 않는다.
             */
            String harmfulBehavior,
            Impact impact,
            /** 체크해서 재발 방지 체크리스트로 저장할 후보 */
            List<String> prevention
    ) {
    }

    public record Why(
            /** 확인된 원인 또는 가능한 원인 (개인보다 과정/절차 관점) */
            String cause,
            /** 사용자가 자신을 탓한 표현 원문 (예: "내가 덜렁대서"). 없으면 null */
            String selfBlameQuote,
            /** 자기 비난을 과정 관점으로 돌리는 질문 (예: "그때 실수를 막을 수 있는 확인 장치가 있었나요?") */
            String reflectionQuestion
    ) {
    }

    public record Impact(
            Urgency urgency,
            String content
    ) {
    }

    public enum Urgency {
        /** 긴급 */
        URGENT,
        /** 보통 */
        NORMAL,
        /** 낮음 */
        LOW
    }
}
