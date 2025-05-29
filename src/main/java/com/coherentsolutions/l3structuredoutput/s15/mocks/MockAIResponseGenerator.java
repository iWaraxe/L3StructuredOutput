package com.coherentsolutions.l3structuredoutput.s15.mocks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * Generates mock AI responses for testing structured output converters
 */
public class MockAIResponseGenerator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Class<?>, Function<MockConfig, String>> responseGenerators = new HashMap<>();
    
    public MockAIResponseGenerator() {
        registerDefaultGenerators();
    }
    
    /**
     * Generate a mock response for a given type
     */
    public <T> String generateMockResponse(Class<T> targetType, MockConfig config) {
        Function<MockConfig, String> generator = responseGenerators.get(targetType);
        if (generator != null) {
            return generator.apply(config);
        }
        
        // Try to generate based on class structure
        return generateFromClassStructure(targetType, config);
    }
    
    /**
     * Generate a mock ChatResponse
     */
    public ChatResponse generateMockChatResponse(String content) {
        // Create Generation with AssistantMessage
        org.springframework.ai.chat.messages.AssistantMessage message = 
                new org.springframework.ai.chat.messages.AssistantMessage(content);
        Generation generation = new Generation(message);
        return new ChatResponse(List.of(generation));
    }
    
    /**
     * Generate response with controlled errors
     */
    public String generateErrorResponse(ErrorType errorType, Class<?> targetType) {
        return switch (errorType) {
            case MALFORMED_JSON -> "{\"incomplete\": ";
            case MISSING_REQUIRED_FIELD -> generateWithMissingField(targetType);
            case WRONG_TYPE -> generateWithWrongType(targetType);
            case EXTRA_FIELDS -> generateWithExtraFields(targetType);
            case INVALID_VALUES -> generateWithInvalidValues(targetType);
        };
    }
    
    /**
     * Generate batch responses
     */
    public List<String> generateBatchResponses(Class<?> targetType, int count, MockConfig config) {
        List<String> responses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MockConfig batchConfig = config.withVariation(i);
            responses.add(generateMockResponse(targetType, batchConfig));
        }
        return responses;
    }
    
    /**
     * Register default generators for common types
     */
    private void registerDefaultGenerators() {
        // String generator
        responseGenerators.put(String.class, config -> 
                config.useRandom() ? generateRandomString(config.length()) : config.defaultValue()
        );
        
        // Integer generator
        responseGenerators.put(Integer.class, config -> 
                String.valueOf(config.useRandom() ? 
                        ThreadLocalRandom.current().nextInt(config.minValue(), config.maxValue()) : 
                        config.defaultIntValue())
        );
        
        // List generator
        responseGenerators.put(List.class, config -> {
            List<String> items = new ArrayList<>();
            for (int i = 0; i < config.listSize(); i++) {
                items.add("Item " + i);
            }
            return items.toString();
        });
        
        // Map generator
        responseGenerators.put(Map.class, config -> {
            try {
                ObjectNode node = objectMapper.createObjectNode();
                for (int i = 0; i < config.mapSize(); i++) {
                    node.put("key" + i, "value" + i);
                }
                return objectMapper.writeValueAsString(node);
            } catch (JsonProcessingException e) {
                return "{}";
            }
        });
    }
    
    /**
     * Generate response from class structure using reflection
     */
    private <T> String generateFromClassStructure(Class<T> targetType, MockConfig config) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            
            // Generate fields based on class structure
            Arrays.stream(targetType.getDeclaredFields())
                    .filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                    .forEach(field -> {
                        String fieldName = field.getName();
                        Class<?> fieldType = field.getType();
                        
                        if (fieldType == String.class) {
                            node.put(fieldName, config.fieldValue(fieldName, "test-" + fieldName));
                        } else if (fieldType == int.class || fieldType == Integer.class) {
                            node.put(fieldName, config.fieldIntValue(fieldName, 42));
                        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                            node.put(fieldName, config.fieldBoolValue(fieldName, true));
                        } else if (fieldType == double.class || fieldType == Double.class) {
                            node.put(fieldName, config.fieldDoubleValue(fieldName, 3.14));
                        } else if (List.class.isAssignableFrom(fieldType)) {
                            node.putArray(fieldName).add("item1").add("item2");
                        } else {
                            node.putObject(fieldName);
                        }
                    });
            
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * Generate response with missing required field
     */
    private String generateWithMissingField(Class<?> targetType) {
        try {
            String fullResponse = generateFromClassStructure(targetType, MockConfig.defaults());
            ObjectNode node = (ObjectNode) objectMapper.readTree(fullResponse);
            
            // Remove first field
            Iterator<String> fieldNames = node.fieldNames();
            if (fieldNames.hasNext()) {
                node.remove(fieldNames.next());
            }
            
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * Generate response with wrong type for a field
     */
    private String generateWithWrongType(Class<?> targetType) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("stringField", 123); // Wrong type
            node.put("intField", "not a number"); // Wrong type
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * Generate response with extra fields
     */
    private String generateWithExtraFields(Class<?> targetType) {
        try {
            String baseResponse = generateFromClassStructure(targetType, MockConfig.defaults());
            ObjectNode node = (ObjectNode) objectMapper.readTree(baseResponse);
            
            // Add extra fields
            node.put("unexpectedField1", "value1");
            node.put("unexpectedField2", 42);
            node.putArray("unexpectedArray").add("item");
            
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * Generate response with invalid values
     */
    private String generateWithInvalidValues(Class<?> targetType) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("email", "not-an-email");
            node.put("age", -5);
            node.put("percentage", 150); // Over 100
            node.put("url", "not a url");
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * Generate random string
     */
    private String generateRandomString(int length) {
        return ThreadLocalRandom.current()
                .ints('a', 'z' + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
    
    /**
     * Error types for testing error handling
     */
    public enum ErrorType {
        MALFORMED_JSON,
        MISSING_REQUIRED_FIELD,
        WRONG_TYPE,
        EXTRA_FIELDS,
        INVALID_VALUES
    }
    
    /**
     * Configuration for mock generation
     */
    public static class MockConfig {
        private final Map<String, Object> fieldValues = new HashMap<>();
        private boolean useRandom = false;
        private int length = 10;
        private int minValue = 0;
        private int maxValue = 100;
        private String defaultValue = "test";
        private int defaultIntValue = 42;
        private int listSize = 3;
        private int mapSize = 3;
        private int variation = 0;
        
        public static MockConfig defaults() {
            return new MockConfig();
        }
        
        public MockConfig withRandom() {
            this.useRandom = true;
            return this;
        }
        
        public MockConfig withLength(int length) {
            this.length = length;
            return this;
        }
        
        public MockConfig withFieldValue(String field, Object value) {
            this.fieldValues.put(field, value);
            return this;
        }
        
        public MockConfig withVariation(int variation) {
            this.variation = variation;
            return this;
        }
        
        // Getters
        public boolean useRandom() { return useRandom; }
        public int length() { return length; }
        public int minValue() { return minValue; }
        public int maxValue() { return maxValue; }
        public String defaultValue() { return defaultValue; }
        public int defaultIntValue() { return defaultIntValue + variation; }
        public int listSize() { return listSize; }
        public int mapSize() { return mapSize; }
        
        public String fieldValue(String field, String defaultValue) {
            return (String) fieldValues.getOrDefault(field, defaultValue);
        }
        
        public int fieldIntValue(String field, int defaultValue) {
            return (int) fieldValues.getOrDefault(field, defaultValue + variation);
        }
        
        public boolean fieldBoolValue(String field, boolean defaultValue) {
            return (boolean) fieldValues.getOrDefault(field, defaultValue);
        }
        
        public double fieldDoubleValue(String field, double defaultValue) {
            return (double) fieldValues.getOrDefault(field, defaultValue);
        }
    }
}