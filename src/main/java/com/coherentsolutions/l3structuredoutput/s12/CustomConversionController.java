package com.coherentsolutions.l3structuredoutput.s12;

import com.coherentsolutions.l3structuredoutput.s12.models.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

/**
 * REST controller demonstrating custom format providers and conversion service integration.
 */
@RestController
@RequestMapping("/api/custom-conversion")
public class CustomConversionController {
    
    private final CustomConversionService customConversionService;
    private final CustomFormatProvider formatProvider;
    
    public CustomConversionController(CustomConversionService customConversionService,
                                    CustomFormatProvider formatProvider) {
        this.customConversionService = customConversionService;
        this.formatProvider = formatProvider;
    }
    
    /**
     * Generates an invoice with custom type conversion.
     * Example: POST /api/custom-conversion/invoice
     * Body: { "description": "Web development services for January 2024, 40 hours at $150/hour" }
     */
    @PostMapping("/invoice")
    public ResponseEntity<Invoice> generateInvoice(@RequestBody InvoiceRequest request) {
        Invoice invoice = customConversionService.generateInvoice(request.description());
        return ResponseEntity.ok(invoice);
    }
    
    /**
     * Generates a scheduled event with custom type conversion.
     * Example: POST /api/custom-conversion/event
     * Body: { "description": "Team building workshop next Friday 2-5 PM at Central Park, $50 per person" }
     */
    @PostMapping("/event")
    public ResponseEntity<ScheduledEvent> generateEvent(@RequestBody EventRequest request) {
        ScheduledEvent event = customConversionService.generateEvent(request.description());
        return ResponseEntity.ok(event);
    }
    
    /**
     * Demonstrates custom type conversion.
     * Example: POST /api/custom-conversion/convert
     * Body: { "value": "100.50 USD", "targetType": "Money" }
     */
    @PostMapping("/convert")
    public ResponseEntity<ConversionResult> convertValue(@RequestBody ConversionRequest request) {
        try {
            Class<?> targetClass = getTargetClass(request.targetType());
            Object converted = customConversionService.convertValue(request.value(), targetClass);
            
            return ResponseEntity.ok(new ConversionResult(
                    true,
                    request.value(),
                    request.targetType(),
                    converted.toString(),
                    converted
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ConversionResult(
                    false,
                    request.value(),
                    request.targetType(),
                    e.getMessage(),
                    null
            ));
        }
    }
    
    /**
     * Returns examples of custom types and their formats.
     * Example: GET /api/custom-conversion/examples
     */
    @GetMapping("/examples")
    public ResponseEntity<Map<String, Object>> getExamples() {
        return ResponseEntity.ok(Map.of(
                "money_formats", Map.of(
                        "object", formatProvider.generateExample(Money.class),
                        "string_formats", new String[]{
                                "100.50 USD",
                                "$100.50",
                                "EUR 100.50",
                                "100.50"
                        }
                ),
                "date_formats", Map.of(
                        "object", formatProvider.generateExample(CustomDate.class),
                        "string_formats", new String[]{
                                "2024-01-15",
                                "01/15/2024",
                                "15-01-2024",
                                "Jan 15, 2024"
                        }
                ),
                "duration_formats", Map.of(
                        "iso", "PT2H30M",
                        "human_readable", new String[]{
                                "2 hours",
                                "30 minutes",
                                "2h 30m",
                                "2 hours 30 minutes"
                        }
                ),
                "event_example", formatProvider.generateExample(ScheduledEvent.class)
        ));
    }
    
    /**
     * Demonstrates creating complex objects with custom types.
     * Example: GET /api/custom-conversion/demo
     */
    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> demonstrateCustomTypes() {
        // Create sample objects using custom types
        Money price = Money.of(150.00, "USD");
        CustomDate today = CustomDate.of(LocalDate.now());
        Duration meetingDuration = Duration.ofHours(2);
        
        // Create a sample invoice item
        Invoice.InvoiceItem item = new Invoice.InvoiceItem(
                "Consulting Services",
                10,
                price,
                price.multiply(10)
        );
        
        // Create location
        ScheduledEvent.Location location = new ScheduledEvent.Location(
                "Conference Center",
                "123 Business Ave, New York, NY",
                new ScheduledEvent.Coordinates(40.7128, -74.0060)
        );
        
        return ResponseEntity.ok(Map.of(
                "custom_types", Map.of(
                        "money", Map.of(
                                "object", price,
                                "formatted", price.formatted(),
                                "currency", price.currency().getDisplayName()
                        ),
                        "custom_date", Map.of(
                                "object", today,
                                "formatted", today.formatted(),
                                "timezone", today.timezone()
                        ),
                        "duration", Map.of(
                                "iso_format", meetingDuration.toString(),
                                "hours", meetingDuration.toHours(),
                                "minutes", meetingDuration.toMinutes()
                        )
                ),
                "complex_objects", Map.of(
                        "invoice_item", item,
                        "location", location
                )
        ));
    }
    
    private Class<?> getTargetClass(String typeName) {
        return switch (typeName.toLowerCase()) {
            case "money" -> Money.class;
            case "customdate" -> CustomDate.class;
            case "duration" -> Duration.class;
            default -> throw new IllegalArgumentException("Unknown type: " + typeName);
        };
    }
    
    // Request/Response DTOs
    public record InvoiceRequest(String description) {}
    public record EventRequest(String description) {}
    public record ConversionRequest(String value, String targetType) {}
    public record ConversionResult(
            boolean success,
            String originalValue,
            String targetType,
            String message,
            Object convertedValue
    ) {}
}