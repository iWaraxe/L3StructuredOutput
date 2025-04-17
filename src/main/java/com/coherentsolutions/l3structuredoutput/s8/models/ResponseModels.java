package com.coherentsolutions.l3structuredoutput.s8.models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Models used across different JSON output examples
 */
public class ResponseModels {

    /**
     * Simple product recommendation model
     */
    @JsonPropertyOrder({"name", "category", "price", "rating", "features", "availability"})
    public record ProductRecommendation(
            String name,
            String category,
            Double price,
            Double rating,
            List<String> features,
            Boolean availability
    ) {}

    /**
     * More complex model with nested structures
     */
    @JsonPropertyOrder({"query", "results", "metadata"})
    public record SearchResults(
            String query,
            List<SearchResult> results,
            SearchMetadata metadata
    ) {}

    @JsonPropertyOrder({"title", "url", "relevanceScore", "summary"})
    public record SearchResult(
            String title,
            String url,
            Double relevanceScore,
            String summary
    ) {}

    @JsonPropertyOrder({"totalResults", "searchTime", "filters"})
    public record SearchMetadata(
            Integer totalResults,
            Double searchTime,
            List<String> filters
    ) {}

    /**
     * Model for finance data that might require high precision in responses
     */
    @JsonPropertyOrder({"stockSymbol", "currentPrice", "analysis", "metrics"})
    public record StockAnalysis(
            String stockSymbol,
            Double currentPrice,
            String analysis,
            FinancialMetrics metrics
    ) {}

    @JsonPropertyOrder({"peRatio", "marketCap", "dividendYield", "fiftyTwoWeekRange"})
    public record FinancialMetrics(
            Double peRatio,
            String marketCap,
            Double dividendYield,
            String fiftyTwoWeekRange
    ) {}
}