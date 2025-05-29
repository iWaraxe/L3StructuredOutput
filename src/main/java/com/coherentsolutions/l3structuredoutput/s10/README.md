# Section 10: Multi-Model Support

## Overview

This section demonstrates how to implement structured output across different AI providers including OpenAI, Azure OpenAI, Anthropic, Mistral, and Ollama. It shows patterns for creating provider-agnostic structured responses while handling provider-specific features.

## Key Concepts

### 1. **Provider Abstraction**
- Create universal response models that work across all providers
- Abstract provider-specific implementation details
- Maintain consistent structured output format

### 2. **Provider-Specific Features**
Different providers support structured output differently:

#### OpenAI
- **JSON Mode**: Native support via `response_format`
- **JSON Schema**: Strict schema validation
- **Function Calling**: Alternative for structured output
- **Best For**: Production applications with strict requirements

#### Azure OpenAI
- Same capabilities as OpenAI
- Additional deployment flexibility
- Regional compliance options
- **Best For**: Enterprise applications

#### Anthropic (Claude)
- No native JSON mode
- Excellent instruction following
- XML-style formatting works well
- **Best For**: Complex reasoning with structured prompts

#### Mistral
- JSON mode support
- Function calling capabilities
- Good performance/cost ratio
- **Best For**: European deployments, cost-sensitive applications

#### Ollama (Local Models)
- Varies by model
- No guaranteed structured output
- Privacy-preserving (runs locally)
- **Best For**: Sensitive data, offline usage

### 3. **Universal Response Model**
```java
@JsonPropertyOrder({"requestId", "provider", "model", "timestamp", "content", "metadata", "usage"})
public record UniversalResponse(
    String requestId,
    String provider,
    String model,
    LocalDateTime timestamp,
    ResponseContent content,
    Map<String, Object> metadata,
    UsageInfo usage
)
```

## Implementation Examples

### Code Analysis Model
The `CodeAnalysis` model demonstrates a complex structured output that can be used across providers:
- Language detection
- Complexity assessment
- Issue identification with severity levels
- Code metrics
- Improvement suggestions

### Multi-Model Service
The service shows how to:
1. Configure different providers for structured output
2. Handle provider-specific options
3. Maintain consistent response format
4. Implement fallback strategies

## API Endpoints

### 1. Analyze Code
```bash
POST /api/multi-model/analyze-code
Content-Type: application/json

{
  "code": "def factorial(n):\n    return 1 if n <= 1 else n * factorial(n-1)",
  "language": "python"
}
```
Analyzes code and returns structured analysis using OpenAI.

### 2. Generate with Provider
```bash
POST /api/multi-model/generate?provider=openai
Content-Type: application/json

{
  "prompt": "Explain the benefits of microservices architecture"
}
```
Generates structured output using specified provider.

### 3. Compare Providers
```bash
GET /api/multi-model/compare-providers
```
Returns comparison of structured output capabilities across providers.

### 4. Provider-Specific Endpoints
```bash
POST /api/multi-model/azure/generate
POST /api/multi-model/anthropic/generate
POST /api/multi-model/mistral/generate
POST /api/multi-model/ollama/generate
```
Each endpoint demonstrates provider-specific structured output generation.

### 5. Best Practices
```bash
GET /api/multi-model/best-practices
```
Returns best practices for multi-model structured output.

## Best Practices

### 1. **Design Universal Models**
- Use standard JSON types
- Avoid provider-specific features in models
- Include clear property descriptions
- Test with multiple providers

### 2. **Handle Provider Differences**
```java
// OpenAI - Use JSON mode
OpenAiChatOptions.builder()
    .withResponseFormat(ResponseFormat.JSON_OBJECT)
    .build();

// Anthropic - Use clear formatting
"Please respond with a JSON object with the following structure:"

// Mistral - Similar to OpenAI
MistralOptions.builder()
    .withJsonMode(true)
    .build();
```

### 3. **Implement Fallbacks**
```java
try {
    return generateWithOpenAI(prompt);
} catch (Exception e) {
    logger.warn("OpenAI failed, falling back to Anthropic", e);
    return generateWithAnthropic(prompt);
}
```

### 4. **Cache and Reuse**
- Cache generated schemas
- Reuse converter instances
- Pool provider connections

## Provider Selection Guide

| Use Case | Recommended Provider | Reason |
|----------|---------------------|---------|
| Production API | OpenAI/Azure | Best structured output support |
| Complex Analysis | Anthropic | Superior reasoning capabilities |
| European Compliance | Mistral | EU-based, GDPR compliant |
| Sensitive Data | Ollama | Runs locally, no data leaves premises |
| Cost-Sensitive | Mistral/Ollama | Lower costs than OpenAI |

## Testing Multi-Model Support

1. **Unit Tests**: Mock each provider's response
2. **Integration Tests**: Test with actual provider APIs
3. **Compatibility Tests**: Verify models work across providers
4. **Performance Tests**: Compare response times and costs

## Common Challenges

### 1. **Inconsistent Formatting**
- Solution: Implement robust parsing with fallbacks
- Use lenient JSON parsing when needed

### 2. **Provider Limitations**
- Solution: Document provider capabilities
- Design models around lowest common denominator

### 3. **Error Handling**
- Solution: Provider-specific error handlers
- Implement circuit breakers for reliability

## Future Considerations

1. **New Providers**: Design for easy addition of new AI providers
2. **Version Changes**: Handle API version differences
3. **Feature Parity**: Track provider feature updates
4. **Cost Optimization**: Implement intelligent routing based on task

This multi-model approach ensures your structured output implementation remains flexible and can adapt to the rapidly evolving AI landscape.