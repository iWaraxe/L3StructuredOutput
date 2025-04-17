package com.coherentsolutions.l3structuredoutput.converter;

import com.coherentsolutions.l3structuredoutput.model.Product;
import org.springframework.ai.converter.AbstractConversionServiceOutputConverter;
import org.springframework.ai.converter.AbstractMessageOutputConverter;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

import java.util.List;
import java.util.Map;

/**
 * Factory class providing examples of various structured output converters.
 */
public class ConverterFactory {

    /**
     * Creates a BeanOutputConverter for a Product.
     * This converter can transform AI output into a Product object.
     */
    public static BeanOutputConverter<Product> createProductConverter() {
        return new BeanOutputConverter<>(Product.class);
    }

    /**
     * Creates a BeanOutputConverter for a List of Products.
     * This converter can transform AI output into a List<Product>.
     */
    public static BeanOutputConverter<List<Product>> createProductListConverter() {
        return new BeanOutputConverter<>(List.class, Product.class);
    }

    /**
     * Creates a MapOutputConverter.
     * This converter can transform AI output into a Map<String, Object>.
     */
    public static MapOutputConverter createMapConverter() {
        return new MapOutputConverter();
    }

    /**
     * Creates a ListOutputConverter.
     * This converter can transform comma-separated values into a List<String>.
     */
    public static ListOutputConverter createListConverter() {
        return new ListOutputConverter(new DefaultConversionService());
    }

    /**
     * Creates a custom AbstractConversionServiceOutputConverter.
     * This demonstrates how to create a custom converter using the conversion service.
     */
    public static AbstractConversionServiceOutputConverter<ProductSummary> createCustomConversionServiceConverter() {
        return new AbstractConversionServiceOutputConverter<ProductSummary>(new DefaultConversionService()) {
            @Override
            public String getFormat() {
                return """
                    Your response should be a single line in the following format:
                    ProductID|ProductName|Price|Rating
                    
                    For example:
                    ABC123|Wireless Headphones|99.99|4.5
                    
                    Do not include any explanations or additional text, only return the formatted line.
                    """;
            }

            @Override
            public ProductSummary convert(String source) {
                String[] parts = source.trim().split("\\|");
                if (parts.length != 4) {
                    throw new IllegalArgumentException("Invalid format: expected 4 parts separated by |");
                }

                return new ProductSummary(
                        parts[0],
                        parts[1],
                        Double.parseDouble(parts[2]),
                        Double.parseDouble(parts[3])
                );
            }
        };
    }

    /**
     * Creates a custom AbstractMessageOutputConverter.
     * This demonstrates how to create a custom converter using the message converter.
     */
    public static AbstractMessageOutputConverter<ProductReview> createCustomMessageConverter() {
        return new AbstractMessageOutputConverter<ProductReview>(new MappingJackson2MessageConverter()) {
            @Override
            public String getFormat() {
                return """
                    Your response should be a JSON object representing a product review.
                    The JSON should have the following structure:
                    {
                      "productId": "string",
                      "reviewTitle": "string",
                      "reviewContent": "string",
                      "rating": number
                    }
                    
                    For example:
                    {
                      "productId": "ABC123",
                      "reviewTitle": "Great product",
                      "reviewContent": "This product exceeded my expectations...",
                      "rating": 4.5
                    }
                    
                    Ensure the response is a valid JSON object with the exact field names shown above.
                    """;
            }
        };
    }

    /**
     * Simple record for product summary information.
     */
    public record ProductSummary(String id, String name, double price, double rating) {}

    /**
     * Simple record for product review information.
     */
    public record ProductReview(String productId, String reviewTitle, String reviewContent, double rating) {}
}