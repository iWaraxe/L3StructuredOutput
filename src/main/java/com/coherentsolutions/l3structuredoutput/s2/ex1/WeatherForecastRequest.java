package com.coherentsolutions.l3structuredoutput.s2.ex1;

/**
 * Record representing a request for a weather forecast.
 * Contains the location and forecast type.
 */
public record WeatherForecastRequest(String location, String forecastType) {
}