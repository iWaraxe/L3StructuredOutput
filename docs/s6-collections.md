# S6: Map and List Converters in Practice - When Simple is Better

## Why Not Always Use BeanOutputConverter?

After mastering complex nested structures, you might think BeanOutputConverter is always the answer. But consider these scenarios:

```java
// Scenario 1: Dynamic travel destinations with unknown attributes
Map<String, Object> parisInfo = getDestinationInfo("Paris");
// Returns: {"currency": "EUR", "language": "French", "famousFor": ["Eiffel Tower", "Louvre"], 
//           "bestTimeToVisit": "April-June", "visaRequired": true}

Map<String, Object> tokyoInfo = getDestinationInfo("Tokyo");
// Returns: {"currency": "JPY", "language": "Japanese", "famousFor": ["Mount Fuji", "Sushi"],
//           "timeZone": "JST", "publicTransport": ["JR Pass", "Metro"], "earthquakeRisk": "moderate"}
```

**Different cities have different relevant attributes!** A rigid Bean structure would force unnecessary fields.

## MapOutputConverter - Embracing Flexibility

### The Power of Dynamic Structures

```java
@Service
public class DestinationMapService {
    public Map<String, Object> getDestinationInfo(String destination) {
        MapOutputConverter converter = new MapOutputConverter();
        
        String prompt = String.format("""
            Provide comprehensive information about %s as a travel destination.
            Include relevant details such as:
            - Currency and typical costs
            - Language(s) spoken
            - Famous attractions
            - Best time to visit
            - Cultural considerations
            - Safety information
            - Any unique local information
            
            Only include information that is specifically relevant to this destination.
            %s
            """, destination, converter.getFormat());
        
        return chatClient.prompt()
            .user(prompt)
            .call()
            .entity(converter);
    }
}
```

**Why MapOutputConverter shines here:**
1. **No Predefined Schema**: Each destination has unique attributes
2. **Evolutionary Design**: Discover patterns before creating beans
3. **API Flexibility**: Third-party integrations with varying responses
4. **Rapid Prototyping**: Quick experiments without model changes

### Real-World Use Cases for Maps

#### Configuration Management
```java
public Map<String, Object> getApplicationConfig(String environment) {
    // Dev might have: {"debug": true, "mockServices": ["payment", "email"]}
    // Prod might have: {"monitoring": true, "alertThreshold": 0.95, "scaling": "auto"}
    return fetchDynamicConfig(environment, new MapOutputConverter());
}
```

#### Feature Flags
```java
public Map<String, Object> getUserFeatures(String userId) {
    // Power user: {"advancedEditor": true, "apiAccess": true, "customThemes": true}
    // New user: {"tutorial": true, "limitedFeatures": true}
    return fetchUserFeatures(userId, new MapOutputConverter());
}
```

#### Dynamic Forms
```java
public Map<String, Object> getFormFields(String formType) {
    // Contact form: {"name": "required", "email": "required", "message": "required"}
    // Survey form: {"satisfaction": "1-10", "comments": "optional", "recommend": "yes/no"}
    return generateFormSchema(formType, new MapOutputConverter());
}
```

## ListOutputConverter - The Minimalist's Choice

### Why Simple Lists Matter

```java
@Service
public class ActivityListService {
    public List<String> getActivities(TravelRequest request) {
        ListOutputConverter converter = new ListOutputConverter(conversionService);
        
        String prompt = buildActivityPrompt(request) + "\n\n" + converter.getFormat();
        
        return chatClient.prompt()
            .user(prompt)
            .call()
            .entity(converter);
    }
}
```

**The elegance of simplicity:**
- **Minimal Tokens**: Comma-separated = fewer tokens = lower cost
- **Universal Format**: Every AI model understands lists
- **Fast Processing**: No JSON parsing overhead
- **Human Readable**: Easy to debug and verify

### Perfect List Converter Scenarios

#### Brainstorming
```java
public List<String> generateBusinessNames(String industry, String style) {
    // Returns: ["TechNova", "Digital Dynamics", "Cloud Catalyst", ...]
    return generateNames(industry, style, new ListOutputConverter(conversionService));
}
```

#### Categorization
```java
public List<String> categorizeExpenses(String description) {
    // "Coffee at Starbucks" -> ["Food & Dining", "Coffee Shops", "Quick Service"]
    return categorize(description, new ListOutputConverter(conversionService));
}
```

#### Keyword Extraction
```java
public List<String> extractKeywords(String article) {
    // Long article -> ["spring-boot", "microservices", "cloud-native", "kubernetes"]
    return extractKeywords(article, new ListOutputConverter(conversionService));
}
```

## Choosing Between Converters - A Practical Guide

### Decision Framework

```
Question 1: Do I know all possible fields?
├─ Yes → Question 2: Do I need type safety?
│   ├─ Yes → BeanOutputConverter
│   └─ No → MapOutputConverter (flexibility over types)
└─ No → Question 3: Is it just a list of strings?
    ├─ Yes → ListOutputConverter
    └─ No → MapOutputConverter
```

### Real Example: Travel Planning System

```java
@RestController
@RequestMapping("/api/travel")
public class TravelController {
    
    // Known structure - use Bean
    @GetMapping("/flights/{from}/{to}")
    public List<FlightInfo> getFlights(@PathVariable String from, @PathVariable String to) {
        var converter = new BeanOutputConverter<>(
            new ParameterizedTypeReference<List<FlightInfo>>() {}
        );
        // Returns structured flight data with fixed fields
    }
    
    // Dynamic structure - use Map
    @GetMapping("/destination/{city}/info")
    public Map<String, Object> getDestinationInfo(@PathVariable String city) {
        MapOutputConverter converter = new MapOutputConverter();
        // Returns varying attributes based on destination
    }
    
    // Simple strings - use List
    @GetMapping("/activities/{city}")
    public List<String> getActivities(@PathVariable String city) {
        ListOutputConverter converter = new ListOutputConverter(conversionService);
        // Returns simple activity names
    }
}
```

## Advanced Patterns with Maps and Lists

### Progressive Structure Discovery

```java
public class DataStructureEvolution {
    // Phase 1: Exploration with Maps
    public Map<String, Object> exploreProductData(String query) {
        return fetchData(query, new MapOutputConverter());
    }
    
    // Phase 2: Identify patterns
    public void analyzeDataPatterns(List<Map<String, Object>> samples) {
        Set<String> commonKeys = findCommonKeys(samples);
        Map<String, Class<?>> typePatterns = inferTypes(samples);
    }
    
    // Phase 3: Create structured model
    public record Product(
        String name,
        Double price,
        List<String> categories
        // Fields discovered from Map analysis
    ) {}
}
```

### Hybrid Approaches

```java
public class HybridDataService {
    // Structured core with flexible extensions
    public record ProductWithExtras(
        String id,
        String name,
        BigDecimal price,
        Map<String, Object> additionalProperties  // Flexible part
    ) {}
    
    public ProductWithExtras getProduct(String id) {
        // Combines structure with flexibility
        return fetchProduct(id, new BeanOutputConverter<>(ProductWithExtras.class));
    }
}
```

## Performance Comparison

### Token Usage Analysis

```java
// ListOutputConverter - Minimal tokens
// Prompt: "List 5 activities in Paris"
// Response: "Eiffel Tower,Louvre Museum,Seine River Cruise,Montmartre,Versailles"
// Tokens: ~20

// MapOutputConverter - Moderate tokens  
// Response: {"activity1": "Eiffel Tower", "duration": "2 hours", ...}
// Tokens: ~100

// BeanOutputConverter - Maximum tokens
// Response: [{"name": "Eiffel Tower", "type": "Monument", "duration": 120, ...}]
// Tokens: ~300
```

### Processing Speed

```java
@Component
public class PerformanceMonitor {
    public void compareConverters() {
        long start = System.currentTimeMillis();
        
        // ListOutputConverter: ~50ms parsing
        List<String> list = parseList("item1,item2,item3");
        
        // MapOutputConverter: ~200ms parsing
        Map<String, Object> map = parseMap("{\"key\": \"value\"}");
        
        // BeanOutputConverter: ~500ms parsing + validation
        ComplexBean bean = parseBean("{...complex json...}");
    }
}
```

## Error Handling Strategies

### Maps - Flexible Recovery

```java
public Map<String, Object> getInfoWithFallback(String query) {
    try {
        return getInfo(query);
    } catch (Exception e) {
        // Partial data is better than nothing
        return Map.of(
            "error", "Partial data available",
            "status", "degraded",
            "availableData", getFromCache(query)
        );
    }
}
```

### Lists - Simple Validation

```java
public List<String> getValidatedList(String query) {
    List<String> raw = getRawList(query);
    
    return raw.stream()
        .filter(item -> item != null && !item.trim().isEmpty())
        .map(String::trim)
        .distinct()
        .limit(100)  // Prevent memory issues
        .toList();
}
```

## Best Practices

### 1. Start Simple, Evolve Structure
```java
// Version 1: List
List<String> restaurants = getRestaurants("Italian");

// Version 2: Map
Map<String, Object> restaurants = getRestaurantInfo("Italian");

// Version 3: Bean (when structure stabilizes)
List<Restaurant> restaurants = getStructuredRestaurants("Italian");
```

### 2. Use Maps for External APIs
```java
// Unknown API response structure
public Map<String, Object> callExternalAPI(String endpoint) {
    // Map handles whatever comes back
    return parseResponse(apiCall(endpoint), new MapOutputConverter());
}
```

### 3. Lists for User-Facing Features
```java
// Auto-complete suggestions
public List<String> getSuggestions(String partial) {
    return generateSuggestions(partial, new ListOutputConverter());
}
```

## Common Pitfalls

### Pitfall 1: Over-structuring Dynamic Data
```java
// Bad - forcing structure on dynamic data
public record CityInfo(
    String currency,  // What if not applicable?
    String visa,      // What if varies by nationality?
    String language,  // What if multiple?
    // ... 50 more fields that might not apply
) {}

// Good - embrace the flexibility
public Map<String, Object> getCityInfo(String city) {
    // Returns only relevant fields
}
```

### Pitfall 2: Type Loss with Maps
```java
// Problem: Everything becomes Object
Map<String, Object> data = getData();
Integer count = (Integer) data.get("count");  // ClassCastException risk!

// Solution: Validate and convert safely
int count = convertSafely(data.get("count"), Integer.class, 0);
```

## Key Takeaways

1. **Maps for exploration**: When you don't know the structure
2. **Lists for simplicity**: When strings are enough
3. **Performance matters**: Simpler formats = better performance
4. **Evolution is natural**: List → Map → Bean as requirements clarify
5. **Flexibility has value**: Not everything needs rigid structure
6. **Choose the right tool**: Match converter to use case

## Next Steps

We've seen when simple converters outshine complex ones. Next, in [Section 7](s7-api-comparison.md), we'll compare ChatClient and ChatModel APIs to understand when to use each approach.