package com.coherentsolutions.l3structuredoutput.s5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RobustCapitalInfoService {

    private static final Logger logger = LoggerFactory.getLogger(RobustCapitalInfoService.class);
    private final OpenAiChatModel chatModel;

    public RobustCapitalInfoService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Gets detailed information about a capital city with error handling.
     */
    public CapitalInfo getCapitalInfoWithErrorHandling(String country) {
        BeanOutputConverter<CapitalInfo> converter =
                new BeanOutputConverter<>(CapitalInfo.class);

        String format = converter.getFormat();

        String promptText = """
            Provide detailed information about the capital city of {country}.
            Include the city name, population (in millions), region/state,
            primary language, currency, and an array of at least 3 famous landmarks.
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        String renderedPrompt = template.render(Map.of(
                "country", country,
                "format", format
        ));

        Prompt prompt = new Prompt(renderedPrompt);

        try {
            ChatResponse response = chatModel.call(prompt);
            String responseText = response.getResult().getOutput().getText();

            try {
                // Attempt to convert the response
                return converter.convert(responseText);
            } catch (Exception e) {
                // Log the conversion error and the original response for debugging
                logger.error("Failed to convert AI response to CapitalInfo", e);
                logger.debug("Raw AI response: {}", responseText);

                // Implement a fallback strategy
                return fallbackCapitalInfo(country, responseText);
            }
        } catch (Exception e) {
            // Handle API call failures
            logger.error("Failed to call AI service", e);

            // Return a default or fallback response
            return createDefaultCapitalInfo(country);
        }
    }

    /**
     * Fallback strategy: attempt to extract partial information from the response.
     */
    private CapitalInfo fallbackCapitalInfo(String country, String responseText) {
        // In a real implementation, you might use regex or more sophisticated parsing
        // to extract what information you can from the response

        // For this example, we'll just create a placeholder
        return new CapitalInfo(
                extractCityName(responseText, country),  // Attempt to extract city name
                null,  // Population unknown
                null,  // Region unknown
                null,  // Language unknown
                null,  // Currency unknown
                new String[]{"Information unavailable"}  // No landmarks
        );
    }

    /**
     * Create a default capital info object when all else fails.
     */
    private CapitalInfo createDefaultCapitalInfo(String country) {
        return new CapitalInfo(
                "Unknown capital of " + country,
                null,
                "Unknown",
                "Unknown",
                "Unknown",
                new String[]{"Information unavailable"}
        );
    }

    /**
     * Simple extraction method for city names from response text.
     */
    private String extractCityName(String responseText, String country) {
        // Very simplistic extraction - in reality, you would use more robust methods
        if (responseText.contains("\"city\":")) {
            int startIndex = responseText.indexOf("\"city\":") + 8;
            int endIndex = responseText.indexOf("\"", startIndex);
            if (endIndex > startIndex) {
                return responseText.substring(startIndex, endIndex);
            }
        }

        // Fallback
        return "Unknown capital of " + country;
    }
}