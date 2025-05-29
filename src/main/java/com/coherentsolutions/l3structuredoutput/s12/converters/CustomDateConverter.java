package com.coherentsolutions.l3structuredoutput.s12.converters;

import com.coherentsolutions.l3structuredoutput.s12.models.CustomDate;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Converts various date string formats to CustomDate objects.
 */
@Component
public class CustomDateConverter implements Converter<String, CustomDate> {
    
    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("MMM dd, yyyy"),
            DateTimeFormatter.ofPattern("dd MMM yyyy")
    );
    
    @Override
    public CustomDate convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        source = source.trim();
        
        // Try each formatter
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(source, formatter);
                return CustomDate.of(date);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        
        // If no formatter worked, throw exception
        throw new IllegalArgumentException("Cannot parse date: " + source);
    }
}