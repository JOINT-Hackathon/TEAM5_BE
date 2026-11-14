package com.team5.MayDay.analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                        request.answers(), request.skip()),
                BlackboxAiResult.class);

        if (needsMoreInfo(result.needMoreInfo(), result.questions(), request.skip())) {
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

        if (needsMoreInfo(result.needMoreInfo(), result.questions(), request.skip())) {
            return new ReportEmailResponse(GenerationStatus.NEED_MORE_INFO, limit(result.questions()), null);
        }
        if (result.email() == null) {
            throw new AiException("수습 보고 메일이 비어 있습니다.");
        }

        List<AppliedAction> appliedActions = toAppliedActions(request.checklist(), tone, result.email());
        String body = ReportEmailFormatter.body(result.email(), appliedActions, recipient,
                tone == ReportEmailRequest.Tone.FORMAL);
        return new ReportEmailResponse(GenerationStatus.COMPLETED, List.of(),
                new ReportEmailResponse.Email(result.email().subject(), body, appliedActions));
    }

    /**
     * 체크리스트 상태 → 메일 [현재 조치] 문장.
     * 완료만 "완료", 진행 중은 "진행 중", 미착수는 "예정"으로 쓰고 해당 없음은 뺀다.
     * 상태는 항상 사용자가 표시한 값을 그대로 쓰고, AI 는 문장 다듬기와 '받는 사람에게 보고하기' 항목 제외만 한다.
     */
    private List<AppliedAction> toAppliedActions(List<ChecklistState> checklist, ReportEmailRequest.Tone tone,
                                                 EmailParts parts) {
        if (checklist == null) {
            return List.of();
        }
        Map<Integer, String> phrases = new HashMap<>();
        if (parts.actionPhrases() != null) {
            parts.actionPhrases().stream()
                    .filter(p -> p.order() != null && StringUtils.hasText(p.phrase()))
                    .forEach(p -> phrases.put(p.order(), p.phrase().strip()));
        }
        Set<Integer> excluded = parts.excludedOrders() == null ? Set.of() : Set.copyOf(parts.excludedOrders());

        List<AppliedAction> actions = new ArrayList<>();
        checklist.stream()
                .filter(item -> item.status() != ChecklistState.Status.NOT_APPLICABLE)
                .filter(item -> !excluded.contains(item.order()))
                .sorted(Comparator.comparingInt(ChecklistState::order))
                .forEach(item -> {
                    String phrase = phrases.getOrDefault(item.order(), item.action());
                    actions.add(new AppliedAction(item.order(), item.action(), item.status(),
                            phrase + " (" + statusLabel(item.status(), tone) + ")"));
                });
        return actions;
    }

    private static String statusLabel(ChecklistState.Status status, ReportEmailRequest.Tone tone) {
        boolean formal = tone == ReportEmailRequest.Tone.FORMAL;
        return switch (status) {
            case COMPLETED -> "완료";
            case IN_PROGRESS -> formal ? "진행 중" : "하는 중";
            case NOT_STARTED -> "예정";
            case NOT_APPLICABLE -> "";
        };
    }

    private static boolean needsMoreInfo(Boolean needMoreInfo, List<String> questions, boolean skipQuestions) {
        return !skipQuestions && Boolean.TRUE.equals(needMoreInfo) && questions != null && !questions.isEmpty();
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
    record BlackboxAiResult(Boolean needMoreInfo, List<String> questions, BlackboxResponse.Report report) {
    }

    record EmailAiResult(Boolean needMoreInfo, List<String> questions, EmailParts email) {
    }

    record EmailParts(String subject, String greeting, String situation,
                      List<ActionPhrase> actionPhrases, List<Integer> excludedOrders,
                      List<String> nextSteps, List<String> requests, String prevention, String closing) {
    }

    /** 체크리스트 항목을 보고서 말투의 명사형으로 다듬은 것 (상태는 포함하지 않음) */
    record ActionPhrase(Integer order, String phrase) {
    }
}
