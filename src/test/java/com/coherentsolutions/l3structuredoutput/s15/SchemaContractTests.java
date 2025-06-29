package com.coherentsolutions.l3structuredoutput.s15.contracts;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.junit.jupiter.api.*;
import org.springframework.ai.converter.BeanOutputConverter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Contract tests to ensure schema compatibility and consistency
 */
public class SchemaContractTests {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
    
    @Nested
    @DisplayName("Schema Generation Contract Tests")
    class SchemaGenerationTests {
        
        @Test
        @DisplayName("Should generate valid JSON Schema for simple POJO")
        void testSimplePojoSchema() {
            // Given
            BeanOutputConverter<SimplePerson> converter = new BeanOutputConverter<>(SimplePerson.class);
            
            // When
            String format = converter.getFormat();
            
            // Then
            assertThat(format).contains("JSON Schema");
            assertThat(format).contains("\"type\": \"object\"");
            assertThat(format).contains("\"properties\"");
            assertThat(format).contains("name");
            assertThat(format).contains("age");
            
            // Verify it's valid JSON
            assertThatCode(() -> objectMapper.readTree(extractJsonSchema(format)))
                    .doesNotThrowAnyException();
        }
        
        @Test
        @DisplayName("Should include property descriptions in schema")
        void testPropertyDescriptions() {
            // Given
            BeanOutputConverter<AnnotatedPerson> converter = new BeanOutputConverter<>(AnnotatedPerson.class);
            
            // When
            String format = converter.getFormat();
            String schema = extractJsonSchema(format);
            
            // Then
            assertThat(schema).contains("Full name of the person");
            assertThat(schema).contains("Age in years");
            assertThat(schema).contains("Email address");
        }
        
        @Test
        @DisplayName("Should handle nested objects in schema")
        void testNestedObjectSchema() {
            // Given
            BeanOutputConverter<UserProfile> converter = new BeanOutputConverter<>(UserProfile.class);
            
            // When
            String format = converter.getFormat();
            
            // Then
            assertThat(format).contains("person");
            assertThat(format).contains("address");
            assertThat(format).contains("street");
            assertThat(format).contains("city");
        }
        
        @Test
        @DisplayName("Should handle collections in schema")
        void testCollectionSchema() {
            // Given
            BeanOutputConverter<ProductCatalog> converter = new BeanOutputConverter<>(ProductCatalog.class);
            
            // When
            String format = converter.getFormat();
            
            // Then
            assertThat(format).contains("\"type\": \"array\"");
            assertThat(format).contains("products");
            assertThat(format).contains("tags");
        }
    }
    
    @Nested
    @DisplayName("Schema Validation Contract Tests")
    class SchemaValidationTests {
        
        @Test
        @DisplayName("Should validate correct data against schema")
        void testValidDataValidation() throws IOException, ProcessingException {
            // Given
            BeanOutputConverter<SimplePerson> converter = new BeanOutputConverter<>(SimplePerson.class);
            String schemaJson = extractJsonSchema(converter.getFormat());
            JsonNode schemaNode = objectMapper.readTree(schemaJson);
            JsonSchema schema = schemaFactory.getJsonSchema(schemaNode);
            
            String validData = """
                {
                    "name": "John Doe",
                    "age": 30
                }
                """;
            JsonNode dataNode = objectMapper.readTree(validData);
            
            // When
            ProcessingReport report = schema.validate(dataNode);
            
            // Then
            assertThat(report.isSuccess()).isTrue();
        }
        
        @Test
        @DisplayName("Should reject invalid data against schema")
        void testInvalidDataValidation() throws IOException, ProcessingException {
            // Given
            BeanOutputConverter<SimplePerson> converter = new BeanOutputConverter<>(SimplePerson.class);
            String schemaJson = extractJsonSchema(converter.getFormat());
            JsonNode schemaNode = objectMapper.readTree(schemaJson);
            JsonSchema schema = schemaFactory.getJsonSchema(schemaNode);
            
            String invalidData = """
                {
                    "name": "John Doe",
                    "age": "thirty"
                }
                """;
            JsonNode dataNode = objectMapper.readTree(invalidData);
            
            // When
            ProcessingReport report = schema.validate(dataNode);
            
            // Then
            assertThat(report.isSuccess()).isFalse();
        }
        
        @Test
        @DisplayName("Should enforce required fields")
        void testRequiredFieldsValidation() throws IOException, ProcessingException {
            // Given
            BeanOutputConverter<RequiredFieldsPerson> converter = new BeanOutputConverter<>(RequiredFieldsPerson.class);
            String schemaJson = extractJsonSchema(converter.getFormat());
            
            // Schema should contain required fields
            assertThat(schemaJson).contains("\"required\"");
            
            JsonNode schemaNode = objectMapper.readTree(schemaJson);
            JsonSchema schema = schemaFactory.getJsonSchema(schemaNode);
            
            String missingFieldData = """
                {
                    "age": 30
                }
                """;
            JsonNode dataNode = objectMapper.readTree(missingFieldData);
            
            // When
            ProcessingReport report = schema.validate(dataNode);
            
            // Then
            assertThat(report.isSuccess()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Schema Compatibility Contract Tests")
    class SchemaCompatibilityTests {
        
        @Test
        @DisplayName("Should maintain backward compatibility")
        void testBackwardCompatibility() {
            // Given - V1 of the model
            BeanOutputConverter<PersonV1> converterV1 = new BeanOutputConverter<>(PersonV1.class);
            String schemaV1 = extractJsonSchema(converterV1.getFormat());
            
            // And - V2 of the model (with additional optional field)
            BeanOutputConverter<PersonV2> converterV2 = new BeanOutputConverter<>(PersonV2.class);
            String schemaV2 = extractJsonSchema(converterV2.getFormat());
            
            // When - V1 data should be valid for V2 schema
            String v1Data = """
                {
                    "name": "John Doe",
                    "age": 30
                }
                """;
            
            // Then
            assertThatCode(() -> {
                JsonNode schemaNode = objectMapper.readTree(schemaV2);
                JsonSchema schema = schemaFactory.getJsonSchema(schemaNode);
                JsonNode dataNode = objectMapper.readTree(v1Data);
                ProcessingReport report = schema.validate(dataNode);
                assertThat(report.isSuccess()).isTrue();
            }).doesNotThrowAnyException();
        }
        
        @Test
        @DisplayName("Should detect breaking changes")
        void testBreakingChanges() {
            // Given - Original model
            BeanOutputConverter<PersonV1> originalConverter = new BeanOutputConverter<>(PersonV1.class);
            
            // And - Breaking change model (changed type)
            BeanOutputConverter<PersonBreaking> breakingConverter = new BeanOutputConverter<>(PersonBreaking.class);
            
            // When
            String originalSchema = extractJsonSchema(originalConverter.getFormat());
            String breakingSchema = extractJsonSchema(breakingConverter.getFormat());
            
            // Then - Schemas should be different
            assertThat(originalSchema).isNotEqualTo(breakingSchema);
            assertThat(originalSchema).contains("\"type\": \"integer\""); // age as integer
            assertThat(breakingSchema).contains("\"type\": \"string\""); // age as string (breaking)
        }
    }
    
    @Nested
    @DisplayName("Cross-Provider Schema Contract Tests")
    class CrossProviderSchemaTests {
        
        @Test
        @DisplayName("Should generate provider-agnostic schemas")
        void testProviderAgnosticSchema() {
            // Given
            BeanOutputConverter<StandardModel> converter = new BeanOutputConverter<>(StandardModel.class);
            
            // When
            String format = converter.getFormat();
            String schema = extractJsonSchema(format);
            
            // Then - Schema should be standard JSON Schema without provider-specific extensions
            assertThat(schema).doesNotContain("x-openai");
            assertThat(schema).doesNotContain("x-anthropic");
            assertThat(schema).doesNotContain("x-azure");
            
            // Should use standard JSON Schema keywords
            assertThat(schema).contains("\"type\"");
            assertThat(schema).contains("\"properties\"");
            assertThat(schema).contains("\"description\"");
        }
        
        @Test
        @DisplayName("Should handle different number formats consistently")
        void testNumberFormatConsistency() {
            // Given
            BeanOutputConverter<NumberFormats> converter = new BeanOutputConverter<>(NumberFormats.class);
            
            // When
            String schema = extractJsonSchema(converter.getFormat());
            
            // Then
            assertThat(schema).contains("\"intValue\"");
            assertThat(schema).contains("\"longValue\"");
            assertThat(schema).contains("\"floatValue\"");
            assertThat(schema).contains("\"doubleValue\"");
            
            // All should have appropriate type definitions
            assertThat(schema.matches(".*\"intValue\".*\"type\"\\s*:\\s*\"integer\".*")).isTrue();
            assertThat(schema.matches(".*\"doubleValue\".*\"type\"\\s*:\\s*\"number\".*")).isTrue();
        }
    }
    
    // Test models
    
    public record SimplePerson(String name, int age) {}
    
    public record AnnotatedPerson(
            @JsonPropertyDescription("Full name of the person") String name,
            @JsonPropertyDescription("Age in years") int age,
            @JsonPropertyDescription("Email address") String email
    ) {}
    
    public record UserProfile(
            String id,
            SimplePerson person,
            Address address,
            Map<String, String> preferences
    ) {}
    
    public record Address(String street, String city, String zipCode) {}
    
    public record ProductCatalog(
            String catalogId,
            List<Product> products,
            Map<String, List<String>> categorizedTags
    ) {}
    
    public record Product(String id, String name, double price, List<String> tags) {}
    
    public record RequiredFieldsPerson(String name, int age, String email) {}
    
    // Version evolution models
    public record PersonV1(String name, int age) {}
    public record PersonV2(String name, int age, String email) {} // Added optional field
    public record PersonBreaking(String name, String age) {} // Changed type (breaking)
    
    public record StandardModel(
            String id,
            String description,
            List<String> items,
            Map<String, Object> metadata
    ) {}
    
    public record NumberFormats(
            int intValue,
            long longValue,
            float floatValue,
            double doubleValue,
            Integer boxedInt,
            Double boxedDouble
    ) {}
    
    // Helper methods
    
    private String extractJsonSchema(String format) {
        // Extract JSON schema from the format string
        // Assuming format contains "You must respond in JSON format with the following schema:"
        int startIndex = format.indexOf("{");
        int endIndex = format.lastIndexOf("}") + 1;
        
        if (startIndex != -1 && endIndex > startIndex) {
            return format.substring(startIndex, endIndex);
        }
        
        // If not found, try to find it after a marker
        String marker = "schema:";
        int markerIndex = format.indexOf(marker);
        if (markerIndex != -1) {
            String afterMarker = format.substring(markerIndex + marker.length()).trim();
            startIndex = afterMarker.indexOf("{");
            endIndex = afterMarker.lastIndexOf("}") + 1;
            if (startIndex != -1 && endIndex > startIndex) {
                return afterMarker.substring(startIndex, endIndex);
            }
        }
        
        throw new IllegalArgumentException("Could not extract JSON schema from format: " + format);
    }
}