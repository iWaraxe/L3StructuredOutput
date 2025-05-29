# Section 9: Property Ordering and Advanced Annotations

## Overview

This section demonstrates how to use Jackson annotations to control the structure and ordering of AI-generated JSON output. By leveraging annotations like `@JsonPropertyOrder`, `@JsonPropertyDescription`, and others, we can guide AI models to produce more consistent and well-structured responses.

## Key Concepts

### 1. **@JsonPropertyOrder**
Controls the order in which properties appear in the JSON output. This is crucial for:
- Maintaining consistency across AI responses
- Creating logical groupings of related fields
- Improving readability of generated data

### 2. **@JsonPropertyDescription**
Provides descriptions for each property that help the AI understand:
- The purpose and meaning of each field
- Expected data formats and constraints
- Relationships between different properties

### 3. **@JsonInclude**
Controls when properties are included in the output:
- `NON_NULL`: Exclude null values
- `NON_EMPTY`: Exclude empty collections and strings
- `NON_ZERO`: Exclude zero values for numbers

### 4. **@JsonProperty**
Customizes property names in JSON:
- Convert Java naming conventions to JSON conventions
- Use underscores or different casing as needed

### 5. **@JsonFormat**
Controls formatting of specific data types:
- Date/time formatting patterns
- Number formatting (decimal places, currency)
- Custom serialization shapes

## Implementation Examples

### UserProfile Model
```java
@JsonPropertyOrder({"userId", "username", "email", "fullName", "age", "preferences", "accountStatus", "lastLogin", "tags"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserProfile(
    @JsonPropertyDescription("Unique identifier for the user")
    @JsonProperty("user_id")
    String userId,
    // ... other fields
)
```

Key features:
- Explicit field ordering for consistent output
- Snake_case JSON property names
- Descriptive annotations guide AI generation
- `@JsonIgnore` for internal fields

### ProductCatalog Model
Demonstrates nested structures with:
- Multiple levels of property ordering
- Enum types for constrained values
- Conditional inclusion rules
- Complex nested records

## API Endpoints

### 1. Generate User Profile
```bash
GET /api/property-ordering/user-profile?description=Tech-savvy developer interested in AI
```
Generates a complete user profile with ordered fields based on the description.

### 2. Generate Product Catalog
```bash
GET /api/property-ordering/product-catalog?type=electronics&categories=5
```
Creates a complex catalog structure with nested categories and featured products.

### 3. View Schema
```bash
GET /api/property-ordering/schema/UserProfile
```
Shows the JSON schema generated from annotations that guides the AI model.

### 4. Demo Property Ordering
```bash
GET /api/property-ordering/demo/user-profile
```
Demonstrates how annotations affect the final JSON output structure.

### 5. Validate Property Order
```bash
POST /api/property-ordering/validate/UserProfile
Content-Type: application/json

{
  "user_id": "123",
  "username": "john",
  ...
}
```
Validates if provided JSON maintains the expected property order.

## Benefits of Property Ordering

1. **Consistency**: AI responses follow a predictable structure
2. **Readability**: Logical field ordering improves comprehension
3. **Documentation**: Annotations serve as inline documentation
4. **Validation**: Easier to validate AI-generated content
5. **Integration**: Consistent structure simplifies downstream processing

## Best Practices

1. **Group Related Fields**: Use @JsonPropertyOrder to group related properties together
2. **Use Descriptive Annotations**: Help the AI understand field purposes
3. **Handle Optional Fields**: Use @JsonInclude to control optional field inclusion
4. **Standardize Naming**: Use @JsonProperty for consistent JSON naming
5. **Document Constraints**: Use @JsonPropertyDescription to document value constraints

## Testing Property Ordering

The PropertyOrderingService includes methods to:
- Generate annotated schemas for AI consumption
- Validate that generated JSON maintains property order
- Compare annotated vs non-annotated output
- Demonstrate the impact of different annotation combinations

## Next Steps

This foundation of property ordering and annotations sets the stage for:
- Multi-model support (different AI providers)
- Advanced validation strategies
- Custom format providers
- Performance optimizations

By mastering these annotations, you can create more reliable and consistent AI-generated structured output.