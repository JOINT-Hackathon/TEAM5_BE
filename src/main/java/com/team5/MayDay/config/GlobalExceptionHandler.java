package com.team5.MayDay.config;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.team5.MayDay.ai.AiException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(String code, String message) {
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_INPUT", message));
    }

    /** JSON 형식 오류, enum 값 오타 등 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_INPUT", "요청 형식이 올바르지 않습니다."));
    }

    /** 예상하지 못한 에러: 해커톤 개발 중 원인 파악을 위해 예외 종류와 메시지를 같이 내려준다. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("[UNEXPECTED] {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", e.getClass().getSimpleName() + ": " + e.getMessage()));
    }

    /** AI 응답 실패: 사용자 입력은 프론트에서 유지하고 재시도할 수 있도록 502 로 내려준다. */
    @ExceptionHandler(AiException.class)
    public ResponseEntity<ErrorResponse> handleAi(AiException e) {
        log.error("[AI] {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("AI_ANALYSIS_FAILED", "AI 분석에 실패했어요. 잠시 후 다시 시도해주세요."));
    }
}
