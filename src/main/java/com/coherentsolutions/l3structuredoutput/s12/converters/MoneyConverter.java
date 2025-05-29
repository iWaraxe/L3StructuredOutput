package com.coherentsolutions.l3structuredoutput.s12.converters;

import com.coherentsolutions.l3structuredoutput.s12.models.Money;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts string representations to Money objects.
 * Supports formats like "100.50 USD", "$100.50", "EUR 100.50"
 */
@Component
public class MoneyConverter implements Converter<String, Money> {
    
    private static final Pattern MONEY_PATTERN = Pattern.compile(
            "^(?:([A-Z]{3})\\s+)?(?:\\$|€|£)?(\\d+(?:\\.\\d{2})?)(?:\\s+([A-Z]{3}))?$"
    );
    
    @Override
    public Money convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        source = source.trim();
        
        // Try to parse various money formats
        Matcher matcher = MONEY_PATTERN.matcher(source);
        if (matcher.matches()) {
            String amount = matcher.group(2);
            String currency = matcher.group(1) != null ? matcher.group(1) : 
                            matcher.group(3) != null ? matcher.group(3) : "USD";
            
            return Money.of(amount, currency);
        }
        
        // Fallback: try simple format "amount currency"
        String[] parts = source.split("\\s+");
        if (parts.length == 2) {
            try {
                return Money.of(parts[0], parts[1]);
            } catch (Exception e) {
                // Continue to next attempt
            }
        }
        
        // Last attempt: assume USD if only amount
        try {
            return Money.of(source, "USD");
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse money value: " + source);
        }
    }
}