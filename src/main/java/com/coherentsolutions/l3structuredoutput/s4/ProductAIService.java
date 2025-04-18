package com.coherentsolutions.l3structuredoutput.s4;

import com.coherentsolutions.l3structuredoutput.s4.ConverterFactory.ProductReview;
import com.coherentsolutions.l3structuredoutput.s4.ConverterFactory.ProductSummary;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that demonstrates the use of different structured output converters
 * in Spring AI.
 */
@Service
public class ProductAIService {

    private final OpenAiChatModel chatModel;

    public ProductAIService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Generates a product using BeanOutputConverter.
     */
    public Product generateProduct(String category, String priceRange) {
        // Using BeanOutputConverter for a single Product
        BeanOutputConverter<Product> converter = ConverterFactory.createProductConverter();
        String format = converter.getFormat();

        String promptText = """
            Generate a detailed product in the {category} category, with a price in the {priceRange} range.
            Include a descriptive name, thorough description, realistic price, and at least 5 features.
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("category", category);
        parameters.put("priceRange", priceRange);
        parameters.put("format", format);

        String renderedPrompt = template.render(parameters);
        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        return converter.convert(response.getResult().getOutput().getText());
    }

    /**
     * Generates multiple products using BeanOutputConverter with List.
     */
    public List<Product> generateProductList(String category, int count) {
        // Using BeanOutputConverter for a List of Products
        BeanOutputConverter<List<Product>> converter = ConverterFactory.createProductListConverter();
        String format = converter.getFormat();

        String promptText = """
            Generate {count} different products in the {category} category.
            Ensure each product has a unique name, detailed description, realistic price, and at least 3 features.
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("category", category);
        parameters.put("count", count);
        parameters.put("format", format);

        String renderedPrompt = template.render(parameters);
        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        return converter.convert(response.getResult().getOutput().getText());
    }

    /**
     * Generates a product summary using a custom converter.
     */
    public ProductSummary generateProductSummary(String productType) {
        // Using a custom AbstractConversionServiceOutputConverter
        StructuredOutputConverter<ProductSummary> converter =
                ConverterFactory.createCustomConversionServiceConverter();
        String format = converter.getFormat();

        String promptText = """
            Generate a basic summary for a {productType} product.
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        String renderedPrompt = template.render(Map.of(
                "productType", productType,
                "format", format
        ));

        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        return converter.convert(response.getResult().getOutput().getText());
    }

    /**
     * Generates a product review using a custom message converter.
     */
    public ProductReview generateProductReview(String productId, String sentiment) {
        // Using a custom AbstractMessageOutputConverter
        StructuredOutputConverter<ProductReview> converter =
                ConverterFactory.createCustomMessageConverter();
        String format = converter.getFormat();

        String promptText = """
            Generate a {sentiment} review for the product with ID {productId}.
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        String renderedPrompt = template.render(Map.of(
                "productId", productId,
                "sentiment", sentiment,
                "format", format
        ));

        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        return converter.convert(response.getResult().getOutput().getText());
    }

    /**
     * Generates product features as a simple list.
     */
    public List<String> generateProductFeatures(String productType, int featureCount) {
        // Using ListOutputConverter
        ListOutputConverter converter = ConverterFactory.createListConverter();
        String format = converter.getFormat();

        String promptText = """
            List {featureCount} key features for a {productType} product.
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        String renderedPrompt = template.render(Map.of(
                "productType", productType,
                "featureCount", featureCount,
                "format", format
        ));

        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        return converter.convert(response.getResult().getOutput().getText());
    }

    /**
     * Generates a product description as a map of properties.
     */
    public Map<String, Object> generateProductProperties(String productType) {
        // Using MapOutputConverter
        MapOutputConverter converter = ConverterFactory.createMapConverter();
        String format = converter.getFormat();

        String promptText = """
            Generate key properties for a {productType} product, including:
            - dimensions
            - weight
            - material
            - warranty
            - country_of_origin
            
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        String renderedPrompt = template.render(Map.of(
                "productType", productType,
                "format", format
        ));

        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        return converter.convert(response.getResult().getOutput().getText());
    }
}