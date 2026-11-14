package com.team5.MayDay.analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.team5.MayDay.ai.AiClient;
import com.team5.MayDay.ai.AiException;
import com.team5.MayDay.analysis.dto.AnalysisRequest;
import com.team5.MayDay.analysis.dto.AnalysisResponse;
import com.team5.MayDay.analysis.dto.BlackboxRequest;
import com.team5.MayDay.analysis.dto.BlackboxResponse;
import com.team5.MayDay.analysis.dto.ChecklistState;
import com.team5.MayDay.analysis.dto.GenerationStatus;
import com.team5.MayDay.analysis.dto.ReportEmailRequest;
import com.team5.MayDay.analysis.dto.ReportEmailResponse;
import com.team5.MayDay.analysis.dto.ReportEmailResponse.AppliedAction;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    /** 사건 입력 → 감정 트랙 + 수습 체크리스트 + 재발 방지 후보 */
    public AnalysisResponse analyze(AnalysisRequest request) {
        return ask(AnalysisPrompt.ANALYSIS_SYSTEM, AnalysisPrompt.analysisUser(request), AnalysisResponse.class);
    }

    /** 블랙박스 보고서. 정보가 부족하면 질문을 먼저 돌려준다. */
    public BlackboxResponse blackbox(BlackboxRequest request) {
        BlackboxAiResult result = ask(AnalysisPrompt.BLACKBOX_SYSTEM,
                AnalysisPrompt.blackboxUser(request.incident(), request.checklist(),
                        request.answers(), request.skipQuestions()),
                BlackboxAiResult.class);

        if (needsMoreInfo(result.needMoreInfo(), result.questions(), request.skipQuestions())) {
            return new BlackboxResponse(GenerationStatus.NEED_MORE_INFO, limit(result.questions()), null);
        }
        if (result.report() == null) {
            throw new AiException("블랙박스 보고서가 비어 있습니다.");
        }
        return new BlackboxResponse(GenerationStatus.COMPLETED, List.of(), result.report());
    }

    /** 상사용 수습 보고 메일. [현재 조치]는 AI 가 아니라 체크리스트 상태로 직접 만든다. */
    public ReportEmailResponse reportEmail(ReportEmailRequest request) {
        ReportEmailRequest.Tone tone = request.tone() != null ? request.tone() : ReportEmailRequest.Tone.FORMAL;
        String recipient = StringUtils.hasText(request.recipient()) ? request.recipient() : "팀장님";

        EmailAiResult result = ask(AnalysisPrompt.EMAIL_SYSTEM,
                AnalysisPrompt.emailUser(request, recipient, tone),
                EmailAiResult.class);

        if (needsMoreInfo(result.needMoreInfo(), result.questions(), request.skipQuestions())) {
            return new ReportEmailResponse(GenerationStatus.NEED_MORE_INFO, limit(result.questions()), null);
        }
        if (result.email() == null) {
            throw new AiException("수습 보고 메일이 비어 있습니다.");
        }

        List<AppliedAction> appliedActions = toAppliedActions(request.checklist(), tone);
        String body = ReportEmailFormatter.body(result.email(), appliedActions);
        return new ReportEmailResponse(GenerationStatus.COMPLETED, List.of(),
                new ReportEmailResponse.Email(result.email().subject(), body, appliedActions));
    }

    /**
     * 체크리스트 상태 → 메일 [현재 조치] 문장.
     * 완료만 "완료", 진행 중은 "진행 중", 미착수는 "예정"으로 쓰고 해당 없음은 뺀다.
     */
    private List<AppliedAction> toAppliedActions(List<ChecklistState> checklist, ReportEmailRequest.Tone tone) {
        if (checklist == null) {
            return List.of();
        }
        List<AppliedAction> actions = new ArrayList<>();
        checklist.stream()
                .filter(item -> item.status() != ChecklistState.Status.NOT_APPLICABLE)
                .sorted(Comparator.comparingInt(ChecklistState::order))
                .forEach(item -> actions.add(new AppliedAction(item.order(), item.action(), item.status(),
                        item.action() + " " + statusLabel(item.status(), tone))));
        return actions;
    }

    private static String statusLabel(ChecklistState.Status status, ReportEmailRequest.Tone tone) {
        boolean formal = tone == ReportEmailRequest.Tone.FORMAL;
        return switch (status) {
            case DONE -> formal ? "완료" : "완료했어요";
            case IN_PROGRESS -> formal ? "진행 중" : "진행 중이에요";
            case TODO -> formal ? "예정" : "할 예정이에요";
            case NOT_APPLICABLE -> "";
        };
    }

    private static boolean needsMoreInfo(boolean needMoreInfo, List<String> questions, boolean skipQuestions) {
        return !skipQuestions && needMoreInfo && questions != null && !questions.isEmpty();
    }

    private static List<String> limit(List<String> questions) {
        return questions.stream().limit(3).toList();
    }

    private <T> T ask(String system, String user, Class<T> type) {
        String json = aiClient.generateJson(system, user);
        try {
            return objectMapper.readValue(json, type);
        } catch (JacksonException e) {
            throw new AiException("Gemini 응답을 결과로 변환하지 못했습니다: " + json, e);
        }
    }

    // AI 가 돌려주는 원본 형식 (needMoreInfo 플래그 포함)
    record BlackboxAiResult(boolean needMoreInfo, List<String> questions, BlackboxResponse.Report report) {
    }

    record EmailAiResult(boolean needMoreInfo, List<String> questions, EmailParts email) {
    }

    record EmailParts(String subject, String greeting, String situation, List<String> nextSteps,
                      List<String> requests, String prevention, String closing) {
    }
}
