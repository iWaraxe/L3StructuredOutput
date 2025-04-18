package com.coherentsolutions.l3structuredoutput.s2;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Record representing the result of a sentiment analysis.
 * Provides the sentiment classification along with confidence scores.
 */
@JsonPropertyOrder({"text", "sentiment", "confidence", "explanation"})
public record SentimentAnalysis(
        @JsonPropertyDescription("The text that was analyzed")
        String text,

        @JsonPropertyDescription("The sentiment classification (POSITIVE, NEGATIVE, or NEUTRAL)")
        Sentiment sentiment,

        @JsonPropertyDescription("Confidence score between 0.0 and 1.0")
        Double confidence,

        @JsonPropertyDescription("Brief explanation of the sentiment classification")
        String explanation
) {
    /**
     * Enum representing the possible sentiment classifications.
     */
    public enum Sentiment {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }
}