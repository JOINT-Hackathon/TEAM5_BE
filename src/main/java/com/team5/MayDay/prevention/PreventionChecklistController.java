package com.team5.MayDay.prevention;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.CheckRequest;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.ManualRequest;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.SaveSelectedRequest;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.SaveSelectedResponse;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.StreakResponse;
import com.team5.MayDay.prevention.dto.PreventionChecklistDtos.TagResponse;
import com.team5.MayDay.prevention.dto.PreventionResponse;

import jakarta.validation.Valid;

/**
 * 상단 [재발 방지 체크리스트 ▲] 팝업.
 * 항목 목록은 기존 GET /api/preventions?category= 를 그대로 쓴다.
 */
@RestController
public class PreventionChecklistController {

    private final PreventionChecklistService service;

    public PreventionChecklistController(PreventionChecklistService service) {
        this.service = service;
    }

    /** [재발 방지 체크리스트에 저장] 버튼: 체크한 후보를 질문형으로 다듬고 태그를 붙여 새 것만 추가 */
    @PostMapping("/api/experiences/{id}/preventions/selected")
    @ResponseStatus(HttpStatus.CREATED)
    public SaveSelectedResponse saveSelected(@PathVariable Long id,
                                             @Valid @RequestBody SaveSelectedRequest request) {
        return service.saveSelected(id, request.items());
    }

    /** ③ 태그 칩 목록 (최근에 저장된 태그 순) */
    @GetMapping("/api/preventions/tags")
    public List<TagResponse> tags() {
        return service.tags();
    }

    /** ⑥ [+ 항목 추가] */
    @PostMapping("/api/preventions/manual")
    @ResponseStatus(HttpStatus.CREATED)
    public PreventionResponse addManual(@Valid @RequestBody ManualRequest request) {
        return service.addManual(request);
    }

    /** ⑦ [확인 완료] → ⑤ "N번 연속으로 막았어요" */
    @PostMapping("/api/preventions/check")
    public StreakResponse check(@Valid @RequestBody CheckRequest request) {
        return service.check(request.category());
    }

    /** ⑤ 팝업을 열 때 현재 배너 문구 */
    @GetMapping("/api/preventions/streak")
    public StreakResponse streak(@RequestParam String category) {
        return service.streak(category);
    }
}
