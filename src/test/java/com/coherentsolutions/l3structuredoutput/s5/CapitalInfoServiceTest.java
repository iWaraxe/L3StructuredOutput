package com.coherentsolutions.l3structuredoutput.s5;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
class CapitalInfoServiceTest extends BaseStructuredOutputTest {

    @Mock
    private OpenAiChatModel chatModel;

    private CapitalInfoService capitalInfoService;

    @BeforeEach
    void setUp() {
        capitalInfoService = new CapitalInfoService(chatModel);
    }

    @Test
    void getCapitalInfo_ShouldReturnValidCapitalInfo() {
        // Given
        String country = "France";
        String mockResponse = """
            {
              "city": "Paris",
              "population": 2.2,
              "region": "Île-de-France",
              "language": "French",
              "currency": "Euro",
              "landmarks": ["Eiffel Tower", "Louvre Museum", "Arc de Triomphe", "Notre-Dame Cathedral"]
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        CapitalInfo capitalInfo = capitalInfoService.getCapitalInfo(country);

        // Then
        assertNotNull(capitalInfo);
        assertEquals("Paris", capitalInfo.city());
        assertEquals(2.2, capitalInfo.population());
        assertEquals("Île-de-France", capitalInfo.region());
        assertEquals("French", capitalInfo.language());
        assertEquals("Euro", capitalInfo.currency());
        assertNotNull(capitalInfo.landmarks());
        assertEquals(4, capitalInfo.landmarks().length);
        assertEquals("Eiffel Tower", capitalInfo.landmarks()[0]);
    }

    @Test
    void getCapitalInfo_WithAsianCountry_ShouldReturnAppropriateInfo() {
        // Given
        String country = "Japan";
        String mockResponse = """
            {
              "city": "Tokyo",
              "population": 13.9,
              "region": "Kanto",
              "language": "Japanese",
              "currency": "Yen",
              "landmarks": ["Tokyo Tower", "Senso-ji Temple", "Imperial Palace", "Shibuya Crossing", "Tokyo Skytree"]
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        CapitalInfo capitalInfo = capitalInfoService.getCapitalInfo(country);

        // Then
        assertNotNull(capitalInfo);
        assertEquals("Tokyo", capitalInfo.city());
        assertEquals(13.9, capitalInfo.population());
        assertEquals("Japanese", capitalInfo.language());
        assertEquals("Yen", capitalInfo.currency());
        assertTrue(capitalInfo.landmarks().length >= 3);
    }

    @Test
    void compareCapitals_ShouldReturnMapOfCapitalInfos() {
        // Given
        List<String> countries = Arrays.asList("USA", "UK", "Germany");
        String mockResponse = """
            {
              "USA": {
                "city": "Washington D.C.",
                "population": 0.7,
                "region": "District of Columbia",
                "language": "English",
                "currency": "US Dollar",
                "landmarks": ["White House", "Lincoln Memorial", "Capitol Building"]
              },
              "UK": {
                "city": "London",
                "population": 9.0,
                "region": "Greater London",
                "language": "English",
                "currency": "Pound Sterling",
                "landmarks": ["Big Ben", "Tower Bridge", "Buckingham Palace"]
              },
              "Germany": {
                "city": "Berlin",
                "population": 3.7,
                "region": "Berlin",
                "language": "German",
                "currency": "Euro",
                "landmarks": ["Brandenburg Gate", "Berlin Wall Memorial", "Museum Island"]
              }
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        Map<String, CapitalInfo> capitals = capitalInfoService.compareCapitals(countries);

        // Then
        assertNotNull(capitals);
        assertEquals(3, capitals.size());
        
        // Verify USA
        CapitalInfo usaCapital = capitals.get("USA");
        assertNotNull(usaCapital);
        assertEquals("Washington D.C.", usaCapital.city());
        assertEquals(0.7, usaCapital.population());
        assertEquals("US Dollar", usaCapital.currency());
        
        // Verify UK
        CapitalInfo ukCapital = capitals.get("UK");
        assertNotNull(ukCapital);
        assertEquals("London", ukCapital.city());
        assertEquals(9.0, ukCapital.population());
        
        // Verify Germany
        CapitalInfo germanyCapital = capitals.get("Germany");
        assertNotNull(germanyCapital);
        assertEquals("Berlin", germanyCapital.city());
        assertEquals("German", germanyCapital.language());
    }

    @Test
    void compareCapitals_WithSingleCountry_ShouldReturnSingleEntry() {
        // Given
        List<String> countries = List.of("Brazil");
        String mockResponse = """
            {
              "Brazil": {
                "city": "Brasília",
                "population": 3.0,
                "region": "Federal District",
                "language": "Portuguese",
                "currency": "Brazilian Real",
                "landmarks": ["Cathedral of Brasília", "National Congress", "Palácio da Alvorada"]
              }
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        Map<String, CapitalInfo> capitals = capitalInfoService.compareCapitals(countries);

        // Then
        assertNotNull(capitals);
        assertEquals(1, capitals.size());
        assertTrue(capitals.containsKey("Brazil"));
        
        CapitalInfo brazilCapital = capitals.get("Brazil");
        assertEquals("Brasília", brazilCapital.city());
        assertEquals("Portuguese", brazilCapital.language());
    }

    @Test
    void compareCapitals_WithEmptyList_ShouldReturnEmptyMap() {
        // Given
        List<String> countries = List.of();
        String mockResponse = "{}";

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        Map<String, CapitalInfo> capitals = capitalInfoService.compareCapitals(countries);

        // Then
        assertNotNull(capitals);
        assertTrue(capitals.isEmpty());
    }
}