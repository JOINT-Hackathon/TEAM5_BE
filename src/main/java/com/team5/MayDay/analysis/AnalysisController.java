package com.team5.MayDay.analysis;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team5.MayDay.analysis.dto.AnalysisRequest;
import com.team5.MayDay.analysis.dto.AnalysisResponse;
import com.team5.MayDay.analysis.dto.BlackboxRequest;
import com.team5.MayDay.analysis.dto.BlackboxResponse;
import com.team5.MayDay.analysis.dto.ReportEmailRequest;
import com.team5.MayDay.analysis.dto.ReportEmailResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    /** 사건 입력 → 감정 트랙 + 수습 체크리스트 + 재발 방지 후보 */
    @PostMapping
    public AnalysisResponse analyze(@Valid @RequestBody AnalysisRequest request) {
        return analysisService.analyze(request);
    }

    /** 블랙박스 보고서 토글을 켰을 때. 정보가 부족하면 NEED_MORE_INFO + questions */
    @PostMapping("/blackbox")
    public BlackboxResponse blackbox(@Valid @RequestBody BlackboxRequest request) {
        return analysisService.blackbox(request);
    }

    /** 상사용 수습 보고 메일 토글을 켰을 때, 또는 체크리스트 상태가 바뀌었을 때 */
    @PostMapping("/report-email")
    public ReportEmailResponse reportEmail(@Valid @RequestBody ReportEmailRequest request) {
        return analysisService.reportEmail(request);
    }
}
