package com.team5.MayDay.prevention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.team5.MayDay.ai.AiClient;
import com.team5.MayDay.experience.Experience;
import com.team5.MayDay.experience.ExperienceRepository;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.ManualRequest;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.SaveSelectedResponse;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.StreakResponse;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.TagResponse;
import com.team5.MayDay.prevention.dto.PreventionResponse;

/** Gemini 대신 고정된 JSON 을 돌려주게 하고, 저장·중복·연속 기록 로직을 검증한다. */
@SpringBootTest
@Transactional
class PreventionChecklistServiceTest {

    @MockitoBean
    AiClient aiClient;

    @Autowired
    PreventionChecklistService service;

    @Autowired
    PreventionService preventionService;

    @Autowired
    ExperienceRepository experienceRepository;

    Long experienceId;

    @BeforeEach
    void setUp() {
        experienceId = experienceRepository.save(
                new Experience("MISTAKE_FOUND", "거래처에 견적서 옛날 버전(v1)을 보냈어요.", "외부메일발송")).getId();
    }

    private void aiReturns(String json) {
        when(aiClient.generateJson(anyString(), anyString())).thenReturn(json);
    }

    private static final String FIRST_SAVE = """
            {"sourceTitle": "견적서 실수", "items": [
              {"candidate": "외부 발송 전 첨부파일 직접 열어보기", "question": "첨부파일을 직접 열어서 확인했나요?",
               "content": "외부 발송 전 첨부파일 직접 열어보기", "category": "#외부 메일발송", "triggerPoint": "BEFORE_SUBMIT", "duplicateOfId": null},
              {"candidate": "파일명에 최종_날짜 규칙 적용", "question": "파일명이 '최종_날짜' 형식인가요?",
               "content": "파일명에 '최종_날짜' 규칙 적용", "category": "외부메일발송", "triggerPoint": "BEFORE_START", "duplicateOfId": null}
            ]}
            """;

    @Test
    @DisplayName("저장 버튼: 질문형 문장, 태그, 출처 표시와 함께 저장된다")
    void saveSelected() {
        aiReturns(FIRST_SAVE);

        SaveSelectedResponse result = service.saveSelected(experienceId,
                List.of("외부 발송 전 첨부파일 직접 열어보기", "파일명에 최종_날짜 규칙 적용"));

        assertThat(result.saved()).hasSize(2);
        assertThat(result.skipped()).isEmpty();
        PreventionResponse first = result.saved().getFirst();
        assertThat(first.question()).isEqualTo("첨부파일을 직접 열어서 확인했나요?");
        assertThat(first.category()).isEqualTo("외부메일발송"); // "#외부 메일발송" 정리됨
        assertThat(first.triggerPoint()).isEqualTo("제출 전");
        assertThat(first.sourceLabel()).endsWith("견적서 실수에서 저장");
    }

    @Test
    @DisplayName("추가만 된다: 기존 항목과 같은 의미면 저장하지 않고 skipped 로 돌려준다")
    void onlyNewItemsAreAdded() {
        aiReturns(FIRST_SAVE);
        SaveSelectedResponse first = service.saveSelected(experienceId,
                List.of("외부 발송 전 첨부파일 직접 열어보기", "파일명에 최종_날짜 규칙 적용"));
        Long attachmentRuleId = first.saved().getFirst().id();

        // 두 번째 사건: 하나는 AI 가 기존 항목과 같다고 판단, 하나는 새 항목
        aiReturns("""
                {"sourceTitle": "메일 오발송", "items": [
                  {"candidate": "보내기 전 첨부 확인", "question": "첨부파일을 열어봤나요?", "content": "보내기 전 첨부 확인",
                   "category": "외부메일발송", "triggerPoint": "BEFORE_SUBMIT", "duplicateOfId": %d},
                  {"candidate": "받는 사람 주소 다시 확인", "question": "받는 사람 주소를 다시 확인했나요?",
                   "content": "받는 사람 주소 다시 확인", "category": "외부메일발송", "triggerPoint": "BEFORE_SUBMIT", "duplicateOfId": null}
                ]}
                """.formatted(attachmentRuleId));
        SaveSelectedResponse second = service.saveSelected(experienceId,
                List.of("보내기 전 첨부 확인", "받는 사람 주소 다시 확인"));

        assertThat(second.saved()).extracting(PreventionResponse::question)
                .containsExactly("받는 사람 주소를 다시 확인했나요?");
        assertThat(second.skipped()).hasSize(1);
        assertThat(second.skipped().getFirst().duplicateOf().id()).isEqualTo(attachmentRuleId);
        // 기존 2개 + 새 1개, 기존 항목은 그대로
        assertThat(preventionService.findByCategory("외부메일발송")).hasSize(3);
    }

    @Test
    @DisplayName("AI 가 중복을 놓쳐도 완전히 같은 문장은 저장하지 않는다")
    void exactDuplicateIsSkippedEvenIfAiMissed() {
        aiReturns(FIRST_SAVE);
        service.saveSelected(experienceId, List.of("외부 발송 전 첨부파일 직접 열어보기", "파일명에 최종_날짜 규칙 적용"));

        aiReturns(FIRST_SAVE); // duplicateOfId 가 null 인 같은 응답
        SaveSelectedResponse again = service.saveSelected(experienceId,
                List.of("외부 발송 전 첨부파일 직접 열어보기", "파일명에 최종_날짜 규칙 적용"));

        assertThat(again.saved()).isEmpty();
        assertThat(again.skipped()).hasSize(2);
    }

    @Test
    @DisplayName("확인 완료를 누를 때마다 연속 횟수가 늘고, 같은 태그로 새 사건이 저장되면 0부터 다시 센다")
    void streak() {
        aiReturns(FIRST_SAVE);
        service.saveSelected(experienceId, List.of("외부 발송 전 첨부파일 직접 열어보기", "파일명에 최종_날짜 규칙 적용"));

        assertThat(service.streak("외부메일발송").message()).isNull();
        assertThat(service.check("외부메일발송").message()).isEqualTo("#외부메일발송, 1번 막았어요");
        service.check("외부메일발송");
        StreakResponse third = service.check("#외부메일발송");
        assertThat(third.streak()).isEqualTo(3);
        assertThat(third.message()).isEqualTo("#외부메일발송, 3번 연속으로 막았어요");

        aiReturns(FIRST_SAVE); // 같은 태그로 새 사건 저장
        service.saveSelected(experienceId, List.of("외부 발송 전 첨부파일 직접 열어보기"));

        StreakResponse afterNewIncident = service.streak("외부메일발송");
        assertThat(afterNewIncident.streak()).isZero();
        assertThat(afterNewIncident.totalChecks()).isEqualTo(3); // 누적은 유지
    }

    @Test
    @DisplayName("+ 항목 추가: AI 없이 저장되고, 같은 항목은 409")
    void addManual() {
        PreventionResponse added = service.addManual(new ManualRequest("#보고서 작성", "기준 월을 확인했나요?"));
        assertThat(added.category()).isEqualTo("보고서작성");
        assertThat(added.sourceLabel()).isEqualTo("직접 추가");

        assertThatThrownBy(() -> service.addManual(new ManualRequest("보고서작성", "기준 월을 확인했나요?")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    @DisplayName("태그 칩: 태그별 항목 수")
    void tags() {
        aiReturns(FIRST_SAVE);
        service.saveSelected(experienceId, List.of("외부 발송 전 첨부파일 직접 열어보기", "파일명에 최종_날짜 규칙 적용"));
        service.addManual(new ManualRequest("보고서작성", "기준 월을 확인했나요?"));

        assertThat(service.tags()).extracting(TagResponse::category, TagResponse::count)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("외부메일발송", 2L),
                        org.assertj.core.groups.Tuple.tuple("보고서작성", 1L));
    }

    @Test
    @DisplayName("없는 사건이면 404")
    void experienceNotFound() {
        assertThatThrownBy(() -> service.saveSelected(9999L, List.of("아무거나")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("기존 API(POST /api/experiences/{id}/preventions)로 저장한 항목도 그대로 동작한다")
    void legacySaveStillWorks() {
        PreventionResponse saved = preventionService.save(experienceId,
                new com.team5.MayDay.prevention.dto.PreventionRequest("회의준비", "회의 자료 미리 공유", "업무 시작 전"));

        assertThat(saved.question()).isEqualTo("회의 자료 미리 공유"); // 질문형이 없으면 content
        assertThat(saved.sourceLabel()).endsWith("기록에서 저장");
    }
}
