package com.coherentsolutions.l3structuredoutput.s5;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"city", "population", "region", "language", "currency", "landmarks"})
public record CapitalInfo(
        @JsonPropertyDescription("The name of the capital city")
        String city,

        @JsonPropertyDescription("The population of the city in millions")
        Double population,

        @JsonPropertyDescription("The region or state where the city is located")
        String region,

        @JsonPropertyDescription("The primary language spoken in the city")
        String language,

        @JsonPropertyDescription("The currency used in the country")
        String currency,

        @JsonPropertyDescription("An array of famous landmarks in the city")
        String[] landmarks
) {}