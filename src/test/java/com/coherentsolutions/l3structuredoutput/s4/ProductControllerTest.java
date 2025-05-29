package com.coherentsolutions.l3structuredoutput.s4;

import com.coherentsolutions.l3structuredoutput.s4.ConverterFactory.ProductReview;
import com.coherentsolutions.l3structuredoutput.s4.ConverterFactory.ProductSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductAIService productAIService;

    @Test
    void generateProduct_shouldReturnProduct() throws Exception {
        // Given
        String category = "Electronics";
        String priceRange = "$100-$500";
        Product product = new Product(
            "E123",
            "Wireless Earbuds",
            "Premium noise-canceling earbuds",
            299.99,
            "Electronics",
            List.of("Noise canceling", "30hr battery", "Waterproof"),
            4.8
        );

        when(productAIService.generateProduct(category, priceRange)).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/api/products/generate")
                .param("category", category)
                .param("priceRange", priceRange))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("E123"))
            .andExpect(jsonPath("$.name").value("Wireless Earbuds"))
            .andExpect(jsonPath("$.price").value(299.99))
            .andExpect(jsonPath("$.features").isArray())
            .andExpect(jsonPath("$.features[0]").value("Noise canceling"));
    }

    @Test
    void generateProductList_shouldReturnProductList() throws Exception {
        // Given
        String category = "Books";
        List<Product> products = Arrays.asList(
            new Product("B1", "Book 1", "Description 1", 19.99, "Books", List.of("Feature 1"), 4.5),
            new Product("B2", "Book 2", "Description 2", 24.99, "Books", List.of("Feature 2"), 4.3)
        );

        when(productAIService.generateProductList(category, 3)).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/generate/list")
                .param("category", category))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].id").value("B1"))
            .andExpect(jsonPath("$[1].id").value("B2"));
    }

    @Test
    void generateProductSummary_shouldReturnProductSummary() throws Exception {
        // Given
        String productType = "Laptop";
        ProductSummary summary = new ProductSummary("LAP-001", "Gaming Laptop Pro", 1599.99, 4.7);

        when(productAIService.generateProductSummary(productType)).thenReturn(summary);

        // When & Then
        mockMvc.perform(get("/api/products/generate/summary")
                .param("productType", productType))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("LAP-001"))
            .andExpect(jsonPath("$.name").value("Gaming Laptop Pro"))
            .andExpect(jsonPath("$.price").value(1599.99))
            .andExpect(jsonPath("$.rating").value(4.7));
    }

    @Test
    void generateProductReview_shouldReturnProductReview() throws Exception {
        // Given
        String productId = "PROD-456";
        ProductReview review = new ProductReview(
            "PROD-456",
            "Amazing product!",
            "I'm very happy with this purchase. Highly recommend!",
            4.5
        );

        when(productAIService.generateProductReview(productId, "positive")).thenReturn(review);

        // When & Then
        mockMvc.perform(get("/api/products/generate/review")
                .param("productId", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value("PROD-456"))
            .andExpect(jsonPath("$.reviewTitle").value("Amazing product!"))
            .andExpect(jsonPath("$.rating").value(4.5));
    }

    @Test
    void generateProductFeatures_shouldReturnFeatureList() throws Exception {
        // Given
        String productType = "Smartphone";
        List<String> features = Arrays.asList(
            "5G connectivity",
            "48MP camera",
            "All-day battery",
            "Water resistant"
        );

        when(productAIService.generateProductFeatures(productType, 5)).thenReturn(features);

        // When & Then
        mockMvc.perform(get("/api/products/generate/features")
                .param("productType", productType))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0]").value("5G connectivity"))
            .andExpect(jsonPath("$[1]").value("48MP camera"));
    }

    @Test
    void generateProductProperties_shouldReturnPropertiesMap() throws Exception {
        // Given
        String productType = "Table";
        Map<String, Object> properties = Map.of(
            "dimensions", "150cm x 90cm x 75cm",
            "weight", "30kg",
            "material", "Solid wood",
            "warranty", "2 years"
        );

        when(productAIService.generateProductProperties(productType)).thenReturn(properties);

        // When & Then
        mockMvc.perform(get("/api/products/generate/properties")
                .param("productType", productType))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dimensions").value("150cm x 90cm x 75cm"))
            .andExpect(jsonPath("$.weight").value("30kg"))
            .andExpect(jsonPath("$.material").value("Solid wood"))
            .andExpect(jsonPath("$.warranty").value("2 years"));
    }

    @Test
    void generateProduct_withMissingParameters_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products/generate")
                .param("category", "Electronics"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void generateProductReview_withCustomSentiment_shouldWork() throws Exception {
        // Given
        String productId = "PROD-789";
        String sentiment = "negative";
        ProductReview review = new ProductReview(
            "PROD-789",
            "Disappointed",
            "Product didn't meet expectations",
            2.0
        );

        when(productAIService.generateProductReview(productId, sentiment)).thenReturn(review);

        // When & Then
        mockMvc.perform(get("/api/products/generate/review")
                .param("productId", productId)
                .param("sentiment", sentiment))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rating").value(2.0));
    }
}