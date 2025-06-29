package com.coherentsolutions.l3structuredoutput.s6;

/**
 * Simple request class for travel recommendations.
 */
public record TravelRequest(
        String season,
        String budget,
        String travelStyle,
        String region
) {}