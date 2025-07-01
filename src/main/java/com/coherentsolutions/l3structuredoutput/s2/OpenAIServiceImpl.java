package com.coherentsolutions.l3structuredoutput.s2;

import com.coherentsolutions.l3structuredoutput.s2.ex1.WeatherForecast;
import com.coherentsolutions.l3structuredoutput.s2.ex1.WeatherForecastRequest;
import com.coherentsolutions.l3structuredoutput.s2.ex2.Recipe;
import com.coherentsolutions.l3structuredoutput.s2.ex2.RecipeRequest;
import com.coherentsolutions.l3structuredoutput.s2.ex3.SentimentAnalysis;
import com.coherentsolutions.l3structuredoutput.s2.ex3.SentimentAnalysisRequest;
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

        // Create a parameters map (using HashMap to handle potential null values)
        Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("location", request.location() != null ? request.location() : "Unknown");
        parameters.put("forecastType", request.forecastType() != null ? request.forecastType() : "general");
        parameters.put("format", format);

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
        String ingredientsList = request.ingredients() != null && !request.ingredients().isEmpty() 
                ? request.ingredients().stream().collect(Collectors.joining(", ")) 
                : "any available ingredients";

        // Format dietary restrictions list
        String dietaryRestrictionsList = request.dietaryRestrictions() != null && !request.dietaryRestrictions().isEmpty()
                ? request.dietaryRestrictions().stream().collect(Collectors.joining(", "))
                : "none";

        // Create a parameters map (using HashMap to handle potential null values)
        Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("cuisine", request.cuisine() != null ? request.cuisine() : "any");
        parameters.put("dishType", request.dishType() != null ? request.dishType() : "main course");
        parameters.put("ingredients", ingredientsList);
        parameters.put("dietaryRestrictions", dietaryRestrictionsList);
        parameters.put("cookingTime", request.cookingTime() != null ? request.cookingTime() : 30);
        parameters.put("servings", request.servings() != null ? request.servings() : 4);
        parameters.put("difficulty", request.difficulty() != null ? request.difficulty() : "intermediate");
        parameters.put("format", format);

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

        // Create a parameters map (using HashMap to handle potential null values)
        Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("text", request.text() != null ? request.text() : "No text provided");
        parameters.put("format", format);

        // Render the template and create a prompt
        Prompt prompt = new Prompt(promptTemplate.render(parameters));

        // Call the AI model
        ChatResponse response = chatModel.call(prompt);

        // Convert and return the result
        return converter.convert(response.getResult().getOutput().getText());
    }
}