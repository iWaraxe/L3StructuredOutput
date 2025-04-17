package com.coherentsolutions.l3structuredoutput.model.s3;

/**
 * Represents a request for movie recommendations based on user preferences.
 */
public record MoviePreferenceRequest(
        String genre,
        Integer releaseYearAfter,
        String mood,
        Integer maxResults
) {}