package com.coherentsolutions.l3structuredoutput.s15.fixtures;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Test data fixtures and generators for structured output testing
 */
public class TestDataFixtures {

    private static final Faker faker = new Faker();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Random random = new Random();
    
    /**
     * Domain-specific test data generators
     */
    public static class Generators {
        
        public static PersonData generatePerson() {
            return new PersonData(
                    faker.name().fullName(),
                    faker.number().numberBetween(18, 80),
                    faker.internet().emailAddress(),
                    faker.phoneNumber().phoneNumber(),
                    generateAddress()
            );
        }
        
        public static AddressData generateAddress() {
            return new AddressData(
                    faker.address().streetAddress(),
                    faker.address().city(),
                    faker.address().state(),
                    faker.address().zipCode(),
                    faker.address().country()
            );
        }
        
        public static ProductData generateProduct() {
            return new ProductData(
                    UUID.randomUUID().toString(),
                    faker.commerce().productName(),
                    faker.lorem().sentence(),
                    faker.number().randomDouble(2, 10, 1000),
                    faker.number().numberBetween(0, 100),
                    faker.commerce().department(),
                    generateTags(3, 5)
            );
        }
        
        public static OrderData generateOrder() {
            int itemCount = faker.number().numberBetween(1, 5);
            List<OrderItemData> items = IntStream.range(0, itemCount)
                    .mapToObj(i -> generateOrderItem())
                    .collect(Collectors.toList());
            
            double total = items.stream()
                    .mapToDouble(item -> item.price() * item.quantity())
                    .sum();
            
            return new OrderData(
                    UUID.randomUUID().toString(),
                    faker.code().isbn10(),
                    LocalDateTime.now().minusDays(faker.number().numberBetween(0, 30)),
                    items,
                    total,
                    faker.options().option("PENDING", "PROCESSING", "SHIPPED", "DELIVERED"),
                    generateAddress()
            );
        }
        
        public static OrderItemData generateOrderItem() {
            ProductData product = generateProduct();
            int quantity = faker.number().numberBetween(1, 5);
            return new OrderItemData(
                    product.id(),
                    product.name(),
                    product.price(),
                    quantity
            );
        }
        
        public static WeatherData generateWeather() {
            return new WeatherData(
                    faker.address().city(),
                    faker.number().numberBetween(-10, 40),
                    faker.number().numberBetween(0, 100),
                    faker.options().option("Sunny", "Cloudy", "Rainy", "Snowy", "Foggy"),
                    faker.number().randomDouble(1, 0, 50),
                    faker.number().numberBetween(950, 1050)
            );
        }
        
        public static AnalyticsData generateAnalytics() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            Map<String, Integer> dailyVisits = new HashMap<>();
            Map<String, Double> pageMetrics = new HashMap<>();
            
            for (int i = 0; i < 30; i++) {
                String date = startDate.plusDays(i).toString();
                dailyVisits.put(date, faker.number().numberBetween(100, 1000));
            }
            
            List<String> pages = List.of("/home", "/products", "/about", "/contact");
            pages.forEach(page -> 
                    pageMetrics.put(page, faker.number().randomDouble(2, 0, 100))
            );
            
            return new AnalyticsData(
                    faker.internet().domainName(),
                    dailyVisits,
                    pageMetrics,
                    faker.number().numberBetween(1000, 10000),
                    faker.number().randomDouble(2, 1, 5),
                    faker.number().randomDouble(2, 0, 100)
            );
        }
        
        private static List<String> generateTags(int min, int max) {
            int count = faker.number().numberBetween(min, max);
            return IntStream.range(0, count)
                    .mapToObj(i -> faker.lorem().word())
                    .distinct()
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Fixture templates for common test scenarios
     */
    public static class Templates {
        
        public static String personJsonTemplate() {
            return """
                {
                    "name": "%s",
                    "age": %d,
                    "email": "%s",
                    "phone": "%s",
                    "address": {
                        "street": "%s",
                        "city": "%s",
                        "state": "%s",
                        "zipCode": "%s",
                        "country": "%s"
                    }
                }
                """;
        }
        
        public static String productJsonTemplate() {
            return """
                {
                    "id": "%s",
                    "name": "%s",
                    "description": "%s",
                    "price": %.2f,
                    "stock": %d,
                    "category": "%s",
                    "tags": %s
                }
                """;
        }
        
        public static String errorResponseTemplate() {
            return """
                {
                    "error": "%s",
                    "message": "%s",
                    "timestamp": "%s",
                    "details": %s
                }
                """;
        }
    }
    
    /**
     * Batch data generators
     */
    public static class BatchGenerators {
        
        public static <T> List<T> generateBatch(Supplier<T> generator, int size) {
            return IntStream.range(0, size)
                    .mapToObj(i -> generator.get())
                    .collect(Collectors.toList());
        }
        
        public static List<PersonData> generatePeople(int count) {
            return generateBatch(Generators::generatePerson, count);
        }
        
        public static List<ProductData> generateProducts(int count) {
            return generateBatch(Generators::generateProduct, count);
        }
        
        public static List<OrderData> generateOrders(int count) {
            return generateBatch(Generators::generateOrder, count);
        }
        
        public static Map<String, List<?>> generateMixedDataset() {
            return Map.of(
                    "people", generatePeople(10),
                    "products", generateProducts(20),
                    "orders", generateOrders(5),
                    "weather", List.of(Generators.generateWeather()),
                    "analytics", List.of(Generators.generateAnalytics())
            );
        }
    }
    
    /**
     * Edge case generators
     */
    public static class EdgeCaseGenerators {
        
        public static PersonData generateMinimalPerson() {
            return new PersonData("", 0, "", null, null);
        }
        
        public static PersonData generateMaximalPerson() {
            return new PersonData(
                    "A".repeat(100),
                    Integer.MAX_VALUE,
                    "test@" + "x".repeat(50) + ".com",
                    "+1".repeat(20),
                    generateMaximalAddress()
            );
        }
        
        public static AddressData generateMaximalAddress() {
            return new AddressData(
                    "Street".repeat(20),
                    "City".repeat(10),
                    "State".repeat(5),
                    "12345-6789",
                    "Country".repeat(5)
            );
        }
        
        public static ProductData generateFreeProduct() {
            return new ProductData(
                    UUID.randomUUID().toString(),
                    "Free Product",
                    "This product is free!",
                    0.0,
                    1000,
                    "Free",
                    List.of("free", "promotion")
            );
        }
        
        public static ProductData generateExpensiveProduct() {
            return new ProductData(
                    UUID.randomUUID().toString(),
                    "Luxury Item",
                    "Very expensive product",
                    999999.99,
                    1,
                    "Luxury",
                    List.of("expensive", "luxury", "premium")
            );
        }
        
        public static Map<String, Object> generateDeeplyNestedStructure(int depth) {
            if (depth <= 0) {
                return Map.of("value", faker.lorem().word());
            }
            
            return Map.of(
                    "level", depth,
                    "data", faker.lorem().sentence(),
                    "nested", generateDeeplyNestedStructure(depth - 1)
            );
        }
    }
    
    /**
     * Validation test data
     */
    public static class ValidationFixtures {
        
        public static List<String> getValidEmails() {
            return List.of(
                    "user@example.com",
                    "test.user@domain.co.uk",
                    "name+tag@company.org",
                    "123@numbers.com"
            );
        }
        
        public static List<String> getInvalidEmails() {
            return List.of(
                    "notanemail",
                    "@missing-user.com",
                    "user@",
                    "user@.com",
                    "user..double@domain.com"
            );
        }
        
        public static List<String> getValidUrls() {
            return List.of(
                    "https://example.com",
                    "http://subdomain.example.com/path",
                    "https://example.com:8080/path?query=value",
                    "ftp://files.example.com"
            );
        }
        
        public static List<String> getInvalidUrls() {
            return List.of(
                    "not a url",
                    "htp://wrong-protocol.com",
                    "//missing-protocol.com",
                    "https://",
                    "https://[invalid]"
            );
        }
    }
    
    // Data models
    
    public record PersonData(
            @JsonPropertyDescription("Full name") String name,
            @JsonPropertyDescription("Age in years") int age,
            @JsonPropertyDescription("Email address") String email,
            @JsonPropertyDescription("Phone number") String phone,
            @JsonPropertyDescription("Home address") AddressData address
    ) {}
    
    public record AddressData(
            String street,
            String city,
            String state,
            String zipCode,
            String country
    ) {}
    
    public record ProductData(
            String id,
            String name,
            String description,
            double price,
            int stock,
            String category,
            List<String> tags
    ) {}
    
    public record OrderData(
            String orderId,
            String customerId,
            LocalDateTime orderDate,
            List<OrderItemData> items,
            double totalAmount,
            String status,
            AddressData shippingAddress
    ) {}
    
    public record OrderItemData(
            String productId,
            String productName,
            double price,
            int quantity
    ) {}
    
    public record WeatherData(
            String location,
            int temperature,
            int humidity,
            String conditions,
            double windSpeed,
            int pressure
    ) {}
    
    public record AnalyticsData(
            String website,
            Map<String, Integer> dailyVisits,
            Map<String, Double> pageMetrics,
            int totalVisitors,
            double avgSessionDuration,
            double bounceRate
    ) {}
}