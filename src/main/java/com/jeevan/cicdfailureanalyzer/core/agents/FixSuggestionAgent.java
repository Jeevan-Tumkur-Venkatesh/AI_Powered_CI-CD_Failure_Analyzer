package com.jeevan.cicdfailureanalyzer.core.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeevan.cicdfailureanalyzer.core.openai.OpenAiTextRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FixSuggestionAgent {

    private final OpenAiTextRunner runner;
    private final ObjectMapper mapper = new ObjectMapper();

    public record FixResult(String suggestedFix, List<String> signals) {}

    // If model returns broken JSON, we try to extract suggested_fix text
    private static final Pattern SUGGESTED_FIX_FIELD =
            Pattern.compile("(?s)\"suggested_fix\"\\s*:\\s*\"(.*?)\"\\s*\\}"); // non-greedy capture

    public FixSuggestionAgent(OpenAiTextRunner runner) {
        this.runner = runner;
    }

    private String stripCodeFences(String s) {
        if (s == null) return "";
        return s.replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();
    }

    private String bestEffortExtractSuggestedFix(String raw) {
        if (raw == null) return "";

        // Try regex extraction of "suggested_fix": "<...>"
        Matcher m = SUGGESTED_FIX_FIELD.matcher(raw);
        if (m.find()) {
            String extracted = m.group(1);

            // Unescape common JSON escapes and normalize line breaks
            extracted = extracted
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\\"", "\"")
                    .trim();

            return extracted;
        }

        // Otherwise just return raw cleaned
        return raw.trim();
    }

    public FixResult suggestFix(LogExtractorAgent.Extraction extraction,
                                RootCauseAgent.RootCauseResult rootCause) {

        List<String> signals = new ArrayList<>();
        signals.add("agent3:fix_suggester");

        String prompt =
                "Return ONLY valid JSON. No markdown. No extra text.\n" +
                "Schema:\n" +
                "{\n" +
                "  \"suggested_fix\": \"<max 8 short lines, separated by \\n>\"\n" +
                "}\n\n" +
                "Rules:\n" +
                "- The value must be a JSON string.\n" +
                "- Use \\n for line breaks (do NOT put raw newlines inside the string).\n\n" +
                "You are a senior DevOps engineer.\n" +
                "Make suggestions safe and realistic (no destructive commands).\n\n" +
                "error_type: " + rootCause.errorType() + "\n" +
                "root_cause: " + rootCause.rootCause() + "\n\n" +
                "LOG BLOCK:\n" +
                "-----\n" + extraction.extractedBlock() + "\n-----\n";

        String raw = runner.run(prompt);
        raw = stripCodeFences(raw);

        try {
            JsonNode node = mapper.readTree(raw);
            String fix = node.path("suggested_fix").asText("No fix suggestion available.");
            signals.add("agent3:parsed_json=true");
            return new FixResult(fix, signals);
        } catch (Exception e) {
            signals.add("agent3:parsed_json=false");
            signals.add("agent3:raw_fallback");

            String extracted = bestEffortExtractSuggestedFix(raw);
            signals.add("agent3:best_effort_extract=" + (!extracted.isBlank()));

            return new FixResult(extracted, signals);
        }
    }
}