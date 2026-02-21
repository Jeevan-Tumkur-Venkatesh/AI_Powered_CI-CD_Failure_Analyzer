package com.jeevan.cicdfailureanalyzer.core.openai;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Executes a prompt against an OpenAI-compatible endpoint.
 *
 * Option A (FREE): This is backed by Ollama running locally:
 *   - Start:   ollama serve
 *   - Pull:    ollama pull llama3.1
 *   - Endpoint: http://localhost:11434/v1
 *
 * We use the OpenAI Java SDK, but point it to Ollama via OpenAiClientFactory.
 */
@Component
public class OpenAiTextRunner {

    private final OpenAiClientFactory factory;

    public OpenAiTextRunner(OpenAiClientFactory factory) {
        this.factory = factory;
    }

    public String run(String input) {
        OpenAIClient client = factory.client();

        String model = factory.model();
        if (model == null || model.isBlank()) {
            model = "llama3.1";
        }

        // Responses API call (works with OpenAI-compatible servers)
        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(model)   // For Ollama: "llama3.1" (or another local model name)
                .input(input)
                .build();

        Response response = client.responses().create(params);

        // SDK returns chunks as ResponseOutputText objects; convert each to string via .text()
        String out = response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(t -> t.text())
                .collect(Collectors.joining("\n"))
                .trim();

        // Fallback if model returns nothing in output_text path
        if (out.isBlank()) {
            return "(no text output returned by model)";
        }
        return out;
    }
}
