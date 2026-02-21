package com.jeevan.cicdfailureanalyzer.core.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Creates an OpenAI-compatible client.
 *
 * For Option A (FREE): we point this client to Ollama's OpenAI-compatible server:
 *   baseUrl = http://localhost:11434/v1
 *
 * Ollama doesn't require a real API key, but the SDK expects one, so we send a dummy.
 */
@Component
public class OpenAiClientFactory {

    private final String baseUrl;
    private final String model;
    private final int requestTimeoutSeconds;

    public OpenAiClientFactory(
            @Value("${app.llm.baseUrl:http://localhost:11434/v1}") String baseUrl,
            @Value("${app.llm.model:llama3.1}") String model,
            @Value("${app.llm.requestTimeoutSeconds:60}") int requestTimeoutSeconds
    ) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public String model() {
        return model;
    }

    public OpenAIClient client() {
        // NOTE:
        // - baseUrl points to Ollama
        // - apiKey is a placeholder (Ollama ignores it)
        return OpenAIOkHttpClient.builder()
                .baseUrl(baseUrl)
                .apiKey("ollama") // dummy token
                .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                .build();
    }
}
