package com.coherentsolutions.l3structuredoutput.s6;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TravelController.class)
class TravelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DestinationMapService destinationMapService;

    @MockitoBean
    private ActivityListService activityListService;

    @Test
    void recommendDestination_ShouldReturnDestinationMap() throws Exception {
        // Given
        TravelRequest request = new TravelRequest("summer", "medium", "beach", "Mediterranean");
        
        Map<String, Object> mockDestination = new HashMap<>();
        mockDestination.put("name", "Santorini");
        mockDestination.put("country", "Greece");
        mockDestination.put("bestTimeToVisit", "May to October");
        mockDestination.put("estimatedCost", 120);
        mockDestination.put("highlights", Arrays.asList("Sunset in Oia", "Black beaches", "Wine tasting"));
        mockDestination.put("weather", "Hot and dry summers");
        mockDestination.put("localCuisine", "Fresh seafood, fava, and local wines");

        when(destinationMapService.recommendDestination(any(TravelRequest.class)))
                .thenReturn(mockDestination);

        // When & Then
        mockMvc.perform(post("/api/travel/destinations/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Santorini"))
                .andExpect(jsonPath("$.country").value("Greece"))
                .andExpect(jsonPath("$.estimatedCost").value(120))
                .andExpect(jsonPath("$.highlights").isArray())
                .andExpect(jsonPath("$.highlights[0]").value("Sunset in Oia"));
    }

    @Test
    void suggestDestinations_ShouldReturnMultipleDestinations() throws Exception {
        // Given
        TravelRequest request = new TravelRequest("winter", "budget", "cultural", "Asia");
        
        Map<String, Object> mockResult = new HashMap<>();
        
        List<Map<String, Object>> destinations = Arrays.asList(
                Map.of("name", "Delhi", "country", "India", "estimatedCost", 20),
                Map.of("name", "Bangkok", "country", "Thailand", "estimatedCost", 25),
                Map.of("name", "Siem Reap", "country", "Cambodia", "estimatedCost", 15)
        );
        
        mockResult.put("destinations", destinations);
        mockResult.put("summary", "Budget-friendly Asian destinations with rich cultural heritage");

        when(destinationMapService.suggestMultipleDestinations(any(TravelRequest.class), eq(3)))
                .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/travel/destinations/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("count", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinations").isArray())
                .andExpect(jsonPath("$.destinations.length()").value(3))
                .andExpect(jsonPath("$.destinations[0].name").value("Delhi"))
                .andExpect(jsonPath("$.summary").exists());
    }

    @Test
    void suggestActivities_ShouldReturnListOfActivities() throws Exception {
        // Given
        List<String> mockActivities = Arrays.asList(
                "Visit the Colosseum",
                "Explore Vatican Museums",
                "Toss coin at Trevi Fountain",
                "Walk through Roman Forum",
                "Enjoy gelato in Piazza Navona"
        );

        when(activityListService.suggestActivities("Rome", "historical", 5))
                .thenReturn(mockActivities);

        // When & Then
        mockMvc.perform(get("/api/travel/activities")
                        .param("destination", "Rome")
                        .param("travelStyle", "historical")
                        .param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0]").value("Visit the Colosseum"))
                .andExpect(jsonPath("$[4]").value("Enjoy gelato in Piazza Navona"));
    }

    @Test
    void getDailyItinerary_ShouldReturnChronologicalActivities() throws Exception {
        // Given
        List<String> mockItinerary = Arrays.asList(
                "Morning: Hike to Fushimi Inari shrine",
                "Late Morning: Visit Kiyomizu-dera temple",
                "Lunch: Traditional kaiseki meal in Gion",
                "Afternoon: Explore Arashiyama Bamboo Grove",
                "Evening: Stroll through Philosopher's Path",
                "Night: Dinner in Pontocho Alley"
        );

        when(activityListService.suggestDailyItinerary("Kyoto", "cultural"))
                .thenReturn(mockItinerary);

        // When & Then
        mockMvc.perform(get("/api/travel/itinerary")
                        .param("destination", "Kyoto")
                        .param("travelStyle", "cultural"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0]").value("Morning: Hike to Fushimi Inari shrine"))
                .andExpect(jsonPath("$[5]").value("Night: Dinner in Pontocho Alley"));
    }

    @Test
    void getPackingList_ShouldReturnPackingItems() throws Exception {
        // Given
        List<String> mockPackingList = Arrays.asList(
                "Warm winter coat",
                "Thermal underwear",
                "Waterproof boots",
                "Wool sweaters",
                "Scarf and gloves",
                "Passport",
                "Travel adapter",
                "Camera",
                "Moisturizer",
                "Lip balm"
        );

        when(activityListService.createPackingList("Norway", "winter"))
                .thenReturn(mockPackingList);

        // When & Then
        mockMvc.perform(get("/api/travel/packing-list")
                        .param("destination", "Norway")
                        .param("season", "winter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0]").value("Warm winter coat"))
                .andExpect(jsonPath("$[5]").value("Passport"));
    }

    @Test
    void suggestActivities_WithDefaultCount_ShouldReturnFiveActivities() throws Exception {
        // Given
        List<String> mockActivities = Arrays.asList(
                "Activity 1", "Activity 2", "Activity 3", "Activity 4", "Activity 5"
        );

        when(activityListService.suggestActivities("Paris", "romantic", 5))
                .thenReturn(mockActivities);

        // When & Then - not providing count parameter, should default to 5
        mockMvc.perform(get("/api/travel/activities")
                        .param("destination", "Paris")
                        .param("travelStyle", "romantic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void suggestDestinations_WithDefaultCount_ShouldReturnThreeDestinations() throws Exception {
        // Given
        TravelRequest request = new TravelRequest("spring", "moderate", "nature", "Europe");
        
        Map<String, Object> mockResult = new HashMap<>();
        List<Map<String, Object>> destinations = Arrays.asList(
                Map.of("name", "Destination 1"),
                Map.of("name", "Destination 2"),
                Map.of("name", "Destination 3")
        );
        mockResult.put("destinations", destinations);
        mockResult.put("summary", "Three great destinations");

        when(destinationMapService.suggestMultipleDestinations(any(TravelRequest.class), eq(3)))
                .thenReturn(mockResult);

        // When & Then - not providing count parameter, should default to 3
        mockMvc.perform(post("/api/travel/destinations/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinations.length()").value(3));
    }

    @Test
    void recommendDestination_WithInvalidJson_ShouldReturn400() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/api/travel/destinations/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}