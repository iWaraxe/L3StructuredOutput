package com.coherentsolutions.l3structuredoutput.s10.models;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

/**
 * Model for code analysis that can be used across different AI providers.
 * Demonstrates structured output for technical analysis tasks.
 */
@JsonPropertyOrder({"language", "complexity", "summary", "issues", "suggestions", "metrics"})
public record CodeAnalysis(
        @JsonPropertyDescription("Programming language of the analyzed code")
        String language,

        @JsonPropertyDescription("Complexity level: LOW, MEDIUM, HIGH")
        ComplexityLevel complexity,

        @JsonPropertyDescription("Brief summary of what the code does")
        String summary,

        @JsonPropertyDescription("List of identified issues or potential problems")
        List<Issue> issues,

        @JsonPropertyDescription("Improvement suggestions")
        List<String> suggestions,

        @JsonPropertyDescription("Code metrics and statistics")
        CodeMetrics metrics
) {
    
    public enum ComplexityLevel {
        LOW, MEDIUM, HIGH
    }

    @JsonPropertyOrder({"severity", "type", "line", "description", "suggestion"})
    public record Issue(
            @JsonPropertyDescription("Severity level: INFO, WARNING, ERROR")
            Severity severity,

            @JsonPropertyDescription("Type of issue (bug, performance, style, security)")
            String type,

            @JsonPropertyDescription("Line number where the issue was found")
            Integer line,

            @JsonPropertyDescription("Description of the issue")
            String description,

            @JsonPropertyDescription("Suggested fix for the issue")
            String suggestion
    ) {}

    public enum Severity {
        INFO, WARNING, ERROR
    }

    @JsonPropertyOrder({"linesOfCode", "cyclomaticComplexity", "functions", "classes", "testCoverage"})
    public record CodeMetrics(
            @JsonPropertyDescription("Total lines of code")
            Integer linesOfCode,

            @JsonPropertyDescription("Cyclomatic complexity score")
            Integer cyclomaticComplexity,

            @JsonPropertyDescription("Number of functions/methods")
            Integer functions,

            @JsonPropertyDescription("Number of classes")
            Integer classes,

            @JsonPropertyDescription("Estimated test coverage percentage")
            Double testCoverage
    ) {}
}