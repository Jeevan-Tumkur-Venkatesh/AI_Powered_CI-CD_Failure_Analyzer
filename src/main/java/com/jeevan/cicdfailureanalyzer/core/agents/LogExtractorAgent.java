package com.jeevan.cicdfailureanalyzer.core.agents;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LogExtractorAgent {

    // Common CI/CD “error-ish” patterns
    private static final Pattern ERROR_LINE = Pattern.compile(
            "(?im)^(.*(error|exception|fail(ed)?|fatal|segmentation fault|cannot find symbol|command not found|permission denied|no such file or directory).*)$"
    );

    public record Extraction(String filename, String summary, String extractedBlock, List<String> signals) {}

    public Extraction extract(String filename, String log) {
        List<String> signals = new ArrayList<>();
        signals.add("agent1:log_extractor");

        if (log == null || log.isBlank()) {
            return new Extraction(filename, "Empty log", "", List.of("agent1:empty_log"));
        }

        List<String> matchedLines = new ArrayList<>();
        Matcher m = ERROR_LINE.matcher(log);
        while (m.find()) {
            String line = m.group(1).trim();
            if (line.length() > 0) matchedLines.add(line);
        }

        String extracted;
        if (!matchedLines.isEmpty()) {
            // Take up to last 30 error-ish lines (most relevant near the end)
            int from = Math.max(0, matchedLines.size() - 30);
            extracted = String.join("\n", matchedLines.subList(from, matchedLines.size()));
            signals.add("agent1:found_error_lines=" + matchedLines.size());
        } else {
            // fallback: last 200 lines
            String[] lines = log.split("\n");
            int from = Math.max(0, lines.length - 200);
            StringBuilder sb = new StringBuilder();
            for (int i = from; i < lines.length; i++) sb.append(lines[i]).append("\n");
            extracted = sb.toString().trim();
            signals.add("agent1:fallback_last_lines=200");
        }

        String summary = "Extracted failure-focused section from CI/CD log";
        return new Extraction(filename, summary, extracted, signals);
    }
}
