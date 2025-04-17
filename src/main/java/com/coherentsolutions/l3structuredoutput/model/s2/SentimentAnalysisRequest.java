package com.coherentsolutions.l3structuredoutput.model.s2;

/**
 * Record representing a request for sentiment analysis.
 * Contains the text to be analyzed.
 */
public record SentimentAnalysisRequest(String text) {
}