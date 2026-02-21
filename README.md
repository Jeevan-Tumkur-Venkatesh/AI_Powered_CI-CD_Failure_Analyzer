# AI-Powered CI/CD Failure Analyzer

An intelligent **Spring Boot** service that analyzes CI/CD pipeline logs and returns:

- Failure type  
- Root cause  
- Step-by-step suggested fixes  
- Confidence score  

The goal is to **reduce manual debugging time** by extracting only the relevant error block and generating actionable remediation steps.

---

## Problem

CI/CD logs are often **large, noisy, and hard to inspect manually**.  
Finding the real failure reason takes time and slows down delivery.

This service automates:

- Extracting the important error section  
- Classifying the failure  
- Suggesting safe fixes  

---

## High-Level Flow

1. Upload a CI/CD log file to `/api/analyze`  
2. Extract error-related lines using deterministic regex  
3. Analyze extracted block to classify, find root cause, and generate fixes  
4. Return structured JSON  

---

## Agent-Style Pipeline

### LogExtractorAgent

- Regex-based filtering  
- Removes noise from large logs  
- Extracts only relevant error lines  
- Reduces token usage before sending to the model  

### RootCauseAgent

Classifies failures into:

- Build Failure  
- Dependency Issue  
- Test Failure  
- Timeout  
- Environment Issue  
- Lint/Format Issue  
- Unknown  

Returns:

- `errorType`  
- `rootCause`  
- `confidence`  

### FixSuggestionAgent

- Generates safe, actionable remediation steps  
- Returns fixes as a list  
- Avoids destructive commands  

---

## Defensive LLM Handling

Model outputs are not always valid JSON. To make the API reliable:

- Strip code fences before parsing  
- Best-effort JSON extraction  
- Fallback text parsing if JSON parsing fails  

This ensures the API always returns structured output.

---

## Tech Stack

- Java 17  
- Spring Boot  
- Maven  
- Jackson  
- Ollama (local LLM runtime)  
- REST API  

---

## Running Locally

You should have **Java 17** and **Maven** available.

### Prerequisites

```bash
java -version
mvn -version
ollama --version
1. Clone the Repository
git clone <your-repo-url>
cd AI-Powered-CI-CD-Failure-Analyzer
2. Start Ollama
ollama serve

Pull the model (first time only):

ollama pull llama3.1
3. Run the Spring Boot Application
mvn clean spring-boot:run

Wait until you see:

Tomcat started on port 8080
4. Verify the Service
curl http://localhost:8080/api/health

Expected output:

ok
5. Analyze a Sample Log
curl -s -X POST http://localhost:8080/api/analyze \
  -F "file=@samples/test-fail.log" | jq

Other examples:

curl -s -X POST http://localhost:8080/api/analyze \
  -F "file=@samples/env-fail.log" | jq

curl -s -X POST http://localhost:8080/api/analyze \
  -F "file=@samples/maven-dependency-fail.log" | jq
Example Output
{
  "errorType": "Test Failure",
  "rootCause": "One of the tests failed due to an unexpected status code.",
  "suggestedFix": [
    "Check the API endpoint response code",
    "Run tests in isolation to identify the failing case",
    "Review service logs for related errors"
  ],
  "confidence": "High"
}
API Endpoints
GET /api/health

Checks if the service is running.

POST /api/analyze

Accepts multipart file upload:

file → log file

Returns structured JSON with failure analysis.

Design Decisions

Deterministic log extraction before LLM usage

Structured JSON schema for all responses

Step-based fix suggestions

Fallback parsing for malformed model output

Safe, non-destructive remediation steps

Limitations

Does not automatically apply fixes

Classification depends on log quality

Local LLM performance depends on hardware

Future Improvements

GitHub Actions integration

Auto-generate pull requests for fixes

Hybrid rule-based + LLM classification

Store historical failures for pattern detection

Confidence calibration using heuristics

Use Case

A CI/CD triage tool to quickly identify:

Why the pipeline failed

The root cause

What to do next

without manually scanning large logs.

Author

Jeevan Tumkur Venkatesh
MS Computer Science – Syracuse University
