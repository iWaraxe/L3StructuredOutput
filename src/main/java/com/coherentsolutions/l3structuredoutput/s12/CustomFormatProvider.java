package com.coherentsolutions.l3structuredoutput.s12;

import com.coherentsolutions.l3structuredoutput.s12.models.CustomDate;
import com.coherentsolutions.l3structuredoutput.s12.models.Money;
import com.coherentsolutions.l3structuredoutput.s12.models.ScheduledEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.ai.converter.FormatProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom format provider that generates JSON schemas with custom type information
 * and formatting instructions for AI models.
 */
@Component
public class CustomFormatProvider implements FormatProvider {
    
    private final ObjectMapper objectMapper;
    private final Map<Class<?>, String> customTypeDescriptions;
    
    public CustomFormatProvider() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT);
        
        this.customTypeDescriptions = new HashMap<>();
        initializeCustomTypeDescriptions();
    }
    
    @Override
    public String getFormat() {
        return getFormat(null);
    }
    
    public String getFormat(Object schema) {
        StringBuilder format = new StringBuilder();
        format.append("Generate a JSON response that matches this schema:\n\n");
        
        // Add the schema
        try {
            format.append(objectMapper.writeValueAsString(schema));
        } catch (JsonProcessingException e) {
            format.append(schema.toString());
        }
        
        // Add custom type instructions
        format.append("\n\nCustom Type Formatting Instructions:\n");
        for (Map.Entry<Class<?>, String> entry : customTypeDescriptions.entrySet()) {
            format.append("- ").append(entry.getKey().getSimpleName())
                    .append(": ").append(entry.getValue()).append("\n");
        }
        
        // Add general formatting rules
        format.append("\nGeneral Rules:\n");
        format.append("- All monetary values should include currency code (e.g., 'USD', 'EUR')\n");
        format.append("- Dates should be in ISO format (yyyy-MM-dd) unless specified otherwise\n");
        format.append("- Durations can be in ISO 8601 format (PT2H30M) or human-readable (2 hours 30 minutes)\n");
        format.append("- Coordinates should use decimal degrees\n");
        format.append("- Ensure all required fields are populated\n");
        
        return format.toString();
    }
    
    private void initializeCustomTypeDescriptions() {
        customTypeDescriptions.put(Money.class, 
                "Format as {amount: '100.50', currency: 'USD'} or as string '100.50 USD'");
        
        customTypeDescriptions.put(CustomDate.class, 
                "Format as {date: 'yyyy-MM-dd', format: 'yyyy-MM-dd', timezone: 'UTC'}");
        
        customTypeDescriptions.put(Duration.class, 
                "Format as ISO 8601 string (e.g., 'PT2H30M') or human-readable (e.g., '2 hours 30 minutes')");
        
        customTypeDescriptions.put(ScheduledEvent.Location.class,
                "Include name, address, and coordinates with latitude/longitude");
        
        customTypeDescriptions.put(ScheduledEvent.EventStatus.class,
                "Use one of: SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED");
    }
    
    /**
     * Generates example JSON for a given class with custom types.
     */
    public String generateExample(Class<?> clazz) {
        if (clazz == Money.class) {
            return """
                {
                    "amount": "100.50",
                    "currency": "USD"
                }
                """;
        } else if (clazz == CustomDate.class) {
            return """
                {
                    "date": "2024-01-15",
                    "format": "yyyy-MM-dd",
                    "timezone": "UTC"
                }
                """;
        } else if (clazz == ScheduledEvent.class) {
            return """
                {
                    "eventId": "EVT-001",
                    "title": "Team Meeting",
                    "description": "Weekly team sync",
                    "startTime": "2024-01-15T10:00:00Z",
                    "duration": "PT1H",
                    "location": {
                        "name": "Conference Room A",
                        "address": "123 Business Ave, New York, NY 10001",
                        "coordinates": {
                            "latitude": 40.7128,
                            "longitude": -74.0060
                        }
                    },
                    "attendees": ["john@example.com", "jane@example.com"],
                    "metadata": {
                        "project": "Spring AI",
                        "priority": "high"
                    },
                    "status": "SCHEDULED",
                    "cost": {
                        "amount": "0.00",
                        "currency": "USD"
                    }
                }
                """;
        }
        
        return "{}";
    }
}