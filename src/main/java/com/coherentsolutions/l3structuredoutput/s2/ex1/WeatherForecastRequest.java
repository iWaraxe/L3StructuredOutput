package com.coherentsolutions.l3structuredoutput.s2.ex1;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Record representing a request for a weather forecast.
 * Contains the location and forecast type.
 */
public record WeatherForecastRequest(
        @JsonProperty("city") String location, 
        String forecastType) {
}