package com.coherentsolutions.l3structuredoutput.s15.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Base test class providing common setup and utilities for structured output tests
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.openai.api-key=test-key",
    "spring.ai.openai.chat.options.model=gpt-3.5-turbo",
    "spring.ai.retry.max-attempts=3",
    "spring.ai.retry.backoff.initial-interval=1000"
})
public abstract class BaseStructuredOutputTest {

    protected ObjectMapper objectMapper;
    protected TestDataGenerator testDataGenerator;
    protected PerformanceProfiler performanceProfiler;
    
    @BeforeEach
    void setupBase() {
        this.objectMapper = new ObjectMapper();
        this.testDataGenerator = new TestDataGenerator();
        this.performanceProfiler = new PerformanceProfiler();
        
        // Allow subclasses to add their setup
        setUp();
    }
    
    /**
     * Override in subclasses for additional setup
     */
    protected void setUp() {
        // Default empty implementation
    }
    
    /**
     * Create a BeanOutputConverter with performance tracking
     */
    protected <T> BeanOutputConverter<T> createTrackedBeanConverter(Class<T> targetClass) {
        return performanceProfiler.track(
                "BeanConverter-" + targetClass.getSimpleName(),
                () -> new BeanOutputConverter<>(targetClass)
        );
    }
    
    /**
     * Create a MapOutputConverter with performance tracking
     */
    protected MapOutputConverter createTrackedMapConverter() {
        return performanceProfiler.track(
                "MapConverter",
                MapOutputConverter::new
        );
    }
    
    /**
     * Create a ListOutputConverter with performance tracking  
     */
    protected ListOutputConverter createTrackedListConverter() {
        return performanceProfiler.track(
                "ListConverter",
                ListOutputConverter::new
        );
    }
    
    /**
     * Assert that a converter produces valid output for given input
     */
    protected <T> void assertConverterProducesValidOutput(
            BeanOutputConverter<T> converter,
            String mockResponse,
            Class<T> expectedType) {
        
        T result = converter.convert(mockResponse);
        TestingUtilities.StructuredOutputAssertions.assertValidOutput(result, expectedType);
    }
    
    /**
     * Performance profiler for tracking converter operations
     */
    protected static class PerformanceProfiler {
        private final TestingUtilities.PerformanceMetrics metrics = new TestingUtilities.PerformanceMetrics();
        
        public <T> T track(String operation, java.util.function.Supplier<T> supplier) {
            var timedResult = TestingUtilities.measureExecutionTime(v -> supplier.get());
            metrics.recordExecution(timedResult.toMillis());
            
            System.out.printf("Operation '%s' took %d ms%n", 
                    operation, timedResult.toMillis());
            
            return timedResult.result();
        }
        
        public void printSummary() {
            System.out.println("\n=== Performance Summary ===");
            System.out.printf("Average: %.2f ms%n", metrics.getAverageTime());
            System.out.printf("Min: %d ms%n", metrics.getMinTime());
            System.out.printf("Max: %d ms%n", metrics.getMaxTime());
            System.out.printf("P95: %d ms%n", metrics.getPercentile(95));
            System.out.printf("P99: %d ms%n", metrics.getPercentile(99));
        }
    }
    
    /**
     * Test data generator
     */
    protected static class TestDataGenerator {
        private final java.util.Random random = new java.util.Random();
        
        public String generatePrompt(int length) {
            return "Test prompt ".repeat(Math.max(1, length / 12));
        }
        
        public String generateJsonResponse(String template, Object... values) {
            return String.format(template, values);
        }
        
        public String generateRandomString(int length) {
            return random.ints('a', 'z' + 1)
                    .limit(length)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        }
        
        public int generateRandomInt(int min, int max) {
            return random.nextInt(max - min + 1) + min;
        }
        
        public double generateRandomDouble(double min, double max) {
            return min + (max - min) * random.nextDouble();
        }
    }
}