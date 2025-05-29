# S3: Structured Output Fundamentals Guide

## Why Go Beyond Single Objects?

In Section 2, we learned to convert AI responses into single objects. But real applications need more:

```java
// Single movie recommendation - limited use case
MovieRecommendation movie = getRecommendation("action");

// What users actually want - multiple options
List<MovieRecommendation> movies = getRecommendations("action", 5);
```

**The fundamental challenge:** How do we reliably get collections and complex structures from AI?

## The List Challenge

### Why Lists Are Tricky

```java
// Naive approach - doesn't work!
BeanOutputConverter<List<MovieRecommendation>> converter = 
    new BeanOutputConverter<>(List<MovieRecommendation>.class); // Compile error!

// Why? Java type erasure removes generic information at runtime
```

**The problems:**
1. **Type Erasure**: Java doesn't retain `List<Movie>` at runtime, only `List`
2. **JSON Schema Generation**: How to generate schema for generic types?
3. **Deserialization**: Jackson needs type information to deserialize correctly

### Enter ParameterizedTypeReference

```java
// The solution - preserve type information
BeanOutputConverter<List<MovieRecommendation>> converter = 
    new BeanOutputConverter<>(new ParameterizedTypeReference<List<MovieRecommendation>>() {});
```

**Why this works:**
- Anonymous inner class captures generic type information
- Spring's `ParameterizedTypeReference` preserves it at runtime
- Enables proper JSON schema generation and deserialization

## Architecture Deep Dive

### The Movie Recommendation System

```java
@Service
public class MovieRecommendationService {
    public List<MovieRecommendation> getRecommendations(MoviePreferenceRequest request) {
        // Why validate preferences?
        validatePreferences(request);
        
        // Why use ParameterizedTypeReference?
        var converter = new BeanOutputConverter<>(
            new ParameterizedTypeReference<List<MovieRecommendation>>() {}
        );
        
        // Why structure the prompt this way?
        String prompt = buildRecommendationPrompt(request) + converter.getFormat();
        
        return chatClient.prompt()
            .user(prompt)
            .call()
            .entity(converter);
    }
}
```

**Design decisions explained:**

1. **Validation First**: Ensure input quality before expensive AI calls
2. **Type-Safe Lists**: Guarantee return type at compile time
3. **Structured Prompts**: Consistent format for predictable results

### Why Validate Preferences?

```java
private void validatePreferences(MoviePreferenceRequest request) {
    if (request.genre() == null || request.genre().isBlank()) {
        throw new IllegalArgumentException("Genre is required");
    }
    if (request.limit() < 1 || request.limit() > 20) {
        throw new IllegalArgumentException("Limit must be between 1 and 20");
    }
}
```

**Benefits:**
- **Cost Savings**: Avoid AI calls for invalid requests
- **Better UX**: Immediate feedback for users
- **Predictable Results**: AI performs better with valid inputs
- **System Protection**: Prevent abuse or excessive requests

## Advanced Prompt Engineering

### Why Dynamic Prompts Matter

```java
private String buildRecommendationPrompt(MoviePreferenceRequest request) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Generate ").append(request.limit())
          .append(" movie recommendations based on these preferences:\n\n");
    
    prompt.append("Genre: ").append(request.genre()).append("\n");
    
    // Why conditional sections?
    if (request.mood() != null) {
        prompt.append("Mood: ").append(request.mood()).append("\n");
    }
    
    if (request.decade() != null) {
        prompt.append("Decade: ").append(request.decade()).append("\n");
    }
    
    // Why explicit instructions?
    prompt.append("\nProvide diverse recommendations with accurate ratings.");
    
    return prompt.toString();
}
```

**Dynamic benefits:**
- **Flexibility**: Adapt to optional parameters
- **Efficiency**: Only include relevant information
- **Clarity**: AI gets exactly what it needs
- **Performance**: Shorter prompts = faster responses

## The Data Model Strategy

### Rich vs. Simple Models

```java
// Simple model - basic information
public record SimpleMovie(String title, String genre) {}

// Rich model - comprehensive information
public record MovieRecommendation(
    @JsonPropertyDescription("Movie title") String title,
    @JsonPropertyDescription("Primary genre") String genre,
    @JsonPropertyDescription("Release year") int year,
    @JsonPropertyDescription("IMDB rating (1-10)") double rating,
    @JsonPropertyDescription("Brief plot summary") String summary,
    @JsonPropertyDescription("Main cast members") List<String> cast,
    @JsonPropertyDescription("Viewing platform availability") List<String> platforms,
    @JsonPropertyDescription("Reasons for recommendation") List<String> matchReasons
) {}
```

**When to use rich models:**
- **User-facing applications**: More information = better UX
- **Decision support**: Multiple factors for consideration
- **Integration needs**: Downstream systems need details

**When to use simple models:**
- **High-volume processing**: Minimize token usage
- **Intermediate steps**: Processing pipelines
- **Performance critical**: Faster processing

## Error Handling Philosophy

### Why Explicit Error Handling?

```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException e) {
    return ResponseEntity.badRequest()
        .body(new ErrorResponse("Validation Error", e.getMessage()));
}

@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGeneralError(Exception e) {
    logger.error("Unexpected error", e);
    return ResponseEntity.internalServerError()
        .body(new ErrorResponse("Internal Error", "Failed to generate recommendations"));
}
```

**Benefits:**
- **User Trust**: Clear error messages build confidence
- **Debugging**: Detailed logs help troubleshooting
- **Graceful Degradation**: System remains usable despite errors
- **Security**: Don't expose internal details to users

## Performance Considerations

### Why Limit Recommendations?

```java
if (request.limit() > 20) {
    throw new IllegalArgumentException("Limit must not exceed 20");
}
```

**Reasons:**
1. **Token Costs**: More items = higher API costs
2. **Response Time**: Linear increase with item count
3. **User Experience**: Too many choices overwhelm users
4. **Memory Usage**: Large lists consume more resources

### Caching Strategy

```java
@Cacheable(value = "movieRecommendations", 
           key = "#request.genre() + '-' + #request.mood() + '-' + #request.decade()")
public List<MovieRecommendation> getRecommendations(MoviePreferenceRequest request) {
    // Implementation
}
```

**Why cache?**
- **Cost Reduction**: Reuse expensive AI responses
- **Performance**: Instant responses for common queries
- **Reliability**: Serve cached data during AI outages

## Real-World Scenarios

### E-commerce Product Recommendations
```java
public List<ProductRecommendation> getPersonalizedProducts(CustomerProfile profile) {
    var converter = new BeanOutputConverter<>(
        new ParameterizedTypeReference<List<ProductRecommendation>>() {}
    );
    // Generate based on purchase history, preferences, trending items
}
```

### Content Curation Systems
```java
public List<ArticleRecommendation> getCuratedContent(UserInterests interests) {
    var converter = new BeanOutputConverter<>(
        new ParameterizedTypeReference<List<ArticleRecommendation>>() {}
    );
    // Personalized content based on reading history
}
```

## Best Practices

1. **Always Use ParameterizedTypeReference for Collections**
   - Preserves type information
   - Enables proper deserialization

2. **Validate Before AI Calls**
   - Save costs
   - Improve results
   - Better user experience

3. **Design Models for Use Case**
   - Rich models for user-facing features
   - Simple models for processing

4. **Plan for Scale**
   - Implement caching
   - Set reasonable limits
   - Monitor performance

## Common Pitfalls

### Pitfall 1: Forgetting Type Information
```java
// Wrong
BeanOutputConverter<List> converter = new BeanOutputConverter<>(List.class);

// Right
BeanOutputConverter<List<Movie>> converter = new BeanOutputConverter<>(
    new ParameterizedTypeReference<List<Movie>>() {}
);
```

### Pitfall 2: Unbounded Lists
```java
// Wrong
"Generate movie recommendations" // How many?

// Right
"Generate exactly 5 movie recommendations"
```

## Key Takeaways

1. **ParameterizedTypeReference solves type erasure**: Essential for collections
2. **Validation prevents problems**: Check inputs before AI calls
3. **Dynamic prompts provide flexibility**: Adapt to user needs
4. **Rich models enhance UX**: More data when users need it
5. **Limits protect the system**: Balance features with performance

## Next Steps

We've mastered lists and basic collections. Next, in [Section 4](s4-converters.md), we'll explore the full converter ecosystem and learn when to use each type of converter for maximum effectiveness.