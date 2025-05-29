package com.coherentsolutions.l3structuredoutput.s12.models;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Complex event model demonstrating various custom type conversions.
 */
public record ScheduledEvent(
        String eventId,
        String title,
        String description,
        ZonedDateTime startTime,
        Duration duration,
        Location location,
        List<String> attendees,
        Map<String, String> metadata,
        EventStatus status,
        Money cost
) {
    public record Location(
            String name,
            String address,
            Coordinates coordinates
    ) {}
    
    public record Coordinates(
            double latitude,
            double longitude
    ) {}
    
    public enum EventStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}