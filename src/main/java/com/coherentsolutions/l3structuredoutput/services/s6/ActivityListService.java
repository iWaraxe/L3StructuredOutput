package com.coherentsolutions.l3structuredoutput.services.s6;

import com.coherentsolutions.l3structuredoutput.model.s6.TravelRequest;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that uses ListOutputConverter to generate travel activity recommendations
 * as simple lists.
 */
@Service
public class ActivityListService {

    private final OpenAiChatModel chatModel;
    private final ListOutputConverter listConverter;

    public ActivityListService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
        // Initialize ListOutputConverter with DefaultConversionService
        this.listConverter = new ListOutputConverter(new DefaultConversionService());
    }

    /**
     * Recommends activities for a travel destination and returns them as a simple List<String>.
     * This demonstrates the basic use of ListOutputConverter.
     */
    public List<String> suggestActivities(String destination, String travelStyle, int count) {
        // Get the format instructions
        String format = listConverter.getFormat();

        // Create a prompt template
        String promptText = """
            Suggest {count} different activities or experiences for travelers visiting {destination} 
            who enjoy a {travelStyle} travel style.
            
            Provide a list of interesting, specific activities that match this travel style.
            Each activity should be descriptive but concise (under 10 words).
            
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);

        // Create parameters for template rendering
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("destination", destination);
        parameters.put("travelStyle", travelStyle);
        parameters.put("count", count);
        parameters.put("format", format);

        // Render the template
        String renderedPrompt = template.render(parameters);

        // Create a prompt and call the AI model
        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        // Convert the response to a List<String>
        String responseText = response.getResult().getOutput().getText();
        return listConverter.convert(responseText);
    }

    /**
     * Recommends activities for different times of day and returns them as a list.
     * This demonstrates a more complex use of ListOutputConverter.
     */
    public List<String> suggestDailyItinerary(String destination, String travelStyle) {
        // Get the format instructions
        String format = listConverter.getFormat();

        // Create a prompt template
        String promptText = """
            Create a one-day itinerary for a traveler visiting {destination} who enjoys a {travelStyle} travel style.
            
            List activities and experiences in chronological order from morning to evening.
            Format each item with the time period and the activity, for example: "Morning: Visit the local market"
            Include at least 5 different activities throughout the day.
            
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);

        // Create parameters for template rendering
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("destination", destination);
        parameters.put("travelStyle", travelStyle);
        parameters.put("format", format);

        // Render the template
        String renderedPrompt = template.render(parameters);

        // Create a prompt and call the AI model
        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        // Convert the response to a List<String>
        String responseText = response.getResult().getOutput().getText();
        return listConverter.convert(responseText);
    }

    /**
     * Recommends a packing list specific to a destination and season.
     * This demonstrates using ListOutputConverter with categorized items.
     */
    public List<String> createPackingList(String destination, String season) {
        // Get the format instructions
        String format = listConverter.getFormat();

        // Create a prompt template
        String promptText = """
            Create a comprehensive packing list for a trip to {destination} during {season}.
            
            Include essential items across these categories:
            - Clothing
            - Toiletries
            - Electronics
            - Documents
            - Destination-specific items
            
            List each item separately. Be specific and practical.
            
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);

        // Create parameters for template rendering
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("destination", destination);
        parameters.put("season", season);
        parameters.put("format", format);

        // Render the template
        String renderedPrompt = template.render(parameters);

        // Create a prompt and call the AI model
        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        // Convert the response to a List<String>
        String responseText = response.getResult().getOutput().getText();
        return listConverter.convert(responseText);
    }
}