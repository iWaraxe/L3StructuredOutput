package com.coherentsolutions.l3structuredoutput.s15.mocks;

import org.springframework.ai.chat.model.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptions;
import org.springframework.ai.model.ModelRequest;
import org.springframework.ai.model.ModelResponse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Mock ChatModel implementation for testing
 */
public class MockChatModel implements ChatModel {

    private final Map<String, Function<Prompt, ChatResponse>> responseHandlers = new ConcurrentHashMap<>();
    private final Map<String, ChatResponse> cannedResponses = new ConcurrentHashMap<>();
    private final AtomicInteger callCount = new AtomicInteger(0);
    private final List<Prompt> promptHistory = Collections.synchronizedList(new ArrayList<>());
    private final MockAIResponseGenerator responseGenerator = new MockAIResponseGenerator();
    
    private Function<Prompt, ChatResponse> defaultHandler;
    private boolean simulateErrors = false;
    private int errorRate = 10; // 10% error rate when enabled
    private long responseDelay = 0; // milliseconds
    
    public MockChatModel() {
        this.defaultHandler = prompt -> {
            String content = "{\"result\": \"Mock response for: " + prompt.getContents() + "\"}";
            return responseGenerator.generateMockChatResponse(content);
        };
    }
    
    @Override
    public ChatResponse call(Prompt prompt) {
        callCount.incrementAndGet();
        promptHistory.add(prompt);
        
        // Simulate delay if configured
        if (responseDelay > 0) {
            try {
                Thread.sleep(responseDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Simulate errors if enabled
        if (simulateErrors && new Random().nextInt(100) < errorRate) {
            throw new RuntimeException("Simulated API error");
        }
        
        // Check for canned responses
        String promptText = prompt.getContents();
        for (Map.Entry<String, ChatResponse> entry : cannedResponses.entrySet()) {
            if (promptText.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Check for specific handlers
        for (Map.Entry<String, Function<Prompt, ChatResponse>> entry : responseHandlers.entrySet()) {
            if (promptText.contains(entry.getKey())) {
                return entry.getValue().apply(prompt);
            }
        }
        
        // Use default handler
        return defaultHandler.apply(prompt);
    }
    
    
    /**
     * Configure mock responses
     */
    public MockChatModel withResponse(String promptContains, String response) {
        cannedResponses.put(promptContains, responseGenerator.generateMockChatResponse(response));
        return this;
    }
    
    /**
     * Configure mock response handler
     */
    public MockChatModel withResponseHandler(String promptContains, Function<Prompt, ChatResponse> handler) {
        responseHandlers.put(promptContains, handler);
        return this;
    }
    
    /**
     * Configure default response handler
     */
    public MockChatModel withDefaultHandler(Function<Prompt, ChatResponse> handler) {
        this.defaultHandler = handler;
        return this;
    }
    
    /**
     * Enable error simulation
     */
    public MockChatModel withErrorSimulation(boolean enable, int errorRatePercent) {
        this.simulateErrors = enable;
        this.errorRate = errorRatePercent;
        return this;
    }
    
    /**
     * Configure response delay
     */
    public MockChatModel withResponseDelay(long delayMs) {
        this.responseDelay = delayMs;
        return this;
    }
    
    /**
     * Generate response based on expected type
     */
    public <T> MockChatModel withTypedResponse(Class<T> expectedType, MockAIResponseGenerator.MockConfig config) {
        String mockResponse = responseGenerator.generateMockResponse(expectedType, config);
        this.defaultHandler = prompt -> responseGenerator.generateMockChatResponse(mockResponse);
        return this;
    }
    
    /**
     * Generate error response
     */
    public MockChatModel withErrorResponse(MockAIResponseGenerator.ErrorType errorType, Class<?> targetType) {
        String errorResponse = responseGenerator.generateErrorResponse(errorType, targetType);
        this.defaultHandler = prompt -> responseGenerator.generateMockChatResponse(errorResponse);
        return this;
    }
    
    /**
     * Get test metrics
     */
    public TestMetrics getMetrics() {
        return new TestMetrics(
                callCount.get(),
                new ArrayList<>(promptHistory),
                getAveragePromptLength(),
                getUniquePromptCount()
        );
    }
    
    /**
     * Reset mock state
     */
    public void reset() {
        callCount.set(0);
        promptHistory.clear();
        cannedResponses.clear();
        responseHandlers.clear();
        simulateErrors = false;
        responseDelay = 0;
    }
    
    /**
     * Verify prompt was called
     */
    public boolean verifyPromptCalled(String expectedContent) {
        return promptHistory.stream()
                .anyMatch(prompt -> prompt.getContents().contains(expectedContent));
    }
    
    /**
     * Verify prompt was called with specific count
     */
    public boolean verifyPromptCalledTimes(String expectedContent, int times) {
        long count = promptHistory.stream()
                .filter(prompt -> prompt.getContents().contains(expectedContent))
                .count();
        return count == times;
    }
    
    private double getAveragePromptLength() {
        return promptHistory.stream()
                .mapToInt(prompt -> prompt.getContents().length())
                .average()
                .orElse(0.0);
    }
    
    private int getUniquePromptCount() {
        return (int) promptHistory.stream()
                .map(Prompt::getContents)
                .distinct()
                .count();
    }
    
    /**
     * Test metrics record
     */
    public record TestMetrics(
            int totalCalls,
            List<Prompt> promptHistory,
            double averagePromptLength,
            int uniquePromptCount
    ) {
        public void printSummary() {
            System.out.println("\n=== Mock ChatModel Metrics ===");
            System.out.printf("Total calls: %d%n", totalCalls);
            System.out.printf("Unique prompts: %d%n", uniquePromptCount);
            System.out.printf("Average prompt length: %.2f chars%n", averagePromptLength);
        }
    }
}