package com.coherentsolutions.l3structuredoutput.s11;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import com.coherentsolutions.l3structuredoutput.s11.models.OrderRequest;
import com.coherentsolutions.l3structuredoutput.s11.models.ValidationResult;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest extends BaseStructuredOutputTest {

    @Mock
    private OpenAiChatModel chatModel;

    private ValidationService validationService;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validationService = new ValidationService(chatModel, validator);
    }

    @Test
    void generateAndValidateOrder_WithValidOrder_ShouldReturnValid() {
        // Given
        String description = "Customer wants 2 laptops for $3000";
        String mockResponse = """
                {
                    "orderId": "ORD-123456",
                    "customerEmail": "customer@example.com",
                    "orderDate": "2024-01-15",
                    "items": [
                        {
                            "productId": "PROD-001",
                            "productName": "Laptop",
                            "quantity": 2,
                            "unitPrice": 1500.00
                        }
                    ],
                    "totalAmount": 3000.00,
                    "shippingAddress": {
                        "street": "123 Main St",
                        "city": "New York",
                        "state": "NY",
                        "zipCode": "10001",
                        "country": "US"
                    },
                    "paymentMethod": "CREDIT_CARD"
                }
                """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        ValidationResult result = validationService.generateAndValidateOrder(description);

        // Then
        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
        assertNotNull(result.finalOutput());
        assertInstanceOf(OrderRequest.class, result.finalOutput());
        
        OrderRequest order = (OrderRequest) result.finalOutput();
        assertEquals("ORD-123456", order.orderId());
        assertEquals("customer@example.com", order.customerEmail());
        assertEquals(2, order.items().get(0).quantity());
    }

    @Test
    void generateAndValidateOrder_WithInvalidOrderId_ShouldRecoverAndFix() {
        // Given
        String description = "Order for office supplies";
        String mockResponse = """
                {
                    "orderId": "ORDER123",
                    "customerEmail": "office@company.com",
                    "orderDate": "2024-01-15",
                    "items": [
                        {
                            "productId": "PROD-002",
                            "productName": "Office Chair",
                            "quantity": 5,
                            "unitPrice": 200.00
                        }
                    ],
                    "totalAmount": 1000.00,
                    "shippingAddress": {
                        "street": "456 Business Ave",
                        "city": "Boston",
                        "state": "MA",
                        "zipCode": "02101",
                        "country": "US"
                    },
                    "paymentMethod": "BANK_TRANSFER"
                }
                """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        ValidationResult result = validationService.generateAndValidateOrder(description);

        // Then
        assertTrue(result.valid()); // Should be valid after recovery
        assertFalse(result.errors().isEmpty()); // Should have original errors recorded
        assertTrue(result.errors().stream()
                .anyMatch(e -> e.field().equals("orderId")));
        assertFalse(result.recoveryAttempts().isEmpty());
        assertTrue(result.recoveryAttempts().stream()
                .anyMatch(r -> r.strategy().equals("fix_order_id") && r.success()));
    }

    @Test
    void generateAndValidateOrder_WithFutureDate_ShouldRecoverAndFix() {
        // Given
        String description = "Future order";
        LocalDate futureDate = LocalDate.now().plusDays(10);
        String mockResponse = String.format("""
                {
                    "orderId": "ORD-999999",
                    "customerEmail": "future@example.com",
                    "orderDate": "%s",
                    "items": [
                        {
                            "productId": "PROD-003",
                            "productName": "Widget",
                            "quantity": 1,
                            "unitPrice": 50.00
                        }
                    ],
                    "totalAmount": 50.00,
                    "shippingAddress": {
                        "street": "789 Future Blvd",
                        "city": "Chicago",
                        "state": "IL",
                        "zipCode": "60601",
                        "country": "US"
                    },
                    "paymentMethod": "PAYPAL"
                }
                """, futureDate);

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        ValidationResult result = validationService.generateAndValidateOrder(description);

        // Then
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream()
                .anyMatch(e -> e.field().contains("orderDate")));
        assertTrue(result.recoveryAttempts().stream()
                .anyMatch(r -> r.strategy().equals("fix_future_date")));
    }

    @Test
    void generateAndValidateOrder_WithMalformedJson_ShouldAttemptRecovery() {
        // Given
        String description = "Malformed order";
        String mockResponse = """
                Here is the order: {
                    'orderId': 'ORD-111111',
                    'customerEmail': 'test@example.com',
                    'orderDate': '2024-01-15',
                    'items': [
                        {
                            'productId': 'PROD-004',
                            'productName': 'Test Product',
                            'quantity': 1,
                            'unitPrice': 100.00
                        }
                    ],
                    'totalAmount': 100.00,
                    'shippingAddress': {
                        'street': '123 Test St',
                        'city': 'Seattle',
                        'state': 'WA',
                        'zipCode': '98101',
                        'country': 'US'
                    },
                    'paymentMethod': 'CREDIT_CARD'
                }
                """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        ValidationResult result = validationService.generateAndValidateOrder(description);

        // Then
        assertFalse(result.recoveryAttempts().isEmpty());
        assertTrue(result.recoveryAttempts().stream()
                .anyMatch(r -> r.strategy().equals("extract_json") || r.strategy().equals("fix_json")));
    }

    @Test
    void validateRawJson_WithValidJson_ShouldReturnValid() {
        // Given
        String validJson = """
                {
                    "name": "Test",
                    "value": 123,
                    "items": ["a", "b", "c"]
                }
                """;

        // When
        ValidationResult result = validationService.validateRawJson(validJson);

        // Then
        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
        assertNotNull(result.finalOutput());
    }

    @Test
    void validateRawJson_WithInvalidJson_ShouldAttemptRecovery() {
        // Given
        String invalidJson = """
                {
                    'name': 'Test',
                    'value': 123,
                    'items': ['a', 'b', 'c'],
                }
                """;

        // When
        ValidationResult result = validationService.validateRawJson(invalidJson);

        // Then
        assertFalse(result.recoveryAttempts().isEmpty());
        assertTrue(result.recoveryAttempts().stream()
                .anyMatch(r -> r.strategy().equals("fix_json_syntax")));
    }

    @Test
    void validateRawJson_WithEmptyObject_ShouldAddWarning() {
        // Given
        String emptyJson = "{}";

        // When
        ValidationResult result = validationService.validateRawJson(emptyJson);

        // Then
        assertTrue(result.valid());
        assertFalse(result.warnings().isEmpty());
        assertTrue(result.warnings().stream()
                .anyMatch(w -> w.message().contains("Empty JSON object")));
    }

    @Test
    void validateRawJson_WithFieldNamesContainingSpaces_ShouldAddError() {
        // Given
        String jsonWithSpaces = """
                {
                    "field name": "value",
                    "valid_field": "another value"
                }
                """;

        // When
        ValidationResult result = validationService.validateRawJson(jsonWithSpaces);

        // Then
        assertFalse(result.valid());
        assertTrue(result.errors().stream()
                .anyMatch(e -> e.message().contains("Field name contains spaces")));
    }

    @Test
    void generateAndValidateOrder_WithHighAmount_ShouldAddWarning() {
        // Given
        String description = "Large order";
        String mockResponse = """
                {
                    "orderId": "ORD-777777",
                    "customerEmail": "bigspender@example.com",
                    "orderDate": "2024-01-15",
                    "items": [
                        {
                            "productId": "PROD-005",
                            "productName": "Luxury Item",
                            "quantity": 10,
                            "unitPrice": 5000.00
                        }
                    ],
                    "totalAmount": 50000.00,
                    "shippingAddress": {
                        "street": "999 Luxury Lane",
                        "city": "Miami",
                        "state": "FL",
                        "zipCode": "33101",
                        "country": "US"
                    },
                    "paymentMethod": "BANK_TRANSFER"
                }
                """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        ValidationResult result = validationService.generateAndValidateOrder(description);

        // Then
        assertTrue(result.valid());
        assertFalse(result.warnings().isEmpty());
        assertTrue(result.warnings().stream()
                .anyMatch(w -> w.message().contains("unusually high")));
    }
}