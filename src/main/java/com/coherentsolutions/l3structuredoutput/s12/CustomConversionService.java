package com.coherentsolutions.l3structuredoutput.s12;

import com.coherentsolutions.l3structuredoutput.s12.models.Invoice;
import com.coherentsolutions.l3structuredoutput.s12.models.Money;
import com.coherentsolutions.l3structuredoutput.s12.models.ScheduledEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Service demonstrating custom conversion with AI-generated content.
 */
@Service
public class CustomConversionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomConversionService.class);
    
    private final OpenAiChatModel chatModel;
    private final ConversionService conversionService;
    private final CustomFormatProvider formatProvider;
    private final ObjectMapper objectMapper;
    
    public CustomConversionService(OpenAiChatModel chatModel, 
                                 ConversionService conversionService,
                                 CustomFormatProvider formatProvider) {
        this.chatModel = chatModel;
        this.conversionService = conversionService;
        this.formatProvider = formatProvider;
        this.objectMapper = createCustomObjectMapper();
    }
    
    /**
     * Generates an invoice using AI with custom type conversion.
     */
    public Invoice generateInvoice(String description) {
        BeanOutputConverter<Invoice> converter = new BeanOutputConverter<>(Invoice.class);
        String schema = converter.getFormat();
        String enhancedFormat = formatProvider.getFormat(schema);
        
        String promptText = """
                Generate a complete invoice based on this description:
                {description}
                
                Requirements:
                - Invoice number should follow pattern: INV-YYYYMM-XXXX
                - Include customer details with valid email and tax ID
                - Calculate subtotal, tax (10%), and total correctly
                - Use appropriate currency based on context
                - Dates should be realistic (issue date today, due date 30 days later)
                
                {format}
                """;
        
        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Prompt prompt = new Prompt(promptTemplate.render(Map.of(
                "description", description,
                "format", enhancedFormat
        )));
        
        ChatResponse response = chatModel.call(prompt);
        String content = response.getResult().getOutput().getText();
        
        try {
            // Use custom ObjectMapper for parsing
            return objectMapper.readValue(content, Invoice.class);
        } catch (Exception e) {
            logger.error("Failed to parse invoice", e);
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }
    
    /**
     * Generates a scheduled event using AI with custom type conversion.
     */
    public ScheduledEvent generateEvent(String description) {
        BeanOutputConverter<ScheduledEvent> converter = new BeanOutputConverter<>(ScheduledEvent.class);
        String schema = converter.getFormat();
        String enhancedFormat = formatProvider.getFormat(schema);
        
        String promptText = """
                Create a scheduled event based on this description:
                {description}
                
                Requirements:
                - Generate unique event ID (EVT-XXXX format)
                - Set appropriate start time and duration
                - Include location with real coordinates
                - Add relevant metadata
                - Set initial status as SCHEDULED
                - Include cost if applicable (0 if free)
                
                {format}
                """;
        
        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Prompt prompt = new Prompt(promptTemplate.render(Map.of(
                "description", description,
                "format", enhancedFormat
        )));
        
        ChatResponse response = chatModel.call(prompt);
        String content = response.getResult().getOutput().getText();
        
        try {
            // Parse with custom handling
            JsonNode rootNode = objectMapper.readTree(content);
            return parseScheduledEvent(rootNode);
        } catch (Exception e) {
            logger.error("Failed to parse event", e);
            throw new RuntimeException("Failed to generate event", e);
        }
    }
    
    /**
     * Demonstrates manual conversion using ConversionService.
     */
    public Object convertValue(String value, Class<?> targetType) {
        if (conversionService.canConvert(String.class, targetType)) {
            return conversionService.convert(value, targetType);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to " + targetType);
    }
    
    /**
     * Creates a custom ObjectMapper with type handlers.
     */
    private ObjectMapper createCustomObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        SimpleModule module = new SimpleModule();
        
        // Add custom deserializers if needed
        module.addDeserializer(Money.class, new MoneyDeserializer());
        
        mapper.registerModule(module);
        return mapper;
    }
    
    /**
     * Custom parsing for ScheduledEvent with type conversion.
     */
    private ScheduledEvent parseScheduledEvent(JsonNode node) throws Exception {
        // This method would handle custom parsing logic
        // For simplicity, using direct mapping
        return objectMapper.treeToValue(node, ScheduledEvent.class);
    }
    
    /**
     * Custom deserializer for Money type.
     */
    private static class MoneyDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<Money> {
        @Override
        public Money deserialize(com.fasterxml.jackson.core.JsonParser p, 
                               com.fasterxml.jackson.databind.DeserializationContext ctxt) 
                throws java.io.IOException {
            JsonNode node = p.getCodec().readTree(p);
            
            if (node.isTextual()) {
                // Handle string format "100.50 USD"
                String[] parts = node.asText().split("\\s+");
                if (parts.length == 2) {
                    return Money.of(parts[0], parts[1]);
                }
            } else if (node.isObject()) {
                // Handle object format {amount: "100.50", currency: "USD"}
                String amount = node.get("amount").asText();
                String currency = node.get("currency").asText();
                return Money.of(amount, currency);
            }
            
            throw new IllegalArgumentException("Cannot parse Money from: " + node);
        }
    }
}