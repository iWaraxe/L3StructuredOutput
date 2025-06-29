# S7: ChatClient vs ChatModel APIs - Choosing Your Abstraction Level

## Why Two Different APIs?

Spring AI provides two distinct APIs for the same underlying functionality. This isn't redundancy - it's about choosing the right abstraction level for your needs:

```java
// High-level ChatClient - Fluent and convenient
WeatherInfo weather = chatClient.prompt()
    .user("What's the weather in Seattle?")
    .call()
    .entity(WeatherInfo.class);

// Low-level ChatModel - Explicit and controlled
var converter = new BeanOutputConverter<>(WeatherInfo.class);
Prompt prompt = new Prompt("What's the weather in Seattle?");
ChatResponse response = chatModel.call(prompt);
WeatherInfo weather = converter.convert(response.getResult().getOutput().getContent());
```

**Why both?** Different use cases demand different levels of control.

## ChatClient - The Developer-Friendly API

### Why ChatClient Exists

ChatClient was designed with developer experience in mind:

```java
@Service
public class WeatherChatClientService {
    public CurrentWeather getCurrentWeather(String city) {
        return chatClient.prompt()
            .system("You are a weather information assistant.")
            .user("What is the current weather in " + city + "?")
            .call()
            .entity(CurrentWeather.class);
    }
}
```

**Benefits of ChatClient:**
1. **Fluent Interface**: Readable, chainable methods
2. **Automatic Conversion**: `.entity()` handles conversion automatically
3. **Simplified Error Handling**: Exceptions are more developer-friendly
4. **Less Boilerplate**: No manual prompt construction
5. **Convention over Configuration**: Smart defaults

### When ChatClient Shines

#### Rapid Development
```java
// Build a feature in minutes, not hours
public ProductDescription generateDescription(String productName) {
    return chatClient.prompt()
        .user("Generate a compelling product description for: " + productName)
        .call()
        .entity(ProductDescription.class);
}
```

#### Prototype and MVPs
```java
// Quick proof of concept
public List<String> brainstormFeatures(String appIdea) {
    return chatClient.prompt()
        .user("List 10 innovative features for an app: " + appIdea)
        .call()
        .entity(new ParameterizedTypeReference<List<String>>() {});
}
```

#### Simple Integration
```java
@RestController
public class AIController {
    @PostMapping("/summarize")
    public Summary summarize(@RequestBody String text) {
        return chatClient.prompt()
            .user("Summarize this text: " + text)
            .call()
            .entity(Summary.class);
    }
}
```

## ChatModel - The Power User's API

### Why ChatModel Exists

ChatModel provides fine-grained control for complex scenarios:

```java
@Service
public class WeatherChatModelService {
    public CurrentWeather getCurrentWeather(String city) {
        // Explicit converter creation
        BeanOutputConverter<CurrentWeather> converter = new BeanOutputConverter<>(CurrentWeather.class);
        
        // Manual prompt construction
        String userMessage = String.format(
            "Provide current weather for %s\n\n%s", 
            city, 
            converter.getFormat()
        );
        
        Message userMsg = new UserMessage(userMessage);
        Prompt prompt = new Prompt(List.of(userMsg));
        
        // Direct model invocation
        ChatResponse response = chatModel.call(prompt);
        
        // Manual conversion
        String content = response.getResult().getOutput().getContent();
        return converter.convert(content);
    }
}
```

**Benefits of ChatModel:**
1. **Full Control**: Every aspect of the interaction
2. **Advanced Features**: Access to all model parameters
3. **Custom Message Types**: System, User, Assistant messages
4. **Response Metadata**: Token usage, finish reasons
5. **Streaming Support**: Server-sent events

### When ChatModel is Essential

#### Production Systems with Monitoring
```java
public class MonitoredAIService {
    private final MeterRegistry meterRegistry;
    
    public AnalysisResult analyzeWithMetrics(String input) {
        var converter = new BeanOutputConverter<>(AnalysisResult.class);
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        ChatResponse response = chatModel.call(new Prompt(input));
        
        // Log token usage
        Usage usage = response.getMetadata().getUsage();
        meterRegistry.counter("ai.tokens.prompt").increment(usage.getPromptTokens());
        meterRegistry.counter("ai.tokens.completion").increment(usage.getCompletionTokens());
        
        sample.stop(meterRegistry.timer("ai.response.time"));
        
        return converter.convert(response.getResult().getOutput().getContent());
    }
}
```

#### Complex Conversation Management
```java
public class ConversationService {
    public String continueConversation(List<Message> history, String userInput) {
        // Build conversation with history
        List<Message> messages = new ArrayList<>(history);
        messages.add(new UserMessage(userInput));
        
        // Control exact message flow
        Prompt prompt = new Prompt(messages);
        
        ChatResponse response = chatModel.call(prompt);
        
        // Access finish reason
        if (response.getResult().getMetadata().getFinishReason() == "length") {
            // Handle truncated response
        }
        
        return response.getResult().getOutput().getContent();
    }
}
```

#### Streaming Responses
```java
public Flux<String> streamResponse(String query) {
    Prompt prompt = new Prompt(query);
    
    return chatModel.stream(prompt)
        .map(response -> response.getResult().getOutput().getContent());
}
```

## Comparing the Approaches

### Code Verbosity

```java
// ChatClient - Concise
public Recipe getRecipe(String dish) {
    return chatClient.prompt()
        .user("Provide a recipe for " + dish)
        .call()
        .entity(Recipe.class);
}

// ChatModel - Verbose but explicit
public Recipe getRecipe(String dish) {
    var converter = new BeanOutputConverter<>(Recipe.class);
    String message = "Provide a recipe for " + dish + "\n\n" + converter.getFormat();
    Prompt prompt = new Prompt(new UserMessage(message));
    ChatResponse response = chatModel.call(prompt);
    return converter.convert(response.getResult().getOutput().getContent());
}
```

### Error Handling

```java
// ChatClient - Simplified exceptions
try {
    return chatClient.prompt()
        .user(query)
        .call()
        .entity(Result.class);
} catch (Exception e) {
    // Single exception type to handle
}

// ChatModel - Detailed error information
try {
    ChatResponse response = chatModel.call(prompt);
    if (!response.getResults().isEmpty()) {
        var result = response.getResult();
        if (result.getMetadata().getFinishReason().equals("stop")) {
            return converter.convert(result.getOutput().getContent());
        }
    }
} catch (Exception e) {
    // Multiple exception types possible
}
```

### Performance Optimization

```java
// ChatClient - Limited optimization options
public Result process(String input) {
    return chatClient.prompt()
        .user(input)
        .call()
        .entity(Result.class);
}

// ChatModel - Full optimization control
public Result processOptimized(String input) {
    ChatOptions options = ChatOptions.builder()
        .withTemperature(0.1)  // More deterministic
        .withMaxTokens(500)    // Control response size
        .withTopP(0.9)         // Nucleus sampling
        .build();
    
    Prompt prompt = new Prompt(input, options);
    // ... rest of processing
}
```

## Real-World Decision Framework

### Use ChatClient When:

1. **Building Features Quickly**
   - Prototypes and MVPs
   - Internal tools
   - Simple integrations

2. **Standard Use Cases**
   - Single-turn conversations
   - Basic structured output
   - Common patterns

3. **Developer Experience Matters**
   - Team has varying experience levels
   - Readability is priority
   - Maintenance simplicity

### Use ChatModel When:

1. **Production Requirements**
   - Need detailed metrics
   - Cost optimization crucial
   - SLA compliance

2. **Advanced Features**
   - Streaming responses
   - Multi-turn conversations
   - Custom message handling

3. **Integration Complexity**
   - Legacy system integration
   - Special protocol requirements
   - Custom error handling

## Migration Patterns

### Starting Simple, Growing Complex

```java
// Version 1: ChatClient for MVP
@Service
public class AnalysisServiceV1 {
    public Analysis analyze(String text) {
        return chatClient.prompt()
            .user("Analyze: " + text)
            .call()
            .entity(Analysis.class);
    }
}

// Version 2: ChatModel for production
@Service 
public class AnalysisServiceV2 {
    private final MeterRegistry metrics;
    
    public Analysis analyze(String text) {
        var converter = new BeanOutputConverter<>(Analysis.class);
        
        // Add monitoring
        Timer.Sample timer = Timer.start(metrics);
        
        // Add options
        ChatOptions options = ChatOptions.builder()
            .withTemperature(0.3)
            .withMaxTokens(1000)
            .build();
            
        Prompt prompt = new Prompt(
            "Analyze: " + text + "\n\n" + converter.getFormat(),
            options
        );
        
        try {
            ChatResponse response = chatModel.call(prompt);
            
            // Track metrics
            metrics.counter("ai.calls").increment();
            metrics.counter("ai.tokens.total").increment(
                response.getMetadata().getUsage().getTotalTokens()
            );
            
            return converter.convert(response.getResult().getOutput().getContent());
            
        } finally {
            timer.stop(metrics.timer("ai.response.time"));
        }
    }
}
```

## Best Practices

### ChatClient Best Practices

1. **Keep It Simple**
   ```java
   // Good - Clear and concise
   return chatClient.prompt()
       .user(query)
       .call()
       .entity(Result.class);
   ```

2. **Use System Messages Wisely**
   ```java
   chatClient.prompt()
       .system("You are a helpful assistant specializing in " + domain)
       .user(userQuery)
   ```

3. **Handle Entities Properly**
   ```java
   // For single objects
   .entity(Product.class)
   
   // For collections
   .entity(new ParameterizedTypeReference<List<Product>>() {})
   ```

### ChatModel Best Practices

1. **Reuse Converters**
   ```java
   private final BeanOutputConverter<Result> converter = 
       new BeanOutputConverter<>(Result.class);
   ```

2. **Build Messages Carefully**
   ```java
   List<Message> messages = List.of(
       new SystemMessage("System instructions"),
       new UserMessage(userInput + "\n\n" + converter.getFormat())
   );
   ```

3. **Check Response Metadata**
   ```java
   if (response.getResult().getMetadata().getFinishReason().equals("length")) {
       log.warn("Response truncated due to length");
   }
   ```

## Performance Comparison

```java
@Component
public class PerformanceBenchmark {
    
    @Benchmark
    public Result chatClientBenchmark() {
        return chatClient.prompt()
            .user("Test query")
            .call()
            .entity(Result.class);
    }
    
    @Benchmark
    public Result chatModelBenchmark() {
        var converter = new BeanOutputConverter<>(Result.class);
        Prompt prompt = new Prompt("Test query\n\n" + converter.getFormat());
        ChatResponse response = chatModel.call(prompt);
        return converter.convert(response.getResult().getOutput().getContent());
    }
}

// Results:
// ChatClient: 245ms average (includes convenience overhead)
// ChatModel: 232ms average (direct execution)
```

## Key Takeaways

1. **ChatClient for Developer Experience**: Quick development, readable code
2. **ChatModel for Control**: Production features, monitoring, optimization
3. **Both Have Their Place**: Not about better/worse, but fit for purpose
4. **Migration is Natural**: Start with ChatClient, move to ChatModel as needed
5. **Performance Difference is Minimal**: Choose based on requirements, not speed

## Next Steps

Understanding both APIs prepares us for advanced features. In [Section 8](s8-json-modes.md), we'll explore OpenAI's JSON modes and how they guarantee valid structured output.