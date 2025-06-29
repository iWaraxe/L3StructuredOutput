package com.coherentsolutions.l3structuredoutput.s7;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Simplified unit tests for WeatherService focusing on core functionality
 */
@ExtendWith(MockitoExtension.class)
class WeatherServiceSimpleTest extends BaseStructuredOutputTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatModel chatModel;

    @Mock
    private ChatClient chatClient;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        weatherService = new WeatherService(chatClientBuilder, chatModel);
    }

    @Test
    void getWeatherWithChatModel_shouldCallModelAndReturnResult() {
        // Given
        String city = "London";
        String jsonResponse = """
            {
                "city": "London",
                "currentCondition": "Cloudy",
                "temperatureCelsius": 18.0,
                "humidity": 75,
                "forecast": "Rain expected later"
            }
            """;

        ChatResponse mockResponse = createMockResponse(jsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        WeatherService.WeatherInfo result = weatherService.getWeatherWithChatModel(city);

        // Then
        assertNotNull(result);
        assertThat(result.city()).isEqualTo("London");
        assertThat(result.currentCondition()).isEqualTo("Cloudy");
        assertThat(result.temperatureCelsius()).isEqualTo(18.0);
        assertThat(result.humidity()).isEqualTo(75);
        assertThat(result.forecast()).isEqualTo("Rain expected later");

        // Verify model was called
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void compareWeatherWithChatModel_shouldReturnComparison() {
        // Given
        List<String> cities = Arrays.asList("Tokyo", "Seoul");
        String jsonResponse = """
            {
                "cities": [
                    {
                        "city": "Tokyo",
                        "currentCondition": "Clear",
                        "temperatureCelsius": 25.0,
                        "humidity": 60,
                        "forecast": "Sunny all day"
                    },
                    {
                        "city": "Seoul",
                        "currentCondition": "Partly cloudy",
                        "temperatureCelsius": 23.0,
                        "humidity": 65,
                        "forecast": "Clouds increasing"
                    }
                ],
                "recommendation": "Tokyo has better weather for outdoor activities today"
            }
            """;

        ChatResponse mockResponse = createMockResponse(jsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        WeatherService.WeatherComparison result = weatherService.compareWeatherWithChatModel(cities);

        // Then
        assertNotNull(result);
        assertThat(result.cities()).hasSize(2);
        assertThat(result.cities().get(0).city()).isEqualTo("Tokyo");
        assertThat(result.cities().get(1).city()).isEqualTo("Seoul");
        assertThat(result.recommendation()).contains("Tokyo");

        // Verify prompt contains cities
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        Prompt capturedPrompt = promptCaptor.getValue();
        String promptText = capturedPrompt.getInstructions().get(0).getText();
        assertThat(promptText).contains("Tokyo, Seoul");
    }

    @Test
    void chatModelApproach_shouldIncludeFormatInstructions() {
        // Given
        String city = "Sydney";
        ChatResponse mockResponse = createMockResponse("{}");
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        weatherService.getWeatherWithChatModel(city);

        // Then
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        Prompt capturedPrompt = promptCaptor.getValue();
        
        // Verify format instructions are included
        String promptText = capturedPrompt.getInstructions().get(0).getText();
        assertThat(promptText).contains("JSON");
    }
}