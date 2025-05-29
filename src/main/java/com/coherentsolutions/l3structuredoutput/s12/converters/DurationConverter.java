package com.coherentsolutions.l3structuredoutput.s12.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts human-readable duration strings to Duration objects.
 * Supports formats like "2 hours", "30 minutes", "1h 30m", "PT2H30M"
 */
@Component
public class DurationConverter implements Converter<String, Duration> {
    
    private static final Pattern HUMAN_DURATION = Pattern.compile(
            "(?:(\\d+)\\s*(?:hours?|hrs?|h))?" +
            "\\s*(?:(\\d+)\\s*(?:minutes?|mins?|m))?" +
            "\\s*(?:(\\d+)\\s*(?:seconds?|secs?|s))?"
    );
    
    @Override
    public Duration convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        source = source.trim();
        
        // Try ISO 8601 format first (e.g., "PT2H30M")
        if (source.startsWith("PT") || source.startsWith("P")) {
            try {
                return Duration.parse(source);
            } catch (Exception e) {
                // Try human-readable format
            }
        }
        
        // Try human-readable format
        Matcher matcher = HUMAN_DURATION.matcher(source.toLowerCase());
        if (matcher.matches()) {
            long hours = parseGroup(matcher.group(1));
            long minutes = parseGroup(matcher.group(2));
            long seconds = parseGroup(matcher.group(3));
            
            if (hours == 0 && minutes == 0 && seconds == 0) {
                // No match found, try simple number + unit
                return parseSimpleDuration(source);
            }
            
            return Duration.ofHours(hours)
                    .plusMinutes(minutes)
                    .plusSeconds(seconds);
        }
        
        return parseSimpleDuration(source);
    }
    
    private long parseGroup(String group) {
        return group != null ? Long.parseLong(group) : 0;
    }
    
    private Duration parseSimpleDuration(String source) {
        String[] parts = source.toLowerCase().split("\\s+");
        if (parts.length == 2) {
            try {
                long value = Long.parseLong(parts[0]);
                String unit = parts[1];
                
                if (unit.startsWith("hour") || unit.equals("h")) {
                    return Duration.ofHours(value);
                } else if (unit.startsWith("minute") || unit.equals("m")) {
                    return Duration.ofMinutes(value);
                } else if (unit.startsWith("second") || unit.equals("s")) {
                    return Duration.ofSeconds(value);
                } else if (unit.startsWith("day") || unit.equals("d")) {
                    return Duration.ofDays(value);
                }
            } catch (NumberFormatException e) {
                // Fall through
            }
        }
        
        throw new IllegalArgumentException("Cannot parse duration: " + source);
    }
}