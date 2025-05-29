package com.coherentsolutions.l3structuredoutput.s15.controllers;

import com.coherentsolutions.l3structuredoutput.s15.fixtures.TestDataFixtures;
import com.coherentsolutions.l3structuredoutput.s15.mocks.MockAIResponseGenerator;
import com.coherentsolutions.l3structuredoutput.s15.mocks.MockChatModel;
import com.coherentsolutions.l3structuredoutput.s15.testing.TestingUtilities;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Controller demonstrating various testing strategies for Spring AI structured output
 */
@RestController
@RequestMapping("/api/testing")
public class TestingStrategiesController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MockAIResponseGenerator responseGenerator = new MockAIResponseGenerator();
    
    /**
     * Demonstrate unit testing with mocks
     */
    @GetMapping("/unit-test-demo")
    public TestResult demonstrateUnitTesting() {
        // Create mock model
        MockChatModel mockModel = new MockChatModel()
                .withTypedResponse(
                        TestDataFixtures.PersonData.class,
                        MockAIResponseGenerator.MockConfig.defaults()
                );
        
        // Create ChatClient with mock
        ChatClient chatClient = ChatClient.builder(mockModel).build();
        
        // Test converter
        BeanOutputConverter<TestDataFixtures.PersonData> converter = 
                new BeanOutputConverter<>(TestDataFixtures.PersonData.class);
        
        // Execute test
        TestDataFixtures.PersonData result = chatClient.prompt()
                .user("Generate a person")
                .call()
                .entity(converter);
        
        // Verify mock was called
        boolean promptVerified = mockModel.verifyPromptCalled("Generate a person");
        
        return new TestResult(
                "Unit Test Demo",
                result != null,
                Map.of(
                        "mockCalled", promptVerified,
                        "resultGenerated", result != null,
                        "callCount", mockModel.getMetrics().totalCalls()
                ),
                result
        );
    }
    
    /**
     * Demonstrate integration testing patterns
     */
    @GetMapping("/integration-test-demo")
    public IntegrationTestResult demonstrateIntegrationTesting() {
        List<TestScenario> scenarios = new ArrayList<>();
        
        // Scenario 1: Happy path
        scenarios.add(testScenario(
                "Happy Path",
                MockAIResponseGenerator.MockConfig.defaults(),
                null
        ));
        
        // Scenario 2: Error handling
        scenarios.add(testScenario(
                "Error Handling",
                MockAIResponseGenerator.MockConfig.defaults(),
                MockAIResponseGenerator.ErrorType.MALFORMED_JSON
        ));
        
        // Scenario 3: Edge cases
        scenarios.add(testScenario(
                "Edge Cases",
                MockAIResponseGenerator.MockConfig.defaults().withFieldValue("age", 999),
                null
        ));
        
        return new IntegrationTestResult(
                "Integration Test Demo",
                scenarios,
                calculateSuccessRate(scenarios)
        );
    }
    
    /**
     * Demonstrate performance testing
     */
    @GetMapping("/performance-test-demo")
    public PerformanceTestResult demonstratePerformanceTesting() {
        int iterations = 100;
        List<Long> executionTimes = new ArrayList<>();
        
        MockChatModel mockModel = new MockChatModel()
                .withTypedResponse(
                        TestDataFixtures.ProductData.class,
                        MockAIResponseGenerator.MockConfig.defaults()
                );
        
        ChatClient chatClient = ChatClient.builder(mockModel).build();
        BeanOutputConverter<TestDataFixtures.ProductData> converter = 
                new BeanOutputConverter<>(TestDataFixtures.ProductData.class);
        
        // Warm up
        for (int i = 0; i < 10; i++) {
            chatClient.prompt().user("Warmup").call().entity(converter);
        }
        
        // Performance test
        for (int i = 0; i < iterations; i++) {
            final int index = i;
            var timedResult = TestingUtilities.measureExecutionTime(v -> 
                    chatClient.prompt()
                            .user("Generate product " + index)
                            .call()
                            .entity(converter)
            );
            executionTimes.add(timedResult.toMillis());
        }
        
        TestingUtilities.PerformanceMetrics metrics = new TestingUtilities.PerformanceMetrics();
        executionTimes.forEach(metrics::recordExecution);
        
        return new PerformanceTestResult(
                iterations,
                metrics.getAverageTime(),
                metrics.getMinTime(),
                metrics.getMaxTime(),
                metrics.getPercentile(95),
                metrics.getPercentile(99)
        );
    }
    
    /**
     * Demonstrate test data generation
     */
    @GetMapping("/test-data-demo")
    public TestDataDemo demonstrateTestDataGeneration() {
        return new TestDataDemo(
                TestDataFixtures.Generators.generatePerson(),
                TestDataFixtures.BatchGenerators.generateProducts(5),
                TestDataFixtures.EdgeCaseGenerators.generateMaximalPerson(),
                TestDataFixtures.ValidationFixtures.getValidEmails(),
                TestDataFixtures.ValidationFixtures.getInvalidEmails()
        );
    }
    
    /**
     * Demonstrate error scenario testing
     */
    @PostMapping("/error-testing")
    public ErrorTestingResult testErrorScenarios(@RequestBody ErrorTestRequest request) {
        Map<String, ErrorTestResult> results = new HashMap<>();
        
        for (MockAIResponseGenerator.ErrorType errorType : MockAIResponseGenerator.ErrorType.values()) {
            String errorResponse = responseGenerator.generateErrorResponse(
                    errorType, 
                    TestDataFixtures.PersonData.class
            );
            
            BeanOutputConverter<TestDataFixtures.PersonData> converter = 
                    new BeanOutputConverter<>(TestDataFixtures.PersonData.class);
            
            ErrorTestResult testResult;
            try {
                TestDataFixtures.PersonData result = converter.convert(errorResponse);
                testResult = new ErrorTestResult(false, "No error thrown", result);
            } catch (Exception e) {
                testResult = new ErrorTestResult(true, e.getMessage(), null);
            }
            
            results.put(errorType.name(), testResult);
        }
        
        return new ErrorTestingResult(results);
    }
    
    /**
     * Demonstrate concurrent testing
     */
    @GetMapping("/concurrent-test-demo")
    public ConcurrentTestResult demonstrateConcurrentTesting() {
        int threadCount = 10;
        int requestsPerThread = 5;
        
        MockChatModel mockModel = new MockChatModel()
                .withTypedResponse(
                        TestDataFixtures.OrderData.class,
                        MockAIResponseGenerator.MockConfig.defaults()
                );
        
        ChatClient chatClient = ChatClient.builder(mockModel).build();
        BeanOutputConverter<TestDataFixtures.OrderData> converter = 
                new BeanOutputConverter<>(TestDataFixtures.OrderData.class);
        
        long startTime = System.currentTimeMillis();
        
        List<CompletableFuture<TestDataFixtures.OrderData>> futures = new ArrayList<>();
        
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            for (int r = 0; r < requestsPerThread; r++) {
                final int requestId = r;
                CompletableFuture<TestDataFixtures.OrderData> future = 
                        CompletableFuture.supplyAsync(() ->
                                chatClient.prompt()
                                        .user(String.format("Order T%d-R%d", threadId, requestId))
                                        .call()
                                        .entity(converter)
                        );
                futures.add(future);
            }
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long duration = System.currentTimeMillis() - startTime;
        
        List<TestDataFixtures.OrderData> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        
        return new ConcurrentTestResult(
                threadCount,
                requestsPerThread,
                results.size(),
                duration,
                (results.size() * 1000.0) / duration,
                mockModel.getMetrics()
        );
    }
    
    /**
     * Demonstrate retry and resilience testing
     */
    @PostMapping("/resilience-test")
    public ResilienceTestResult testResilience(@RequestBody ResilienceTestRequest request) {
        MockChatModel mockModel = new MockChatModel()
                .withErrorSimulation(true, request.errorRate())
                .withResponseDelay(request.responseDelayMs());
        
        ChatClient chatClient = ChatClient.builder(mockModel).build();
        BeanOutputConverter<TestDataFixtures.WeatherData> converter = 
                new BeanOutputConverter<>(TestDataFixtures.WeatherData.class);
        
        List<AttemptResult> attempts = new ArrayList<>();
        
        for (int i = 0; i < request.attempts(); i++) {
            long attemptStart = System.currentTimeMillis();
            boolean success = false;
            String error = null;
            
            try {
                TestDataFixtures.WeatherData result = TestingUtilities.retryWithBackoff(
                        v -> chatClient.prompt()
                                .user("Get weather")
                                .call()
                                .entity(converter),
                        request.maxRetries(),
                        java.time.Duration.ofMillis(request.retryDelayMs())
                );
                success = result != null;
            } catch (Exception e) {
                error = e.getMessage();
            }
            
            long attemptDuration = System.currentTimeMillis() - attemptStart;
            attempts.add(new AttemptResult(i + 1, success, attemptDuration, error));
        }
        
        long successCount = attempts.stream().filter(AttemptResult::success).count();
        
        return new ResilienceTestResult(
                request,
                attempts,
                (double) successCount / request.attempts(),
                mockModel.getMetrics()
        );
    }
    
    // Helper methods
    
    private TestScenario testScenario(String name, MockAIResponseGenerator.MockConfig config, 
                                      MockAIResponseGenerator.ErrorType errorType) {
        MockChatModel mockModel = new MockChatModel();
        
        if (errorType != null) {
            mockModel.withErrorResponse(errorType, TestDataFixtures.PersonData.class);
        } else {
            mockModel.withTypedResponse(TestDataFixtures.PersonData.class, config);
        }
        
        ChatClient chatClient = ChatClient.builder(mockModel).build();
        BeanOutputConverter<TestDataFixtures.PersonData> converter = 
                new BeanOutputConverter<>(TestDataFixtures.PersonData.class);
        
        boolean success = false;
        String errorMessage = null;
        Object result = null;
        
        try {
            result = chatClient.prompt()
                    .user("Test scenario: " + name)
                    .call()
                    .entity(converter);
            success = result != null;
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }
        
        return new TestScenario(name, success, errorMessage, result);
    }
    
    private double calculateSuccessRate(List<TestScenario> scenarios) {
        long successCount = scenarios.stream().filter(TestScenario::success).count();
        return scenarios.isEmpty() ? 0.0 : (double) successCount / scenarios.size();
    }
    
    // DTOs
    
    public record TestResult(
            String testName,
            boolean success,
            Map<String, Object> metadata,
            Object result
    ) {}
    
    public record IntegrationTestResult(
            String testSuite,
            List<TestScenario> scenarios,
            double successRate
    ) {}
    
    public record TestScenario(
            String name,
            boolean success,
            String error,
            Object result
    ) {}
    
    public record PerformanceTestResult(
            int iterations,
            double averageTimeMs,
            long minTimeMs,
            long maxTimeMs,
            long p95TimeMs,
            long p99TimeMs
    ) {}
    
    public record TestDataDemo(
            TestDataFixtures.PersonData samplePerson,
            List<TestDataFixtures.ProductData> sampleProducts,
            TestDataFixtures.PersonData edgeCasePerson,
            List<String> validEmails,
            List<String> invalidEmails
    ) {}
    
    public record ErrorTestRequest(
            Class<?> targetClass
    ) {}
    
    public record ErrorTestingResult(
            Map<String, ErrorTestResult> errorTypeResults
    ) {}
    
    public record ErrorTestResult(
            boolean errorThrown,
            String errorMessage,
            Object partialResult
    ) {}
    
    public record ConcurrentTestResult(
            int threadCount,
            int requestsPerThread,
            int totalSuccessful,
            long totalDurationMs,
            double requestsPerSecond,
            MockChatModel.TestMetrics metrics
    ) {}
    
    public record ResilienceTestRequest(
            int attempts,
            int errorRate,
            long responseDelayMs,
            int maxRetries,
            long retryDelayMs
    ) {}
    
    public record ResilienceTestResult(
            ResilienceTestRequest request,
            List<AttemptResult> attempts,
            double successRate,
            MockChatModel.TestMetrics metrics
    ) {}
    
    public record AttemptResult(
            int attemptNumber,
            boolean success,
            long durationMs,
            String error
    ) {}
}