package com.coherentsolutions.l3structuredoutput.s12.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Custom date type demonstrating custom formatting and conversion.
 */
public record CustomDate(
        LocalDate date,
        String format,
        String timezone
) {
    public String formatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return date.format(formatter);
    }
    
    public static CustomDate of(LocalDate date) {
        return new CustomDate(date, "yyyy-MM-dd", "UTC");
    }
    
    public static CustomDate of(LocalDate date, String format) {
        return new CustomDate(date, format, "UTC");
    }
}