package com.coherentsolutions.l3structuredoutput.s8.controllers;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import com.coherentsolutions.l3structuredoutput.s8.models.ResponseModels.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.ai.chat.prompt.Prompt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OpenAiJsonModesController.class)
class OpenAiJsonModesControllerTest extends BaseStructuredOutputTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatModel openAiChatModel;

    @BeforeEach
    void setUp() {
        // Setup is handled by BaseStructuredOutputTest
    }

    @Test
    void getProductRecommendationJsonObject_ShouldReturnJsonString() throws Exception {
        // Given
        String mockJsonResponse = """
            {
              "name": "iPhone 15 Pro",
              "category": "smartphone",
              "price": 999.99,
              "rating": 4.8,
              "features": ["A17 Pro chip", "Titanium design", "48MP camera", "USB-C port"],
              "availability": "In stock"
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockJsonResponse);
        when(openAiChatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When & Then
        mockMvc.perform(get("/json-modes/openai/json-object")
                        .param("productType", "smartphone"))
                .andExpect(status().isOk())
                .andExpect(content().json(mockJsonResponse));
    }

    @Test
    void getProductRecommendationJsonSchema_ShouldReturnProductRecommendation() throws Exception {
        // Given
        String mockJsonResponse = """
            {
              "name": "MacBook Pro 14",
              "category": "laptop",
              "price": 1999.00,
              "rating": 4.9,
              "features": ["M3 Pro chip", "14-inch Liquid Retina XDR display", "18-hour battery life"],
              "availability": true
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockJsonResponse);
        when(openAiChatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When & Then
        mockMvc.perform(get("/json-modes/openai/json-schema")
                        .param("productType", "laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MacBook Pro 14"))
                .andExpect(jsonPath("$.category").value("laptop"))
                .andExpect(jsonPath("$.price").value(1999.00))
                .andExpect(jsonPath("$.rating").value(4.9))
                .andExpect(jsonPath("$.features").isArray())
                .andExpect(jsonPath("$.features[0]").value("M3 Pro chip"))
                .andExpect(jsonPath("$.availability").value(true));
    }

    @Test
    void searchWithJsonSchema_ShouldReturnSearchResults() throws Exception {
        // Given
        String mockJsonResponse = """
            {
              "query": "best coffee shops",
              "results": [
                {
                  "title": "Top 10 Coffee Shops in NYC",
                  "url": "https://example.com/coffee-nyc",
                  "relevanceScore": 0.95,
                  "summary": "Discover the best coffee shops in New York City"
                },
                {
                  "title": "Coffee Shop Reviews 2024",
                  "url": "https://example.com/reviews",
                  "relevanceScore": 0.88,
                  "summary": "Comprehensive reviews of coffee shops worldwide"
                }
              ],
              "metadata": {
                "totalResults": 42,
                "searchTime": 0.234,
                "filters": ["location", "rating", "price"]
              }
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockJsonResponse);
        when(openAiChatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When & Then
        mockMvc.perform(get("/json-modes/openai/search")
                        .param("query", "best coffee shops")
                        .param("numResults", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("best coffee shops"))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(2))
                .andExpect(jsonPath("$.results[0].title").value("Top 10 Coffee Shops in NYC"))
                .andExpect(jsonPath("$.results[0].relevanceScore").value(0.95))
                .andExpect(jsonPath("$.metadata.totalResults").value(42))
                .andExpect(jsonPath("$.metadata.searchTime").value(0.234))
                .andExpect(jsonPath("$.metadata.filters").isArray());
    }

    @Test
    void compareJsonModes_ShouldReturnComparisonResult() throws Exception {
        // Given
        String jsonObjectResponse = """
            {
              "ticker": "AAPL",
              "currentPrice": 185.50,
              "peRatio": 30.2,
              "marketCap": 2900000000000,
              "dividendYield": 0.5,
              "fiftyTwoWeekRange": {
                "low": 164.08,
                "high": 198.23
              },
              "analysis": "Apple shows strong fundamentals with consistent growth."
            }
            """;

        String jsonSchemaResponse = """
            {
              "stockSymbol": "AAPL",
              "currentPrice": 185.50,
              "analysis": "Apple shows strong fundamentals with consistent growth.",
              "metrics": {
                "peRatio": 30.2,
                "marketCap": "2.9T",
                "dividendYield": 0.5,
                "fiftyTwoWeekRange": "164.08 - 198.23"
              }
            }
            """;

        // Set up mock responses for the two different calls
        ChatResponse chatResponse1 = createMockResponse(jsonObjectResponse);
        ChatResponse chatResponse2 = createMockResponse(jsonSchemaResponse);
        
        when(openAiChatModel.call(any(Prompt.class)))
                .thenReturn(chatResponse1)
                .thenReturn(chatResponse2);

        // When & Then
        mockMvc.perform(post("/json-modes/openai/compare-json-modes")
                        .param("stockSymbol", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonObjectResponse").exists())
                .andExpect(jsonPath("$.jsonSchemaResponse").exists())
                .andExpect(jsonPath("$.parsedStockAnalysis").exists())
                .andExpect(jsonPath("$.parsedStockAnalysis.stockSymbol").value("AAPL"))
                .andExpect(jsonPath("$.parsedStockAnalysis.currentPrice").value(185.50))
                .andExpect(jsonPath("$.parsedStockAnalysis.metrics.peRatio").value(30.2));
    }

    @Test
    void getProductRecommendationJsonObject_WithDefaultProductType_ShouldUseSmartphone() throws Exception {
        // Given
        String mockJsonResponse = """
            {
              "name": "Samsung Galaxy S24",
              "category": "smartphone",
              "price": 899.99,
              "rating": 4.7,
              "features": ["Snapdragon 8 Gen 3", "AMOLED display", "50MP camera"],
              "availability": "In stock"
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockJsonResponse);
        when(openAiChatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When & Then - not providing productType, should default to smartphone
        mockMvc.perform(get("/json-modes/openai/json-object"))
                .andExpect(status().isOk())
                .andExpect(content().json(mockJsonResponse));
    }

    @Test
    void getProductRecommendationJsonSchema_WithDefaultProductType_ShouldUseLaptop() throws Exception {
        // Given
        String mockJsonResponse = """
            {
              "name": "Dell XPS 15",
              "category": "laptop",
              "price": 1599.00,
              "rating": 4.6,
              "features": ["Intel Core i7", "16GB RAM", "512GB SSD"],
              "availability": true
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockJsonResponse);
        when(openAiChatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When & Then - not providing productType, should default to laptop
        mockMvc.perform(get("/json-modes/openai/json-schema"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("laptop"));
    }

    @Test
    void searchWithJsonSchema_WithDefaultNumResults_ShouldReturnFiveResults() throws Exception {
        // Given
        String mockJsonResponse = """
            {
              "query": "spring boot tutorials",
              "results": [
                {"title": "Result 1", "url": "url1", "relevanceScore": 0.9, "summary": "Summary 1"},
                {"title": "Result 2", "url": "url2", "relevanceScore": 0.8, "summary": "Summary 2"},
                {"title": "Result 3", "url": "url3", "relevanceScore": 0.7, "summary": "Summary 3"},
                {"title": "Result 4", "url": "url4", "relevanceScore": 0.6, "summary": "Summary 4"},
                {"title": "Result 5", "url": "url5", "relevanceScore": 0.5, "summary": "Summary 5"}
              ],
              "metadata": {
                "totalResults": 100,
                "searchTime": 0.150,
                "filters": []
              }
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockJsonResponse);
        when(openAiChatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When & Then - not providing numResults, should default to 5
        mockMvc.perform(get("/json-modes/openai/search")
                        .param("query", "spring boot tutorials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(5));
    }

    @Test
    void searchWithJsonSchema_WithEmptyQuery_ShouldStillWork() throws Exception {
        // Given
        String mockJsonResponse = """
            {
              "query": "",
              "results": [],
              "metadata": {
                "totalResults": 0,
                "searchTime": 0.001,
                "filters": []
              }
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockJsonResponse);
        when(openAiChatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When & Then
        mockMvc.perform(get("/json-modes/openai/search")
                        .param("query", "")
                        .param("numResults", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value(""))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(0))
                .andExpect(jsonPath("$.metadata.totalResults").value(0));
    }
}