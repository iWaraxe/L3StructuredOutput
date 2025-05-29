package com.coherentsolutions.l3structuredoutput.s10.models;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * A universal response model that works across different AI providers.
 * This demonstrates how to create models that are compatible with
 * OpenAI, Azure OpenAI, Anthropic, Mistral, and Ollama.
 */
@JsonPropertyOrder({"requestId", "provider", "model", "timestamp", "content", "metadata", "usage"})
public record UniversalResponse(
        @JsonPropertyDescription("Unique identifier for this request")
        String requestId,

        @JsonPropertyDescription("The AI provider used (openai, azure, anthropic, mistral, ollama)")
        String provider,

        @JsonPropertyDescription("The specific model used for generation")
        String model,

        @JsonPropertyDescription("Timestamp when the response was generated")
        LocalDateTime timestamp,

        @JsonPropertyDescription("The main content of the response")
        ResponseContent content,

        @JsonPropertyDescription("Additional metadata about the response")
        Map<String, Object> metadata,

        @JsonPropertyDescription("Token usage information if available")
        UsageInfo usage
) {
    
    @JsonPropertyOrder({"type", "data", "format", "confidence"})
    public record ResponseContent(
            @JsonPropertyDescription("Type of content (text, code, analysis, etc.)")
            String type,

            @JsonPropertyDescription("The actual response data")
            Object data,

            @JsonPropertyDescription("Format of the data (plain, markdown, json, etc.)")
            String format,

            @JsonPropertyDescription("Confidence score if applicable (0.0 to 1.0)")
            Double confidence
    ) {}

    @JsonPropertyOrder({"promptTokens", "completionTokens", "totalTokens"})
    public record UsageInfo(
            @JsonPropertyDescription("Number of tokens in the prompt")
            Integer promptTokens,

            @JsonPropertyDescription("Number of tokens in the completion")
            Integer completionTokens,

            @JsonPropertyDescription("Total tokens used")
            Integer totalTokens
    ) {}
}