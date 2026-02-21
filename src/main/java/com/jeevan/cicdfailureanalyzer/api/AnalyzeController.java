package com.jeevan.cicdfailureanalyzer.api;

import com.jeevan.cicdfailureanalyzer.core.AnalyzeService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private final AnalyzeService analyzeService;

    public AnalyzeController(AnalyzeService analyzeService) {
        this.analyzeService = analyzeService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalyzeResponse> analyze(@RequestPart("file") @NotNull MultipartFile file) throws Exception {
        String originalName = file.getOriginalFilename() == null ? "log.txt" : file.getOriginalFilename();
        String content = new String(file.getBytes());
        AnalyzeResponse response = analyzeService.analyze(originalName, content);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}
