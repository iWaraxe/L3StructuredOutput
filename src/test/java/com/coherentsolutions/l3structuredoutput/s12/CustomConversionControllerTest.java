package com.coherentsolutions.l3structuredoutput.s12;

import com.coherentsolutions.l3structuredoutput.s12.models.CustomDate;
import com.coherentsolutions.l3structuredoutput.s12.models.Invoice;
import com.coherentsolutions.l3structuredoutput.s12.models.Money;
import com.coherentsolutions.l3structuredoutput.s12.models.ScheduledEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomConversionController.class)
class CustomConversionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CustomConversionService customConversionService;
    
    @MockBean
    private CustomFormatProvider formatProvider;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void generateInvoice_ShouldReturnInvoice() throws Exception {
        // Given
        CustomConversionController.InvoiceRequest request = 
                new CustomConversionController.InvoiceRequest("Web development services");
        
        Invoice mockInvoice = new Invoice(
                "INV-202401-0001",
                CustomDate.of(LocalDate.now()),
                CustomDate.of(LocalDate.now().plusDays(30)),
                new Invoice.Customer("Tech Corp", "billing@techcorp.com", "123 Tech St", "12-3456789"),
                List.of(new Invoice.InvoiceItem("Development", 40, Money.of(150, "USD"), Money.of(6000, "USD"))),
                Money.of(6000, "USD"),
                Money.of(600, "USD"),
                Money.of(6600, "USD")
        );
        
        when(customConversionService.generateInvoice(any())).thenReturn(mockInvoice);
        
        // When & Then
        mockMvc.perform(post("/api/custom-conversion/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceNumber").value("INV-202401-0001"))
                .andExpect(jsonPath("$.customer.name").value("Tech Corp"))
                .andExpect(jsonPath("$.total.amount").value(new BigDecimal("6600.00")));
    }
    
    @Test
    void generateEvent_ShouldReturnEvent() throws Exception {
        // Given
        CustomConversionController.EventRequest request = 
                new CustomConversionController.EventRequest("Team meeting tomorrow at 2PM");
        
        ScheduledEvent mockEvent = new ScheduledEvent(
                "EVT-0001",
                "Team Meeting",
                "Weekly sync",
                ZonedDateTime.now().plusDays(1).withHour(14),
                Duration.ofHours(1),
                new ScheduledEvent.Location("Conference Room", "123 Office St", 
                        new ScheduledEvent.Coordinates(40.7128, -74.0060)),
                List.of("john@example.com"),
                Map.of("type", "meeting"),
                ScheduledEvent.EventStatus.SCHEDULED,
                Money.of(0, "USD")
        );
        
        when(customConversionService.generateEvent(any())).thenReturn(mockEvent);
        
        // When & Then
        mockMvc.perform(post("/api/custom-conversion/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("EVT-0001"))
                .andExpect(jsonPath("$.title").value("Team Meeting"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }
    
    @Test
    void convertValue_WithValidConversion_ShouldReturnSuccess() throws Exception {
        // Given
        CustomConversionController.ConversionRequest request = 
                new CustomConversionController.ConversionRequest("100.50 USD", "Money");
        
        Money mockMoney = Money.of(100.50, "USD");
        when(customConversionService.convertValue("100.50 USD", Money.class)).thenReturn(mockMoney);
        
        // When & Then
        mockMvc.perform(post("/api/custom-conversion/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.originalValue").value("100.50 USD"))
                .andExpect(jsonPath("$.targetType").value("Money"));
    }
    
    @Test
    void convertValue_WithInvalidConversion_ShouldReturnError() throws Exception {
        // Given
        CustomConversionController.ConversionRequest request = 
                new CustomConversionController.ConversionRequest("invalid", "Money");
        
        when(customConversionService.convertValue(any(), eq(Money.class)))
                .thenThrow(new IllegalArgumentException("Cannot convert"));
        
        // When & Then
        mockMvc.perform(post("/api/custom-conversion/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot convert"));
    }
    
    @Test
    void getExamples_ShouldReturnExamples() throws Exception {
        // Given
        when(formatProvider.generateExample(Money.class)).thenReturn("{\"amount\": \"100.50\", \"currency\": \"USD\"}");
        when(formatProvider.generateExample(CustomDate.class)).thenReturn("{\"date\": \"2024-01-15\"}");
        when(formatProvider.generateExample(ScheduledEvent.class)).thenReturn("{\"eventId\": \"EVT-001\"}");
        
        // When & Then
        mockMvc.perform(get("/api/custom-conversion/examples"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.money_formats").exists())
                .andExpect(jsonPath("$.date_formats").exists())
                .andExpect(jsonPath("$.duration_formats").exists())
                .andExpect(jsonPath("$.event_example").exists());
    }
    
    @Test
    void demonstrateCustomTypes_ShouldReturnDemoData() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/custom-conversion/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custom_types.money").exists())
                .andExpect(jsonPath("$.custom_types.custom_date").exists())
                .andExpect(jsonPath("$.custom_types.duration").exists())
                .andExpect(jsonPath("$.complex_objects.invoice_item").exists())
                .andExpect(jsonPath("$.complex_objects.location").exists());
    }
}