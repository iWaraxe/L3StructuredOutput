package com.coherentsolutions.l3structuredoutput.s11;

import com.coherentsolutions.l3structuredoutput.s11.models.OrderRequest;
import com.coherentsolutions.l3structuredoutput.s11.models.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ValidationController.class)
class ValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ValidationService validationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void validateOrder_WithValidResult_ShouldReturnOk() throws Exception {
        // Given
        ValidationController.OrderValidationRequest request = 
                new ValidationController.OrderValidationRequest("Order 2 laptops");
        
        OrderRequest mockOrder = new OrderRequest(
                "ORD-123456",
                "test@example.com",
                LocalDate.now(),
                List.of(new OrderRequest.OrderItem("PROD-001", "Laptop", 2, 1500.00)),
                3000.00,
                new OrderRequest.ShippingAddress("123 Main St", "New York", "NY", "10001", "US"),
                OrderRequest.PaymentMethod.CREDIT_CARD
        );
        
        ValidationResult mockResult = new ValidationResult(
                true,
                LocalDateTime.now(),
                List.of(),
                List.of(),
                List.of(),
                mockOrder,
                Map.of("originalDescription", "Order 2 laptops")
        );

        when(validationService.generateAndValidateOrder(anyString())).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/validation/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.finalOutput.orderId").value("ORD-123456"));
    }

    @Test
    void validateOrder_WithInvalidResult_ShouldReturnBadRequest() throws Exception {
        // Given
        ValidationController.OrderValidationRequest request = 
                new ValidationController.OrderValidationRequest("Invalid order");
        
        ValidationResult mockResult = new ValidationResult(
                false,
                LocalDateTime.now(),
                List.of(new ValidationResult.ValidationError(
                        "orderId",
                        "Invalid format",
                        "ORDER123",
                        "Pattern"
                )),
                List.of(),
                List.of(new ValidationResult.RecoveryAttempt(
                        "fix_order_id",
                        false,
                        "Attempted to fix order ID",
                        null
                )),
                null,
                Map.of("stage", "validation_failed")
        );

        when(validationService.generateAndValidateOrder(anyString())).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/validation/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("orderId"))
                .andExpect(jsonPath("$.recoveryAttempts").isNotEmpty());
    }

    @Test
    void validateJson_WithValidJson_ShouldReturnOk() throws Exception {
        // Given
        String validJson = "{\"name\": \"test\", \"value\": 123}";
        
        ValidationResult mockResult = new ValidationResult(
                true,
                LocalDateTime.now(),
                List.of(),
                List.of(),
                List.of(),
                Map.of("name", "test", "value", 123),
                Map.of("type", "raw_json_validation")
        );

        when(validationService.validateRawJson(anyString())).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/validation/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void validateJson_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Given
        String invalidJson = "{'invalid': json'}";
        
        ValidationResult mockResult = new ValidationResult(
                false,
                LocalDateTime.now(),
                List.of(new ValidationResult.ValidationError(
                        "json",
                        "Invalid JSON syntax",
                        invalidJson,
                        "valid_json"
                )),
                List.of(),
                List.of(new ValidationResult.RecoveryAttempt(
                        "fix_json_syntax",
                        true,
                        "Fixed single quotes",
                        "{\"invalid\": \"json\"}"
                )),
                "{\"invalid\": \"json\"}",
                Map.of("recovered", true)
        );

        when(validationService.validateRawJson(anyString())).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/validation/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0].message").value("Invalid JSON syntax"))
                .andExpect(jsonPath("$.recoveryAttempts[0].success").value(true));
    }

    @Test
    void getValidationStrategies_ShouldReturnStrategies() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/validation/strategies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validation_types").exists())
                .andExpect(jsonPath("$.recovery_strategies").exists())
                .andExpect(jsonPath("$.best_practices").exists())
                .andExpect(jsonPath("$.recovery_strategies.parsing_recovery.extract_json").exists());
    }

    @Test
    void getValidationExamples_ShouldReturnExamples() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/validation/examples"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format_errors").exists())
                .andExpect(jsonPath("$.json_errors").exists())
                .andExpect(jsonPath("$.recovery_examples").exists())
                .andExpect(jsonPath("$.format_errors.invalid_order_id.fixed").value("ORD-000123"));
    }

    @Test
    void getValidationMetrics_ShouldReturnMetrics() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/validation/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validation_stats").exists())
                .andExpect(jsonPath("$.recovery_stats").exists())
                .andExpect(jsonPath("$.common_errors").exists())
                .andExpect(jsonPath("$.performance").exists())
                .andExpect(jsonPath("$.validation_stats.success_rate").value("85%"));
    }

    @Test
    void validateOrder_WithWarnings_ShouldStillReturnOk() throws Exception {
        // Given
        ValidationController.OrderValidationRequest request = 
                new ValidationController.OrderValidationRequest("Large order");
        
        OrderRequest mockOrder = new OrderRequest(
                "ORD-999999",
                "bigorder@example.com",
                LocalDate.now(),
                List.of(new OrderRequest.OrderItem("PROD-999", "Expensive Item", 100, 1000.00)),
                100000.00,
                new OrderRequest.ShippingAddress("999 Rich St", "Beverly Hills", "CA", "90210", "US"),
                OrderRequest.PaymentMethod.CRYPTOCURRENCY
        );
        
        ValidationResult mockResult = new ValidationResult(
                true,
                LocalDateTime.now(),
                List.of(),
                List.of(
                        new ValidationResult.ValidationWarning(
                                "totalAmount",
                                "Order amount is unusually high",
                                "Consider verifying the amount"
                        ),
                        new ValidationResult.ValidationWarning(
                                "paymentMethod",
                                "Cryptocurrency payment selected",
                                "Ensure compliance"
                        )
                ),
                List.of(),
                mockOrder,
                Map.of()
        );

        when(validationService.generateAndValidateOrder(anyString())).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/validation/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.warnings").isNotEmpty())
                .andExpect(jsonPath("$.warnings[0].field").value("totalAmount"));
    }
}