package com.coherentsolutions.l3structuredoutput.s6;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
class DestinationMapServiceTest extends BaseStructuredOutputTest {

    @Mock
    private OpenAiChatModel chatModel;

    private DestinationMapService destinationMapService;

    @BeforeEach
    void setUp() {
        destinationMapService = new DestinationMapService(chatModel);
    }

    @Test
    void recommendDestination_ShouldReturnDestinationMap() {
        // Given
        TravelRequest request = new TravelRequest("summer", "medium", "beach", "Caribbean");

        String mockResponse = """
            {
              "name": "Barbados",
              "country": "Barbados",
              "bestTimeToVisit": "December to April",
              "estimatedCost": 150,
              "highlights": ["Beautiful beaches", "Rum distilleries", "Harrison's Cave", "Swimming with turtles"],
              "weather": "Warm and sunny with occasional showers",
              "localCuisine": "Flying fish, cou-cou, and rum punch"
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        Map<String, Object> destination = destinationMapService.recommendDestination(request);

        // Then
        assertNotNull(destination);
        assertEquals("Barbados", destination.get("name"));
        assertEquals("Barbados", destination.get("country"));
        assertEquals("December to April", destination.get("bestTimeToVisit"));
        assertEquals(150, destination.get("estimatedCost"));
        
        @SuppressWarnings("unchecked")
        List<String> highlights = (List<String>) destination.get("highlights");
        assertNotNull(highlights);
        assertEquals(4, highlights.size());
        assertTrue(highlights.contains("Beautiful beaches"));
        
        assertEquals("Warm and sunny with occasional showers", destination.get("weather"));
        assertEquals("Flying fish, cou-cou, and rum punch", destination.get("localCuisine"));
    }

    @Test
    void recommendDestination_WithDifferentPreferences_ShouldReturnAppropriateDestination() {
        // Given
        TravelRequest request = new TravelRequest("winter", "luxury", "skiing", "Europe");

        String mockResponse = """
            {
              "name": "St. Moritz",
              "country": "Switzerland",
              "bestTimeToVisit": "December to March",
              "estimatedCost": 500,
              "highlights": ["World-class skiing", "Luxury hotels", "High-end shopping", "Glacier Express train"],
              "weather": "Cold and snowy, perfect for winter sports",
              "localCuisine": "Fondue, raclette, and fine dining options"
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        Map<String, Object> destination = destinationMapService.recommendDestination(request);

        // Then
        assertNotNull(destination);
        assertEquals("St. Moritz", destination.get("name"));
        assertEquals("Switzerland", destination.get("country"));
        assertEquals(500, destination.get("estimatedCost"));
        assertTrue(destination.get("weather").toString().contains("snow"));
    }

    @Test
    void suggestMultipleDestinations_ShouldReturnMultipleDestinationsWithSummary() {
        // Given
        TravelRequest request = new TravelRequest("spring", "budget", "cultural", "Asia");
        int count = 3;

        String mockResponse = """
            {
              "destinations": [
                {
                  "name": "Hanoi",
                  "country": "Vietnam",
                  "bestTimeToVisit": "March to May",
                  "estimatedCost": 30,
                  "highlights": ["Old Quarter", "Street food tours", "Temple of Literature"]
                },
                {
                  "name": "Luang Prabang",
                  "country": "Laos",
                  "bestTimeToVisit": "October to April",
                  "estimatedCost": 25,
                  "highlights": ["Buddhist temples", "Night market", "Kuang Si Falls"]
                },
                {
                  "name": "Kathmandu",
                  "country": "Nepal",
                  "bestTimeToVisit": "March to May",
                  "estimatedCost": 20,
                  "highlights": ["Durbar Square", "Swayambhunath Temple", "Thamel district"]
                }
              ],
              "summary": "These budget-friendly Asian destinations offer rich cultural experiences with ancient temples, vibrant markets, and authentic local cuisine."
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        Map<String, Object> result = destinationMapService.suggestMultipleDestinations(request, count);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("destinations"));
        assertTrue(result.containsKey("summary"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> destinations = (List<Map<String, Object>>) result.get("destinations");
        assertEquals(3, destinations.size());
        
        // Check first destination
        Map<String, Object> firstDestination = destinations.get(0);
        assertEquals("Hanoi", firstDestination.get("name"));
        assertEquals("Vietnam", firstDestination.get("country"));
        assertEquals(30, firstDestination.get("estimatedCost"));
        
        // Check summary
        String summary = (String) result.get("summary");
        assertTrue(summary.contains("budget-friendly") || summary.contains("cultural"));
    }

    @Test
    void suggestMultipleDestinations_WithSingleDestination_ShouldReturnOneDestination() {
        // Given
        TravelRequest request = new TravelRequest("autumn", "moderate", "wine", "Europe");
        int count = 1;

        String mockResponse = """
            {
              "destinations": [
                {
                  "name": "Porto",
                  "country": "Portugal",
                  "bestTimeToVisit": "September to November",
                  "estimatedCost": 80,
                  "highlights": ["Port wine cellars", "Douro Valley", "Historic center"]
                }
              ],
              "summary": "Porto offers an excellent wine tourism experience with historic charm and moderate prices."
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        Map<String, Object> result = destinationMapService.suggestMultipleDestinations(request, count);

        // Then
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> destinations = (List<Map<String, Object>>) result.get("destinations");
        assertEquals(1, destinations.size());
        assertEquals("Porto", destinations.get(0).get("name"));
    }

    @Test
    void recommendDestination_WithMissingFields_ShouldStillReturnPartialData() {
        // Given
        TravelRequest request = new TravelRequest("summer", "budget", "backpacking", "South America");

        String mockResponse = """
            {
              "name": "La Paz",
              "country": "Bolivia",
              "estimatedCost": 25,
              "highlights": ["Valle de la Luna", "Witches' Market", "Cable car system"]
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        Map<String, Object> destination = destinationMapService.recommendDestination(request);

        // Then
        assertNotNull(destination);
        assertEquals("La Paz", destination.get("name"));
        assertEquals("Bolivia", destination.get("country"));
        assertEquals(25, destination.get("estimatedCost"));
        assertNotNull(destination.get("highlights"));
        // Missing fields should be null or not present
        assertTrue(!destination.containsKey("weather") || destination.get("weather") == null);
    }
}