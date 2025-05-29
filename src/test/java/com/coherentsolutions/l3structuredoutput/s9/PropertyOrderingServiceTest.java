package com.coherentsolutions.l3structuredoutput.s9;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import com.coherentsolutions.l3structuredoutput.s9.models.ProductCatalog;
import com.coherentsolutions.l3structuredoutput.s9.models.UserProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyOrderingServiceTest extends BaseStructuredOutputTest {

    @Mock
    private OpenAiChatModel chatModel;

    private PropertyOrderingService propertyOrderingService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        propertyOrderingService = new PropertyOrderingService(chatModel);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void generateUserProfile_ShouldReturnOrderedUserProfile() {
        // Given
        String description = "Tech-savvy developer interested in AI and machine learning";
        String mockResponse = """
                {
                    "user_id": "dev123",
                    "username": "aidev",
                    "email": "dev@techcompany.com",
                    "full_name": "Alex Developer",
                    "age": 28,
                    "preferences": {
                        "theme": "dark",
                        "language": "en",
                        "notifications": true,
                        "editor": "vscode"
                    },
                    "account_status": "ACTIVE",
                    "last_login": "2024-01-15T10:30:00",
                    "tags": ["developer", "ai-enthusiast", "early-adopter"]
                }
                """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        UserProfile profile = propertyOrderingService.generateUserProfile(description);

        // Then
        assertNotNull(profile);
        assertEquals("dev123", profile.userId());
        assertEquals("aidev", profile.username());
        assertEquals("dev@techcompany.com", profile.email());
        assertEquals("Alex Developer", profile.fullName());
        assertEquals(28, profile.age());
        assertNotNull(profile.preferences());
        assertEquals("dark", profile.preferences().get("theme"));
        assertEquals(UserProfile.AccountStatus.ACTIVE, profile.accountStatus());
        assertNotNull(profile.lastLogin());
        assertEquals(3, profile.tags().size());
        assertTrue(profile.tags().contains("developer"));
    }

    @Test
    void generateProductCatalog_ShouldReturnOrderedCatalog() {
        // Given
        String catalogType = "electronics";
        int categoryCount = 3;
        String mockResponse = """
                {
                    "catalog_id": "elec2024",
                    "catalog_name": "Electronics Superstore",
                    "last_updated": "2024-01-15",
                    "total_products": 1500,
                    "categories": [
                        {
                            "category_id": "cat1",
                            "category_name": "Laptops",
                            "description": "High-performance computing devices",
                            "product_count": 250
                        },
                        {
                            "category_id": "cat2",
                            "category_name": "Smartphones",
                            "description": "Latest mobile devices",
                            "product_count": 400
                        },
                        {
                            "category_id": "cat3",
                            "category_name": "Audio",
                            "description": "Headphones and speakers",
                            "product_count": 300
                        }
                    ],
                    "featured_products": [
                        {
                            "product_id": "prod1",
                            "product_name": "MacBook Pro 16",
                            "price": 2499.99,
                            "discount": 10,
                            "rating": 4.8,
                            "availability": "IN_STOCK"
                        },
                        {
                            "product_id": "prod2",
                            "product_name": "iPhone 15 Pro",
                            "price": 1199.99,
                            "rating": 4.9,
                            "availability": "LOW_STOCK"
                        }
                    ]
                }
                """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        ProductCatalog catalog = propertyOrderingService.generateProductCatalog(catalogType, categoryCount);

        // Then
        assertNotNull(catalog);
        assertEquals("elec2024", catalog.catalogId());
        assertEquals("Electronics Superstore", catalog.catalogName());
        assertNotNull(catalog.lastUpdated());
        assertEquals(1500, catalog.totalProducts());
        assertEquals(3, catalog.categories().size());
        assertEquals("Laptops", catalog.categories().get(0).categoryName());
        assertEquals(2, catalog.featuredProducts().size());
        assertEquals(2499.99, catalog.featuredProducts().get(0).price());
        assertEquals(ProductCatalog.Availability.IN_STOCK, catalog.featuredProducts().get(0).availability());
    }

    @Test
    void getAnnotatedSchema_ShouldReturnFormattedSchema() {
        // When
        String userProfileSchema = propertyOrderingService.getAnnotatedSchema(UserProfile.class);
        String productCatalogSchema = propertyOrderingService.getAnnotatedSchema(ProductCatalog.class);

        // Then
        assertNotNull(userProfileSchema);
        assertNotNull(productCatalogSchema);
        // The schema format has the class information and structure
        assertTrue(userProfileSchema.length() > 100); // Ensure we got a substantial schema
        assertTrue(productCatalogSchema.length() > 100);
        // The converter generates JSON schema format, so we should see basic JSON structure
        assertTrue(userProfileSchema.contains("{") && userProfileSchema.contains("}"));
        assertTrue(productCatalogSchema.contains("{") && productCatalogSchema.contains("}"));
    }

    @Test
    void demonstratePropertyOrdering_ShouldMaintainFieldOrder() throws JsonProcessingException {
        // Given
        UserProfile profile = new UserProfile(
                "user123",
                "johndoe",
                "john@example.com",
                "John Doe",
                28,
                Map.of("theme", "dark", "language", "en"),
                UserProfile.AccountStatus.ACTIVE,
                LocalDateTime.now(),
                List.of("developer", "gamer"),
                "Internal notes"
        );

        // When
        String json = propertyOrderingService.demonstratePropertyOrdering(profile);
        JsonNode jsonNode = objectMapper.readTree(json);

        // Then
        assertNotNull(json);
        // Verify field order by checking iterator
        Iterator<String> fieldNames = jsonNode.fieldNames();
        assertEquals("user_id", fieldNames.next());
        assertEquals("username", fieldNames.next());
        assertEquals("email", fieldNames.next());
        assertEquals("full_name", fieldNames.next());
        // Verify @JsonIgnore works
        assertFalse(json.contains("internalNotes"));
    }

    @Test
    void validatePropertyOrder_WithValidJson_ShouldReturnTrue() throws JsonProcessingException {
        // Given
        // Create a simple JSON that matches the UserProfile structure
        String validJson = """
                {
                    "user_id": "user123",
                    "username": "johndoe",
                    "email": "john@example.com",
                    "full_name": "John Doe",
                    "age": 28,
                    "preferences": {"theme": "dark"},
                    "account_status": "ACTIVE",
                    "last_login": "2024-01-15T10:30:00",
                    "tags": ["developer"]
                }
                """;

        // When
        boolean isValid = propertyOrderingService.validatePropertyOrder(validJson, UserProfile.class);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validatePropertyOrder_WithInvalidJson_ShouldReturnFalse() {
        // Given
        String invalidJson = "{ invalid json }";

        // When
        boolean isValid = propertyOrderingService.validatePropertyOrder(invalidJson, UserProfile.class);

        // Then
        assertFalse(isValid);
    }

    @Test
    void generateUserProfile_WithComplexPreferences_ShouldHandleNestedMaps() {
        // Given
        String description = "Power user with complex preferences";
        String mockResponse = """
                {
                    "user_id": "power123",
                    "username": "poweruser",
                    "email": "power@example.com",
                    "full_name": "Power User",
                    "age": 35,
                    "preferences": {
                        "theme": "custom",
                        "language": "en",
                        "notifications": {
                            "email": true,
                            "push": false,
                            "sms": true
                        },
                        "privacy": {
                            "profile_visibility": "friends",
                            "activity_tracking": false
                        }
                    },
                    "account_status": "ACTIVE",
                    "last_login": "2024-01-15T15:45:00",
                    "tags": ["power-user", "beta-tester"]
                }
                """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        UserProfile profile = propertyOrderingService.generateUserProfile(description);

        // Then
        assertNotNull(profile);
        assertEquals("power123", profile.userId());
        assertNotNull(profile.preferences());
        assertTrue(profile.preferences().containsKey("notifications"));
        assertTrue(profile.preferences().containsKey("privacy"));
    }
}