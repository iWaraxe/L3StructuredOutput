# S5: Advanced Bean Converter - Mastering Complex Structures

## Why Complex Nested Structures?

Real-world applications rarely deal with flat data. Consider these scenarios:

```java
// Oversimplified - doesn't match reality
public record Book(String title, String author, double price) {}

// Reality - books have complex relationships
public record Book(
    String title,
    List<Author> authors,        // Multiple authors
    Publisher publisher,          // Nested publisher info
    List<Edition> editions,       // Different formats
    Map<String, Review> reviews,  // Reviews by source
    Categories categories         // Multiple category hierarchies
) {}
```

**The challenge:** How do we reliably extract such complex, nested data from AI models?

## The Map<String, List<T>> Pattern

### Why This Pattern Emerges

```java
// Common requirement: Group items by category
Map<String, List<BookRecommendation>> booksByGenre = getRecommendationsByGenre();

// Result:
// {
//   "Science Fiction": [Book1, Book2, Book3],
//   "Mystery": [Book4, Book5],
//   "Romance": [Book6, Book7, Book8]
// }
```

**This pattern appears everywhere:**
- **E-commerce**: Products by category
- **Content Systems**: Articles by topic
- **HR Systems**: Employees by department
- **Educational**: Courses by subject

### The Type Parameter Challenge

```java
// The complexity of the type
new ParameterizedTypeReference<Map<String, List<BookRecommendation>>>() {}

// Why so complex?
// 1. Map<K,V> - Two type parameters
// 2. V is List<T> - Another type parameter
// 3. Total: Three levels of generics
```

## Advanced Bean Converter in Action

### The Book Recommendation Service

```java
@Service
public class BookRecommendationService {
    public Map<String, List<BookRecommendation>> getRecommendationsByGenre(
            List<String> genres, 
            String mood, 
            int perGenre) {
        
        // Why validate inputs?
        validateInputs(genres, perGenre);
        
        // Why this specific type reference?
        var converter = new BeanOutputConverter<>(
            new ParameterizedTypeReference<Map<String, List<BookRecommendation>>>() {}
        );
        
        // Why structure the prompt this way?
        String prompt = buildGenreBasedPrompt(genres, mood, perGenre) + 
                       "\n\n" + converter.getFormat();
        
        return chatClient.prompt()
            .user(prompt)
            .call()
            .entity(converter);
    }
}
```

**Design decisions explained:**

1. **Return Type**: Map allows easy genre-based access
2. **Validation**: Prevents excessive API calls
3. **Type Safety**: Compile-time guarantee of structure
4. **Prompt Structure**: Guides AI to produce organized output

### Rich Data Models - Why the Complexity?

```java
public record BookRecommendation(
    @JsonPropertyDescription("ISBN-13 identifier") String isbn,
    @JsonPropertyDescription("Complete book title") String title,
    @JsonPropertyDescription("All authors") List<String> authors,
    @JsonPropertyDescription("Primary genre") String genre,
    @JsonPropertyDescription("Publication year") int year,
    @JsonPropertyDescription("Average rating 0-5") double rating,
    @JsonPropertyDescription("Total pages") int pages,
    @JsonPropertyDescription("Brief plot summary") String summary,
    @JsonPropertyDescription("Similar books") List<String> similarBooks,
    @JsonPropertyDescription("Available formats") List<String> formats,
    @JsonPropertyDescription("Reasons this matches request") List<String> matchReasons
) {}
```

**Why so many fields?**
- **User Experience**: Rich data enables better decisions
- **Filtering**: Users can filter/sort by multiple criteria
- **Recommendations**: "Similar books" drive engagement
- **Business Logic**: Formats affect pricing/availability

## The Capital Information Comparison Use Case

### Why Comparison Matters

```java
public record CapitalInfo(
    String country,
    String capital,
    long population,
    String currency,
    List<String> languages,
    String timezone,
    Map<String, Object> demographics,
    EconomicIndicators economicIndicators,
    List<String> majorAttractions
) {}
```

**Real-world need:** Users don't just want data - they want to compare:
- **Business**: Compare markets for expansion
- **Travel**: Compare destinations
- **Education**: Compare study abroad options
- **Investment**: Compare economic indicators

### Robust Error Handling

```java
@Service
public class RobustCapitalInfoService {
    private static final int MAX_RETRIES = 3;
    private static final Set<String> VALID_COUNTRIES = Set.of(/* ... */);
    
    public List<CapitalInfo> getCapitalInfoBatch(List<String> countries) {
        // Why validate?
        List<String> validCountries = countries.stream()
            .filter(VALID_COUNTRIES::contains)
            .distinct()
            .limit(10) // Why limit?
            .toList();
        
        // Why use CompletableFuture?
        List<CompletableFuture<CapitalInfo>> futures = validCountries.stream()
            .map(country -> CompletableFuture.supplyAsync(() -> 
                getCapitalInfoWithRetry(country)))
            .toList();
        
        // Why handle partial failures?
        return futures.stream()
            .map(future -> future.exceptionally(ex -> null))
            .map(CompletableFuture::join)
            .filter(Objects::nonNull)
            .toList();
    }
}
```

**Robustness strategies:**
1. **Input Validation**: Prevent invalid requests
2. **Rate Limiting**: Limit batch size to 10
3. **Retry Logic**: Handle transient failures
4. **Partial Success**: Return what succeeded
5. **Async Processing**: Don't block on slow requests

## Complex Prompt Engineering

### Multi-Level Instructions

```java
private String buildGenreBasedPrompt(List<String> genres, String mood, int perGenre) {
    return String.format("""
        Generate book recommendations organized by genre.
        
        Requirements:
        - Genres to cover: %s
        - Books per genre: exactly %d
        - Mood/Theme: %s
        
        For each book, provide:
        - Complete bibliographic information
        - Why it matches the requested mood
        - Similar books for further reading
        
        Ensure:
        - No duplicate books across genres
        - Mix of classic and contemporary works
        - Ratings are realistic (most books 3.5-4.5)
        - Include available formats (hardcover, paperback, ebook, audiobook)
        """, 
        String.join(", ", genres), 
        perGenre, 
        mood != null ? mood : "any"
    );
}
```

**Why this structure?**
- **Clear Requirements**: AI knows exactly what's expected
- **Constraints**: Prevent common issues (duplicates, unrealistic ratings)
- **Quality Control**: "Mix of classic and contemporary"
- **Business Rules**: Format availability affects user experience

## Performance Optimization Strategies

### Caching Complex Structures

```java
@Cacheable(value = "bookRecommendations", 
           key = "#genres.hashCode() + '-' + #mood + '-' + #perGenre")
public Map<String, List<BookRecommendation>> getRecommendationsByGenre(...) {
    // Expensive operation cached
}
```

**Why cache nested structures?**
- **High Cost**: Complex prompts use more tokens
- **Slow Generation**: More data = longer response times
- **Common Queries**: Users often request similar combinations

### Batch Processing Patterns

```java
public class BatchProcessingExample {
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    
    public Map<String, List<BookRecommendation>> processMultipleRequests(
            List<GenreRequest> requests) {
        
        Map<String, List<BookRecommendation>> result = new ConcurrentHashMap<>();
        
        List<CompletableFuture<Void>> futures = requests.stream()
            .map(request -> CompletableFuture.runAsync(() -> {
                var recommendations = getRecommendationsByGenre(
                    request.genres(), 
                    request.mood(), 
                    request.count()
                );
                result.putAll(recommendations);
            }, executor))
            .toList();
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        return result;
    }
}
```

**Benefits:**
- **Parallel Processing**: Multiple genres simultaneously
- **Resource Management**: Controlled thread pool
- **Scalability**: Handles large request volumes

## Error Recovery Patterns

### Graceful Degradation

```java
private BookRecommendation createFallbackRecommendation(String genre) {
    return new BookRecommendation(
        "000-0-00-000000-0",
        "Recommendation temporarily unavailable",
        List.of("Unknown"),
        genre,
        2024,
        0.0,
        0,
        "Unable to generate recommendation at this time",
        List.of(),
        List.of("unknown"),
        List.of("Service temporarily unavailable")
    );
}

public Map<String, List<BookRecommendation>> getRecommendationsWithFallback(
        List<String> genres, String mood, int perGenre) {
    try {
        return getRecommendationsByGenre(genres, mood, perGenre);
    } catch (Exception e) {
        logger.error("Failed to get recommendations", e);
        // Return structure with fallback data
        return genres.stream()
            .collect(Collectors.toMap(
                genre -> genre,
                genre -> List.of(createFallbackRecommendation(genre))
            ));
    }
}
```

**Why fallbacks matter:**
- **User Experience**: Something is better than error
- **System Stability**: Partial failures don't cascade
- **Debugging**: Clear indication of issues
- **Business Continuity**: Service remains operational

## Testing Complex Structures

### Unit Testing Strategies

```java
@Test
void testComplexNestedStructure() {
    // Given
    String jsonResponse = """
        {
            "Science Fiction": [
                {
                    "isbn": "978-0-441-56959-7",
                    "title": "Neuromancer",
                    "authors": ["William Gibson"],
                    "genre": "Science Fiction",
                    "year": 1984,
                    "rating": 4.5
                }
            ]
        }
        """;
    
    // When
    var converter = new BeanOutputConverter<>(
        new ParameterizedTypeReference<Map<String, List<BookRecommendation>>>() {}
    );
    Map<String, List<BookRecommendation>> result = converter.convert(jsonResponse);
    
    // Then
    assertNotNull(result);
    assertTrue(result.containsKey("Science Fiction"));
    assertEquals(1, result.get("Science Fiction").size());
    assertEquals("Neuromancer", result.get("Science Fiction").get(0).title());
}
```

## Best Practices for Complex Structures

1. **Start with Clear Models**
   ```java
   // Define your domain model completely before implementing
   ```

2. **Use Builder Pattern for Complex Objects**
   ```java
   BookRecommendation.builder()
       .title("...")
       .authors(List.of(...))
       .build();
   ```

3. **Implement Equals and HashCode**
   ```java
   // Records do this automatically!
   ```

4. **Document Nested Structures**
   ```java
   /**
    * Returns books grouped by genre.
    * @return Map where key is genre name, value is list of books in that genre
    */
   ```

## Common Pitfalls and Solutions

### Pitfall 1: Type Erasure Confusion
```java
// Wrong - loses type information
Map<String, List> result = converter.convert(response);

// Right - preserves full type
Map<String, List<BookRecommendation>> result = converter.convert(response);
```

### Pitfall 2: Unbounded Nesting
```java
// Avoid infinite nesting
public record Author(
    String name,
    List<Book> books // Book has List<Author> - circular reference!
) {}
```

### Pitfall 3: Over-Engineering
```java
// Too complex
Map<String, Map<String, List<Map<String, BookRecommendation>>>> 

// Better - flatten when possible
Map<String, List<BookRecommendation>>
```

## Key Takeaways

1. **Complex structures reflect reality**: Real data is rarely flat
2. **Type safety matters more with nesting**: Errors compound in nested structures
3. **ParameterizedTypeReference is your friend**: Preserves generic type information
4. **Plan for failure**: Complex structures need robust error handling
5. **Performance requires attention**: Cache and batch complex operations
6. **Testing is crucial**: More complexity = more test cases needed

## Next Steps

We've mastered complex nested structures with BeanOutputConverter. Next, in [Section 6](s6-collections.md), we'll explore when and why to use MapOutputConverter and ListOutputConverter for different scenarios.