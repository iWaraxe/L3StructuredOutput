package com.coherentsolutions.l3structuredoutput.s4;

import org.junit.jupiter.api.Test;
import org.springframework.ai.converter.*;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConverterFactoryTest {

    @Test
    void createProductConverter_shouldReturnBeanOutputConverter() {
        // When
        BeanOutputConverter<Product> converter = ConverterFactory.createProductConverter();

        // Then
        assertThat(converter).isNotNull();
        assertThat(converter.getFormat()).contains("JSON");
        assertThat(converter.getFormat()).contains("schema");
        
        // Test conversion
        String jsonInput = """
            {
                "id": "P123",
                "name": "Laptop",
                "description": "High-performance laptop",
                "price": 999.99,
                "category": "Electronics",
                "inStock": true
            }
            """;
        
        Product result = converter.convert(jsonInput);
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("P123");
        assertThat(result.name()).isEqualTo("Laptop");
        assertThat(result.price()).isEqualTo(999.99);
    }

    @Test
    void createProductListConverter_shouldReturnListConverter() {
        // When
        BeanOutputConverter<List<Product>> converter = ConverterFactory.createProductListConverter();

        // Then
        assertThat(converter).isNotNull();
        assertThat(converter.getFormat()).contains("JSON");
        
        // Test conversion
        String jsonInput = """
            [
                {
                    "id": "P1",
                    "name": "Product 1",
                    "description": "First product",
                    "price": 10.00,
                    "category": "Category A",
                    "inStock": true
                },
                {
                    "id": "P2",
                    "name": "Product 2",
                    "description": "Second product",
                    "price": 20.00,
                    "category": "Category B",
                    "inStock": false
                }
            ]
            """;
        
        List<Product> result = converter.convert(jsonInput);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("P1");
        assertThat(result.get(1).id()).isEqualTo("P2");
    }

    @Test
    void createMapConverter_shouldReturnMapOutputConverter() {
        // When
        MapOutputConverter converter = ConverterFactory.createMapConverter();

        // Then
        assertThat(converter).isNotNull();
        assertThat(converter.getFormat()).contains("JSON");
        
        // Test conversion
        String jsonInput = """
            {
                "productId": "P123",
                "attributes": {
                    "color": "blue",
                    "size": "large"
                },
                "quantity": 5
            }
            """;
        
        Map<String, Object> result = converter.convert(jsonInput);
        assertThat(result).containsKey("productId");
        assertThat(result.get("productId")).isEqualTo("P123");
        assertThat(result.get("quantity")).isEqualTo(5);
    }

    @Test
    void createListConverter_shouldReturnListOutputConverter() {
        // When
        ListOutputConverter converter = ConverterFactory.createListConverter();

        // Then
        assertThat(converter).isNotNull();
        assertThat(converter.getFormat()).contains("comma");
        
        // Test conversion
        String csvInput = "apple, banana, orange, grape";
        
        List<String> result = converter.convert(csvInput);
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly("apple", "banana", "orange", "grape");
    }

    @Test
    void createCustomConversionServiceConverter_shouldReturnCustomConverter() {
        // When
        AbstractConversionServiceOutputConverter<ConverterFactory.ProductSummary> converter = 
            ConverterFactory.createCustomConversionServiceConverter();

        // Then
        assertThat(converter).isNotNull();
        assertThat(converter.getFormat()).contains("ProductID|ProductName|Price|Rating");
        
        // Test conversion
        String pipeDelimitedInput = "ABC123|Wireless Headphones|99.99|4.5";
        
        ConverterFactory.ProductSummary result = converter.convert(pipeDelimitedInput);
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("ABC123");
        assertThat(result.name()).isEqualTo("Wireless Headphones");
        assertThat(result.price()).isEqualTo(99.99);
        assertThat(result.rating()).isEqualTo(4.5);
    }

    @Test
    void createCustomConversionServiceConverter_withInvalidFormat_shouldThrowException() {
        // Given
        AbstractConversionServiceOutputConverter<ConverterFactory.ProductSummary> converter = 
            ConverterFactory.createCustomConversionServiceConverter();
        String invalidInput = "ABC123|Wireless Headphones|99.99"; // Missing rating

        // When & Then
        assertThatThrownBy(() -> converter.convert(invalidInput))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid format: expected 4 parts");
    }

    @Test
    void createCustomMessageConverter_shouldReturnCustomConverter() {
        // When
        AbstractMessageOutputConverter<ConverterFactory.ProductReview> converter = 
            ConverterFactory.createCustomMessageConverter();

        // Then
        assertThat(converter).isNotNull();
        assertThat(converter.getFormat()).contains("productId");
        assertThat(converter.getFormat()).contains("reviewTitle");
        assertThat(converter.getFormat()).contains("rating");
        
        // Test conversion
        String jsonInput = """
            {
                "productId": "ABC123",
                "reviewTitle": "Great product",
                "reviewContent": "Really satisfied with this purchase",
                "rating": 5.0
            }
            """;
        
        ConverterFactory.ProductReview result = converter.convert(jsonInput);
        assertThat(result).isNotNull();
        assertThat(result.productId()).isEqualTo("ABC123");
        assertThat(result.reviewTitle()).isEqualTo("Great product");
        assertThat(result.reviewContent()).contains("satisfied");
        assertThat(result.rating()).isEqualTo(5.0);
    }

    @Test
    void customConverters_withExtraWhitespace_shouldHandleGracefully() {
        // Given
        AbstractConversionServiceOutputConverter<ConverterFactory.ProductSummary> converter = 
            ConverterFactory.createCustomConversionServiceConverter();
        String inputWithSpaces = "  ABC123|Wireless Headphones|99.99|4.5  \n";

        // When
        ConverterFactory.ProductSummary result = converter.convert(inputWithSpaces);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("ABC123");
    }
}