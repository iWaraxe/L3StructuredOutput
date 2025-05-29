package com.coherentsolutions.l3structuredoutput.s12;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import com.coherentsolutions.l3structuredoutput.s12.models.Invoice;
import com.coherentsolutions.l3structuredoutput.s12.models.Money;
import com.coherentsolutions.l3structuredoutput.s12.models.ScheduledEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.convert.ConversionService;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomConversionServiceTest extends BaseStructuredOutputTest {
    
    @Mock
    private OpenAiChatModel chatModel;
    
    @Mock
    private ConversionService conversionService;
    
    private CustomConversionService customConversionService;
    private CustomFormatProvider formatProvider;
    
    @BeforeEach
    void setUp() {
        formatProvider = new CustomFormatProvider();
        customConversionService = new CustomConversionService(chatModel, conversionService, formatProvider);
    }
    
    @Test
    void generateInvoice_ShouldReturnValidInvoice() {
        // Given
        String description = "Web development services for January 2024";
        String mockResponse = """
                {
                    "invoiceNumber": "INV-202401-0001",
                    "issueDate": {
                        "date": "2024-01-15",
                        "format": "yyyy-MM-dd",
                        "timezone": "UTC"
                    },
                    "dueDate": {
                        "date": "2024-02-14",
                        "format": "yyyy-MM-dd",
                        "timezone": "UTC"
                    },
                    "customer": {
                        "name": "Tech Corp",
                        "email": "billing@techcorp.com",
                        "address": "123 Tech Street, San Francisco, CA 94105",
                        "taxId": "12-3456789"
                    },
                    "items": [
                        {
                            "description": "Web Development Services",
                            "quantity": 40,
                            "unitPrice": {
                                "amount": "150.00",
                                "currency": "USD"
                            },
                            "total": {
                                "amount": "6000.00",
                                "currency": "USD"
                            }
                        }
                    ],
                    "subtotal": {
                        "amount": "6000.00",
                        "currency": "USD"
                    },
                    "tax": {
                        "amount": "600.00",
                        "currency": "USD"
                    },
                    "total": {
                        "amount": "6600.00",
                        "currency": "USD"
                    }
                }
                """;
        
        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        
        // When
        Invoice result = customConversionService.generateInvoice(description);
        
        // Then
        assertNotNull(result);
        assertEquals("INV-202401-0001", result.invoiceNumber());
        assertNotNull(result.customer());
        assertEquals("Tech Corp", result.customer().name());
        assertEquals(1, result.items().size());
        assertEquals("6600.00", result.total().amount().toPlainString());
        assertEquals("USD", result.total().currency().getCurrencyCode());
    }
    
    @Test
    void generateEvent_ShouldReturnValidEvent() {
        // Given
        String description = "Team meeting next Friday 2PM";
        String mockResponse = """
                {
                    "eventId": "EVT-0001",
                    "title": "Team Meeting",
                    "description": "Weekly team sync meeting",
                    "startTime": "2024-01-19T14:00:00Z",
                    "duration": "PT1H",
                    "location": {
                        "name": "Conference Room A",
                        "address": "123 Business Ave, New York, NY 10001",
                        "coordinates": {
                            "latitude": 40.7128,
                            "longitude": -74.0060
                        }
                    },
                    "attendees": ["john@example.com", "jane@example.com"],
                    "metadata": {
                        "department": "Engineering",
                        "recurring": "weekly"
                    },
                    "status": "SCHEDULED",
                    "cost": {
                        "amount": "0.00",
                        "currency": "USD"
                    }
                }
                """;
        
        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        
        // When
        ScheduledEvent result = customConversionService.generateEvent(description);
        
        // Then
        assertNotNull(result);
        assertEquals("EVT-0001", result.eventId());
        assertEquals("Team Meeting", result.title());
        assertEquals(ScheduledEvent.EventStatus.SCHEDULED, result.status());
        assertNotNull(result.location());
        assertEquals(40.7128, result.location().coordinates().latitude());
        assertEquals(2, result.attendees().size());
    }
    
    @Test
    void convertValue_WithMoney_ShouldConvert() {
        // Given
        String value = "100.50 USD";
        Money expectedMoney = Money.of(100.50, "USD");
        
        when(conversionService.canConvert(String.class, Money.class)).thenReturn(true);
        when(conversionService.convert(value, Money.class)).thenReturn(expectedMoney);
        
        // When
        Object result = customConversionService.convertValue(value, Money.class);
        
        // Then
        assertNotNull(result);
        assertInstanceOf(Money.class, result);
        Money money = (Money) result;
        assertEquals("100.50", money.amount().toPlainString());
        assertEquals("USD", money.currency().getCurrencyCode());
    }
    
    @Test
    void convertValue_WithDuration_ShouldConvert() {
        // Given
        String value = "2 hours";
        Duration expectedDuration = Duration.ofHours(2);
        
        when(conversionService.canConvert(String.class, Duration.class)).thenReturn(true);
        when(conversionService.convert(value, Duration.class)).thenReturn(expectedDuration);
        
        // When
        Object result = customConversionService.convertValue(value, Duration.class);
        
        // Then
        assertNotNull(result);
        assertInstanceOf(Duration.class, result);
        Duration duration = (Duration) result;
        assertEquals(120, duration.toMinutes());
    }
    
    @Test
    void convertValue_WithUnsupportedType_ShouldThrowException() {
        // Given
        String value = "test";
        
        when(conversionService.canConvert(String.class, String.class)).thenReturn(false);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> customConversionService.convertValue(value, String.class));
    }
}