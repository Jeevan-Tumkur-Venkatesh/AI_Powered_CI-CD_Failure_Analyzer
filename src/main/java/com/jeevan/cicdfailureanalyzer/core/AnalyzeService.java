package com.jeevan.cicdfailureanalyzer.core;

import com.jeevan.cicdfailureanalyzer.api.AnalyzeResponse;
import com.jeevan.cicdfailureanalyzer.core.agents.FixSuggestionAgent;
import com.jeevan.cicdfailureanalyzer.core.agents.LogExtractorAgent;
import com.jeevan.cicdfailureanalyzer.core.agents.RootCauseAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyzeService {

    private final LogExtractorAgent logExtractorAgent;
    private final RootCauseAgent rootCauseAgent;
    private final FixSuggestionAgent fixSuggestionAgent;
    private final int maxInputChars;

    public AnalyzeService(
            LogExtractorAgent logExtractorAgent,
            RootCauseAgent rootCauseAgent,
            FixSuggestionAgent fixSuggestionAgent,
            @Value("${app.openai.maxInputChars:20000}") int maxInputChars
    ) {
        this.logExtractorAgent = logExtractorAgent;
        this.rootCauseAgent = rootCauseAgent;
        this.fixSuggestionAgent = fixSuggestionAgent;
        this.maxInputChars = maxInputChars;
    }

    public AnalyzeResponse analyze(String filename, String fullLog) {
        String trimmed = fullLog == null ? "" : fullLog;
        if (trimmed.length() > maxInputChars) {
            trimmed = trimmed.substring(trimmed.length() - maxInputChars); // keep tail
        }

        LogExtractorAgent.Extraction extraction = logExtractorAgent.extract(filename, trimmed);
        RootCauseAgent.RootCauseResult rc = rootCauseAgent.analyze(extraction);
        FixSuggestionAgent.FixResult fix = fixSuggestionAgent.suggestFix(extraction, rc);

        List<String> fixSteps = Arrays.stream((fix.suggestedFix() == null ? "" : fix.suggestedFix()).split("\\r?\\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());

        List<String> signals = new ArrayList<>();
        signals.addAll(extraction.signals());
        signals.addAll(rc.signals());
        signals.addAll(fix.signals());

        return new AnalyzeResponse(
                rc.errorType(),
                rc.rootCause(),
                fixSteps,
                rc.confidence(),
                signals
        );
    }
}