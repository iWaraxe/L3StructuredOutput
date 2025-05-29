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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
class ActivityListServiceTest extends BaseStructuredOutputTest {

    @Mock
    private OpenAiChatModel chatModel;

    private ActivityListService activityListService;

    @BeforeEach
    void setUp() {
        activityListService = new ActivityListService(chatModel);
    }

    @Test
    void suggestActivities_ShouldReturnListOfActivities() {
        // Given
        String destination = "Paris";
        String travelStyle = "cultural";
        int count = 5;

        String mockResponse = "Visit the Louvre Museum, Climb the Eiffel Tower at sunset, Explore Montmartre artist quarter, Take a Seine River cruise, Tour the Palace of Versailles";

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        List<String> activities = activityListService.suggestActivities(destination, travelStyle, count);

        // Then
        assertNotNull(activities);
        assertEquals(5, activities.size());
        assertTrue(activities.contains("Visit the Louvre Museum"));
        assertTrue(activities.contains("Climb the Eiffel Tower at sunset"));
        assertTrue(activities.stream().anyMatch(activity -> activity.contains("Louvre")));
    }

    @Test
    void suggestActivities_WithAdventureTravelStyle_ShouldReturnAdventureActivities() {
        // Given
        String destination = "New Zealand";
        String travelStyle = "adventure";
        int count = 3;

        String mockResponse = "Bungee jump at Queenstown, Hike the Milford Track, Go skydiving over Lake Taupo";

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        List<String> activities = activityListService.suggestActivities(destination, travelStyle, count);

        // Then
        assertNotNull(activities);
        assertEquals(3, activities.size());
        assertTrue(activities.stream().anyMatch(activity -> 
                activity.contains("Bungee") || activity.contains("skydiving") || activity.contains("Hike")));
    }

    @Test
    void suggestDailyItinerary_ShouldReturnChronologicalActivities() {
        // Given
        String destination = "Tokyo";
        String travelStyle = "foodie";

        String mockResponse = "Morning: Visit Tsukiji Outer Market for breakfast, Late Morning: Explore Senso-ji Temple in Asakusa, Lunch: Try authentic ramen in Shibuya, Afternoon: Take a sushi-making class, Evening: Dine at an izakaya in Shinjuku, Night: Experience Golden Gai bar district";

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        List<String> itinerary = activityListService.suggestDailyItinerary(destination, travelStyle);

        // Then
        assertNotNull(itinerary);
        assertTrue(itinerary.size() >= 5);
        assertTrue(itinerary.get(0).startsWith("Morning:"));
        assertTrue(itinerary.stream().anyMatch(item -> item.contains("Lunch:")));
        assertTrue(itinerary.stream().anyMatch(item -> item.contains("Evening:") || item.contains("Night:")));
    }

    @Test
    void createPackingList_ShouldReturnCategorizedItems() {
        // Given
        String destination = "Iceland";
        String season = "winter";

        String mockResponse = "Thermal underwear, Waterproof winter jacket, Warm wool sweaters, Waterproof hiking boots, Wool socks, Winter hat and gloves, Passport, Travel insurance documents, Phone charger, Camera with extra batteries, Toiletries bag, Sunscreen (for snow glare), Swimsuit (for hot springs), Crampons for icy conditions";

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        List<String> packingList = activityListService.createPackingList(destination, season);

        // Then
        assertNotNull(packingList);
        assertTrue(packingList.size() > 10);
        assertTrue(packingList.stream().anyMatch(item -> item.contains("Thermal") || item.contains("warm")));
        assertTrue(packingList.stream().anyMatch(item -> item.contains("Passport")));
        assertTrue(packingList.stream().anyMatch(item -> item.contains("Camera") || item.contains("Phone")));
    }

    @Test
    void createPackingList_ForTropicalDestination_ShouldReturnAppropriateItems() {
        // Given
        String destination = "Bali";
        String season = "summer";

        String mockResponse = "Lightweight cotton clothes, Swimwear, Sunglasses, Sandals, Sun hat, High SPF sunscreen, Insect repellent, Light rain jacket, Passport and visa, Travel adapter, Reusable water bottle, Beach towel, Snorkeling gear";

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        List<String> packingList = activityListService.createPackingList(destination, season);

        // Then
        assertNotNull(packingList);
        assertTrue(packingList.stream().anyMatch(item -> item.contains("Lightweight") || item.contains("cotton")));
        assertTrue(packingList.stream().anyMatch(item -> item.contains("Swimwear") || item.contains("Beach")));
        assertTrue(packingList.stream().anyMatch(item -> item.contains("sunscreen") || item.contains("SPF")));
        assertFalse(packingList.stream().anyMatch(item -> item.contains("winter") || item.contains("thermal")));
    }

    @Test
    void suggestActivities_WithZeroCount_ShouldReturnEmptyList() {
        // Given
        String destination = "London";
        String travelStyle = "historical";
        int count = 0;

        String mockResponse = "";

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        List<String> activities = activityListService.suggestActivities(destination, travelStyle, count);

        // Then
        assertNotNull(activities);
        assertTrue(activities.isEmpty());
    }
}