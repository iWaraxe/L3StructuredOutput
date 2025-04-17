package com.coherentsolutions.l3structuredoutput.services.s2;

import com.coherentsolutions.l3structuredoutput.model.s2.*;

/**
 * Service interface for interacting with OpenAI's API.
 * Provides methods for various AI-powered functionality.
 */
public interface OpenAIService {

    /**
     * Gets a weather forecast for a specified location.
     *
     * @param request The request containing the location and forecast type
     * @return A structured weather forecast
     */
    WeatherForecast getWeatherForecast(WeatherForecastRequest request);

    /**
     * Generates a recipe based on specified parameters.
     *
     * @param request The request containing recipe requirements
     * @return A structured recipe
     */
    Recipe generateRecipe(RecipeRequest request);

    /**
     * Analyzes the sentiment of provided text.
     *
     * @param request The request containing the text to analyze
     * @return A sentiment analysis result
     */
    SentimentAnalysis analyzeSentiment(SentimentAnalysisRequest request);
}