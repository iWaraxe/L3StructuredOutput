package com.coherentsolutions.l3structuredoutput.s9;

import com.coherentsolutions.l3structuredoutput.s9.models.ProductCatalog;
import com.coherentsolutions.l3structuredoutput.s9.models.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropertyOrderingController.class)
class PropertyOrderingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PropertyOrderingService propertyOrderingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateUserProfile_ShouldReturnUserProfile() throws Exception {
        // Given
        UserProfile mockProfile = new UserProfile(
                "user123",
                "johndoe",
                "john@example.com",
                "John Doe",
                28,
                Map.of("theme", "dark", "language", "en"),
                UserProfile.AccountStatus.ACTIVE,
                LocalDateTime.of(2024, 1, 15, 10, 30),
                List.of("developer", "ai-enthusiast"),
                null
        );

        when(propertyOrderingService.generateUserProfile(anyString())).thenReturn(mockProfile);

        // When & Then
        mockMvc.perform(get("/api/property-ordering/user-profile")
                        .param("description", "Tech developer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value("user123"))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.full_name").value("John Doe"))
                .andExpect(jsonPath("$.age").value(28))
                .andExpect(jsonPath("$.preferences.theme").value("dark"))
                .andExpect(jsonPath("$.account_status").value("ACTIVE"))
                .andExpect(jsonPath("$.tags[0]").value("developer"));
    }

    @Test
    void generateProductCatalog_ShouldReturnCatalog() throws Exception {
        // Given
        ProductCatalog mockCatalog = new ProductCatalog(
                "cat001",
                "Electronics Store",
                LocalDate.of(2024, 1, 15),
                1500,
                List.of(
                        new ProductCatalog.Category("cat1", "Laptops", "Computing devices", 250),
                        new ProductCatalog.Category("cat2", "Phones", "Mobile devices", 400)
                ),
                List.of(
                        new ProductCatalog.FeaturedProduct(
                                "prod1", "MacBook Pro", 2499.99, 10, 4.8, 
                                ProductCatalog.Availability.IN_STOCK
                        )
                )
        );

        when(propertyOrderingService.generateProductCatalog(anyString(), anyInt()))
                .thenReturn(mockCatalog);

        // When & Then
        mockMvc.perform(get("/api/property-ordering/product-catalog")
                        .param("type", "electronics")
                        .param("categories", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.catalog_id").value("cat001"))
                .andExpect(jsonPath("$.catalog_name").value("Electronics Store"))
                .andExpect(jsonPath("$.total_products").value(1500))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories[0].category_name").value("Laptops"))
                .andExpect(jsonPath("$.featured_products[0].product_name").value("MacBook Pro"));
    }

    @Test
    void getSchema_WithValidClassName_ShouldReturnSchema() throws Exception {
        // Given
        String mockSchema = "Generated schema for UserProfile with annotations";
        when(propertyOrderingService.getAnnotatedSchema(UserProfile.class))
                .thenReturn(mockSchema);

        // When & Then
        mockMvc.perform(get("/api/property-ordering/schema/UserProfile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.className").value("UserProfile"))
                .andExpect(jsonPath("$.schema").value(mockSchema))
                .andExpect(jsonPath("$.note").exists());
    }

    @Test
    void getSchema_WithInvalidClassName_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/property-ordering/schema/InvalidClass"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown class: InvalidClass"))
                .andExpect(jsonPath("$.availableClasses").isArray());
    }

    @Test
    void demonstrateOrdering_WithUserProfile_ShouldReturnOrderedJson() throws Exception {
        // Given
        String orderedJson = """
                {
                  "user_id" : "user123",
                  "username" : "johndoe",
                  "email" : "john@example.com",
                  "full_name" : "John Doe"
                }
                """;
        when(propertyOrderingService.demonstratePropertyOrdering(any()))
                .thenReturn(orderedJson);

        // When & Then
        mockMvc.perform(get("/api/property-ordering/demo/user-profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("user-profile"))
                .andExpect(jsonPath("$.orderedJson").exists())
                .andExpect(jsonPath("$.note").exists());
    }

    @Test
    void validatePropertyOrder_WithValidJson_ShouldReturnValid() throws Exception {
        // Given
        String validJson = """
                {
                  "user_id": "123",
                  "username": "test",
                  "email": "test@example.com"
                }
                """;
        when(propertyOrderingService.validatePropertyOrder(anyString(), eq(UserProfile.class)))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/property-ordering/validate/UserProfile")
                        .contentType("application/json")
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.className").value("UserProfile"))
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.message").value("JSON respects property ordering"));
    }

    @Test
    void generateUserProfile_WithDefaultDescription_ShouldWork() throws Exception {
        // Given
        UserProfile mockProfile = new UserProfile(
                "default123", "defaultuser", "default@example.com", "Default User",
                25, Map.of("theme", "light"), UserProfile.AccountStatus.ACTIVE,
                LocalDateTime.now(), List.of("user"), null
        );

        when(propertyOrderingService.generateUserProfile(anyString())).thenReturn(mockProfile);

        // When & Then
        mockMvc.perform(get("/api/property-ordering/user-profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value("default123"));
    }

    @Test
    void generateProductCatalog_WithDefaultParameters_ShouldWork() throws Exception {
        // Given
        ProductCatalog mockCatalog = new ProductCatalog(
                "default001", "Default Store", LocalDate.now(), 100,
                List.of(), List.of()
        );

        when(propertyOrderingService.generateProductCatalog(anyString(), anyInt()))
                .thenReturn(mockCatalog);

        // When & Then
        mockMvc.perform(get("/api/property-ordering/product-catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.catalog_id").value("default001"));
    }
}