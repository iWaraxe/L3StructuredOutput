package com.coherentsolutions.l3structuredoutput.controllers;

import com.coherentsolutions.l3structuredoutput.model.*;
import com.coherentsolutions.l3structuredoutput.services.OpenAIService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller demonstrating various prompt templates
 * and structured output techniques with Spring AI.
 */
@RestController
@RequestMapping("/api/ai/structured")
public class StructuredOutputController {

    private final OpenAIService openAIService;

    public StructuredOutputController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    /**
     * Endpoint for getting weather forecasts.
     * Demonstrates using prompt templates with location and forecast type parameters.
     */
    @PostMapping("/weather")
    public WeatherForecast getWeatherForecast(@RequestBody WeatherForecastRequest request) {
        return openAIService.getWeatherForecast(request);
    }

    /**
     * Endpoint for generating recipes.
     * Demonstrates using prompt templates with multiple complex parameters.
     */
    @PostMapping("/recipe")
    public Recipe generateRecipe(@RequestBody RecipeRequest request) {
        return openAIService.generateRecipe(request);
    }

    /**
     * Endpoint for sentiment analysis.
     * Demonstrates few-shot prompting with examples.
     */
    @PostMapping("/sentiment")
    public SentimentAnalysis analyzeSentiment(@RequestBody SentimentAnalysisRequest request) {
        return openAIService.analyzeSentiment(request);
    }
}