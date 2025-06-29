package com.coherentsolutions.l3structuredoutput.s2.ex3;

/**
 * Record representing a request for sentiment analysis.
 * Contains the text to be analyzed.
 */
public record SentimentAnalysisRequest(String text) {
}