package com.coherentsolutions.l3structuredoutput.s10;

import com.coherentsolutions.l3structuredoutput.s10.models.CodeAnalysis;
import com.coherentsolutions.l3structuredoutput.s10.models.UniversalResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller demonstrating multi-model support for structured output.
 * Shows how different AI providers can be used with consistent structured responses.
 */
@RestController
@RequestMapping("/api/multi-model")
public class MultiModelController {

    private final MultiModelService multiModelService;

    public MultiModelController(MultiModelService multiModelService) {
        this.multiModelService = multiModelService;
    }

    /**
     * Analyzes code using OpenAI with structured output.
     * Example: POST /api/multi-model/analyze-code
     * Body: { "code": "def hello(): print('Hello')", "language": "python" }
     */
    @PostMapping("/analyze-code")
    public ResponseEntity<CodeAnalysis> analyzeCode(@RequestBody CodeAnalysisRequest request) {
        CodeAnalysis analysis = multiModelService.analyzeCodeWithOpenAI(
                request.code(), 
                request.language()
        );
        return ResponseEntity.ok(analysis);
    }

    /**
     * Generates structured output using a specific provider.
     * Example: POST /api/multi-model/generate?provider=openai
     * Body: { "prompt": "Explain quantum computing" }
     */
    @PostMapping("/generate")
    public ResponseEntity<UniversalResponse> generateWithProvider(
            @RequestParam(defaultValue = "openai") String provider,
            @RequestBody PromptRequest request) {
        
        UniversalResponse response = multiModelService.generateStructuredOutput(
                request.prompt(), 
                provider
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Compares structured output capabilities across providers.
     * Example: GET /api/multi-model/compare-providers
     */
    @GetMapping("/compare-providers")
    public ResponseEntity<Map<String, Object>> compareProviders() {
        Map<String, Object> comparison = multiModelService.compareProviders("");
        return ResponseEntity.ok(comparison);
    }

    /**
     * Simulates Azure OpenAI structured output.
     * Example: POST /api/multi-model/azure/generate
     */
    @PostMapping("/azure/generate")
    public ResponseEntity<UniversalResponse> generateWithAzure(@RequestBody TaskRequest request) {
        UniversalResponse response = multiModelService.generateWithAzure(
                request.prompt(), 
                request.task()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Simulates Anthropic Claude structured output.
     * Example: POST /api/multi-model/anthropic/generate
     */
    @PostMapping("/anthropic/generate")
    public ResponseEntity<UniversalResponse> generateWithAnthropic(@RequestBody TaskRequest request) {
        UniversalResponse response = multiModelService.generateWithAnthropic(
                request.prompt(), 
                request.task()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Simulates Mistral AI structured output.
     * Example: POST /api/multi-model/mistral/generate
     */
    @PostMapping("/mistral/generate")
    public ResponseEntity<UniversalResponse> generateWithMistral(@RequestBody TaskRequest request) {
        UniversalResponse response = multiModelService.generateWithMistral(
                request.prompt(), 
                request.task()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Simulates Ollama (local model) structured output.
     * Example: POST /api/multi-model/ollama/generate
     */
    @PostMapping("/ollama/generate")
    public ResponseEntity<UniversalResponse> generateWithOllama(@RequestBody TaskRequest request) {
        UniversalResponse response = multiModelService.generateWithOllama(
                request.prompt(), 
                request.task()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Demonstrates best practices for multi-model structured output.
     * Example: GET /api/multi-model/best-practices
     */
    @GetMapping("/best-practices")
    public ResponseEntity<Map<String, Object>> getBestPractices() {
        Map<String, Object> bestPractices = Map.of(
                "universal_models", "Design models that work across all providers",
                "provider_abstraction", "Use interfaces to abstract provider differences",
                "fallback_strategy", "Implement fallback to alternative providers",
                "format_compatibility", "Test structured output formats with each provider",
                "error_handling", "Handle provider-specific errors gracefully",
                "performance", Map.of(
                        "cache_schemas", "Cache JSON schemas for reuse",
                        "batch_requests", "Use batch APIs when available",
                        "local_models", "Consider Ollama for sensitive data"
                ),
                "tips", Map.of(
                        "openai", "Use JSON mode or function calling for best results",
                        "anthropic", "Use XML tags or clear formatting instructions",
                        "mistral", "Similar to OpenAI, supports JSON mode",
                        "ollama", "Performance varies by model, test thoroughly"
                )
        );
        return ResponseEntity.ok(bestPractices);
    }

    // Request DTOs
    public record CodeAnalysisRequest(String code, String language) {}
    public record PromptRequest(String prompt) {}
    public record TaskRequest(String prompt, String task) {}
}