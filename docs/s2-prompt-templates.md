# S2: Prompt Templates & Basic Structured Output Guide

## Why Prompt Templates?

Imagine building an e-commerce system where you need to generate product descriptions. Without templates, every developer might write prompts differently:

```java
// Developer A's approach
String prompt1 = "Generate a product description for " + productName;

// Developer B's approach  
String prompt2 = "Create marketing copy for our " + productName + " product";

// Developer C's approach
String prompt3 = productName + " - write a description";
```

**Problems with this approach:**
- **Inconsistent results**: Different prompts yield different quality outputs
- **Maintenance nightmare**: Prompts scattered throughout codebase
- **No reusability**: Can't share proven prompt patterns
- **Testing difficulty**: Hard to test string concatenation logic

## The Power of Templates

### Why StringTemplate4?

Spring AI chose StringTemplate4 (ST4) for prompt templates because:

1. **Separation of Concerns**: Templates live separately from logic
2. **Type Safety**: Compile-time checking of template variables
3. **Powerful Features**: Conditionals, iterations, and transformations
4. **Proven Technology**: Battle-tested in ANTLR and other projects

### Template Benefits in Action

```java
// Without templates - messy and error-prone
String prompt = "Generate a weather forecast for " + city + 
                " for " + days + " days. Include temperature in " + 
                (useCelsius ? "Celsius" : "Fahrenheit") + 
                " and wind speed in " + (useMetric ? "km/h" : "mph");

// With templates - clean and maintainable
// weather-forecast.st
weatherForecast(city, days, units) ::= <<
Generate a detailed weather forecast for <city> covering the next <days> days.

Requirements:
- Temperature in <units.temperature>
- Wind speed in <units.windSpeed>
- Include precipitation probability
- Provide daily summaries

{format}
>>
```

## Why BeanOutputConverter?

### The Problem It Solves

```java
// Without BeanOutputConverter
String response = chatClient.prompt()
    .user("Get weather for Seattle")
    .call()
    .content();
// Now what? Parse JSON manually? Use regex? Hope for the best?

// With BeanOutputConverter
BeanOutputConverter<WeatherForecast> converter = new BeanOutputConverter<>(WeatherForecast.class);
WeatherForecast forecast = chatClient.prompt()
    .user(prompt)
    .call()
    .entity(converter);
// Type-safe, validated, ready to use!
```

### Why Records for Data Models?

```java
public record WeatherForecast(
    @JsonPropertyDescription("City name") String city,
    @JsonPropertyDescription("Temperature in specified units") Temperature temperature,
    @JsonPropertyDescription("Weather conditions") List<Condition> conditions
) {}
```

**Records are perfect because:**
- **Immutable**: AI responses shouldn't change after creation
- **Concise**: Less boilerplate, more clarity
- **Pattern Matching**: Modern Java features work seamlessly
- **Serialization**: Automatic support for JSON conversion

## The @JsonPropertyDescription Advantage

### Why Descriptions Matter

```java
// Without descriptions
public record Product(
    String name,
    double price,
    List<String> features
) {}
// AI has to guess what these fields mean

// With descriptions
public record Product(
    @JsonPropertyDescription("Product name for customer display") String name,
    @JsonPropertyDescription("Retail price in USD including taxes") double price,
    @JsonPropertyDescription("Key selling points and features") List<String> features
) {}
// AI knows exactly what to provide
```

**Benefits:**
- **Better Accuracy**: AI understands field purposes
- **Consistency**: Same interpretation every time
- **Documentation**: Self-documenting code
- **Validation**: Can specify constraints in descriptions

## Real-World Example Analysis

### Weather Forecast Service

```java
@Component
public class PromptTemplateService {
    private final STGroupFile stGroup = new STGroupFile("templates/weather-forecast.st");
    
    public WeatherForecast getWeatherForecast(String city) {
        // Why this pattern?
        ST template = stGroup.getInstanceOf("weatherForecast");
        template.add("city", city);
        
        // Why BeanOutputConverter?
        BeanOutputConverter<WeatherForecast> converter = new BeanOutputConverter<>(WeatherForecast.class);
        
        String prompt = template.render() + "\n\n" + converter.getFormat();
        
        return chatClient.prompt()
            .user(prompt)
            .call()
            .entity(converter);
    }
}
```

**Why this architecture?**
1. **Template Separation**: Templates can be modified without recompiling
2. **Converter Reuse**: Same converter works for all weather requests
3. **Type Safety**: Compile-time guarantee of WeatherForecast return type
4. **Testability**: Can test template rendering separately from AI calls

## When to Use Prompt Templates

### Use Templates When:
- **Multiple Variations**: Same concept, different parameters
- **Complex Prompts**: Multi-line instructions with conditions
- **Team Development**: Ensuring consistency across developers
- **A/B Testing**: Testing different prompt strategies
- **Internationalization**: Supporting multiple languages

### Skip Templates When:
- **Simple Queries**: One-line prompts without variables
- **Prototyping**: Quick experiments and PoCs
- **Dynamic Prompts**: Prompts generated programmatically

## Best Practices

### 1. Template Organization
```
templates/
├── weather-forecast.st
├── recipe-generator.st
└── sentiment-analysis.st
```
**Why?** Centralized management and easy updates

### 2. Variable Naming
```
weatherForecast(cityName, forecastDays, temperatureUnit) ::= <<
```
**Why?** Clear, descriptive names prevent confusion

### 3. Format Integration
```java
String prompt = template.render() + "\n\n" + converter.getFormat();
```
**Why?** Ensures AI knows the exact output structure expected

## Common Pitfalls and Solutions

### Pitfall 1: Hardcoded Strings in Prompts
```java
// Bad
String prompt = "Generate forecast for " + city + " in Celsius";

// Good
template.add("units", new Units("Celsius", "km/h"));
```

### Pitfall 2: Missing Format Instructions
```java
// Bad
return chatClient.prompt().user(template.render()).call().entity(converter);

// Good
return chatClient.prompt().user(template.render() + "\n\n" + converter.getFormat()).call().entity(converter);
```

## Key Takeaways

1. **Templates provide consistency**: Standardized prompts across your application
2. **BeanOutputConverter ensures type safety**: No more string parsing
3. **Descriptions improve accuracy**: Help AI understand your intent
4. **Separation of concerns**: Templates, logic, and models stay independent
5. **Testability improves**: Each component can be tested in isolation

## Next Steps

Now that we understand templates and basic converters, let's explore the fundamentals of structured output in [Section 3](s3-fundamentals.md), where we'll work with more complex data structures and lists.