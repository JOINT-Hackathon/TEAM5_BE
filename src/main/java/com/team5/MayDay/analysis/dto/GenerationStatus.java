package com.team5.MayDay.analysis.dto;

/** 토글로 생성하는 결과물(블랙박스, 메일)의 생성 상태 */
public enum GenerationStatus {
    /** 정보가 부족해 결과 대신 질문을 반환함. 답변을 answers 에 담아 다시 요청한다. */
    NEED_MORE_INFO,
    /** 생성 완료 */
    COMPLETED
}
