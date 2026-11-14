package com.team5.MayDay.ai;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import lombok.extern.slf4j.Slf4j;

/**
 * Gemini generateContent REST API 호출 담당.
 * 응답을 JSON 문자열로 받도록 responseMimeType=application/json 으로 요청한다.
 */
@Slf4j
@Component
@EnableConfigurationProperties(AiProperties.class)
public class AiClient {

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1500;
    private static final long READ_TIMEOUT_SECONDS = 40;

    private final AiProperties properties;
    private final RestClient restClient;

    public AiClient(AiProperties properties) {
        this.properties = properties;
        // 타임아웃이 없으면 Gemini 가 응답을 안 줄 때 요청이 무한정 붙잡혀 있게 된다.
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(READ_TIMEOUT_SECONDS));
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    /** systemPrompt + userPrompt 를 보내고 모델이 생성한 JSON 텍스트를 그대로 반환한다. */
    public String generateJson(String systemPrompt, String userPrompt) {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw new AiException("GEMINI_API_KEY 환경변수가 설정되지 않았습니다.");
        }

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userPrompt)))),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "temperature", properties.temperature()));

        GeminiResponse response = callWithRetry(body);

        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new AiException("Gemini 응답이 비어 있습니다.");
        }
        GeminiResponse.Content content = response.candidates().getFirst().content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            throw new AiException("Gemini 응답에 내용이 없습니다. (finishReason="
                    + response.candidates().getFirst().finishReason() + ")");
        }
        return content.parts().getFirst().text();
    }

    /** 429(요청 과다), 503(서버 혼잡)처럼 잠깐 뒤에 다시 하면 되는 실패는 최대 MAX_ATTEMPTS 번까지 재시도한다. */
    private GeminiResponse callWithRetry(Map<String, Object> body) {
        for (int attempt = 1; ; attempt++) {
            try {
                return restClient.post()
                        .uri("/models/{model}:generateContent", properties.model())
                        .header("x-goog-api-key", properties.apiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(GeminiResponse.class);
            } catch (RestClientResponseException e) {
                int status = e.getStatusCode().value();
                boolean retryable = status == 429 || status == 503;
                if (!retryable || attempt >= MAX_ATTEMPTS) {
                    throw new AiException("Gemini API 호출에 실패했습니다: " + e.getMessage(), e);
                }
                log.warn("[AI] Gemini {} 응답, {}ms 후 재시도 ({}/{})", status, RETRY_DELAY_MS * attempt, attempt, MAX_ATTEMPTS);
                sleep(RETRY_DELAY_MS * attempt);
            } catch (RestClientException e) {
                throw new AiException("Gemini API 호출에 실패했습니다: " + e.getMessage(), e);
            }
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiException("Gemini 재시도 대기 중 중단되었습니다.", e);
        }
    }

    record GeminiResponse(List<Candidate> candidates) {
        record Candidate(Content content, String finishReason) {
        }

        record Content(List<Part> parts) {
        }

        record Part(String text) {
        }
    }
}
