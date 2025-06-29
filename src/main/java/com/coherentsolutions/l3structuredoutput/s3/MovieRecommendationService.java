package com.coherentsolutions.l3structuredoutput.s3;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that provides movie recommendations based on user preferences
 * using AI-generated structured output.
 */
@Service
public class MovieRecommendationService {

    private final OpenAiChatModel chatModel;

    public MovieRecommendationService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Get a single movie recommendation based on user preferences.
     */
    public MovieRecommendation getRecommendation(MoviePreferenceRequest request) {
        // 1. Create a converter for our target type
        BeanOutputConverter<MovieRecommendation> converter =
                new BeanOutputConverter<>(MovieRecommendation.class);

        // 2. Get the format instructions
        String format = converter.getFormat();

        // 3. Create a prompt template
        String promptText = """
            You are a movie recommendation expert. Suggest a movie based on the following preferences:
            - Genre: {genre}
            - Released after year: {releaseYearAfter}
            - Mood: {mood}
            
            Provide one strong recommendation that matches these criteria.
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);

        // 4. Create parameters map and render the template
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("genre", request.genre());
        parameters.put("releaseYearAfter", request.releaseYearAfter());
        parameters.put("mood", request.mood());
        parameters.put("format", format);

        String renderedPrompt = template.render(parameters);

        // 5. Create a prompt and call the model
        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        // 6. Convert the response text to our target type
        String responseText = response.getResult().getOutput().getText();
        return converter.convert(responseText);
    }

    /**
     * Get multiple movie recommendations based on user preferences.
     */
    public List<MovieRecommendation> getRecommendations(MoviePreferenceRequest request) {
        // 1. Create a converter for our target type (a List of MovieRecommendation)
        BeanOutputConverter<List<MovieRecommendation>> converter =
                new BeanOutputConverter<>(new ParameterizedTypeReference<List<MovieRecommendation>>() {});

        // 2. Get the format instructions
        String format = converter.getFormat();

        // 3. Create a prompt template
        String promptText = """
            You are a movie recommendation expert. Suggest {maxResults} movies based on the following preferences:
            - Genre: {genre}
            - Released after year: {releaseYearAfter}
            - Mood: {mood}
            
            Provide strong recommendations that match these criteria.
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);

        // 4. Create parameters map and render the template
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("genre", request.genre());
        parameters.put("releaseYearAfter", request.releaseYearAfter());
        parameters.put("mood", request.mood());
        parameters.put("maxResults", request.maxResults());
        parameters.put("format", format);

        String renderedPrompt = template.render(parameters);

        // 5. Create a prompt and call the model
        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        // 6. Convert the response text to our target type
        String responseText = response.getResult().getOutput().getText();
        return converter.convert(responseText);
    }
}