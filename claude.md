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

## Branch Review & Postman Collection Process

### Standard Branch Analysis Workflow

This project uses a **branching strategy** where each learning section exists in its own branch. When analyzing or updating branches:

1. **Checkout target branch**: `git checkout {branch-name}`
2. **Analyze controller structure**: `find src -name "*Controller.java"`
3. **Document API endpoints and patterns**
4. **Update Postman collection with standardized structure**
5. **Test all endpoints with sample data**
6. **Commit and propagate CLAUDE.md updates**

### Branch Structure & Progression

**Foundation Branches:**
- `01-api-keys-and-properties` (s1): Basic setup, no API endpoints
- `02-prompt-templates` (s2): `/api/ai/structured/*` - Weather, recipes, sentiment
- `03-structured-output-fundamentals` (s3): `/api/movies/*` - Single and multiple recommendations

**Advanced Converter Branches:**
- `04-structured-output-converters` (s4): `/api/products/*` - Factory pattern demos
- `05-bean-output-converter` (s5): `/api/books/*`, `/api/capitals/*` - Complex structures
- `06-other-output-converters` (s6): `/api/travel/*` - Map and List converters
- `07-chat-client-and-model` (s7): `/api/weather/*`, `/api/finance/*` - API comparisons
- `08-model-specific-json-modes` (s8): `/api/json-modes/openai/*` - JSON modes

**Production Branches:**
- `14-advanced-generics` (s14): `/api/performance/*` - Optimization patterns
- `15-performance-optimization` (s15): `/api/testing/*` - Testing strategies
- `16-testing-strategies` (s16): `/api/s16/real-world-demo/*` - Production use cases

### Postman Collection Standards

The master collection (`postman/Spring-AI-Structured-Output-Course.postman_collection.json`) follows this structure:

```json
{
  "name": "Spring AI Structured Output Course",
  "variable": [
    {"key": "baseUrl", "value": "http://localhost:8080"},
    {"key": "openai_api_key", "value": "{{OPENAI_API_KEY}}"}
  ],
  "item": [
    {"name": "S01 - Foundation Setup"},
    {"name": "S02 - Prompt Templates & Basic Structured Output"},
    // ... continuing through S16
  ]
}
```

**Collection Organization Rules:**
1. Use environment variables for all dynamic values
2. Include comprehensive request documentation
3. Add validation tests for all responses
4. Organize requests by functional categories
5. Include realistic example payloads
6. Test both success and error scenarios

### API Endpoint Patterns

**URL Conventions:**
- Foundation: Health checks only (`/actuator/health`)
- Basic APIs: `/api/{feature}/*` (e.g., `/api/ai/structured/weather`)
- Feature APIs: `/api/{domain}/*` (e.g., `/api/movies/recommend`)
- Advanced APIs: `/api/{domain}/{subdomain}/*` (e.g., `/api/performance/cached`)
- Production: `/api/s16/real-world-demo/{use-case}/*`

**Request/Response Patterns:**
- All POST requests use `Content-Type: application/json`
- Request bodies follow domain-specific DTO patterns
- Responses return structured objects with `@JsonPropertyDescription` annotations
- Error responses include meaningful messages and HTTP status codes

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
spring.ai.openai.chat.options.model=gpt-3.5-turbo
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

## CLAUDE.md Propagation Process

When updating branches, always propagate the latest CLAUDE.md:

```bash
# From current branch with latest CLAUDE.md
git checkout target-branch
cp /path/to/latest/CLAUDE.md ./CLAUDE.md

# Update branch-specific context
# Add section-specific patterns and examples
# Commit changes
git add CLAUDE.md
git commit -m "Update CLAUDE.md with latest project guidelines"
```

## Testing Endpoints by Section

### S02 - Prompt Templates
```bash
# Weather forecast
curl -X POST http://localhost:8080/api/ai/structured/weather \
  -H "Content-Type: application/json" \
  -d '{"location": "Seattle, WA", "forecastType": "daily"}'

# Recipe generation  
curl -X POST http://localhost:8080/api/ai/structured/recipe \
  -H "Content-Type: application/json" \
  -d '{"dishName": "Pasta Carbonara", "servings": 4}'

# Sentiment analysis
curl -X POST http://localhost:8080/api/ai/structured/sentiment \
  -H "Content-Type: application/json" \
  -d '{"text": "I love Spring AI!", "includeConfidence": true}'
```

### S03 - Structured Fundamentals  
```bash
# Single movie recommendation
curl -X POST http://localhost:8080/api/movies/recommend \
  -H "Content-Type: application/json" \
  -d '{"genre": "Action", "releaseYearAfter": 2020, "mood": "exciting", "maxResults": 1}'

# Multiple recommendations
curl -X POST http://localhost:8080/api/movies/recommend/multiple \
  -H "Content-Type: application/json" \
  -d '{"genre": "Sci-Fi", "releaseYearAfter": 2015, "maxResults": 5}'
```

### S16 - Real-World Use Cases
```bash
# E-commerce catalog generation
curl -X POST http://localhost:8080/api/s16/real-world-demo/ecommerce/catalog \
  -H "Content-Type: application/json" \
  -d '{"category": "Electronics", "productCount": 10, "minPrice": 100.0, "maxPrice": 1000.0}'

# Executive report generation
curl -X POST http://localhost:8080/api/s16/real-world-demo/reports/executive \
  -H "Content-Type: application/json" \
  -d '{"reportType": "quarterly-business", "department": "Technology", "period": "Q4-2024"}'
```