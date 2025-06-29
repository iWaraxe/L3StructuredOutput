package com.coherentsolutions.l3structuredoutput.s7;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherChatModelControllerTest extends BaseStructuredOutputTest {

    @Mock
    private OpenAiChatModel chatModel;

    private WeatherChatModelController controller;

    @BeforeEach
    void setUp() {
        controller = new WeatherChatModelController(chatModel);
    }

    @Test
    void getWeatherInfoModel_shouldReturnWeatherInfo() {
        // Given
        String city = "Barcelona";
        String jsonResponse = """
            {
                "city": "Barcelona",
                "currentCondition": "Sunny",
                "temperatureCelsius": 26.0,
                "humidity": 55,
                "forecast": "Clear and warm"
            }
            """;

        ChatResponse mockResponse = createMockResponse(jsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        WeatherChatModelController.WeatherInfo result = controller.getWeatherInfoModel(city);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.city()).isEqualTo("Barcelona");
        assertThat(result.currentCondition()).isEqualTo("Sunny");
        assertThat(result.temperatureCelsius()).isEqualTo(26.0);
        assertThat(result.humidity()).isEqualTo(55);
        assertThat(result.forecast()).isEqualTo("Clear and warm");

        // Verify the prompt
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        Prompt capturedPrompt = promptCaptor.getValue();
        assertThat(promptContains(capturedPrompt, city, "weather information")).isTrue();
    }

    @Test
    void getWeatherInfoModel_shouldIncludeStructuredOutputFormat() {
        // Given
        String city = "Munich";
        ChatResponse mockResponse = createMockResponse("{}");
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        controller.getWeatherInfoModel(city);

        // Then
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        Prompt capturedPrompt = promptCaptor.getValue();
        
        // Verify format instructions are included
        String promptText = capturedPrompt.getInstructions().get(0).getText();
        assertThat(promptText).contains("JSON");
        assertThat(promptText).contains(city);
    }

    @Test
    void getWeatherInfoModel_withInvalidJsonResponse_shouldHandleGracefully() {
        // Given
        String city = "Invalid City";
        String invalidJsonResponse = "This is not valid JSON";

        ChatResponse mockResponse = createMockResponse(invalidJsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When & Then
        // The actual behavior would depend on how BeanOutputConverter handles invalid JSON
        // This test ensures the method doesn't throw unexpected exceptions
        try {
            controller.getWeatherInfoModel(city);
        } catch (Exception e) {
            // Expected behavior for invalid JSON
            assertThat(e).hasMessageContaining("JSON");
        }
    }
}