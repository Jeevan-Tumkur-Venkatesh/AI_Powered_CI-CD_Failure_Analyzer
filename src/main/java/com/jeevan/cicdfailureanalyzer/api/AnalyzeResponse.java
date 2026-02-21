package com.jeevan.cicdfailureanalyzer.api;

import java.util.List;

public class AnalyzeResponse {
    private String errorType;
    private String rootCause;

    // ✅ changed from String -> List<String>
    private List<String> suggestedFix;

    private String confidence;
    private List<String> signals;

    public AnalyzeResponse() {}

    public AnalyzeResponse(String errorType,
                           String rootCause,
                           List<String> suggestedFix,
                           String confidence,
                           List<String> signals) {
        this.errorType = errorType;
        this.rootCause = rootCause;
        this.suggestedFix = suggestedFix;
        this.confidence = confidence;
        this.signals = signals;
    }

    public String getErrorType() { return errorType; }
    public void setErrorType(String errorType) { this.errorType = errorType; }

    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public List<String> getSuggestedFix() { return suggestedFix; }
    public void setSuggestedFix(List<String> suggestedFix) { this.suggestedFix = suggestedFix; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public List<String> getSignals() { return signals; }
    public void setSignals(List<String> signals) { this.signals = signals; }
}