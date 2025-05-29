package com.coherentsolutions.l3structuredoutput.s4;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import com.coherentsolutions.l3structuredoutput.s4.ConverterFactory.ProductReview;
import com.coherentsolutions.l3structuredoutput.s4.ConverterFactory.ProductSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductAIServiceTest extends BaseStructuredOutputTest {

    @Mock
    private OpenAiChatModel chatModel;

    private ProductAIService productAIService;

    @BeforeEach
    void setUp() {
        productAIService = new ProductAIService(chatModel);
    }

    @Test
    void generateProduct_shouldReturnProduct() {
        // Given
        String category = "Electronics";
        String priceRange = "$500-$1000";
        String jsonResponse = """
            {
                "id": "ELEC-001",
                "name": "Smart Home Hub Pro",
                "description": "Advanced home automation controller with AI",
                "price": 799.99,
                "category": "Electronics",
                "features": [
                    "Voice control",
                    "100+ device compatibility",
                    "AI learning",
                    "Energy monitoring",
                    "Mobile app control"
                ],
                "rating": 4.7
            }
            """;

        ChatResponse mockResponse = createMockResponse(jsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        Product result = productAIService.generateProduct(category, priceRange);

        // Then
        assertNotNull(result);
        assertThat(result.id()).isEqualTo("ELEC-001");
        assertThat(result.name()).isEqualTo("Smart Home Hub Pro");
        assertThat(result.price()).isEqualTo(799.99);
        assertThat(result.category()).isEqualTo("Electronics");
        assertThat(result.features()).hasSize(5);

        // Verify prompt contains parameters
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        String promptText = promptCaptor.getValue().getInstructions().get(0).getText();
        assertThat(promptText).contains(category);
        assertThat(promptText).contains(priceRange);
    }

    @Test
    void generateProductList_shouldReturnMultipleProducts() {
        // Given
        String category = "Books";
        int count = 3;
        String jsonResponse = """
            [
                {
                    "id": "BOOK-001",
                    "name": "AI Programming Guide",
                    "description": "Comprehensive guide to AI development",
                    "price": 49.99,
                    "category": "Books",
                    "features": ["Beginner friendly", "Code examples", "Online resources"],
                    "rating": 4.8
                },
                {
                    "id": "BOOK-002",
                    "name": "Machine Learning Basics",
                    "description": "Introduction to ML concepts",
                    "price": 39.99,
                    "category": "Books",
                    "features": ["Theory", "Practical exercises", "Case studies"],
                    "rating": 4.6
                },
                {
                    "id": "BOOK-003",
                    "name": "Deep Learning Advanced",
                    "description": "Advanced neural network techniques",
                    "price": 59.99,
                    "category": "Books",
                    "features": ["Advanced topics", "Research papers", "Implementation guides"],
                    "rating": 4.9
                }
            ]
            """;

        ChatResponse mockResponse = createMockResponse(jsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        List<Product> results = productAIService.generateProductList(category, count);

        // Then
        assertNotNull(results);
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Product::name)
            .containsExactly("AI Programming Guide", "Machine Learning Basics", "Deep Learning Advanced");
        assertThat(results).allMatch(p -> p.category().equals("Books"));

        // Verify prompt contains count
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        String promptText = promptCaptor.getValue().getInstructions().get(0).getText();
        assertThat(promptText).contains(String.valueOf(count));
    }

    @Test
    void generateProductSummary_shouldReturnProductSummary() {
        // Given
        String productType = "Smartphone";
        String pipeDelimitedResponse = "PHONE-X1|Galaxy Ultra Pro|1299.99|4.8";

        ChatResponse mockResponse = createMockResponse(pipeDelimitedResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        ProductSummary result = productAIService.generateProductSummary(productType);

        // Then
        assertNotNull(result);
        assertThat(result.id()).isEqualTo("PHONE-X1");
        assertThat(result.name()).isEqualTo("Galaxy Ultra Pro");
        assertThat(result.price()).isEqualTo(1299.99);
        assertThat(result.rating()).isEqualTo(4.8);

        // Verify custom format in prompt
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        String promptText = promptCaptor.getValue().getInstructions().get(0).getText();
        assertThat(promptText).contains("ProductID|ProductName|Price|Rating");
    }

    @Test
    void generateProductReview_shouldReturnProductReview() {
        // Given
        String productId = "PROD-123";
        String sentiment = "positive";
        String jsonResponse = """
            {
                "productId": "PROD-123",
                "reviewTitle": "Excellent purchase!",
                "reviewContent": "Very satisfied with this product. Works exactly as described.",
                "rating": 5.0
            }
            """;

        ChatResponse mockResponse = createMockResponse(jsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        ProductReview result = productAIService.generateProductReview(productId, sentiment);

        // Then
        assertNotNull(result);
        assertThat(result.productId()).isEqualTo("PROD-123");
        assertThat(result.reviewTitle()).isEqualTo("Excellent purchase!");
        assertThat(result.reviewContent()).contains("satisfied");
        assertThat(result.rating()).isEqualTo(5.0);

        // Verify sentiment in prompt
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        String promptText = promptCaptor.getValue().getInstructions().get(0).getText();
        assertThat(promptText).contains(sentiment);
    }

    @Test
    void generateProductFeatures_shouldReturnFeatureList() {
        // Given
        String productType = "Laptop";
        int featureCount = 5;
        String csvResponse = "High-resolution display, Long battery life, Fast processor, Lightweight design, Multiple ports";

        ChatResponse mockResponse = createMockResponse(csvResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        List<String> results = productAIService.generateProductFeatures(productType, featureCount);

        // Then
        assertNotNull(results);
        assertThat(results).hasSize(5);
        assertThat(results).contains("High-resolution display", "Long battery life", "Fast processor");

        // Verify list format in prompt
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        String promptText = promptCaptor.getValue().getInstructions().get(0).getText();
        assertThat(promptText).contains("comma");
    }

    @Test
    void generateProductProperties_shouldReturnPropertiesMap() {
        // Given
        String productType = "Furniture";
        String jsonResponse = """
            {
                "dimensions": "120cm x 80cm x 75cm",
                "weight": "25kg",
                "material": "Solid oak wood",
                "warranty": "5 years",
                "country_of_origin": "Sweden"
            }
            """;

        ChatResponse mockResponse = createMockResponse(jsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        Map<String, Object> results = productAIService.generateProductProperties(productType);

        // Then
        assertNotNull(results);
        assertThat(results).containsKeys("dimensions", "weight", "material", "warranty", "country_of_origin");
        assertThat(results.get("material")).isEqualTo("Solid oak wood");
        assertThat(results.get("warranty")).isEqualTo("5 years");

        // Verify properties listed in prompt
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        String promptText = promptCaptor.getValue().getInstructions().get(0).getText();
        assertThat(promptText).contains("dimensions");
        assertThat(promptText).contains("material");
    }

    @Test
    void allMethods_shouldIncludeFormatInstructions() {
        // Given
        ChatResponse mockResponse = createMockResponse("{}");
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        productAIService.generateProduct("Test", "$100");

        // Then
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        String promptText = promptCaptor.getValue().getInstructions().get(0).getText();
        
        // All methods should include format instructions
        assertThat(promptText).containsAnyOf("JSON", "format", "schema", "ProductID|ProductName");
    }
}