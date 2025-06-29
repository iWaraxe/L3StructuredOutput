# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

@~/.claude/spring-ai-course.md

## Project-Specific Context

This is **Lecture 3: Structured Output** of the Spring AI course series, focusing specifically on Spring AI's StructuredOutputConverter interface and related conversion patterns.

## Build Commands

```bash
# Build the project
./mvnw clean package

# Run all tests
./mvnw test

# Run performance benchmarks (JMH)
./mvnw test -Dtest=BenchmarkTest

# Run specific test class
./mvnw test -Dtest=ConverterFactoryTest

# Run application
./mvnw spring-boot:run

# Run with specific Spring profile
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

## Architecture Overview

### Core Architectural Pattern
The project demonstrates **progressive learning architecture** with 16 sections (s1-s16), each building upon previous concepts:

- **s1**: Foundation setup (currently basic)
- **s2-s3**: Fundamental converter usage
- **s4-s6**: Advanced converter patterns and factories
- **s7-s8**: API choices and JSON modes
- **s14-s16**: Production-ready features (performance, testing, real-world)

### Key Components

#### StructuredOutputConverter Ecosystem
- **BeanOutputConverter**: Type-safe POJO conversion with JSON Schema
- **MapOutputConverter**: Flexible key-value structures
- **ListOutputConverter**: Simple comma-delimited lists
- **Custom Converters**: Abstract base classes for specialized needs

#### Service Layer Pattern
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ExampleService {
    private final ChatClient chatClient;
    private final StructuredOutputConverter<TargetType> converter;
    
    public TargetType processWithStructuredOutput(String prompt) {
        return chatClient.prompt()
            .user(prompt)
            .call()
            .entity(converter);
    }
}
```

#### Controller Pattern for Each Section
Each section follows consistent REST API pattern:
- Controllers in `s{N}/controllers/` packages
- Endpoints: `/api/s{N}/{feature}`
- Consistent request/response DTOs

## Branch-Specific Behavior

### Current Branch Context
This project uses a **branching strategy** where each section exists in its own branch:
- `01-api-keys-and-properties` (s1)
- `02-prompt-templates` (s2)
- `03-structured-output-fundamentals` (s3)
- ... continuing through `17-real-world-best-practices` (s16)

When working on this project:
1. Always check current branch context with `git branch --show-current`
2. Each branch represents a different learning stage
3. Code complexity increases progressively across branches

## Testing Strategy Specifics

### Mock-First Approach for Cost Control
This is an **educational project** with extensive AI integration testing:

```java
// Preferred: Use MockChatModel for unit tests
@TestConfiguration
static class TestConfig {
    @Bean
    @Primary
    public ChatModel chatModel() {
        return new MockChatModel("mocked structured response");
    }
}

// Use real APIs sparingly for integration validation
@Test
@Disabled("Expensive - enable for integration testing")
void testWithRealOpenAI() {
    // Real OpenAI integration test
}
```

### Performance Testing with JMH
Advanced sections (s14+) include JMH benchmarking:
```bash
# Run performance benchmarks
./mvnw test -Dtest=*Benchmark*
```

## Configuration Patterns

### Required Environment Variables
```bash
export OPENAI_API_KEY=your_key_here
```

### Application Properties Structure
```properties
spring.application.name=L3StructuredOutput
spring.main.allow-bean-definition-overriding=true
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4.1
spring.ai.openai.chat.options.temperature=0.7
```

## Development Patterns

### Converter Usage Patterns
```java
// Bean converter for type-safe objects
BeanOutputConverter<WeatherForecast> converter = 
    new BeanOutputConverter<>(WeatherForecast.class);

// Map converter for flexible structures
MapOutputConverter converter = new MapOutputConverter();

// List converter for simple collections
ListOutputConverter converter = new ListOutputConverter();
```

### Error Handling for AI Responses
Always implement validation since LLMs may not follow instructions perfectly:
```java
public Optional<TargetType> safeConvert(String aiResponse) {
    try {
        TargetType result = converter.convert(aiResponse);
        return Optional.ofNullable(result);
    } catch (Exception e) {
        log.warn("Conversion failed for response: {}", aiResponse, e);
        return Optional.empty();
    }
}
```

## Production Considerations (s16)

### Real-World Use Cases Implemented
- **E-commerce**: Product catalog generation with parallel processing
- **Reports**: Executive dashboard generation with multiple formats
- **Data Extraction**: Invoice/contract processing pipelines
- **API Transformation**: Legacy system modernization patterns

### Performance Patterns
- **Caching**: `StructuredOutputCache` for repeated requests
- **Parallel Processing**: `ParallelProcessingService` for batch operations
- **Memory Efficiency**: `MemoryEfficientService` for large datasets
- **Token Optimization**: `TokenOptimizationService` for cost control

## Common Anti-Patterns to Avoid

1. **Don't** use field injection in service classes (use constructor injection)
2. **Don't** ignore converter validation - always handle potential conversion failures
3. **Don't** use raw ChatModel API when ChatClient provides cleaner abstraction
4. **Don't** forget to set temperature and model parameters for consistent results
5. **Don't** skip testing with mock responses - real AI calls are expensive

## Testing Endpoints

Each section provides REST endpoints for manual testing:
```bash
# View section-specific demos
curl http://localhost:8080/api/s2/demos
curl http://localhost:8080/api/s4/products

# Test structured output
curl -X POST http://localhost:8080/api/s3/movies \
  -H "Content-Type: application/json" \
  -d '{"genres": ["action", "sci-fi"], "maxResults": 5}'
```