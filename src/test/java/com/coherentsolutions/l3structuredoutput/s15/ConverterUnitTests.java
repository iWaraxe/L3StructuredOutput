package com.coherentsolutions.l3structuredoutput.s15.testing;

import com.coherentsolutions.l3structuredoutput.s15.mocks.MockAIResponseGenerator;
import com.coherentsolutions.l3structuredoutput.s15.mocks.MockChatModel;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for structured output converters
 */
public class ConverterUnitTests extends BaseStructuredOutputTest {

    private MockChatModel mockChatModel;
    private MockAIResponseGenerator responseGenerator;
    private ChatClient chatClient;
    
    @Override
    protected void setUp() {
        mockChatModel = new MockChatModel();
        responseGenerator = new MockAIResponseGenerator();
        chatClient = ChatClient.builder(mockChatModel).build();
    }
    
    @Nested
    @DisplayName("BeanOutputConverter Tests")
    class BeanOutputConverterTests {
        
        @Test
        @DisplayName("Should convert valid JSON to bean")
        void testValidJsonConversion() {
            // Given
            String mockResponse = """
                {
                    "name": "John Doe",
                    "age": 30,
                    "email": "john.doe@example.com"
                }
                """;
            
            BeanOutputConverter<TestPerson> converter = new BeanOutputConverter<>(TestPerson.class);
            
            // When
            TestPerson result = converter.convert(mockResponse);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("John Doe");
            assertThat(result.age()).isEqualTo(30);
            assertThat(result.email()).isEqualTo("john.doe@example.com");
        }
        
        @Test
        @DisplayName("Should handle missing optional fields")
        void testMissingOptionalFields() {
            // Given
            String mockResponse = """
                {
                    "name": "Jane Doe",
                    "age": 25
                }
                """;
            
            BeanOutputConverter<TestPerson> converter = new BeanOutputConverter<>(TestPerson.class);
            
            // When
            TestPerson result = converter.convert(mockResponse);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Jane Doe");
            assertThat(result.age()).isEqualTo(25);
            assertThat(result.email()).isNull();
        }
        
        @Test
        @DisplayName("Should throw exception for malformed JSON")
        void testMalformedJson() {
            // Given
            String malformedJson = "{\"name\": \"Invalid";
            BeanOutputConverter<TestPerson> converter = new BeanOutputConverter<>(TestPerson.class);
            
            // When/Then
            assertThatThrownBy(() -> converter.convert(malformedJson))
                    .isInstanceOf(Exception.class);
        }
        
        @Test
        @DisplayName("Should handle nested objects")
        void testNestedObjectConversion() {
            // Given
            String mockResponse = """
                {
                    "id": "123",
                    "user": {
                        "name": "Alice",
                        "age": 28,
                        "email": "alice@example.com"
                    },
                    "metadata": {
                        "created": "2024-01-01",
                        "version": "1.0"
                    }
                }
                """;
            
            BeanOutputConverter<TestUserProfile> converter = new BeanOutputConverter<>(TestUserProfile.class);
            
            // When
            TestUserProfile result = converter.convert(mockResponse);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo("123");
            assertThat(result.user().name()).isEqualTo("Alice");
            assertThat(result.metadata()).containsEntry("created", "2024-01-01");
        }
        
        @ParameterizedTest
        @ValueSource(strings = {
                "null",
                "undefined",
                "[]",
                "{}"
        })
        @DisplayName("Should handle edge cases")
        void testEdgeCases(String input) {
            // Given
            BeanOutputConverter<TestPerson> converter = new BeanOutputConverter<>(TestPerson.class);
            
            // When/Then
            if (input.equals("{}")) {
                TestPerson result = converter.convert(input);
                assertThat(result).isNotNull();
                assertThat(result.name()).isNull();
            } else {
                assertThatThrownBy(() -> converter.convert(input))
                        .isInstanceOf(Exception.class);
            }
        }
    }
    
    @Nested
    @DisplayName("ListOutputConverter Tests")
    class ListOutputConverterTests {
        
        @Test
        @DisplayName("Should convert comma-delimited string to list")
        void testBasicListConversion() {
            // Given
            String mockResponse = "apple, banana, orange, grape";
            ListOutputConverter converter = new ListOutputConverter();
            
            // When
            List<String> result = converter.convert(mockResponse);
            
            // Then
            assertThat(result)
                    .hasSize(4)
                    .containsExactly("apple", "banana", "orange", "grape");
        }
        
        @Test
        @DisplayName("Should handle empty string")
        void testEmptyString() {
            // Given
            ListOutputConverter converter = new ListOutputConverter();
            
            // When
            List<String> result = converter.convert("");
            
            // Then
            assertThat(result).isEmpty();
        }
        
        @Test
        @DisplayName("Should trim whitespace")
        void testWhitespaceTrimming() {
            // Given
            String mockResponse = "  item1  ,   item2   ,  item3  ";
            ListOutputConverter converter = new ListOutputConverter();
            
            // When
            List<String> result = converter.convert(mockResponse);
            
            // Then
            assertThat(result)
                    .hasSize(3)
                    .containsExactly("item1", "item2", "item3");
        }
        
        @Test
        @DisplayName("Should handle single item")
        void testSingleItem() {
            // Given
            String mockResponse = "single-item";
            ListOutputConverter converter = new ListOutputConverter();
            
            // When
            List<String> result = converter.convert(mockResponse);
            
            // Then
            assertThat(result)
                    .hasSize(1)
                    .containsExactly("single-item");
        }
    }
    
    @Nested
    @DisplayName("MapOutputConverter Tests")
    class MapOutputConverterTests {
        
        @Test
        @DisplayName("Should convert JSON object to map")
        void testJsonToMapConversion() {
            // Given
            String mockResponse = """
                {
                    "key1": "value1",
                    "key2": "value2",
                    "key3": "value3"
                }
                """;
            
            MapOutputConverter converter = new MapOutputConverter();
            
            // When
            Map<String, Object> result = converter.convert(mockResponse);
            
            // Then
            assertThat(result)
                    .hasSize(3)
                    .containsEntry("key1", "value1")
                    .containsEntry("key2", "value2")
                    .containsEntry("key3", "value3");
        }
        
        @Test
        @DisplayName("Should handle nested maps")
        void testNestedMapConversion() {
            // Given
            String mockResponse = """
                {
                    "user": {
                        "name": "Bob",
                        "settings": {
                            "theme": "dark",
                            "notifications": true
                        }
                    },
                    "version": "2.0"
                }
                """;
            
            MapOutputConverter converter = new MapOutputConverter();
            
            // When
            Map<String, Object> result = converter.convert(mockResponse);
            
            // Then
            assertThat(result).containsKey("user");
            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) result.get("user");
            assertThat(user).containsEntry("name", "Bob");
            assertThat(user).containsKey("settings");
        }
        
        @Test
        @DisplayName("Should handle arrays in map")
        void testMapWithArrays() {
            // Given
            String mockResponse = """
                {
                    "items": ["item1", "item2", "item3"],
                    "count": 3
                }
                """;
            
            MapOutputConverter converter = new MapOutputConverter();
            
            // When
            Map<String, Object> result = converter.convert(mockResponse);
            
            // Then
            assertThat(result).containsEntry("count", 3);
            assertThat(result.get("items")).isInstanceOf(List.class);
            @SuppressWarnings("unchecked")
            List<String> items = (List<String>) result.get("items");
            assertThat(items).containsExactly("item1", "item2", "item3");
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle converter errors gracefully")
        void testConverterErrorHandling() {
            // Test each error type
            for (MockAIResponseGenerator.ErrorType errorType : MockAIResponseGenerator.ErrorType.values()) {
                String errorResponse = responseGenerator.generateErrorResponse(errorType, TestPerson.class);
                BeanOutputConverter<TestPerson> converter = new BeanOutputConverter<>(TestPerson.class);
                
                // Some errors should throw, others might return partial results
                try {
                    TestPerson result = converter.convert(errorResponse);
                    // If no exception, verify partial result
                    assertThat(result).isNotNull();
                } catch (Exception e) {
                    // Expected for certain error types
                    assertThat(e).isNotNull();
                }
            }
        }
        
        @Test
        @DisplayName("Should retry on transient failures")
        void testRetryMechanism() {
            // Given
            mockChatModel.withErrorSimulation(true, 50); // 50% error rate
            
            BeanOutputConverter<TestPerson> converter = new BeanOutputConverter<>(TestPerson.class);
            
            // When - retry with backoff
            TestPerson result = TestingUtilities.retryWithBackoff(
                    v -> {
                        String response = chatClient.prompt()
                                .user("Generate a person")
                                .call()
                                .content();
                        return converter.convert(response);
                    },
                    3,
                    java.time.Duration.ofMillis(100)
            );
            
            // Then
            assertThat(result).isNotNull();
        }
    }
    
    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should convert within acceptable time")
        void testConversionPerformance() {
            // Given
            String mockResponse = responseGenerator.generateMockResponse(
                    TestPerson.class, 
                    MockAIResponseGenerator.MockConfig.defaults()
            );
            BeanOutputConverter<TestPerson> converter = new BeanOutputConverter<>(TestPerson.class);
            
            // When
            var timedResult = TestingUtilities.measureExecutionTime(v -> converter.convert(mockResponse));
            
            // Then
            assertThat(timedResult.result()).isNotNull();
            assertThat(timedResult.toMillis()).isLessThan(100); // Should be fast
        }
        
        @Test
        @DisplayName("Should handle large responses efficiently")
        void testLargeResponseHandling() {
            // Given - generate large nested structure
            String largeResponse = """
                {
                    "data": %s,
                    "metadata": {
                        "count": 1000,
                        "processed": true
                    }
                }
                """.formatted(generateLargeArray(1000));
            
            MapOutputConverter converter = new MapOutputConverter();
            
            // When
            var timedResult = TestingUtilities.measureExecutionTime(v -> converter.convert(largeResponse));
            
            // Then
            assertThat(timedResult.result()).isNotNull();
            assertThat(timedResult.toMillis()).isLessThan(1000); // Within 1 second
        }
    }
    
    // Test models
    
    public record TestPerson(
            @JsonPropertyDescription("Person's full name")
            String name,
            @JsonPropertyDescription("Person's age")
            int age,
            @JsonPropertyDescription("Person's email address")
            String email
    ) {}
    
    public record TestUserProfile(
            String id,
            TestPerson user,
            Map<String, String> metadata
    ) {}
    
    // Helper methods
    
    private String generateLargeArray(int size) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"item").append(i).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
}