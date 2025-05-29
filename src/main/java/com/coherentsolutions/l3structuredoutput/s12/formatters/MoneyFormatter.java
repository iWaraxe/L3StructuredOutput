package com.coherentsolutions.l3structuredoutput.s12.formatters;

import com.coherentsolutions.l3structuredoutput.s12.models.Money;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

/**
 * Formatter for Money objects supporting locale-specific formatting.
 */
@Component
public class MoneyFormatter implements Formatter<Money> {
    
    @Override
    public Money parse(String text, Locale locale) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        text = text.trim();
        
        // Remove common currency symbols
        text = text.replace("$", "").replace("€", "").replace("£", "").trim();
        
        // Split by space to find amount and currency
        String[] parts = text.split("\\s+");
        
        if (parts.length == 2) {
            // Format: "100.50 USD" or "USD 100.50"
            try {
                Double.parseDouble(parts[0]);
                return Money.of(parts[0], parts[1]);
            } catch (NumberFormatException e) {
                // Try reversed order
                return Money.of(parts[1], parts[0]);
            }
        } else if (parts.length == 1) {
            // Just amount, use locale to determine currency
            String currencyCode = getCurrencyCodeForLocale(locale);
            return Money.of(parts[0], currencyCode);
        }
        
        throw new ParseException("Cannot parse money value: " + text, 0);
    }
    
    @Override
    public String print(Money money, Locale locale) {
        if (money == null) {
            return "";
        }
        
        // Format based on locale
        if (locale.getLanguage().equals("en")) {
            if (locale.getCountry().equals("US")) {
                return "$" + money.amount().toPlainString();
            } else if (locale.getCountry().equals("GB")) {
                return "£" + money.amount().toPlainString();
            }
        } else if (locale.getLanguage().equals("de") || 
                   locale.getLanguage().equals("fr") ||
                   locale.getLanguage().equals("es")) {
            return money.amount().toPlainString() + " €";
        }
        
        // Default format
        return money.amount().toPlainString() + " " + money.currency().getCurrencyCode();
    }
    
    private String getCurrencyCodeForLocale(Locale locale) {
        try {
            return java.util.Currency.getInstance(locale).getCurrencyCode();
        } catch (Exception e) {
            return "USD"; // Default to USD
        }
    }
}