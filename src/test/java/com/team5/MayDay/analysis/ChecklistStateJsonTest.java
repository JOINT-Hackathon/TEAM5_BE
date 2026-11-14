package com.team5.MayDay.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.team5.MayDay.ai.AiClient;
import com.team5.MayDay.analysis.dto.ChecklistState;
import com.team5.MayDay.analysis.dto.ReportEmailRequest;

import tools.jackson.databind.ObjectMapper;

/** 프론트가 체크리스트 조회 응답을 그대로 메일 API 에 넘겨도 읽히는지 (Spring 이 쓰는 ObjectMapper 로 확인) */
@SpringBootTest
class ChecklistStateJsonTest {

    @MockitoBean
    AiClient aiClient;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/experiences/{id}/checklist 응답 형식(content, sortOrder, id 등)을 그대로 받는다")
    void acceptsChecklistApiResponse() {
        String json = """
                {"incident": {"situationType": "MISTAKE_FOUND", "content": "x"},
                 "checklist": [{"id": 7, "experienceId": 1, "content": "정정본 확인", "sortOrder": 1,
                                "status": "COMPLETED", "updatedAt": "2026-11-14T14:00:00"}]}
                """;
        ReportEmailRequest request = objectMapper.readValue(json, ReportEmailRequest.class);

        ChecklistState item = request.checklist().getFirst();
        assertThat(item.action()).isEqualTo("정정본 확인");
        assertThat(item.order()).isEqualTo(1);
        assertThat(item.status()).isEqualTo(ChecklistState.Status.COMPLETED);
        assertThat(request.skip()).isFalse(); // skipQuestions 를 안 보내도 된다
    }

    @Test
    @DisplayName("기존 형식(action, order)도 그대로 받는다")
    void acceptsOriginalNames() {
        String json = """
                {"incident": {"situationType": "CRITICIZED", "content": "x"},
                 "checklist": [{"order": 2, "action": "수정본 공유", "status": "NOT_STARTED"}], "skipQuestions": true}
                """;
        ReportEmailRequest request = objectMapper.readValue(json, ReportEmailRequest.class);

        assertThat(request.checklist().getFirst().action()).isEqualTo("수정본 공유");
        assertThat(request.checklist().getFirst().order()).isEqualTo(2);
        assertThat(request.skip()).isTrue();
    }
}
