package com.coherentsolutions.l3structuredoutput.s7;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller demonstrating the low-level ChatModel API for structured output conversion
 */
@RestController
public class WeatherChatModelController {

    private final ChatModel chatModel;

    @Autowired
    public WeatherChatModelController(ChatModel chatModel) {
        this.chatModel = chatModel;
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
     * Endpoint that uses low-level ChatModel API with explicit conversion steps
     */
    @GetMapping("/weather/model")
    public WeatherInfo getWeatherInfoModel(@RequestParam(defaultValue = "New York") String city) {
        // Create the output converter for WeatherInfo
        BeanOutputConverter<WeatherInfo> outputConverter = new BeanOutputConverter<>(WeatherInfo.class);

        // Get format instructions from the converter
        String format = outputConverter.getFormat();

        // Create a template with parameters for city and format instructions
        String promptTemplate = """
                Provide current weather information for {city}. Include current conditions, 
                temperature in Celsius, humidity percentage, and a brief forecast for tomorrow.
                {format}
                """;

        // Render the template with the parameters
        Prompt prompt = new PromptTemplate(promptTemplate,
                Map.of("city", city, "format", format))
                .create();

        // Call the model with the prompt
        String responseText = chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();

        // Convert the response text to a WeatherInfo object
        return outputConverter.convert(responseText);
    }
}