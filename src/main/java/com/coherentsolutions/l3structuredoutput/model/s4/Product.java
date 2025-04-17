package com.coherentsolutions.l3structuredoutput.model.s4;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * Represents a product in our catalog.
 */
public record Product(
        @JsonPropertyDescription("Unique identifier for the product")
        String id,

        @JsonPropertyDescription("Name of the product")
        String name,

        @JsonPropertyDescription("Description of the product")
        String description,

        @JsonPropertyDescription("Price of the product in USD")
        Double price,

        @JsonPropertyDescription("Category the product belongs to")
        String category,

        @JsonPropertyDescription("Features of the product")
        List<String> features,

        @JsonPropertyDescription("Average rating from 1 to 5")
        Double rating
) {}