package com.jeevan.cicdfailureanalyzer.core.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeevan.cicdfailureanalyzer.core.openai.OpenAiTextRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 2: Root Cause Analyzer
 *
 * Goal:
 * - Classify error type
 * - Provide short, specific root cause
 * - Provide confidence
 *
 * NOTE: We force the model to return ONLY JSON.
 * If parsing fails, we fall back gracefully.
 */
@Component
public class RootCauseAgent {

    private final OpenAiTextRunner runner;
    private final ObjectMapper mapper = new ObjectMapper();

    public record RootCauseResult(String errorType, String rootCause, String confidence, List<String> signals) {}

    public RootCauseAgent(OpenAiTextRunner runner) {
        this.runner = runner;
    }

    // ✅ Strip ```json ... ``` or ``` ... ``` wrappers that some models add
    private String stripCodeFences(String s) {
        if (s == null) return "";
        return s.replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();
    }

    public RootCauseResult analyze(LogExtractorAgent.Extraction extraction) {
        List<String> signals = new ArrayList<>();
        signals.add("agent2:root_cause");

        String prompt =
                "Return ONLY valid JSON. No markdown. No extra text.\n" +
                "Schema:\n" +
                "{\n" +
                "  \"error_type\": \"Build Failure|Dependency Issue|Test Failure|Timeout|Environment Issue|Lint/Format Issue|Unknown\",\n" +
                "  \"root_cause\": \"<one short sentence>\",\n" +
                "  \"confidence\": \"Low|Medium|High\"\n" +
                "}\n\n" +
                "Context: You are analyzing a CI/CD pipeline failure.\n" +
                "Use the log block below to decide.\n\n" +
                "LOG BLOCK:\n" +
                "-----\n" + extraction.extractedBlock() + "\n-----\n";

        String raw = runner.run(prompt);
        raw = stripCodeFences(raw);

        try {
            JsonNode node = mapper.readTree(raw);
            String errorType = node.path("error_type").asText("Unknown");
            String rootCause = node.path("root_cause").asText("Could not determine root cause.");
            String confidence = node.path("confidence").asText("Low");
            signals.add("agent2:parsed_json=true");
            return new RootCauseResult(errorType, rootCause, confidence, signals);
        } catch (Exception e) {
            signals.add("agent2:parsed_json=false");
            signals.add("agent2:raw_fallback");
            return new RootCauseResult("Unknown", raw, "Low", signals);
        }
    }
}