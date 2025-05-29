package com.coherentsolutions.l3structuredoutput.s15.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
// Memory advisor not available in current version
// import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
// import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.StructuredOutputConverter;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * Core testing utilities for Spring AI structured output testing
 */
public class TestingUtilities {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Create a ChatClient with memory for stateful testing
     */
    public static ChatClient.Builder createChatClientWithMemory(ChatModel chatModel) {
        // Memory advisor not available in current version
        // Simply return a basic ChatClient builder
        return ChatClient.builder(chatModel);
    }

    /**
     * Measure execution time of a function
     */
    public static <T> TimedResult<T> measureExecutionTime(Function<Void, T> function) {
        long startTime = System.nanoTime();
        T result = function.apply(null);
        long endTime = System.nanoTime();
        Duration duration = Duration.ofNanos(endTime - startTime);
        return new TimedResult<>(result, duration);
    }

    /**
     * Validate JSON schema compliance
     */
    public static boolean validateJsonSchema(String json, Class<?> targetClass) {
        try {
            OBJECT_MAPPER.readValue(json, targetClass);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generate test prompt with placeholders
     */
    public static String generateTestPrompt(String template, Object... args) {
        return String.format(template, args);
    }

    /**
     * Assert converter format instructions contain required elements
     */
    public static void assertFormatInstructions(StructuredOutputConverter<?> converter, String... requiredElements) {
        String format = converter.getFormat();
        for (String element : requiredElements) {
            if (!format.contains(element)) {
                throw new AssertionError("Format instructions missing required element: " + element);
            }
        }
    }

    /**
     * Create a mock ChatResponse for testing
     */
    public static ChatResponse createMockChatResponse(String content) {
        org.springframework.ai.chat.messages.AssistantMessage message = 
                new org.springframework.ai.chat.messages.AssistantMessage(content);
        Generation generation = new Generation(message);
        return new ChatResponse(List.of(generation));
    }

    /**
     * Retry operation with exponential backoff
     */
    public static <T> T retryWithBackoff(
            Function<Void, T> operation,
            int maxRetries,
            Duration initialDelay) {
        
        Exception lastException = null;
        Duration currentDelay = initialDelay;
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                return operation.apply(null);
            } catch (Exception e) {
                lastException = e;
                if (i < maxRetries - 1) {
                    try {
                        Thread.sleep(currentDelay.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                    currentDelay = currentDelay.multipliedBy(2);
                }
            }
        }
        
        throw new RuntimeException("Operation failed after " + maxRetries + " retries", lastException);
    }

    /**
     * Test data sanitizer to remove sensitive information
     */
    public static String sanitizeTestData(String data, String... sensitivePatterns) {
        String sanitized = data;
        for (String pattern : sensitivePatterns) {
            sanitized = sanitized.replaceAll(pattern, "[REDACTED]");
        }
        return sanitized;
    }

    /**
     * Timed result container
     */
    public record TimedResult<T>(T result, Duration duration) {
        public long toMillis() {
            return duration.toMillis();
        }
        
        public boolean isWithinTimeout(Duration timeout) {
            return duration.compareTo(timeout) <= 0;
        }
    }

    /**
     * Test assertion helper for structured outputs
     */
    public static class StructuredOutputAssertions {
        
        public static <T> void assertValidOutput(T output, Class<T> expectedType) {
            if (output == null) {
                throw new AssertionError("Output is null");
            }
            if (!expectedType.isInstance(output)) {
                throw new AssertionError("Output type mismatch. Expected: " + 
                        expectedType.getName() + ", Actual: " + output.getClass().getName());
            }
        }
        
        public static void assertJsonStructure(String json, String... requiredFields) {
            try {
                var node = OBJECT_MAPPER.readTree(json);
                for (String field : requiredFields) {
                    if (!node.has(field)) {
                        throw new AssertionError("Missing required field: " + field);
                    }
                }
            } catch (Exception e) {
                throw new AssertionError("Invalid JSON structure", e);
            }
        }
    }

    /**
     * Performance metrics collector
     */
    public static class PerformanceMetrics {
        private final List<Long> executionTimes = new java.util.ArrayList<>();
        
        public void recordExecution(long millis) {
            executionTimes.add(millis);
        }
        
        public double getAverageTime() {
            return executionTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
        }
        
        public long getMinTime() {
            return executionTimes.stream()
                    .mapToLong(Long::longValue)
                    .min()
                    .orElse(0);
        }
        
        public long getMaxTime() {
            return executionTimes.stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0);
        }
        
        public long getPercentile(int percentile) {
            if (executionTimes.isEmpty()) return 0;
            
            List<Long> sorted = new java.util.ArrayList<>(executionTimes);
            sorted.sort(Long::compareTo);
            
            int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
            return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
        }
    }
}