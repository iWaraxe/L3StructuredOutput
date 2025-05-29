package com.coherentsolutions.l3structuredoutput.s11.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of validating structured output from AI models.
 * Includes validation status, errors, warnings, and recovery suggestions.
 */
@JsonPropertyOrder({"valid", "timestamp", "errors", "warnings", "recoveryAttempts", "finalOutput", "metadata"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidationResult(
        @JsonPropertyDescription("Whether the validation passed")
        boolean valid,

        @JsonPropertyDescription("When the validation was performed")
        LocalDateTime timestamp,

        @JsonPropertyDescription("List of validation errors found")
        List<ValidationError> errors,

        @JsonPropertyDescription("List of validation warnings")
        List<ValidationWarning> warnings,

        @JsonPropertyDescription("Recovery attempts made to fix errors")
        List<RecoveryAttempt> recoveryAttempts,

        @JsonPropertyDescription("Final output after validation and recovery")
        Object finalOutput,

        @JsonPropertyDescription("Additional metadata about the validation")
        Map<String, Object> metadata
) {
    
    @JsonPropertyOrder({"field", "message", "value", "constraint"})
    public record ValidationError(
            @JsonPropertyDescription("Field that failed validation")
            String field,

            @JsonPropertyDescription("Error message describing the issue")
            String message,

            @JsonPropertyDescription("The invalid value")
            Object value,

            @JsonPropertyDescription("The constraint that was violated")
            String constraint
    ) {}

    @JsonPropertyOrder({"field", "message", "suggestion"})
    public record ValidationWarning(
            @JsonPropertyDescription("Field that triggered the warning")
            String field,

            @JsonPropertyDescription("Warning message")
            String message,

            @JsonPropertyDescription("Suggestion for improvement")
            String suggestion
    ) {}

    @JsonPropertyOrder({"strategy", "success", "description", "result"})
    public record RecoveryAttempt(
            @JsonPropertyDescription("Recovery strategy used")
            String strategy,

            @JsonPropertyDescription("Whether the recovery was successful")
            boolean success,

            @JsonPropertyDescription("Description of what was attempted")
            String description,

            @JsonPropertyDescription("Result of the recovery attempt")
            Object result
    ) {}
}