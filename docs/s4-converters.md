# S4: Converter Factory Pattern - Choosing the Right Tool

## Why Multiple Converter Types?

Imagine you're building a product information system. Different scenarios require different approaches:

```java
// Scenario 1: Structured product data
Product product = getProductDetails("SKU-123"); // Need BeanOutputConverter

// Scenario 2: Dynamic attributes from various sources
Map<String, Object> attributes = getProductAttributes("SKU-123"); // Need MapOutputConverter

// Scenario 3: Simple list of tags
List<String> tags = getProductTags("SKU-123"); // Need ListOutputConverter

// Scenario 4: Custom CSV format for legacy system
String csvData = getProductCSV("SKU-123"); // Need custom converter
```

**One size does NOT fit all!** Each converter serves a specific purpose.

## The Converter Ecosystem

### BeanOutputConverter - The Workhorse

```java
public <T> T demonstrateBeanConverter(Class<T> targetClass) {
    BeanOutputConverter<T> converter = new BeanOutputConverter<>(targetClass);
    return chatClient.prompt()
        .user(buildPrompt() + converter.getFormat())
        .call()
        .entity(converter);
}
```

**Why BeanOutputConverter is the default choice:**
- **Type Safety**: Compile-time guarantees
- **JSON Schema Generation**: Automatic schema from Java classes
- **Validation**: Built-in field validation
- **IDE Support**: Auto-completion and refactoring

**Use when:**
- You know the structure at compile time
- Need type safety and validation
- Working with domain models
- Building APIs with consistent contracts

### MapOutputConverter - The Flexible Friend

```java
public Map<String, Object> demonstrateMapConverter() {
    MapOutputConverter converter = new MapOutputConverter();
    return chatClient.prompt()
        .user("Extract product attributes as key-value pairs: " + converter.getFormat())
        .call()
        .entity(converter);
}
```

**Why MapOutputConverter exists:**
- **Dynamic Structures**: Unknown keys at compile time
- **Integration**: Working with external systems
- **Flexibility**: No predefined schema needed
- **Exploration**: Discovering data patterns

**Real-world example:**
```java
// E-commerce platform with dynamic product attributes
Map<String, Object> phoneSpecs = getProductAttributes("iPhone");
// Returns: {"storage": "128GB", "color": "Blue", "5G": true, "batteryLife": "20 hours"}

Map<String, Object> shirtSpecs = getProductAttributes("T-Shirt");
// Returns: {"size": "L", "material": "Cotton", "washable": "Machine wash cold"}
```

### ListOutputConverter - The Simple Solution

```java
public List<String> demonstrateListConverter() {
    ListOutputConverter converter = new ListOutputConverter(
        new DefaultConversionService()
    );
    return chatClient.prompt()
        .user("List 5 product categories\n" + converter.getFormat())
        .call()
        .entity(converter);
}
```

**Why ListOutputConverter is powerful in simplicity:**
- **Simplicity**: Just comma-separated values
- **Performance**: Minimal parsing overhead
- **Token Efficiency**: Compact format
- **Universal**: Works with any AI model

**Perfect for:**
```java
// Tags
List<String> tags = getTags("Article about Spring Boot");
// Returns: ["java", "spring-boot", "tutorial", "backend"]

// Categories
List<String> categories = getCategories("Nike Running Shoes");
// Returns: ["Footwear", "Sports", "Running", "Athletic"]

// Simple enumerations
List<String> colors = getAvailableColors("Product-123");
// Returns: ["Red", "Blue", "Green", "Black"]
```

## The Factory Pattern - Why?

### The Problem Without Factory

```java
// Without factory - repetitive and error-prone
public class ProductService {
    public Product getProduct(String id) {
        BeanOutputConverter<Product> converter = new BeanOutputConverter<>(Product.class);
        // ... use converter
    }
    
    public List<Product> getProducts() {
        BeanOutputConverter<List<Product>> converter = new BeanOutputConverter<>(
            new ParameterizedTypeReference<List<Product>>() {}
        );
        // ... use converter
    }
    
    public Map<String, Object> getAttributes() {
        MapOutputConverter converter = new MapOutputConverter();
        // ... use converter
    }
}
```

### The Factory Solution

```java
@Component
public class ConverterFactory {
    private final ChatClient chatClient;
    private final ConversionService conversionService;
    
    // Cached converters for performance
    private final Map<Class<?>, BeanOutputConverter<?>> beanConverters = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public <T> BeanOutputConverter<T> getBeanConverter(Class<T> targetClass) {
        return (BeanOutputConverter<T>) beanConverters.computeIfAbsent(
            targetClass, 
            clazz -> new BeanOutputConverter<>(targetClass)
        );
    }
}
```

**Benefits of the factory pattern:**
1. **Reusability**: Converters created once, used many times
2. **Performance**: Caching reduces object creation overhead
3. **Consistency**: Centralized configuration
4. **Maintainability**: Single place to modify converter behavior

## Custom Converters - When and Why

### AbstractConversionServiceOutputConverter

```java
public class CustomProductConverter extends AbstractConversionServiceOutputConverter {
    public CustomProductConverter(ConversionService conversionService) {
        super(conversionService);
    }
    
    @Override
    public String getFormat() {
        return """
            Generate product data in this exact format:
            PRODUCT_ID|PRODUCT_NAME|PRICE|CATEGORY|STOCK
            Use pipe (|) as delimiter. Price in USD. Stock as integer.
            """;
    }
    
    @Override
    public Product convert(String source) {
        String[] parts = source.split("\\|");
        return new Product(
            parts[0], // id
            parts[1], // name
            new BigDecimal(parts[2]), // price
            parts[3], // category
            Integer.parseInt(parts[4]) // stock
        );
    }
}
```

**Why create custom converters?**
- **Legacy Formats**: Integration with old systems
- **Performance**: Optimized parsing for specific formats
- **Special Requirements**: Unique business logic
- **External Standards**: Industry-specific formats

### AbstractMessageOutputConverter

```java
public class XMLProductConverter extends AbstractMessageOutputConverter {
    @Override
    public String getFormat() {
        return "Generate valid XML with root element <product>";
    }
    
    @Override
    protected Product doConvert(Message message) {
        String xml = message.getContent();
        // Custom XML parsing logic
        return parseXML(xml);
    }
}
```

**Use when:**
- Need access to full message metadata
- Handling multiple content types
- Complex parsing requirements
- Integration with message-based systems

## Choosing the Right Converter - Decision Tree

```
Start: What type of data do I need?
│
├─> Fixed structure? → BeanOutputConverter
│   └─> Single object? → BeanOutputConverter<T>
│   └─> Collection? → BeanOutputConverter<List<T>> with ParameterizedTypeReference
│
├─> Dynamic structure? → MapOutputConverter
│   └─> Unknown keys at compile time
│   └─> Integration with external APIs
│
├─> Simple list of strings? → ListOutputConverter
│   └─> Tags, categories, names
│   └─> Comma-separated values
│
└─> Special format? → Custom Converter
    ├─> Industry standard? → AbstractConversionServiceOutputConverter
    └─> Complex parsing? → AbstractMessageOutputConverter
```

## Real-World Patterns

### Pattern 1: Multi-Format API

```java
@RestController
public class ProductController {
    private final ConverterFactory converterFactory;
    
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable String id) {
        // Structured response for API
        return productService.getProduct(id, converterFactory.getBeanConverter(Product.class));
    }
    
    @GetMapping("/product/{id}/export")
    public String exportProduct(@PathVariable String id, @RequestParam String format) {
        // Flexible export formats
        return switch(format) {
            case "csv" -> productService.getProduct(id, new CSVProductConverter());
            case "xml" -> productService.getProduct(id, new XMLProductConverter());
            default -> throw new IllegalArgumentException("Unknown format: " + format);
        };
    }
}
```

### Pattern 2: Progressive Enhancement

```java
public class ProductAnalysisService {
    // Start simple
    public List<String> getProductTags(String description) {
        return extractTags(description, new ListOutputConverter());
    }
    
    // Add structure when needed
    public List<Tag> getStructuredTags(String description) {
        return extractTags(description, new BeanOutputConverter<>(
            new ParameterizedTypeReference<List<Tag>>() {}
        ));
    }
    
    // Full flexibility for advanced use cases
    public Map<String, Object> getCompleteAnalysis(String description) {
        return analyze(description, new MapOutputConverter());
    }
}
```

## Performance Considerations

### Converter Overhead Comparison

```java
// Fastest: ListOutputConverter
// Simple string split operation
List<String> tags = listConverter.convert("tag1,tag2,tag3");

// Moderate: BeanOutputConverter
// JSON parsing + object mapping
Product product = beanConverter.convert("{\"name\":\"...\",\"price\":...}");

// Flexible: MapOutputConverter
// Dynamic JSON parsing
Map<String, Object> attrs = mapConverter.convert("{\"dynamic\":\"data\"}");

// Variable: Custom Converters
// Depends on implementation
```

**Performance tips:**
1. **Cache Converters**: Reuse instances via factory
2. **Choose Wisely**: Simpler formats = better performance
3. **Batch Operations**: Process multiple items together
4. **Monitor Usage**: Track converter performance metrics

## Best Practices

### 1. Start Simple, Evolve as Needed
```java
// Version 1: Simple tags
List<String> tags = getProductTags(description);

// Version 2: Structured tags with confidence
List<TagWithConfidence> tags = getStructuredTags(description);

// Version 3: Full analysis
ProductAnalysis analysis = getCompleteAnalysis(description);
```

### 2. Encapsulate Converter Creation
```java
// Don't scatter converter creation
// Bad: new BeanOutputConverter<>(Product.class) everywhere

// Good: Centralized factory
converterFactory.getBeanConverter(Product.class)
```

### 3. Document Format Requirements
```java
public class ProductConverter extends AbstractConversionServiceOutputConverter {
    @Override
    public String getFormat() {
        return """
            Format: PRODUCT_ID|NAME|PRICE|STOCK
            Example: PROD-001|Laptop|999.99|50
            Rules:
            - Price must be numeric with up to 2 decimal places
            - Stock must be non-negative integer
            - Use pipe (|) as delimiter, no spaces
            """;
    }
}
```

## Key Takeaways

1. **Different converters for different needs**: Not everything is a bean
2. **BeanOutputConverter for structure**: When you know what you want
3. **MapOutputConverter for flexibility**: When structure varies
4. **ListOutputConverter for simplicity**: When you need just strings
5. **Custom converters for integration**: When standard formats don't fit
6. **Factory pattern for organization**: Centralize and optimize

## Next Steps

Now that we understand all converter types, let's explore advanced bean converter usage with complex nested structures in [Section 5](s5-advanced-bean.md).