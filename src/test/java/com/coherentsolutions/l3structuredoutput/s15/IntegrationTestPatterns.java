package com.coherentsolutions.l3structuredoutput.s15.testing;

import com.coherentsolutions.l3structuredoutput.s15.mocks.MockChatModel;
import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration testing patterns for Spring AI structured output
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class IntegrationTestPatterns {

    @TestConfiguration
    static class TestConfig {
        
        @Bean
        @Primary
        public ChatModel mockChatModel() {
            return new MockChatModel()
                    .withTypedResponse(WeatherData.class, 
                            com.coherentsolutions.l3structuredoutput.s15.mocks.MockAIResponseGenerator.MockConfig.defaults());
        }
        
        @Bean
        public ChatClient chatClient(ChatModel chatModel) {
            return ChatClient.builder(chatModel).build();
        }
    }
    
    @Container
    static GenericContainer<?> mockApiContainer = new GenericContainer<>(DockerImageName.parse("mockserver/mockserver:latest"))
            .withExposedPorts(1080);
    
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.ai.openai.base-url", 
                () -> "http://localhost:" + mockApiContainer.getMappedPort(1080));
    }
    
    @Autowired
    private ChatClient chatClient;
    
    @Autowired
    private ChatModel chatModel;
    
    @Nested
    @DisplayName("End-to-End Integration Tests")
    class EndToEndTests {
        
        @Test
        @DisplayName("Should complete full request-response cycle")
        void testFullRequestResponseCycle() {
            // Given
            String prompt = "What's the weather in New York?";
            BeanOutputConverter<WeatherData> converter = new BeanOutputConverter<>(WeatherData.class);
            
            // When
            WeatherData weather = chatClient.prompt()
                    .user(prompt + "\n\n" + converter.getFormat())
                    .call()
                    .entity(converter);
            
            // Then
            assertThat(weather).isNotNull();
            assertThat(weather.location()).isNotNull();
            assertThat(weather.temperature()).isNotNull();
            
            // Verify mock was called
            MockChatModel mockModel = (MockChatModel) chatModel;
            assertThat(mockModel.verifyPromptCalled(prompt)).isTrue();
        }
        
        @Test
        @DisplayName("Should handle concurrent requests")
        void testConcurrentRequests() throws Exception {
            // Given
            int concurrentRequests = 10;
            BeanOutputConverter<WeatherData> converter = new BeanOutputConverter<>(WeatherData.class);
            
            // When
            List<CompletableFuture<WeatherData>> futures = IntStream.range(0, concurrentRequests)
                    .mapToObj(i -> CompletableFuture.supplyAsync(() ->
                            chatClient.prompt()
                                    .user("Weather in city " + i + "\n\n" + converter.getFormat())
                                    .call()
                                    .entity(converter)
                    ))
                    .toList();
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(30, TimeUnit.SECONDS);
            
            // Then
            List<WeatherData> results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
            
            assertThat(results).hasSize(concurrentRequests);
            assertThat(results).allMatch(weather -> weather != null && weather.location() != null);
        }
        
        @Test
        @DisplayName("Should handle service degradation gracefully")
        void testServiceDegradation() {
            // Given
            MockChatModel mockModel = (MockChatModel) chatModel;
            mockModel.withResponseDelay(5000); // 5 second delay
            
            BeanOutputConverter<WeatherData> converter = new BeanOutputConverter<>(WeatherData.class);
            
            // When/Then - should timeout
            assertThatThrownBy(() -> 
                    chatClient.prompt()
                            .user("Weather forecast")
                            .call()
                            .entity(converter)
            ).isInstanceOf(Exception.class);
            
            // Reset
            mockModel.withResponseDelay(0);
        }
    }
    
    @Nested
    @DisplayName("Multi-Provider Integration Tests")
    class MultiProviderTests {
        
        @Test
        @DisplayName("Should work with different AI providers")
        void testMultipleProviders() {
            // This would test different providers in a real scenario
            // For now, we simulate different response formats
            
            MockChatModel mockModel = (MockChatModel) chatModel;
            
            // Test OpenAI-style response
            mockModel.withResponse("openai-test", """
                {
                    "location": "San Francisco",
                    "temperature": 18,
                    "conditions": "Foggy"
                }
                """);
            
            // Test Anthropic-style response
            mockModel.withResponse("anthropic-test", """
                {
                    "location": "Paris",
                    "temperature": 22,
                    "conditions": "Sunny"
                }
                """);
            
            BeanOutputConverter<WeatherData> converter = new BeanOutputConverter<>(WeatherData.class);
            
            // Test both
            WeatherData openAiResult = chatClient.prompt()
                    .user("openai-test")
                    .call()
                    .entity(converter);
            
            WeatherData anthropicResult = chatClient.prompt()
                    .user("anthropic-test")
                    .call()
                    .entity(converter);
            
            assertThat(openAiResult.location()).isEqualTo("San Francisco");
            assertThat(anthropicResult.location()).isEqualTo("Paris");
        }
    }
    
    @Nested
    @DisplayName("Error Recovery Integration Tests")
    class ErrorRecoveryTests {
        
        @Test
        @DisplayName("Should recover from transient failures")
        void testTransientFailureRecovery() {
            // Given
            MockChatModel mockModel = (MockChatModel) chatModel;
            mockModel.withErrorSimulation(true, 30); // 30% error rate
            
            BeanOutputConverter<WeatherData> converter = new BeanOutputConverter<>(WeatherData.class);
            
            int successCount = 0;
            int attempts = 10;
            
            // When
            for (int i = 0; i < attempts; i++) {
                try {
                    WeatherData result = chatClient.prompt()
                            .user("Weather test " + i)
                            .call()
                            .entity(converter);
                    if (result != null) {
                        successCount++;
                    }
                } catch (Exception e) {
                    // Expected some failures
                }
            }
            
            // Then - should have some successes despite errors
            assertThat(successCount).isGreaterThan(0);
            assertThat(successCount).isLessThan(attempts);
            
            // Reset
            mockModel.withErrorSimulation(false, 0);
        }
        
        @Test
        @DisplayName("Should handle circuit breaker scenarios")
        void testCircuitBreakerBehavior() {
            // Given
            MockChatModel mockModel = (MockChatModel) chatModel;
            mockModel.withErrorSimulation(true, 100); // Always fail
            
            BeanOutputConverter<WeatherData> converter = new BeanOutputConverter<>(WeatherData.class);
            
            int failureCount = 0;
            
            // When - make several failing requests
            for (int i = 0; i < 5; i++) {
                try {
                    chatClient.prompt()
                            .user("Test " + i)
                            .call()
                            .entity(converter);
                } catch (Exception e) {
                    failureCount++;
                }
            }
            
            // Then
            assertThat(failureCount).isEqualTo(5);
            
            // In a real scenario, circuit breaker would open and fast-fail subsequent requests
            
            // Reset
            mockModel.withErrorSimulation(false, 0);
        }
    }
    
    @Nested
    @DisplayName("Performance Integration Tests")
    class PerformanceIntegrationTests {
        
        @Test
        @DisplayName("Should maintain performance under load")
        void testPerformanceUnderLoad() {
            // Given
            int requestCount = 100;
            BeanOutputConverter<WeatherData> converter = new BeanOutputConverter<>(WeatherData.class);
            
            // When
            long startTime = System.currentTimeMillis();
            
            List<CompletableFuture<WeatherData>> futures = IntStream.range(0, requestCount)
                    .mapToObj(i -> CompletableFuture.supplyAsync(() ->
                            chatClient.prompt()
                                    .user("Performance test " + i)
                                    .call()
                                    .entity(converter)
                    ))
                    .toList();
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Then
            double requestsPerSecond = (requestCount * 1000.0) / duration;
            assertThat(requestsPerSecond).isGreaterThan(10); // At least 10 req/s
            
            // Verify all succeeded
            List<WeatherData> results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
            assertThat(results).hasSize(requestCount);
        }
    }
    
    // Test models
    
    public record WeatherData(
            String location,
            Integer temperature,
            String conditions,
            Map<String, Object> details
    ) {}
    
    // Helper methods
    
    private java.util.stream.IntStream IntStream = java.util.stream.IntStream;
}