package com.team5.MayDay.analysis;

import java.util.List;

import org.springframework.util.StringUtils;

import com.team5.MayDay.analysis.AnalysisService.EmailParts;
import com.team5.MayDay.analysis.dto.ReportEmailResponse.AppliedAction;

/** AI 가 만든 문장 재료 + 체크리스트 상태로 메일 본문을 조립한다. */
final class ReportEmailFormatter {

    private ReportEmailFormatter() {
    }

    static String body(EmailParts parts, List<AppliedAction> actions, String recipient, boolean formal) {
        StringBuilder sb = new StringBuilder();
        // 인사는 AI 에게 맡기지 않는다. AI 가 이름/소속을 지어내는 경우가 있어서 코드로 고정한다.
        line(sb, recipient + (formal ? ", 보고드립니다." : ", 공유드릴 일이 있어요."));
        sb.append('\n');
        line(sb, parts.situation());

        if (!actions.isEmpty()) {
            sb.append("\n[현재 조치]\n");
            actions.forEach(action -> sb.append("- ").append(action.sentence()).append('\n'));
        }
        section(sb, "[향후 계획]", parts.nextSteps());
        section(sb, "[확인 부탁드릴 사항]", parts.requests());

        if (StringUtils.hasText(parts.prevention())) {
            sb.append('\n');
            line(sb, parts.prevention());
        }
        if (StringUtils.hasText(parts.closing())) {
            sb.append('\n');
            line(sb, parts.closing());
        }
        return sb.toString().strip();
    }

    private static void section(StringBuilder sb, String title, List<String> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        sb.append('\n').append(title).append('\n');
        items.forEach(item -> sb.append("- ").append(item).append('\n'));
    }

    private static void line(StringBuilder sb, String text) {
        if (StringUtils.hasText(text)) {
            sb.append(text.strip()).append('\n');
        }
    }
}
