package com.team5.MayDay.prevention;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.team5.MayDay.ai.AiClient;
import com.team5.MayDay.ai.AiException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * 재발 방지 후보를 AI 로 다듬는다.
 * - 질문형 문장으로 바꾸기 ("첨부파일을 직접 열어서 확인했나요?")
 * - 업무 태그 붙이기 (기존 태그 우선 재사용)
 * - 이미 저장된 항목과 의미가 같은지 판단
 */
@Component
public class PreventionRefiner {

    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public PreventionRefiner(AiClient aiClient, ObjectMapper objectMapper) {
        this.aiClient = aiClient;
        this.objectMapper = objectMapper;
    }

    record Result(String sourceTitle, List<Item> items) {
    }

    record Item(String candidate, String question, String content, String category,
                String triggerPoint, Long duplicateOfId) {
    }

    private static final String SYSTEM = """
            너는 사회초년생의 업무 회복을 돕는 AI 동료 '메이데이'다.
            사용자가 저장하려는 재발 방지 후보를, 다음 업무 전에 확인할 체크리스트 항목으로 다듬는다.

            [규칙]
            - question: 체크박스 옆에 보여줄 질문. "~했나요?"로 끝나는 짧은 문장(30자 안팎).
              예: "첨부파일을 직접 열어서 확인했나요?", "파일명이 '최종_날짜' 형식인가요?"
            - content: 같은 내용을 행동형으로. 예: "외부 발송 전 첨부파일 직접 열어보기"
            - category: 업무 종류 태그. 띄어쓰기와 # 없이 2~8글자. 예: 외부메일발송, 보고서작성, 회의준비, 일정관리
              [기존 태그]에 같은 업무 종류가 있으면 반드시 그 태그를 그대로 쓴다. 없을 때만 새로 만든다.
            - triggerPoint: BEFORE_START(업무 시작 전), DURING(작업 중), BEFORE_SUBMIT(제출 전) 중 하나.
            - duplicateOfId: [기존 항목] 중 표현은 달라도 확인하는 행동이 사실상 같은 항목이 있으면 그 id, 없으면 null.
            - sourceTitle: 사건을 부르는 짧은 이름 6~10글자. 예: "견적서 실수", "주간보고 누락"
            - 후보의 의미를 바꾸거나 새 항목을 만들어 넣지 않는다. 후보 하나당 item 하나.

            반드시 아래 JSON 형식만 출력한다.
            {
              "sourceTitle": "견적서 실수",
              "items": [
                {"candidate": "후보 원문 그대로", "question": "...했나요?", "content": "...", "category": "외부메일발송",
                 "triggerPoint": "BEFORE_SUBMIT", "duplicateOfId": null}
              ]
            }
            """;

    Result refine(String experienceContent, List<String> candidates,
                  List<String> existingCategories, List<PreventionRule> existingRules) {

        String existing = existingRules.isEmpty() ? "(없음)" : existingRules.stream()
                .map(r -> "- id=%d [%s] %s".formatted(r.getId(), r.getCategory(),
                        r.getQuestion() != null ? r.getQuestion() : r.getContent()))
                .collect(Collectors.joining("\n"));

        String user = """
                [사건 내용] %s
                [기존 태그] %s
                [기존 항목]
                %s
                [저장할 후보]
                %s
                """.formatted(
                experienceContent,
                existingCategories.isEmpty() ? "(없음)" : String.join(", ", existingCategories),
                existing,
                candidates.stream().map(c -> "- " + c).collect(Collectors.joining("\n")));

        String json = aiClient.generateJson(SYSTEM, user);
        try {
            Result result = objectMapper.readValue(json, Result.class);
            if (result.items() == null || result.items().isEmpty()) {
                throw new AiException("재발 방지 항목을 다듬지 못했습니다: " + json);
            }
            return result;
        } catch (JacksonException e) {
            throw new AiException("Gemini 응답을 재발 방지 항목으로 변환하지 못했습니다: " + json, e);
        }
    }
}
