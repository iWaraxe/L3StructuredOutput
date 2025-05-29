package com.coherentsolutions.l3structuredoutput.s12.formatters;

import com.coherentsolutions.l3structuredoutput.s12.models.CustomDate;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Formatter for CustomDate objects supporting locale-specific formatting.
 */
@Component
public class CustomDateFormatter implements Formatter<CustomDate> {
    
    @Override
    public CustomDate parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try locale-specific parsing first
            DateTimeFormatter formatter = DateTimeFormatter
                    .ofLocalizedDate(FormatStyle.SHORT)
                    .withLocale(locale);
            LocalDate date = LocalDate.parse(text.trim(), formatter);
            return CustomDate.of(date, getFormatForLocale(locale));
        } catch (Exception e) {
            // Fallback to ISO format
            try {
                LocalDate date = LocalDate.parse(text.trim());
                return CustomDate.of(date);
            } catch (Exception ex) {
                throw new ParseException("Cannot parse date: " + text, 0);
            }
        }
    }
    
    @Override
    public String print(CustomDate customDate, Locale locale) {
        if (customDate == null) {
            return "";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(locale);
        
        return customDate.date().format(formatter);
    }
    
    private String getFormatForLocale(Locale locale) {
        if (locale.getLanguage().equals("en")) {
            if (locale.getCountry().equals("US")) {
                return "MM/dd/yyyy";
            } else {
                return "dd/MM/yyyy";
            }
        } else if (locale.getLanguage().equals("de")) {
            return "dd.MM.yyyy";
        } else if (locale.getLanguage().equals("fr")) {
            return "dd/MM/yyyy";
        }
        
        return "yyyy-MM-dd"; // ISO format as default
    }
}