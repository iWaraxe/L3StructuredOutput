package com.coherentsolutions.l3structuredoutput.s6;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that uses MapOutputConverter to generate travel destination recommendations
 * as a flexible map structure.
 */
@Service
public class DestinationMapService {

    private final OpenAiChatModel chatModel;

    public DestinationMapService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Recommends a travel destination and returns the information as a Map.
     * This demonstrates the use of MapOutputConverter for flexible, schema-less responses.
     */
    public Map<String, Object> recommendDestination(TravelRequest request) {
        // Create a MapOutputConverter
        MapOutputConverter converter = new MapOutputConverter();

        // Get the format instructions
        String format = converter.getFormat();

        // Create a prompt template
        String promptText = """
            Recommend a travel destination based on the following preferences:
            - Season: {season}
            - Budget: {budget}
            - Travel Style: {travelStyle}
            - Region: {region}
            
            Provide information about the destination including:
            - name: the destination name
            - country: the country it's in
            - bestTimeToVisit: when to visit
            - estimatedCost: the estimated daily cost in USD
            - highlights: at least 3 key attractions or experiences
            - weather: typical weather during the requested season
            - localCuisine: what kind of food to expect
            
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);

        // Create parameters for template rendering
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("season", request.season());
        parameters.put("budget", request.budget());
        parameters.put("travelStyle", request.travelStyle());
        parameters.put("region", request.region());
        parameters.put("format", format);

        // Render the template
        String renderedPrompt = template.render(parameters);

        // Create a prompt and call the AI model
        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        // Convert the response to a Map
        String responseText = response.getResult().getOutput().getText();
        return converter.convert(responseText);
    }

    /**
     * Suggests multiple destinations at once and returns a nested map structure.
     * This demonstrates using MapOutputConverter for more complex outputs.
     */
    public Map<String, Object> suggestMultipleDestinations(TravelRequest request, int count) {
        // Create a MapOutputConverter
        MapOutputConverter converter = new MapOutputConverter();

        // Get the format instructions
        String format = converter.getFormat();

        // Create a prompt template
        String promptText = """
            Suggest {count} different travel destinations based on the following preferences:
            - Season: {season}
            - Budget: {budget}
            - Travel Style: {travelStyle}
            - Region: {region}
            
            For each destination, provide:
            - name: the destination name
            - country: the country it's in
            - bestTimeToVisit: when to visit
            - estimatedCost: the estimated daily cost in USD
            - highlights: a list of 3 key attractions or experiences
            
            Structure your response as a JSON object with a "destinations" key that contains
            an array of destination objects, and a "summary" key with a brief overview of the recommendations.
            
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);

        // Create parameters for template rendering
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("season", request.season());
        parameters.put("budget", request.budget());
        parameters.put("travelStyle", request.travelStyle());
        parameters.put("region", request.region());
        parameters.put("count", count);
        parameters.put("format", format);

        // Render the template
        String renderedPrompt = template.render(parameters);

        // Create a prompt and call the AI model
        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        // Convert the response to a Map
        String responseText = response.getResult().getOutput().getText();
        return converter.convert(responseText);
    }
}