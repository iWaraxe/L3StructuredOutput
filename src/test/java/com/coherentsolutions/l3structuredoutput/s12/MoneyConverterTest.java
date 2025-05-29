package com.coherentsolutions.l3structuredoutput.s12;

import com.coherentsolutions.l3structuredoutput.s12.converters.MoneyConverter;
import com.coherentsolutions.l3structuredoutput.s12.models.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyConverterTest {
    
    private MoneyConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new MoneyConverter();
    }
    
    @Test
    void convert_WithAmountAndCurrency_ShouldReturnMoney() {
        // Given
        String input = "100.50 USD";
        
        // When
        Money result = converter.convert(input);
        
        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("100.50"), result.amount());
        assertEquals("USD", result.currency().getCurrencyCode());
    }
    
    @Test
    void convert_WithCurrencySymbol_ShouldReturnMoney() {
        // Given
        String input = "$100.50";
        
        // When
        Money result = converter.convert(input);
        
        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("100.50"), result.amount());
        assertEquals("USD", result.currency().getCurrencyCode());
    }
    
    @Test
    void convert_WithCurrencyFirst_ShouldReturnMoney() {
        // Given
        String input = "EUR 100.50";
        
        // When
        Money result = converter.convert(input);
        
        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("100.50"), result.amount());
        assertEquals("EUR", result.currency().getCurrencyCode());
    }
    
    @Test
    void convert_WithOnlyAmount_ShouldDefaultToUSD() {
        // Given
        String input = "100.50";
        
        // When
        Money result = converter.convert(input);
        
        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("100.50"), result.amount());
        assertEquals("USD", result.currency().getCurrencyCode());
    }
    
    @Test
    void convert_WithNull_ShouldReturnNull() {
        // When
        Money result = converter.convert(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void convert_WithEmptyString_ShouldReturnNull() {
        // When
        Money result = converter.convert("  ");
        
        // Then
        assertNull(result);
    }
    
    @Test
    void convert_WithInvalidFormat_ShouldThrowException() {
        // Given
        String input = "invalid money";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> converter.convert(input));
    }
}