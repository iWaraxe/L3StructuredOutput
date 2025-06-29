package com.coherentsolutions.l3structuredoutput.s5;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
class RobustCapitalInfoServiceTest extends BaseStructuredOutputTest {

    @Mock
    private OpenAiChatModel chatModel;

    private RobustCapitalInfoService robustCapitalInfoService;

    @BeforeEach
    void setUp() {
        robustCapitalInfoService = new RobustCapitalInfoService(chatModel);
    }

    @Test
    void getCapitalInfoWithErrorHandling_ShouldReturnValidCapitalInfo() {
        // Given
        String country = "Italy";
        String mockResponse = """
            {
              "city": "Rome",
              "population": 2.8,
              "region": "Lazio",
              "language": "Italian",
              "currency": "Euro",
              "landmarks": ["Colosseum", "Vatican City", "Trevi Fountain", "Pantheon"]
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        CapitalInfo capitalInfo = robustCapitalInfoService.getCapitalInfoWithErrorHandling(country);

        // Then
        assertNotNull(capitalInfo);
        assertEquals("Rome", capitalInfo.city());
        assertEquals(2.8, capitalInfo.population());
        assertEquals("Lazio", capitalInfo.region());
        assertEquals("Italian", capitalInfo.language());
        assertEquals("Euro", capitalInfo.currency());
        assertEquals(4, capitalInfo.landmarks().length);
    }

    @Test
    void getCapitalInfoWithErrorHandling_WithMalformedJson_ShouldReturnFallbackInfo() {
        // Given
        String country = "Spain";
        String malformedResponse = """
            {
              "city": "Madrid",
              "population": "3.3 million" // This should be a number
              "region": "Community of Madrid",
              // Missing comma
              "language": "Spanish"
              "currency": "Euro",
              "landmarks": ["Royal Palace", "Prado Museum"
            }
            """;

        ChatResponse chatResponse = createMockResponse(malformedResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        CapitalInfo capitalInfo = robustCapitalInfoService.getCapitalInfoWithErrorHandling(country);

        // Then
        assertNotNull(capitalInfo);
        assertTrue(capitalInfo.city().contains("Madrid") || capitalInfo.city().contains("Spain"));
        assertNotNull(capitalInfo.landmarks());
    }

    @Test
    void getCapitalInfoWithErrorHandling_WithPartialJson_ShouldExtractAvailableData() {
        // Given
        String country = "Portugal";
        String partialResponse = """
            {
              "city": "Lisbon",
              "population": null,
              "region": null
            }
            """;

        ChatResponse chatResponse = createMockResponse(partialResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        CapitalInfo capitalInfo = robustCapitalInfoService.getCapitalInfoWithErrorHandling(country);

        // Then
        assertNotNull(capitalInfo);
        assertTrue(capitalInfo.city().contains("Lisbon") || capitalInfo.city().contains("Portugal"));
    }

    @Test
    void getCapitalInfoWithErrorHandling_WhenApiCallFails_ShouldReturnDefaultInfo() {
        // Given
        String country = "Greece";
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("API call failed"));

        // When
        CapitalInfo capitalInfo = robustCapitalInfoService.getCapitalInfoWithErrorHandling(country);

        // Then
        assertNotNull(capitalInfo);
        assertEquals("Unknown capital of Greece", capitalInfo.city());
        assertEquals("Unknown", capitalInfo.region());
        assertEquals("Unknown", capitalInfo.language());
        assertEquals("Unknown", capitalInfo.currency());
        assertNotNull(capitalInfo.landmarks());
        assertEquals(1, capitalInfo.landmarks().length);
        assertEquals("Information unavailable", capitalInfo.landmarks()[0]);
    }

    @Test
    void getCapitalInfoWithErrorHandling_WithEmptyResponse_ShouldReturnFallbackInfo() {
        // Given
        String country = "Sweden";
        String emptyResponse = "";

        ChatResponse chatResponse = createMockResponse(emptyResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        CapitalInfo capitalInfo = robustCapitalInfoService.getCapitalInfoWithErrorHandling(country);

        // Then
        assertNotNull(capitalInfo);
        assertTrue(capitalInfo.city().contains("Sweden"));
        assertNotNull(capitalInfo.landmarks());
    }

    @Test
    void getCapitalInfoWithErrorHandling_WithNullResponse_ShouldHandleGracefully() {
        // Given
        String country = "Norway";
        ChatResponse chatResponse = createMockResponse(null);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        CapitalInfo capitalInfo = robustCapitalInfoService.getCapitalInfoWithErrorHandling(country);

        // Then
        assertNotNull(capitalInfo);
        assertTrue(capitalInfo.city().contains("Norway"));
    }
}