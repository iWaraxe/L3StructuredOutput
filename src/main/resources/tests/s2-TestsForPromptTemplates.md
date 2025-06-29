## Testing the Implementation

To test this implementation, you can send POST requests to each endpoint with appropriate request bodies:

### Weather Forecast

```json
POST /api/ai/structured/weather
{
  "location": "New York City",
  "forecastType": "weekend"
}
```

### Recipe Generation

```json
POST /api/ai/structured/recipe
{
  "cuisine": "Italian",
  "dishType": "pasta",
  "ingredients": ["tomatoes", "garlic", "basil", "olive oil", "parmesan"],
  "dietaryRestrictions": "vegetarian"
}
```

### Sentiment Analysis

```json
POST /api/ai/structured/sentiment
{
  "text": "I was really disappointed with my recent online purchase. The product arrived damaged and customer service was unresponsive."
}
```

## Best Practices and Learning Points

This implementation demonstrates several best practices for working with Spring AI's prompt templates:

1. **Template Organization**: Store templates in resource files for better organization and maintenance.

2. **Utility Classes**: Create utilities for common template operations to reduce code duplication.

3. **Parameter Maps**: Use Map.of() for simple parameter sets and HashMap for more complex cases.

4. **Structured Models**: Define clear request and response models with proper annotations.

5. **Converter Usage**: Use BeanOutputConverter to handle structured output conversion consistently.

6. **Few-Shot Prompting**: Include examples in templates to guide the model's response format.

7. **Reusable Components**: Design components that can be reused across different templates and endpoints.

## Further Enhancements

Additional features you might consider adding to enhance this implementation:

1. **Template Caching**: Cache rendered templates for common parameter combinations.

2. **Error Handling**: Add robust error handling for format conversion issues.

3. **Template Validation**: Validate templates at startup to ensure all required placeholders are present.

4. **Dynamic Templates**: Load templates from a database or external source for runtime updates.

5. **Template Versioning**: Implement a versioning system for templates to track changes over time.

This implementation provides a solid foundation for understanding and working with Spring AI's PromptTemplate class, demonstrating how to effectively create reusable, parameterized prompts for different AI-powered features.