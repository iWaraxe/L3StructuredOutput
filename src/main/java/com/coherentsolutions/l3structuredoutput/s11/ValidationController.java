package com.coherentsolutions.l3structuredoutput.s11;

import com.coherentsolutions.l3structuredoutput.s11.models.ValidationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller demonstrating validation and error recovery strategies
 * for AI-generated structured output.
 */
@RestController
@RequestMapping("/api/validation")
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    /**
     * Generates and validates an order based on description.
     * Example: POST /api/validation/order
     * Body: { "description": "Customer wants to order 3 laptops for $4500 total, ship to NYC office" }
     */
    @PostMapping("/order")
    public ResponseEntity<ValidationResult> validateOrder(@RequestBody OrderValidationRequest request) {
        ValidationResult result = validationService.generateAndValidateOrder(request.description());
        
        return result.valid() 
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    /**
     * Validates raw JSON with recovery attempts.
     * Example: POST /api/validation/json
     * Body: Raw JSON string to validate
     */
    @PostMapping("/json")
    public ResponseEntity<ValidationResult> validateJson(@RequestBody String json) {
        ValidationResult result = validationService.validateRawJson(json);
        
        return result.valid()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    /**
     * Returns validation strategies and best practices.
     * Example: GET /api/validation/strategies
     */
    @GetMapping("/strategies")
    public ResponseEntity<Map<String, Object>> getValidationStrategies() {
        Map<String, Object> strategies = Map.of(
                "validation_types", Map.of(
                        "schema_validation", "Use JSON Schema or Bean Validation annotations",
                        "semantic_validation", "Check business logic and data consistency",
                        "format_validation", "Verify data formats (dates, emails, patterns)",
                        "range_validation", "Ensure values are within acceptable ranges"
                ),
                "recovery_strategies", Map.of(
                        "parsing_recovery", Map.of(
                                "extract_json", "Extract JSON from surrounding text",
                                "fix_syntax", "Fix common JSON syntax errors",
                                "retry_with_guidance", "Re-prompt AI with specific format instructions"
                        ),
                        "validation_recovery", Map.of(
                                "auto_fix", "Automatically fix simple format issues",
                                "default_values", "Apply sensible defaults for missing data",
                                "partial_acceptance", "Accept valid parts, flag invalid ones"
                        ),
                        "ai_recovery", Map.of(
                                "targeted_regeneration", "Regenerate only invalid fields",
                                "example_based", "Provide examples of correct format",
                                "iterative_refinement", "Multiple attempts with feedback"
                        )
                ),
                "best_practices", Map.of(
                        "preventive", new String[]{
                                "Use clear, specific prompts",
                                "Provide format examples in prompts",
                                "Use JSON Schema in AI requests",
                                "Set appropriate temperature (lower for structured output)"
                        },
                        "defensive", new String[]{
                                "Always validate AI output",
                                "Have fallback strategies",
                                "Log validation failures for analysis",
                                "Monitor validation success rates"
                        },
                        "recovery", new String[]{
                                "Implement multiple recovery strategies",
                                "Set maximum retry limits",
                                "Provide user feedback on recovery",
                                "Track recovery success metrics"
                        }
                )
        );
        
        return ResponseEntity.ok(strategies);
    }

    /**
     * Demonstrates common validation errors and their fixes.
     * Example: GET /api/validation/examples
     */
    @GetMapping("/examples")
    public ResponseEntity<Map<String, Object>> getValidationExamples() {
        Map<String, Object> examples = Map.of(
                "format_errors", Map.of(
                        "invalid_order_id", Map.of(
                                "invalid", "ORDER123",
                                "fixed", "ORD-000123",
                                "rule", "Must match pattern ORD-XXXXXX"
                        ),
                        "invalid_email", Map.of(
                                "invalid", "user at example com",
                                "fixed", "user@example.com",
                                "rule", "Must be valid email format"
                        ),
                        "future_date", Map.of(
                                "invalid", "2025-12-31",
                                "fixed", "2024-01-15",
                                "rule", "Date cannot be in the future"
                        )
                ),
                "json_errors", Map.of(
                        "single_quotes", Map.of(
                                "invalid", "{'name': 'value'}",
                                "fixed", "{\\\"name\\\": \\\"value\\\"}",
                                "issue", "JSON requires double quotes"
                        ),
                        "trailing_comma", Map.of(
                                "invalid", "{\\\"a\\\": 1, \\\"b\\\": 2,}",
                                "fixed", "{\\\"a\\\": 1, \\\"b\\\": 2}",
                                "issue", "No trailing commas allowed"
                        ),
                        "unquoted_keys", Map.of(
                                "invalid", "{name: \\\"value\\\"}",
                                "fixed", "{\\\"name\\\": \\\"value\\\"}",
                                "issue", "Keys must be quoted"
                        )
                ),
                "recovery_examples", Map.of(
                        "extracted_json", Map.of(
                                "input", "Here is the order: {\\\"id\\\": \\\"ORD-123456\\\", \\\"total\\\": 100}. Please process.",
                                "extracted", "{\\\"id\\\": \\\"ORD-123456\\\", \\\"total\\\": 100}",
                                "strategy", "Extract JSON from text"
                        ),
                        "fixed_format", Map.of(
                                "input", "ORD123",
                                "fixed", "ORD-000123",
                                "strategy", "Auto-fix to match pattern"
                        )
                )
        );
        
        return ResponseEntity.ok(examples);
    }

    /**
     * Provides metrics about validation performance.
     * Example: GET /api/validation/metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getValidationMetrics() {
        // In a real application, these would be tracked metrics
        Map<String, Object> metrics = Map.of(
                "validation_stats", Map.of(
                        "total_validations", 1000,
                        "successful", 850,
                        "failed", 150,
                        "success_rate", "85%"
                ),
                "recovery_stats", Map.of(
                        "recovery_attempts", 150,
                        "successful_recoveries", 120,
                        "failed_recoveries", 30,
                        "recovery_rate", "80%"
                ),
                "common_errors", Map.of(
                        "format_errors", "45%",
                        "missing_fields", "30%",
                        "invalid_values", "15%",
                        "parsing_errors", "10%"
                ),
                "performance", Map.of(
                        "avg_validation_time", "50ms",
                        "avg_recovery_time", "200ms",
                        "avg_total_time", "75ms"
                )
        );
        
        return ResponseEntity.ok(metrics);
    }

    // Request DTO
    public record OrderValidationRequest(String description) {}
}