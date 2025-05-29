# Section 11: Validation and Error Recovery Strategies

## Overview

This section demonstrates comprehensive validation and error recovery strategies for AI-generated structured output. It shows how to handle common issues like malformed JSON, validation errors, and missing data through automated recovery mechanisms.

## Key Concepts

### 1. **Multi-Layer Validation**
- **Parsing Validation**: Ensure AI output is valid JSON
- **Schema Validation**: Verify structure matches expected format
- **Constraint Validation**: Check business rules using Bean Validation
- **Semantic Validation**: Ensure data makes logical sense

### 2. **Recovery Strategies**

#### Parsing Recovery
- **JSON Extraction**: Extract JSON from surrounding text
- **Syntax Fixing**: Fix common JSON syntax errors
- **Format Correction**: Handle single quotes, trailing commas, unquoted keys

#### Validation Recovery
- **Auto-correction**: Fix simple format issues (e.g., order IDs, emails)
- **Default Values**: Apply sensible defaults for missing data
- **Partial Acceptance**: Accept valid parts, flag invalid ones

#### AI-Based Recovery
- **Targeted Regeneration**: Re-generate only invalid fields
- **Example-Based Prompting**: Provide format examples
- **Iterative Refinement**: Multiple attempts with feedback

### 3. **Bean Validation Integration**
Using Jakarta Bean Validation annotations:
```java
@NotBlank(message = "Order ID is required")
@Pattern(regexp = "^ORD-\\d{6}$", message = "Order ID must match pattern ORD-XXXXXX")
String orderId;
```

## Implementation Examples

### OrderRequest Model
Demonstrates comprehensive validation annotations:
- Pattern validation for order IDs
- Email format validation
- Date range validation
- Nested object validation
- Collection size constraints

### ValidationService
Core service implementing:
1. AI generation with structured output
2. Multi-stage validation
3. Automatic error recovery
4. Detailed result tracking

### Recovery Examples

#### JSON Extraction
```java
// Input: "Here is the order: {"id": "ORD-123456"} end"
// Extracted: {"id": "ORD-123456"}
```

#### Format Fixing
```java
// Input: "ORDER123"
// Fixed: "ORD-000123"
```

#### Email Correction
```java
// Input: "user at example com"
// Fixed: "user@example.com"
```

## API Endpoints

### 1. Validate Order
```bash
POST /api/validation/order
Content-Type: application/json

{
  "description": "Customer wants to order 3 laptops for $4500 total, ship to NYC office"
}
```
Generates an order from description and validates it with recovery.

### 2. Validate JSON
```bash
POST /api/validation/json
Content-Type: application/json

{'name': 'test', 'value': 123,}
```
Validates raw JSON with automatic syntax fixing.

### 3. Validation Strategies
```bash
GET /api/validation/strategies
```
Returns comprehensive validation and recovery strategies.

### 4. Examples
```bash
GET /api/validation/examples
```
Shows common validation errors and their fixes.

### 5. Metrics
```bash
GET /api/validation/metrics
```
Provides validation performance metrics.

## Validation Result Structure

```java
ValidationResult {
    boolean valid;
    LocalDateTime timestamp;
    List<ValidationError> errors;
    List<ValidationWarning> warnings;
    List<RecoveryAttempt> recoveryAttempts;
    Object finalOutput;
    Map<String, Object> metadata;
}
```

## Best Practices

### 1. **Preventive Measures**
- Use clear, specific prompts
- Provide format examples
- Include JSON schema in prompts
- Set appropriate temperature (0.3-0.5 for structured output)

### 2. **Defensive Validation**
- Always validate AI output
- Implement multiple validation layers
- Have fallback strategies
- Log failures for analysis

### 3. **Smart Recovery**
- Implement progressive recovery strategies
- Set maximum retry limits
- Track recovery success rates
- Provide clear error messages

### 4. **Error Categories**

#### High Priority (Auto-fix)
- Format issues (dates, IDs, emails)
- Missing required fields with defaults
- Simple syntax errors

#### Medium Priority (Retry)
- Structural issues
- Invalid value ranges
- Business rule violations

#### Low Priority (Manual)
- Semantic inconsistencies
- Complex business logic errors
- Ambiguous requirements

## Common Validation Patterns

### 1. **Order ID Validation**
```java
@Pattern(regexp = "^ORD-\\d{6}$")
// Fixes: "ORDER123" → "ORD-000123"
```

### 2. **Email Validation**
```java
@Email
// Fixes: "user at example.com" → "user@example.com"
```

### 3. **Date Validation**
```java
@PastOrPresent
// Fixes: Future dates → Today's date
```

### 4. **Amount Validation**
```java
@DecimalMin("0.01")
@DecimalMax("999999.99")
// Validates: Positive amounts within range
```

## Monitoring and Metrics

Track these key metrics:
1. **Validation Success Rate**: % of first-attempt successes
2. **Recovery Success Rate**: % of successful recoveries
3. **Common Error Types**: Distribution of error categories
4. **Performance Impact**: Time added by validation/recovery

## Testing Validation

1. **Unit Tests**: Test each validation rule
2. **Recovery Tests**: Verify recovery strategies
3. **Integration Tests**: End-to-end validation flows
4. **Edge Cases**: Malformed input, extreme values

## Future Enhancements

1. **Machine Learning**: Learn from validation patterns
2. **Custom Validators**: Domain-specific validation rules
3. **Async Validation**: Non-blocking validation for large payloads
4. **Validation Caching**: Cache validation results for similar inputs

This validation and recovery system ensures robust handling of AI-generated structured output, improving reliability and user experience.