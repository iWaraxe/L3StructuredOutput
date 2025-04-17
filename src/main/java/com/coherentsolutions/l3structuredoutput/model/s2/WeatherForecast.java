package com.coherentsolutions.l3structuredoutput.model.s2;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Record representing a weather forecast response.
 * This structure is designed to capture weather information
 * returned by the AI model in a structured format.
 */
@JsonPropertyOrder({"location", "date", "temperatureHigh", "temperatureLow", "precipitationChance", "windSpeed", "windDirection", "weatherAlerts"})
public record WeatherForecast(
        @JsonPropertyDescription("The location for which the forecast is provided")
        String location,

        @JsonPropertyDescription("The date of the forecast in the format YYYY-MM-DD")
        String date,

        @JsonPropertyDescription("The high temperature for the day in Celsius")
        Double temperatureHigh,

        @JsonPropertyDescription("The low temperature for the day in Celsius")
        Double temperatureLow,

        @JsonPropertyDescription("The chance of precipitation as a percentage")
        Integer precipitationChance,

        @JsonPropertyDescription("The wind speed in km/h")
        Double windSpeed,

        @JsonPropertyDescription("The wind direction (N, S, E, W, NE, etc.)")
        String windDirection,

        @JsonPropertyDescription("List of any weather alerts or special conditions")
        List<String> weatherAlerts
) {
}