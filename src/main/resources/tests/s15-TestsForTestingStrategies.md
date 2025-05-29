# S15 - Testing Strategies for Spring AI Structured Output

## Overview

This section demonstrates comprehensive testing strategies for Spring AI structured output, including unit tests, integration tests, performance benchmarks, and contract testing.

## Testing Components

### 1. Core Testing Utilities (`TestingUtilities`)
- Execution time measurement
- JSON schema validation
- Retry mechanisms with exponential backoff
- Performance metrics collection
- Test data sanitization

### 2. Mock AI Response Generators (`MockAIResponseGenerator`)
- Generate type-specific mock responses
- Simulate various error scenarios
- Batch response generation
- Edge case response generation

### 3. Mock ChatModel (`MockChatModel`)
- Configurable response handlers
- Error simulation with adjustable rate
- Response delay simulation
- Call tracking and verification
- Metrics collection

### 4. Test Data Fixtures (`TestDataFixtures`)
- Domain-specific data generators (Person, Product, Order, etc.)
- Batch data generation
- Edge case generators
- Validation test data

### 5. Performance Benchmarks (`PerformanceBenchmarks`)
- JMH-based micro-benchmarks
- Converter performance comparison
- Memory allocation benchmarks
- Concurrent operation benchmarks

### 6. Contract Testing (`SchemaContractTests`)
- JSON Schema generation validation
- Schema backward compatibility testing
- Cross-provider schema consistency
- Breaking change detection

## API Endpoints

### Unit Test Demo
```
GET /api/testing/unit-test-demo
```
Demonstrates unit testing with mock ChatModel and response verification.

### Integration Test Demo
```
GET /api/testing/integration-test-demo
```
Shows various integration testing scenarios including happy path, error handling, and edge cases.

### Performance Test Demo
```
GET /api/testing/performance-test-demo
```
Runs performance tests and returns metrics including average, min, max, and percentile times.

### Test Data Demo
```
GET /api/testing/test-data-demo
```
Generates sample test data using fixtures including normal cases and edge cases.

### Error Testing
```
POST /api/testing/error-testing
{
    "targetClass": "com.coherentsolutions.l3structuredoutput.s15.fixtures.TestDataFixtures$PersonData"
}
```
Tests various error scenarios and their handling.

### Concurrent Test Demo
```
GET /api/testing/concurrent-test-demo
```
Demonstrates concurrent request handling and thread safety.

### Resilience Test
```
POST /api/testing/resilience-test
{
    "attempts": 10,
    "errorRate": 30,
    "responseDelayMs": 100,
    "maxRetries": 3,
    "retryDelayMs": 50
}
```
Tests resilience patterns including retry and error recovery.

## Testing Patterns

### 1. Unit Testing Pattern
```java
// Create mock model
MockChatModel mockModel = new MockChatModel()
    .withTypedResponse(PersonData.class, MockConfig.defaults());

// Test converter
BeanOutputConverter<PersonData> converter = new BeanOutputConverter<>(PersonData.class);
PersonData result = converter.convert(mockResponse);

// Verify
assertThat(result).isNotNull();
assertThat(mockModel.verifyPromptCalled("expected prompt")).isTrue();
```

### 2. Integration Testing Pattern
```java
@SpringBootTest
@TestContainers
class IntegrationTest {
    @Container
    static GenericContainer<?> mockApi = new GenericContainer<>("mockserver/mockserver:latest");
    
    @Test
    void testEndToEnd() {
        // Test full request-response cycle
    }
}
```

### 3. Performance Testing Pattern
```java
@Benchmark
public void benchmarkConverter(Blackhole blackhole) {
    PersonData result = converter.convert(jsonResponse);
    blackhole.consume(result);
}
```

### 4. Contract Testing Pattern
```java
// Validate schema generation
String schema = extractJsonSchema(converter.getFormat());
JsonSchema jsonSchema = schemaFactory.getJsonSchema(schemaNode);
ProcessingReport report = jsonSchema.validate(dataNode);
assertThat(report.isSuccess()).isTrue();
```

## Best Practices

### 1. Mock Usage
- Use `MockChatModel` for deterministic unit tests
- Configure specific responses for test scenarios
- Verify mock interactions

### 2. Test Data Generation
- Use `TestDataFixtures` for consistent test data
- Generate edge cases for boundary testing
- Create domain-specific test data

### 3. Performance Testing
- Use JMH for micro-benchmarks
- Measure both latency and throughput
- Test with various data sizes

### 4. Error Testing
- Test all error scenarios (malformed JSON, missing fields, etc.)
- Verify error recovery mechanisms
- Test retry logic

### 5. Concurrent Testing
- Test thread safety with multiple concurrent requests
- Verify no race conditions
- Check performance under load

## Example Test Suite

```java
@SpringBootTest
class StructuredOutputTestSuite extends BaseStructuredOutputTest {
    
    @Test
    void testConverterWithValidData() {
        // Arrange
        MockChatModel mockModel = new MockChatModel()
            .withTypedResponse(WeatherData.class, MockConfig.defaults());
        ChatClient chatClient = ChatClient.builder(mockModel).build();
        BeanOutputConverter<WeatherData> converter = createTrackedBeanConverter(WeatherData.class);
        
        // Act
        WeatherData weather = chatClient.prompt()
            .user("Get weather")
            .call()
            .entity(converter);
        
        // Assert
        assertThat(weather).isNotNull();
        assertThat(weather.location()).isNotEmpty();
        performanceProfiler.printSummary();
    }
    
    @ParameterizedTest
    @EnumSource(ErrorType.class)
    void testErrorHandling(ErrorType errorType) {
        // Test each error scenario
        String errorResponse = responseGenerator.generateErrorResponse(errorType, TestModel.class);
        // Verify appropriate handling
    }
}
```

## Running the Tests

### Unit Tests
```bash
mvn test -Dtest=ConverterUnitTests
```

### Integration Tests
```bash
mvn test -Dtest=IntegrationTestPatterns
```

### Performance Benchmarks
```bash
mvn test -Dtest=PerformanceBenchmarks
java -jar target/benchmarks.jar
```

### Contract Tests
```bash
mvn test -Dtest=SchemaContractTests
```

## Metrics and Monitoring

The testing framework provides various metrics:
- Call counts and success rates
- Response times (min, max, average, percentiles)
- Memory usage statistics
- Concurrent request handling metrics
- Cache hit/miss rates

## Troubleshooting

### Common Issues

1. **Mock not returning expected response**
   - Verify prompt matching in mock configuration
   - Check response handler registration

2. **Performance tests failing**
   - Increase timeout values
   - Check for resource constraints
   - Verify warm-up iterations

3. **Contract tests failing**
   - Ensure schema extraction is correct
   - Verify JSON Schema validator version
   - Check for schema evolution

4. **Concurrent tests deadlocking**
   - Review thread pool configuration
   - Check for synchronization issues
   - Verify resource cleanup