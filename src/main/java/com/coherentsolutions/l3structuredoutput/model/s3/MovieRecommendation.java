package com.coherentsolutions.l3structuredoutput.model.s3;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Represents a movie recommendation with detailed information.
 * This structure will be used for the AI model's structured output.
 */
@JsonPropertyOrder({"title", "year", "director", "genre", "rating", "summary", "streamingPlatforms"})
public record MovieRecommendation(
        @JsonPropertyDescription("The title of the movie")
        String title,

        @JsonPropertyDescription("The year the movie was released")
        Integer year,

        @JsonPropertyDescription("The director of the movie")
        String director,

        @JsonPropertyDescription("The primary genre of the movie (Action, Comedy, Drama, etc.)")
        String genre,

        @JsonPropertyDescription("The rating out of 10")
        Double rating,

        @JsonPropertyDescription("A brief summary of the movie's plot")
        String summary,

        @JsonPropertyDescription("List of streaming platforms where the movie is available")
        List<String> streamingPlatforms
) {}