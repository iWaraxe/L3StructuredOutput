package com.coherentsolutions.l3structuredoutput.s12;

import com.coherentsolutions.l3structuredoutput.s12.converters.DurationConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DurationConverterTest {
    
    private DurationConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new DurationConverter();
    }
    
    @Test
    void convert_WithISO8601Format_ShouldReturnDuration() {
        // Given
        String input = "PT2H30M";
        
        // When
        Duration result = converter.convert(input);
        
        // Then
        assertNotNull(result);
        assertEquals(150, result.toMinutes());
    }
    
    @Test
    void convert_WithHumanReadableHours_ShouldReturnDuration() {
        // Given
        String input = "2 hours";
        
        // When
        Duration result = converter.convert(input);
        
        // Then
        assertNotNull(result);
        assertEquals(120, result.toMinutes());
    }
    
    @Test
    void convert_WithHumanReadableMinutes_ShouldReturnDuration() {
        // Given
        String input = "30 minutes";
        
        // When
        Duration result = converter.convert(input);
        
        // Then
        assertNotNull(result);
        assertEquals(30, result.toMinutes());
    }
    
    @Test
    void convert_WithHumanReadableMixed_ShouldReturnDuration() {
        // Given
        String input = "2h 30m";
        
        // When
        Duration result = converter.convert(input);
        
        // Then
        assertNotNull(result);
        assertEquals(150, result.toMinutes());
    }
    
    @Test
    void convert_WithFullWords_ShouldReturnDuration() {
        // Given
        String input = "2 hours 30 minutes";
        
        // When
        Duration result = converter.convert(input);
        
        // Then
        assertNotNull(result);
        assertEquals(150, result.toMinutes());
    }
    
    @Test
    void convert_WithDays_ShouldReturnDuration() {
        // Given
        String input = "3 days";
        
        // When
        Duration result = converter.convert(input);
        
        // Then
        assertNotNull(result);
        assertEquals(72, result.toHours());
    }
    
    @Test
    void convert_WithNull_ShouldReturnNull() {
        // When
        Duration result = converter.convert(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void convert_WithEmptyString_ShouldReturnNull() {
        // When
        Duration result = converter.convert("  ");
        
        // Then
        assertNull(result);
    }
    
    @Test
    void convert_WithInvalidFormat_ShouldThrowException() {
        // Given
        String input = "invalid duration";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> converter.convert(input));
    }
}