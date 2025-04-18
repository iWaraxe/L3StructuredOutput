package com.coherentsolutions.l3structuredoutput.s2;

import com.coherentsolutions.l3structuredoutput.s2.utils.PromptTemplateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of OpenAIService that uses Spring AI's ChatModel
 * to interact with OpenAI's API. This service converts user questions
 * into AI-generated responses using various prompt templates.
 */
@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final OpenAiChatModel chatModel;

    @Value("classpath:templates/weather-forecast.st")
    private Resource weatherForecastPrompt;

    @Value("classpath:templates/recipe-generator.st")
    private Resource recipeGeneratorPrompt;

    @Value("classpath:templates/sentiment-analysis.st")
    private Resource sentimentAnalysisPrompt;

    @Autowired
    private ObjectMapper objectMapper;

    public OpenAIServiceImpl(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public WeatherForecast getWeatherForecast(WeatherForecastRequest request) {
        // Create a converter for our target response type
        BeanOutputConverter<WeatherForecast> converter =
                new BeanOutputConverter<>(WeatherForecast.class);

        // Get the format instructions
        String format = converter.getFormat();

        // Create a template from our resource
        PromptTemplate promptTemplate = PromptTemplateUtils.fromResource(weatherForecastPrompt);

        // Create a parameters map
        Map<String, Object> parameters = Map.of(
                "location", request.location(),
                "forecastType", request.forecastType(),
                "format", format
        );

        // Render the template and create a prompt
        Prompt prompt = new Prompt(promptTemplate.render(parameters));

        // Call the AI model
        ChatResponse response = chatModel.call(prompt);

        // Convert and return the result
        return converter.convert(response.getResult().getOutput().getText());
    }

    @Override
    public Recipe generateRecipe(RecipeRequest request) {
        // Create a converter for our target response type
        BeanOutputConverter<Recipe> converter =
                new BeanOutputConverter<>(Recipe.class);

        // Get the format instructions
        String format = converter.getFormat();

        // Create a template from our resource
        PromptTemplate promptTemplate = PromptTemplateUtils.fromResource(recipeGeneratorPrompt);

        // Format ingredients list as a comma-separated string
        String ingredientsList = request.ingredients().stream()
                .collect(Collectors.joining(", "));

        // Create a parameters map
        Map<String, Object> parameters = Map.of(
                "cuisine", request.cuisine(),
                "dishType", request.dishType(),
                "ingredients", ingredientsList,
                "dietaryRestrictions", request.dietaryRestrictions(),
                "format", format
        );

        // Render the template and create a prompt
        Prompt prompt = new Prompt(promptTemplate.render(parameters));

        // Call the AI model
        ChatResponse response = chatModel.call(prompt);

        // Convert and return the result
        return converter.convert(response.getResult().getOutput().getText());
    }

    @Override
    public SentimentAnalysis analyzeSentiment(SentimentAnalysisRequest request) {
        // Create a converter for our target response type
        BeanOutputConverter<SentimentAnalysis> converter =
                new BeanOutputConverter<>(SentimentAnalysis.class);

        // Get the format instructions
        String format = converter.getFormat();

        // Create a template from our resource
        PromptTemplate promptTemplate = PromptTemplateUtils.fromResource(sentimentAnalysisPrompt);

        // Create a parameters map
        Map<String, Object> parameters = Map.of(
                "text", request.text(),
                "format", format
        );

        // Render the template and create a prompt
        Prompt prompt = new Prompt(promptTemplate.render(parameters));

        // Call the AI model
        ChatResponse response = chatModel.call(prompt);

        // Convert and return the result
        return converter.convert(response.getResult().getOutput().getText());
    }
}