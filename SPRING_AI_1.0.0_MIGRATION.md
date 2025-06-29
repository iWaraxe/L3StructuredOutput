# Spring AI 1.0.0 Migration Guide

## Summary
This document outlines the changes made to migrate from Spring AI 1.0.0-M7 to Spring AI 1.0.0.

## Key Changes

### 1. Dependency Update
Updated `pom.xml`:
```xml
<spring-ai.version>1.0.0</spring-ai.version>
```

### 2. PromptTemplate API Changes
The `PromptTemplate` constructor that accepts two parameters (template string and parameters map) has been removed in Spring AI 1.0.0.

#### Before (1.0.0-M7):
```java
Prompt prompt = new PromptTemplate(promptTemplate, 
    Map.of("param1", value1, "param2", value2))
    .create();
```

#### After (1.0.0):
```java
PromptTemplate template = new PromptTemplate(promptTemplate);
String renderedPrompt = template.render(
    Map.of("param1", value1, "param2", value2));
Prompt prompt = new Prompt(renderedPrompt);
```

### 3. Files Updated
The following files were updated to use the new PromptTemplate API:
- `s7/FinancialAdviceController.java` - 2 occurrences
- `s7/FinancialAdviceService.java` - 1 occurrence
- `s7/WeatherChatModelController.java` - 1 occurrence
- `s7/WeatherService.java` - 2 occurrences

### 4. No Changes Required
The following components already use the correct API patterns:
- All `BeanOutputConverter`, `MapOutputConverter`, and `ListOutputConverter` usage
- ChatClient fluent API usage
- Files using single-parameter PromptTemplate constructor with render() method

## Migration Steps for Other Branches

To migrate other branches to Spring AI 1.0.0:

1. Update `pom.xml` to use Spring AI version 1.0.0
2. Search for `new PromptTemplate` with two parameters
3. Replace with the new pattern using `template.render()`
4. Run `./mvnw clean compile` to verify compilation
5. Run `./mvnw test` to ensure tests pass

## Testing
All tests pass successfully with Spring AI 1.0.0. The structured output functionality works as expected with the new version.