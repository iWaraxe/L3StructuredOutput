# S8: OpenAI JSON Modes - Guaranteed Valid Output

## Why JSON Modes Matter

Traditional LLM outputs can be unpredictable:

```java
// Without JSON mode - AI might return:
"Here's the weather data: {\"temp\": 72, \"condition\": \"sunny\"}"
// or
"The temperature is 72°F with sunny conditions"
// or
"{\"temperature\": 72, \"weather\": \"sunny\" (oops, forgot closing brace"
```

**The fundamental problem:** How do we guarantee valid, parseable JSON every time?

## Understanding JSON Modes

### JSON_OBJECT Mode

```java
@GetMapping("/weather/json-object/{city}")
public String getWeatherJsonObject(@PathVariable String city) {
    var options = OpenAiChatOptions.builder()
        .withResponseFormat(ResponseFormat.JSON_OBJECT)
        .build();
    
    return chatModel.call(new Prompt(
        "Provide weather for " + city + " as JSON with temperature and conditions",
        options
    )).getResult().getOutput().getContent();
}
```

**What JSON_OBJECT guarantees:**
- ✅ Valid JSON syntax
- ✅ Proper bracket matching
- ✅ Escaped special characters
- ❌ No schema validation
- ❌ No structure guarantee

### JSON_SCHEMA Mode

```java
@GetMapping("/weather/json-schema/{city}")
public WeatherResponse getWeatherJsonSchema(@PathVariable String city) {
    var converter = new BeanOutputConverter<>(WeatherResponse.class);
    
    var options = OpenAiChatOptions.builder()
        .withResponseFormat(
            ResponseFormat.builder()
                .type(ResponseFormat.Type.JSON_SCHEMA)
                .schema(converter.getJsonSchema())
                .build()
        )
        .build();
    
    String response = chatModel.call(new Prompt(
        "Provide current weather for " + city,
        options
    )).getResult().getOutput().getContent();
    
    return converter.convert(response);
}
```

**What JSON_SCHEMA adds:**
- ✅ Everything from JSON_OBJECT
- ✅ Schema validation
- ✅ Required fields enforcement
- ✅ Type checking
- ✅ Structure guarantee

## Why Two Different Modes?

### JSON_OBJECT - Flexible Discovery

```java
public Map<String, Object> exploreDataStructure(String query) {
    var options = OpenAiChatOptions.builder()
        .withResponseFormat(ResponseFormat.JSON_OBJECT)
        .build();
    
    String response = chatModel.call(new Prompt(
        "Extract relevant information about: " + query,
        options
    )).getResult().getOutput().getContent();
    
    // Parse to Map - structure unknown but JSON is valid
    return objectMapper.readValue(response, Map.class);
}
```

**Use JSON_OBJECT when:**
- Exploring data patterns
- Structure varies by input
- Prototyping responses
- Maximum flexibility needed

### JSON_SCHEMA - Production Reliability

```java
public FinancialReport generateReport(String company, int year) {
    var converter = new BeanOutputConverter<>(FinancialReport.class);
    
    var options = OpenAiChatOptions.builder()
        .withResponseFormat(
            ResponseFormat.builder()
                .type(ResponseFormat.Type.JSON_SCHEMA)
                .schema(converter.getJsonSchema())
                .name("financial_report")
                .strict(true)
                .build()
        )
        .build();
    
    // AI MUST return data matching FinancialReport structure
    String response = chatModel.call(new Prompt(
        String.format("Generate financial report for %s for year %d", company, year),
        options
    )).getResult().getOutput().getContent();
    
    return converter.convert(response);
}
```

**Use JSON_SCHEMA when:**
- Production systems
- API contracts must be honored
- Downstream systems expect specific structure
- Data validation is critical

## Complex Nested Models

### The Challenge

```java
public record Order(
    String orderId,
    Customer customer,
    List<OrderItem> items,
    ShippingInfo shipping,
    PaymentDetails payment,
    Map<String, Object> metadata
) {
    public record Customer(
        String name,
        String email,
        Address address
    ) {}
    
    public record OrderItem(
        String productId,
        String name,
        int quantity,
        BigDecimal price
    ) {}
    
    // ... more nested records
}
```

**Why this is hard for AI:**
- Multiple nesting levels
- Various data types
- Collections within objects
- Optional vs required fields

### JSON_SCHEMA to the Rescue

```java
@Service
public class OrderService {
    public Order generateMockOrder(String scenario) {
        var converter = new BeanOutputConverter<>(Order.class);
        
        // JSON_SCHEMA ensures ALL nested structures are valid
        var options = OpenAiChatOptions.builder()
            .withResponseFormat(
                ResponseFormat.builder()
                    .type(ResponseFormat.Type.JSON_SCHEMA)
                    .schema(converter.getJsonSchema())
                    .name("order_schema")
                    .build()
            )
            .build();
        
        String response = chatModel.call(new Prompt(
            "Generate a realistic order for scenario: " + scenario,
            options
        )).getResult().getOutput().getContent();
        
        return converter.convert(response);
    }
}
```

## Schema Generation Deep Dive

### How Spring AI Generates Schemas

```java
public class SchemaInspector {
    public void inspectSchema() {
        var converter = new BeanOutputConverter<>(WeatherResponse.class);
        String schema = converter.getJsonSchema();
        
        System.out.println(schema);
        // Output:
        // {
        //   "type": "object",
        //   "properties": {
        //     "temperature": {
        //       "type": "number",
        //       "description": "Temperature in Fahrenheit"
        //     },
        //     "conditions": {
        //       "type": "string",
        //       "description": "Weather conditions"
        //     }
        //   },
        //   "required": ["temperature", "conditions"]
        // }
    }
}
```

### Customizing Schema Generation

```java
public record ProductListing(
    @JsonPropertyDescription("Unique product identifier")
    @JsonProperty(required = true)
    String id,
    
    @JsonPropertyDescription("Product name for display")
    @Size(min = 1, max = 200)
    String name,
    
    @JsonPropertyDescription("Price in USD")
    @DecimalMin("0.01")
    @DecimalMax("999999.99")
    BigDecimal price,
    
    @JsonPropertyDescription("Available inventory")
    @Min(0)
    Integer stock
) {}
```

**Schema includes:**
- Descriptions for AI guidance
- Validation constraints
- Required field marking
- Type information

## Error Handling with JSON Modes

### JSON_OBJECT Error Handling

```java
public Map<String, Object> safeJsonObjectParse(String query) {
    try {
        var options = OpenAiChatOptions.builder()
            .withResponseFormat(ResponseFormat.JSON_OBJECT)
            .build();
        
        String response = chatModel.call(new Prompt(query, options))
            .getResult().getOutput().getContent();
        
        return objectMapper.readValue(response, Map.class);
        
    } catch (JsonProcessingException e) {
        // Should rarely happen with JSON_OBJECT
        logger.error("JSON parsing failed", e);
        return Map.of("error", "Invalid JSON from AI");
    }
}
```

### JSON_SCHEMA Error Handling

```java
public Optional<Order> safeSchemaValidation(String query) {
    var converter = new BeanOutputConverter<>(Order.class);
    
    try {
        var options = OpenAiChatOptions.builder()
            .withResponseFormat(
                ResponseFormat.builder()
                    .type(ResponseFormat.Type.JSON_SCHEMA)
                    .schema(converter.getJsonSchema())
                    .build()
            )
            .build();
        
        String response = chatModel.call(new Prompt(query, options))
            .getResult().getOutput().getContent();
        
        // Additional validation layer
        Order order = converter.convert(response);
        if (validateOrder(order)) {
            return Optional.of(order);
        }
        
    } catch (Exception e) {
        logger.error("Schema validation failed", e);
    }
    
    return Optional.empty();
}
```

## Performance Implications

### Mode Performance Comparison

```java
@Component
public class JsonModePerformance {
    
    // No JSON mode - Fastest but risky
    @Timed("json.mode.none")
    public String noJsonMode(String query) {
        return chatModel.call(new Prompt(query))
            .getResult().getOutput().getContent();
    }
    
    // JSON_OBJECT - Small overhead
    @Timed("json.mode.object")
    public String jsonObjectMode(String query) {
        var options = OpenAiChatOptions.builder()
            .withResponseFormat(ResponseFormat.JSON_OBJECT)
            .build();
        
        return chatModel.call(new Prompt(query, options))
            .getResult().getOutput().getContent();
    }
    
    // JSON_SCHEMA - Highest reliability, slight overhead
    @Timed("json.mode.schema")
    public String jsonSchemaMode(String query, String schema) {
        var options = OpenAiChatOptions.builder()
            .withResponseFormat(
                ResponseFormat.builder()
                    .type(ResponseFormat.Type.JSON_SCHEMA)
                    .schema(schema)
                    .build()
            )
            .build();
        
        return chatModel.call(new Prompt(query, options))
            .getResult().getOutput().getContent();
    }
}

// Typical results:
// No JSON mode: 230ms average
// JSON_OBJECT: 245ms average (+6%)
// JSON_SCHEMA: 260ms average (+13%)
```

## Best Practices

### 1. Choose the Right Mode

```java
public class ModeSelector {
    public ResponseFormat selectMode(UseCase useCase) {
        return switch (useCase) {
            case EXPLORATION -> ResponseFormat.JSON_OBJECT;
            case PRODUCTION_API -> ResponseFormat.builder()
                .type(ResponseFormat.Type.JSON_SCHEMA)
                .schema(getSchema())
                .strict(true)
                .build();
            case SIMPLE_QUERY -> null; // No JSON mode
        };
    }
}
```

### 2. Schema Evolution

```java
// Version 1
public record UserV1(String name, String email) {}

// Version 2 - Adding fields
public record UserV2(
    String name, 
    String email,
    @JsonProperty(required = false) String phone // Optional for compatibility
) {}
```

### 3. Combine with Validation

```java
@Service
public class ValidatedJsonService {
    private final Validator validator;
    
    public <T> Optional<T> getValidatedResponse(String query, Class<T> type) {
        var converter = new BeanOutputConverter<>(type);
        
        // Get schema-validated JSON
        T result = getWithJsonSchema(query, converter);
        
        // Additional business validation
        Set<ConstraintViolation<T>> violations = validator.validate(result);
        
        if (violations.isEmpty()) {
            return Optional.of(result);
        }
        
        logger.warn("Validation failures: {}", violations);
        return Optional.empty();
    }
}
```

## Common Pitfalls

### Pitfall 1: Assuming JSON_OBJECT Provides Structure

```java
// Wrong assumption
var options = OpenAiChatOptions.builder()
    .withResponseFormat(ResponseFormat.JSON_OBJECT)
    .build();

// Response might be: {"result": "I don't know"}
// Not the structure you expected!
```

### Pitfall 2: Over-Constraining Schemas

```java
// Too rigid
public record StrictProduct(
    @Pattern(regexp = "^PROD-\\d{6}$") String id,  // AI might struggle
    @Size(min = 50, max = 100) String description  // Too specific
) {}

// Better
public record FlexibleProduct(
    String id,  // Let AI generate, validate after
    @Size(max = 500) String description  // Reasonable limit
) {}
```

## Real-World Patterns

### Progressive JSON Validation

```java
@Service
public class ProgressiveValidationService {
    
    // Level 1: Any valid JSON
    public JsonNode getFlexibleJson(String query) {
        var options = OpenAiChatOptions.builder()
            .withResponseFormat(ResponseFormat.JSON_OBJECT)
            .build();
        
        String response = chatModel.call(new Prompt(query, options))
            .getResult().getOutput().getContent();
        
        return objectMapper.readTree(response);
    }
    
    // Level 2: Typed but flexible
    public Map<String, Object> getTypedJson(String query) {
        // Same as above but parse to Map
    }
    
    // Level 3: Full schema validation
    public <T> T getSchemaValidated(String query, Class<T> type) {
        var converter = new BeanOutputConverter<>(type);
        // Use JSON_SCHEMA mode
    }
}
```

## Key Takeaways

1. **JSON_OBJECT guarantees syntax**: Valid JSON, but not structure
2. **JSON_SCHEMA guarantees structure**: Full compliance with your models
3. **Performance cost is minimal**: 5-15% overhead for reliability
4. **Use JSON_SCHEMA in production**: Reliability > marginal performance
5. **Combine with validation**: Defense in depth approach

## Next Steps

With JSON modes ensuring valid output, we can build even more sophisticated applications. Continue exploring advanced topics in the remaining sections of the course.