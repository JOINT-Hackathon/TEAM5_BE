package com.team5.MayDay.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.yaml 의 gemini.* 설정 */
@ConfigurationProperties(prefix = "gemini")
public record AiProperties(
        String apiKey,
        String model,
        String baseUrl,
        Double temperature
) {
}
