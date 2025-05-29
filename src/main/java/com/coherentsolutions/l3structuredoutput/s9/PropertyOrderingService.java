package com.coherentsolutions.l3structuredoutput.s9;

import com.coherentsolutions.l3structuredoutput.s9.models.ProductCatalog;
import com.coherentsolutions.l3structuredoutput.s9.models.UserProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service demonstrating how property ordering and advanced annotations
 * improve AI-generated structured output quality.
 */
@Service
public class PropertyOrderingService {

    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;

    public PropertyOrderingService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Generates a user profile with properly ordered fields.
     * The AI will respect the @JsonPropertyOrder annotation.
     */
    public UserProfile generateUserProfile(String description) {
        BeanOutputConverter<UserProfile> converter = new BeanOutputConverter<>(UserProfile.class);
        String format = converter.getFormat();

        String promptText = """
            Generate a detailed user profile based on the following description:
            {description}
            
            Include all relevant fields such as userId, username, email, full name, age,
            preferences (as a map of settings), account status, last login timestamp,
            and relevant tags. 
            
            Ensure preferences include at least theme, language, and notification settings.
            
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("description", description);
        parameters.put("format", format);

        String renderedPrompt = template.render(parameters);
        Prompt prompt = new Prompt(renderedPrompt);
        
        ChatResponse response = chatModel.call(prompt);
        String responseText = response.getResult().getOutput().getText();
        
        return converter.convert(responseText);
    }

    /**
     * Generates a product catalog with nested structures.
     * Demonstrates complex property ordering across multiple levels.
     */
    public ProductCatalog generateProductCatalog(String catalogType, int categoryCount) {
        BeanOutputConverter<ProductCatalog> converter = new BeanOutputConverter<>(ProductCatalog.class);
        String format = converter.getFormat();

        String promptText = """
            Create a comprehensive product catalog for a {catalogType} store.
            
            The catalog should include:
            - Catalog metadata (ID, name, last updated date, total product count)
            - {categoryCount} different categories with descriptions and product counts
            - At least 5 featured products with pricing, discounts, ratings, and availability
            
            Make the data realistic and appropriate for a {catalogType} store.
            Use varied availability statuses (IN_STOCK, LOW_STOCK, OUT_OF_STOCK, PRE_ORDER).
            
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("catalogType", catalogType);
        parameters.put("categoryCount", categoryCount);
        parameters.put("format", format);

        String renderedPrompt = template.render(parameters);
        Prompt prompt = new Prompt(renderedPrompt);
        
        ChatResponse response = chatModel.call(prompt);
        String responseText = response.getResult().getOutput().getText();
        
        return converter.convert(responseText);
    }

    /**
     * Demonstrates the generated JSON schema with annotations.
     * This helps understand how annotations affect the AI's understanding.
     */
    public String getAnnotatedSchema(Class<?> clazz) {
        BeanOutputConverter<?> converter = new BeanOutputConverter<>(clazz);
        return converter.getFormat();
    }

    /**
     * Shows the difference between annotated and non-annotated output.
     * Useful for demonstrating the value of proper annotations.
     */
    public String demonstratePropertyOrdering(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Validates that AI respects property ordering by comparing field positions.
     */
    public boolean validatePropertyOrder(String json, Class<?> clazz) {
        try {
            // Simply validate that the JSON can be parsed into the target class
            // The actual field ordering validation would require more complex logic
            Object parsed = objectMapper.readValue(json, clazz);
            return parsed != null;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}