# S11: Validation and Error Recovery - Building Resilient Systems

## Why Validation is Critical for AI Outputs

AI models are powerful but unpredictable. Even with structured output, things can go wrong:

```java
// What you expect
{
  "price": 99.99,
  "stock": 100
}

// What AI might return
{
  "price": "ninety-nine dollars and 99 cents",  // Wrong type
  "stock": -5  // Invalid value
}
```

**The fundamental truth:** Never trust AI output without validation.

## Multi-Layer Validation Strategy

### Layer 1: Schema Validation

```java
@Service
public class SchemaValidationService {
    private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance();
    
    public <T> ValidationResult validateAgainstSchema(String json, Class<T> targetClass) {
        // Why generate schema from class?
        JsonNode schemaNode = generateSchema(targetClass);
        JsonSchema schema = schemaFactory.getSchema(schemaNode);
        
        // Why validate before parsing?
        ProcessingReport report = schema.validate(parseJson(json));
        
        if (!report.isSuccess()) {
            List<String> errors = extractErrors(report);
            return ValidationResult.failure(errors);
        }
        
        return ValidationResult.success();
    }
}
```

**Why schema validation first?**
- Catches structural issues early
- Prevents parsing exceptions
- Provides clear error messages
- Works with any JSON

### Layer 2: Business Rules Validation

```java
@Component
public class BusinessRuleValidator {
    
    @Autowired
    private Validator validator;
    
    public <T> ValidationResult validateBusinessRules(T entity) {
        // Why JSR-303 validation?
        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        
        if (!violations.isEmpty()) {
            List<String> errors = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
            return ValidationResult.failure(errors);
        }
        
        // Why custom business rules?
        return validateCustomRules(entity);
    }
    
    private ValidationResult validateCustomRules(Object entity) {
        return switch (entity) {
            case Product product -> validateProduct(product);
            case Order order -> validateOrder(order);
            case Report report -> validateReport(report);
            default -> ValidationResult.success();
        };
    }
    
    private ValidationResult validateProduct(Product product) {
        List<String> errors = new ArrayList<>();
        
        // Why these specific checks?
        if (product.price().compareTo(product.cost()) < 0) {
            errors.add("Price cannot be less than cost");
        }
        
        if (product.stock() < 0 && !product.allowBackorder()) {
            errors.add("Negative stock requires backorder flag");
        }
        
        if (product.categories().isEmpty()) {
            errors.add("Product must have at least one category");
        }
        
        return errors.isEmpty() 
            ? ValidationResult.success() 
            : ValidationResult.failure(errors);
    }
}
```

### Layer 3: Semantic Validation

```java
public class SemanticValidator {
    
    public ValidationResult validateSemantics(ProductDescription description) {
        List<String> issues = new ArrayList<>();
        
        // Why check content quality?
        if (description.summary().length() < 50) {
            issues.add("Summary too short for SEO effectiveness");
        }
        
        // Why verify consistency?
        if (!description.features().stream()
                .allMatch(f -> description.detailedText().contains(f))) {
            issues.add("Features not mentioned in detailed description");
        }
        
        // Why check for AI hallucinations?
        if (containsHallucinations(description)) {
            issues.add("Description contains unrealistic claims");
        }
        
        return issues.isEmpty() 
            ? ValidationResult.success() 
            : ValidationResult.warning(issues);
    }
    
    private boolean containsHallucinations(ProductDescription desc) {
        List<String> unrealisticClaims = List.of(
            "revolutionary", "world's first", "100% guaranteed",
            "miracle", "breakthrough", "patent pending"
        );
        
        String text = desc.detailedText().toLowerCase();
        return unrealisticClaims.stream().anyMatch(text::contains);
    }
}
```

## Retry Strategies for AI Calls

### Exponential Backoff with Jitter

```java
@Service
public class ResilientAIService {
    
    private static final int MAX_RETRIES = 3;
    private static final Duration INITIAL_DELAY = Duration.ofSeconds(1);
    
    public <T> T callWithRetry(Supplier<T> aiCall, Class<T> responseType) {
        int attempt = 0;
        Duration delay = INITIAL_DELAY;
        
        while (attempt < MAX_RETRIES) {
            try {
                T response = aiCall.get();
                
                // Why validate on each attempt?
                if (isValid(response, responseType)) {
                    return response;
                }
                
                // Invalid response, retry
                logger.warn("Invalid response on attempt {}, retrying", attempt + 1);
                
            } catch (Exception e) {
                // Why different handling for different exceptions?
                if (!isRetryable(e)) {
                    throw new AIException("Non-retryable error", e);
                }
                
                logger.warn("Retryable error on attempt {}: {}", attempt + 1, e.getMessage());
            }
            
            if (++attempt < MAX_RETRIES) {
                // Why jitter?
                Duration jitteredDelay = addJitter(delay);
                sleep(jitteredDelay);
                delay = delay.multipliedBy(2); // Exponential backoff
            }
        }
        
        // Why return fallback instead of throwing?
        return getFallback(responseType);
    }
    
    private Duration addJitter(Duration baseDelay) {
        // Why 0.5 to 1.5x? Prevents thundering herd
        double jitterFactor = 0.5 + Math.random();
        return Duration.ofMillis((long)(baseDelay.toMillis() * jitterFactor));
    }
}
```

### Smart Retry with Modified Prompts

```java
public class SmartRetryService {
    
    public <T> T generateWithSmartRetry(String originalPrompt, Class<T> targetClass) {
        List<String> promptVariations = List.of(
            originalPrompt,
            enhancePromptWithExamples(originalPrompt),
            simplifyPrompt(originalPrompt),
            addExplicitConstraints(originalPrompt)
        );
        
        for (int i = 0; i < promptVariations.size(); i++) {
            try {
                String prompt = promptVariations.get(i);
                
                if (i > 0) {
                    logger.info("Trying alternative prompt strategy {}", i);
                }
                
                T result = callAI(prompt, targetClass);
                
                if (validateResult(result)) {
                    return result;
                }
                
            } catch (Exception e) {
                logger.warn("Attempt {} failed: {}", i + 1, e.getMessage());
            }
        }
        
        throw new AIGenerationException("All prompt strategies failed");
    }
    
    private String enhancePromptWithExamples(String prompt) {
        return prompt + "\n\nExample of expected format:\n" + getExampleOutput();
    }
    
    private String simplifyPrompt(String prompt) {
        // Remove complex instructions that might confuse the model
        return prompt.replaceAll("(?i)(ensure|make sure|remember to)", "")
                     .replaceAll("\\([^)]*\\)", ""); // Remove parenthetical comments
    }
    
    private String addExplicitConstraints(String prompt) {
        return prompt + "\n\nIMPORTANT: Return ONLY valid JSON. " +
                       "All numeric values must be numbers, not strings.";
    }
}
```

## Partial Result Recovery

### Extracting Value from Failed Responses

```java
@Service
public class PartialResultRecovery {
    
    public ProductCatalogResult recoverPartialResults(BatchGenerationRequest request) {
        List<Product> successfulProducts = new ArrayList<>();
        List<FailedProduct> failedProducts = new ArrayList<>();
        
        for (ProductRequest productReq : request.products()) {
            try {
                Product product = generateProduct(productReq);
                successfulProducts.add(product);
                
            } catch (ValidationException e) {
                // Why attempt partial recovery?
                Product partial = attemptPartialRecovery(productReq, e);
                
                if (partial != null) {
                    successfulProducts.add(partial);
                } else {
                    failedProducts.add(new FailedProduct(
                        productReq.id(),
                        e.getMessage(),
                        getSuggestedFix(e)
                    ));
                }
            }
        }
        
        // Why return both successful and failed?
        return new ProductCatalogResult(
            successfulProducts,
            failedProducts,
            calculateSuccessRate(successfulProducts.size(), request.products().size())
        );
    }
    
    private Product attemptPartialRecovery(ProductRequest request, ValidationException e) {
        // Why these recovery strategies?
        
        // Strategy 1: Use defaults for missing fields
        if (e.getType() == ValidationErrorType.MISSING_FIELD) {
            return createWithDefaults(request, e.getMissingFields());
        }
        
        // Strategy 2: Fix invalid values
        if (e.getType() == ValidationErrorType.INVALID_VALUE) {
            return fixInvalidValues(request, e.getInvalidFields());
        }
        
        // Strategy 3: Simplify and retry
        if (e.getType() == ValidationErrorType.COMPLEX_STRUCTURE) {
            return generateSimplified(request);
        }
        
        return null; // Unrecoverable
    }
}
```

## Error Classification and Handling

### Intelligent Error Response

```java
@Component
public class ErrorClassificationService {
    
    public ErrorResponse classifyAndHandle(Exception e, String context) {
        ErrorCategory category = classifyError(e);
        
        return switch (category) {
            case TRANSIENT -> handleTransientError(e, context);
            case VALIDATION -> handleValidationError(e, context);
            case RATE_LIMIT -> handleRateLimitError(e, context);
            case CONTENT_FILTER -> handleContentFilterError(e, context);
            case UNKNOWN -> handleUnknownError(e, context);
        };
    }
    
    private ErrorCategory classifyError(Exception e) {
        // Why pattern matching on exception details?
        String message = e.getMessage().toLowerCase();
        
        if (message.contains("rate limit") || message.contains("429")) {
            return ErrorCategory.RATE_LIMIT;
        }
        
        if (message.contains("content filter") || message.contains("policy")) {
            return ErrorCategory.CONTENT_FILTER;
        }
        
        if (e instanceof ValidationException) {
            return ErrorCategory.VALIDATION;
        }
        
        if (message.contains("timeout") || message.contains("connection")) {
            return ErrorCategory.TRANSIENT;
        }
        
        return ErrorCategory.UNKNOWN;
    }
    
    private ErrorResponse handleValidationError(Exception e, String context) {
        ValidationException ve = (ValidationException) e;
        
        return new ErrorResponse(
            ErrorCategory.VALIDATION,
            "The AI response didn't meet quality standards",
            getUserFriendlyMessage(ve),
            getDetailedErrors(ve),
            getSuggestedActions(ve),
            false // Not retryable without changes
        );
    }
}
```

## Quality Metrics and Monitoring

### Validation Success Tracking

```java
@Component
public class ValidationMetrics {
    private final MeterRegistry registry;
    
    public void recordValidation(ValidationResult result, String validationType) {
        // Why track by type?
        String metricName = "validation." + validationType;
        
        if (result.isSuccess()) {
            registry.counter(metricName + ".success").increment();
        } else {
            registry.counter(metricName + ".failure").increment();
            
            // Why track failure reasons?
            result.getErrors().forEach(error -> {
                String reason = categorizeError(error);
                registry.counter(metricName + ".failure.reason", "reason", reason)
                       .increment();
            });
        }
        
        // Why track validation duration?
        registry.timer(metricName + ".duration").record(result.getDuration());
    }
    
    public void recordRecovery(boolean successful, String recoveryType) {
        // Why track recovery effectiveness?
        registry.counter("recovery.attempt", 
                        "type", recoveryType,
                        "result", successful ? "success" : "failure")
               .increment();
    }
}
```

## Best Practices for Validation

### 1. Fail Fast, Recover Smart

```java
public class ValidationPipeline {
    
    public <T> T processWithValidation(String aiResponse, Class<T> targetClass) {
        // Fast syntax check
        if (!isValidJson(aiResponse)) {
            throw new ValidationException("Invalid JSON syntax");
        }
        
        // Schema validation before parsing
        ValidationResult schemaResult = validateSchema(aiResponse, targetClass);
        if (!schemaResult.isSuccess()) {
            // Try to fix common issues
            aiResponse = attemptSchemaFix(aiResponse, schemaResult);
        }
        
        // Parse to object
        T entity = parse(aiResponse, targetClass);
        
        // Business validation
        ValidationResult businessResult = validateBusiness(entity);
        if (!businessResult.isSuccess()) {
            // Apply corrections if possible
            entity = applyCorrections(entity, businessResult);
        }
        
        return entity;
    }
}
```

### 2. Context-Aware Validation

```java
public class ContextualValidator {
    
    public ValidationResult validate(Product product, ValidationContext context) {
        List<ValidationRule> rules = selectRules(product, context);
        
        return rules.stream()
            .map(rule -> rule.validate(product, context))
            .reduce(ValidationResult::merge)
            .orElse(ValidationResult.success());
    }
    
    private List<ValidationRule> selectRules(Product product, ValidationContext context) {
        List<ValidationRule> rules = new ArrayList<>(getBaseRules());
        
        // Why context-specific rules?
        if (context.isHighValue()) {
            rules.add(new HighValueProductRule());
        }
        
        if (context.isRegulated()) {
            rules.add(new RegulatoryComplianceRule());
        }
        
        if (context.isInternational()) {
            rules.add(new InternationalTradeRule());
        }
        
        return rules;
    }
}
```

## Key Takeaways

1. **Never Trust AI Output**: Always validate, no exceptions
2. **Layer Your Validation**: Schema → Business → Semantic
3. **Retry Intelligently**: Don't just repeat, adapt the approach
4. **Recover When Possible**: Partial success > complete failure
5. **Monitor Everything**: Track validation patterns and failures
6. **Context Matters**: Validation rules should adapt to use case
7. **Fail Fast, Fix Smart**: Early validation, intelligent recovery

## Next Steps

With robust validation in place, we can focus on performance optimization in [Section 14](s14-performance.md), ensuring our validated outputs are delivered quickly and efficiently.