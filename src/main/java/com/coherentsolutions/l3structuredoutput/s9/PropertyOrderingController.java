package com.coherentsolutions.l3structuredoutput.s9;

import com.coherentsolutions.l3structuredoutput.s9.models.ProductCatalog;
import com.coherentsolutions.l3structuredoutput.s9.models.UserProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller demonstrating property ordering and advanced annotations
 * in Spring AI structured output.
 */
@RestController
@RequestMapping("/api/property-ordering")
public class PropertyOrderingController {

    private final PropertyOrderingService propertyOrderingService;

    public PropertyOrderingController(PropertyOrderingService propertyOrderingService) {
        this.propertyOrderingService = propertyOrderingService;
    }

    /**
     * Generates a user profile with ordered properties.
     * Example: GET /api/property-ordering/user-profile?description=Tech-savvy developer interested in AI
     */
    @GetMapping("/user-profile")
    public ResponseEntity<UserProfile> generateUserProfile(
            @RequestParam(defaultValue = "A tech enthusiast who loves coding and AI") String description) {
        UserProfile profile = propertyOrderingService.generateUserProfile(description);
        return ResponseEntity.ok(profile);
    }

    /**
     * Generates a product catalog with nested ordered structures.
     * Example: GET /api/property-ordering/product-catalog?type=electronics&categories=5
     */
    @GetMapping("/product-catalog")
    public ResponseEntity<ProductCatalog> generateProductCatalog(
            @RequestParam(defaultValue = "electronics") String type,
            @RequestParam(defaultValue = "5") int categories) {
        ProductCatalog catalog = propertyOrderingService.generateProductCatalog(type, categories);
        return ResponseEntity.ok(catalog);
    }

    /**
     * Shows the JSON schema generated from annotations.
     * Example: GET /api/property-ordering/schema/UserProfile
     */
    @GetMapping("/schema/{className}")
    public ResponseEntity<Map<String, Object>> getSchema(@PathVariable String className) {
        try {
            Class<?> clazz = switch (className) {
                case "UserProfile" -> UserProfile.class;
                case "ProductCatalog" -> ProductCatalog.class;
                default -> throw new IllegalArgumentException("Unknown class: " + className);
            };
            
            String schema = propertyOrderingService.getAnnotatedSchema(clazz);
            return ResponseEntity.ok(Map.of(
                "className", className,
                "schema", schema,
                "note", "This schema is generated from Jackson annotations and used by the AI model"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage(), 
                                 "availableClasses", new String[]{"UserProfile", "ProductCatalog"}));
        }
    }

    /**
     * Demonstrates property ordering with a sample object.
     * Example: GET /api/property-ordering/demo/user-profile
     */
    @GetMapping("/demo/{type}")
    public ResponseEntity<Map<String, Object>> demonstrateOrdering(@PathVariable String type) {
        try {
            Object sample = switch (type) {
                case "user-profile" -> new UserProfile(
                        "user123",
                        "johndoe",
                        "john@example.com",
                        "John Doe",
                        28,
                        Map.of("theme", "dark", "language", "en", "notifications", true),
                        UserProfile.AccountStatus.ACTIVE,
                        java.time.LocalDateTime.now(),
                        java.util.List.of("developer", "ai-enthusiast"),
                        "Internal note that won't appear in JSON"
                );
                case "product-catalog" -> new ProductCatalog(
                        "cat001",
                        "Electronics Store",
                        java.time.LocalDate.now(),
                        1500,
                        java.util.List.of(
                                new ProductCatalog.Category("cat1", "Laptops", "High-performance computing devices", 250),
                                new ProductCatalog.Category("cat2", "Smartphones", "Latest mobile devices", 400)
                        ),
                        java.util.List.of(
                                new ProductCatalog.FeaturedProduct("prod1", "MacBook Pro", 2499.99, 10, 4.8, ProductCatalog.Availability.IN_STOCK),
                                new ProductCatalog.FeaturedProduct("prod2", "iPhone 15", 999.99, 0, 4.9, ProductCatalog.Availability.LOW_STOCK)
                        )
                );
                default -> throw new IllegalArgumentException("Unknown type: " + type);
            };

            String json = propertyOrderingService.demonstratePropertyOrdering(sample);
            
            return ResponseEntity.ok(Map.of(
                "type", type,
                "orderedJson", json,
                "note", "Notice how fields appear in the order specified by @JsonPropertyOrder"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage(),
                                 "availableTypes", new String[]{"user-profile", "product-catalog"}));
        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to serialize object: " + e.getMessage()));
        }
    }

    /**
     * Validates if a JSON string respects property ordering.
     * Example: POST /api/property-ordering/validate/UserProfile
     */
    @PostMapping("/validate/{className}")
    public ResponseEntity<Map<String, Object>> validatePropertyOrder(
            @PathVariable String className,
            @RequestBody String json) {
        try {
            Class<?> clazz = switch (className) {
                case "UserProfile" -> UserProfile.class;
                case "ProductCatalog" -> ProductCatalog.class;
                default -> throw new IllegalArgumentException("Unknown class: " + className);
            };
            
            boolean isValid = propertyOrderingService.validatePropertyOrder(json, clazz);
            
            return ResponseEntity.ok(Map.of(
                "className", className,
                "isValid", isValid,
                "message", isValid ? "JSON respects property ordering" : "JSON does not maintain property order"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}