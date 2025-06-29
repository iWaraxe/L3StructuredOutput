package com.coherentsolutions.l3structuredoutput.s7;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller demonstrating the high-level ChatClient API for structured output conversion
 */
@RestController
public class WeatherChatClientController {

    private final ChatClient chatClient;

    @Autowired
    public WeatherChatClientController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Record representing weather data for a specific city
     */
    public record WeatherInfo(
            String city,
            String currentCondition,
            Double temperatureCelsius,
            Integer humidity,
            String forecast
    ) {}

    /**
     * Endpoint that uses ChatClient's entity() method for direct conversion
     */
    @GetMapping("/weather/client")
    public WeatherInfo getWeatherInfoClient(@RequestParam(defaultValue = "New York") String city) {
        // Using the high-level ChatClient API with direct entity conversion
        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("Provide current weather information for {city}. Include current conditions, " +
                                "temperature in Celsius, humidity percentage, and a brief forecast for tomorrow.")
                        .param("city", city))
                .call()
                .entity(WeatherInfo.class);  // Direct conversion to WeatherInfo object
    }
}