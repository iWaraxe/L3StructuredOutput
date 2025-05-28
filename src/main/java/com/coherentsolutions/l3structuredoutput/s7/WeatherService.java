package com.coherentsolutions.l3structuredoutput.s7;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service layer implementation for weather-related AI interactions
 */
@Service
public class WeatherService {

    private final ChatClient chatClient;
    private final ChatModel chatModel;

    @Autowired
    public WeatherService(ChatClient.Builder chatClientBuilder, ChatModel chatModel) {
        this.chatClient = chatClientBuilder.build();
        this.chatModel = chatModel;
    }

    /**
     * Record for individual weather information
     */
    public record WeatherInfo(
            String city,
            String currentCondition,
            Double temperatureCelsius,
            Integer humidity,
            String forecast
    ) {}

    /**
     * Record for multiple cities weather comparison
     */
    public record WeatherComparison(
            List<WeatherInfo> cities,
            String recommendation
    ) {}

    /**
     * High-level ChatClient approach for a single city weather
     */
    public WeatherInfo getWeatherWithChatClient(String city) {
        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("Provide current weather information for {city}. Include current conditions, " +
                                "temperature in Celsius, humidity percentage, and a brief forecast for tomorrow.")
                        .param("city", city))
                .call()
                .entity(WeatherInfo.class);
    }

    /**
     * Low-level ChatModel approach for a single city weather
     */
    public WeatherInfo getWeatherWithChatModel(String city) {
        BeanOutputConverter<WeatherInfo> outputConverter = new BeanOutputConverter<>(WeatherInfo.class);
        String format = outputConverter.getFormat();

        String promptTemplate = """
                Provide current weather information for {city}. Include current conditions, 
                temperature in Celsius, humidity percentage, and a brief forecast for tomorrow.
                {format}
                """;

        PromptTemplate template = new PromptTemplate(promptTemplate);
        String renderedPrompt = template.render(Map.of("city", city, "format", format));
        Prompt prompt = new Prompt(renderedPrompt);

        String responseText = chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();

        return outputConverter.convert(responseText);
    }

    /**
     * ChatClient approach for comparing weather in multiple cities
     * Demonstrates handling generic types with ParameterizedTypeReference
     */
    public WeatherComparison compareWeatherWithChatClient(List<String> cities) {
        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("Compare the current weather conditions for these cities: {cities}. " +
                                "For each city, provide current conditions, temperature in Celsius, " +
                                "humidity percentage, and a brief forecast. " +
                                "Finally, recommend which city has the best weather for outdoor activities today.")
                        .param("cities", String.join(", ", cities)))
                .call()
                .entity(WeatherComparison.class);
    }

    /**
     * Low-level ChatModel approach for comparing weather in multiple cities
     * Demonstrates handling generic types with ParameterizedTypeReference
     */
    public WeatherComparison compareWeatherWithChatModel(List<String> cities) {
        BeanOutputConverter<WeatherComparison> outputConverter =
                new BeanOutputConverter<>(WeatherComparison.class);
        String format = outputConverter.getFormat();

        String promptTemplate = """
                Compare the current weather conditions for these cities: {cities}.
                For each city, provide current conditions, temperature in Celsius,
                humidity percentage, and a brief forecast.
                Finally, recommend which city has the best weather for outdoor activities today.
                {format}
                """;

        PromptTemplate template = new PromptTemplate(promptTemplate);
        String renderedPrompt = template.render(Map.of("cities", String.join(", ", cities), "format", format));
        Prompt prompt = new Prompt(renderedPrompt);

        String responseText = chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();

        return outputConverter.convert(responseText);
    }
}