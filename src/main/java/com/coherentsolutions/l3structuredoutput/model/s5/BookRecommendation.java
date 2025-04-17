package com.coherentsolutions.l3structuredoutput.model.s5;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A simple book recommendation model with basic properties.
 */
@JsonPropertyOrder({"title", "author", "genre", "publicationYear", "summary"})
public record BookRecommendation(
        @JsonPropertyDescription("The title of the book")
        String title,

        @JsonPropertyDescription("The author of the book")
        String author,

        @JsonPropertyDescription("The book's genre (e.g., Fiction, Non-fiction, Mystery)")
        String genre,

        @JsonPropertyDescription("The year the book was published")
        Integer publicationYear,

        @JsonPropertyDescription("A brief summary of the book's content")
        String summary
) {}