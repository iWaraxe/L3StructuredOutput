package com.coherentsolutions.l3structuredoutput.s16.ecommerce;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Production-ready e-commerce product catalog generation service
 * demonstrating real-world usage of Spring AI structured output
 */
@Service
public class ProductCatalogService {

    private static final Logger logger = LoggerFactory.getLogger(ProductCatalogService.class);
    
    private final ChatClient chatClient;
    private final Map<String, ProductTemplate> templates = new ConcurrentHashMap<>();
    private final ProductValidator validator = new ProductValidator();
    private final PriceOptimizer priceOptimizer = new PriceOptimizer();
    
    public ProductCatalogService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        initializeTemplates();
    }
    
    /**
     * Generate complete product catalog with AI assistance
     */
    public CatalogGenerationResult generateCatalog(CatalogRequest request) {
        logger.info("Starting catalog generation for category: {}", request.category());
        
        long startTime = System.currentTimeMillis();
        List<ProductListing> products = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            // Generate products in batches for better performance
            List<CompletableFuture<List<ProductListing>>> futures = 
                    generateProductBatches(request);
            
            // Collect results
            for (CompletableFuture<List<ProductListing>> future : futures) {
                try {
                    List<ProductListing> batchProducts = future.join();
                    products.addAll(batchProducts);
                } catch (Exception e) {
                    logger.error("Error in batch generation", e);
                    errors.add("Batch generation failed: " + e.getMessage());
                }
            }
            
            // Apply business rules and optimizations
            products = applyBusinessRules(products, request);
            
            // Validate all products
            products = validateAndFilterProducts(products, errors);
            
            long duration = System.currentTimeMillis() - startTime;
            
            return new CatalogGenerationResult(
                    products,
                    products.size(),
                    errors,
                    duration,
                    generateCatalogMetadata(products, request)
            );
            
        } catch (Exception e) {
            logger.error("Critical error in catalog generation", e);
            throw new CatalogGenerationException("Failed to generate catalog", e);
        }
    }
    
    /**
     * Generate single product with AI enhancement
     */
    public ProductListing generateProduct(ProductRequest request) {
        logger.debug("Generating product: {}", request.name());
        
        BeanOutputConverter<AIGeneratedProduct> converter = 
                new BeanOutputConverter<>(AIGeneratedProduct.class);
        
        String prompt = buildProductPrompt(request);
        
        AIGeneratedProduct aiProduct = chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
        
        // Convert AI product to business product
        ProductListing product = enhanceProduct(aiProduct, request);
        
        // Validate and apply business rules
        product = validator.validate(product);
        product = priceOptimizer.optimizePrice(product, request.market());
        
        return product;
    }
    
    /**
     * Bulk product enhancement from existing data
     */
    public List<ProductListing> enhanceExistingProducts(List<BasicProduct> basicProducts) {
        logger.info("Enhancing {} existing products", basicProducts.size());
        
        return basicProducts.parallelStream()
                .map(this::enhanceBasicProduct)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Generate SEO-optimized product descriptions
     */
    public SEOOptimizedContent generateSEOContent(ProductListing product, SEOParameters seoParams) {
        BeanOutputConverter<SEOOptimizedContent> converter = 
                new BeanOutputConverter<>(SEOOptimizedContent.class);
        
        String prompt = String.format("""
                Generate SEO-optimized content for this product:
                Product: %s
                Category: %s
                Target Keywords: %s
                Competition Level: %s
                Target Audience: %s
                
                Focus on search engine optimization while maintaining natural language flow.
                """, 
                product.name(),
                product.category(),
                String.join(", ", seoParams.targetKeywords()),
                seoParams.competitionLevel(),
                seoParams.targetAudience()
        );
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    /**
     * Generate competitive analysis
     */
    public CompetitiveAnalysis analyzeCompetition(String category, List<String> competitorProducts) {
        BeanOutputConverter<CompetitiveAnalysis> converter = 
                new BeanOutputConverter<>(CompetitiveAnalysis.class);
        
        String prompt = String.format("""
                Analyze the competitive landscape for category: %s
                
                Competitor products:
                %s
                
                Provide insights on pricing, features, market positioning, and opportunities.
                """,
                category,
                String.join("\n", competitorProducts)
        );
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    // Private helper methods
    
    private List<CompletableFuture<List<ProductListing>>> generateProductBatches(CatalogRequest request) {
        int batchSize = calculateOptimalBatchSize(request.productCount());
        int batches = (int) Math.ceil((double) request.productCount() / batchSize);
        
        List<CompletableFuture<List<ProductListing>>> futures = new ArrayList<>();
        
        for (int i = 0; i < batches; i++) {
            final int batchIndex = i;
            final int startIndex = i * batchSize;
            final int endIndex = Math.min(startIndex + batchSize, request.productCount());
            
            CompletableFuture<List<ProductListing>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return generateProductBatch(request, batchIndex, startIndex, endIndex);
                } catch (Exception e) {
                    logger.error("Error in batch {} generation", batchIndex, e);
                    return Collections.emptyList();
                }
            });
            
            futures.add(future);
        }
        
        return futures;
    }
    
    private List<ProductListing> generateProductBatch(CatalogRequest request, int batchIndex, 
                                                      int startIndex, int endIndex) {
        logger.debug("Generating batch {} (products {}-{})", batchIndex, startIndex, endIndex - 1);
        
        BeanOutputConverter<ProductBatch> converter = new BeanOutputConverter<>(ProductBatch.class);
        
        String prompt = String.format("""
                Generate %d unique products for category: %s
                Price range: $%.2f - $%.2f
                Target market: %s
                Brand style: %s
                Batch index: %d
                
                Ensure each product is unique and commercially viable.
                """,
                endIndex - startIndex,
                request.category(),
                request.minPrice(),
                request.maxPrice(),
                request.targetMarket(),
                request.brandStyle(),
                batchIndex
        );
        
        ProductBatch batch = chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
        
        return batch.products().stream()
                .map(aiProduct -> enhanceProduct(aiProduct, 
                        new ProductRequest(aiProduct.name(), request.category(), request.targetMarket())))
                .collect(Collectors.toList());
    }
    
    private ProductListing enhanceProduct(AIGeneratedProduct aiProduct, ProductRequest request) {
        // Apply business logic and enhancements
        String enhancedName = enhanceProductName(aiProduct.name(), request.category());
        String enhancedDescription = enhanceDescription(aiProduct.description());
        BigDecimal optimizedPrice = optimizePrice(aiProduct.price(), request.market());
        
        return new ProductListing(
                generateProductId(),
                enhancedName,
                enhancedDescription,
                optimizedPrice,
                request.category(),
                aiProduct.features(),
                aiProduct.specifications(),
                generateTags(aiProduct),
                ProductStatus.DRAFT,
                LocalDateTime.now(),
                calculateInventory(aiProduct.price()),
                generateImageUrls(enhancedName),
                generateSEOMetadata(enhancedName, enhancedDescription)
        );
    }
    
    private ProductListing enhanceBasicProduct(BasicProduct basic) {
        try {
            ProductRequest request = new ProductRequest(basic.name(), basic.category(), "general");
            return generateProduct(request);
        } catch (Exception e) {
            logger.error("Failed to enhance product: {}", basic.name(), e);
            return null;
        }
    }
    
    private List<ProductListing> applyBusinessRules(List<ProductListing> products, CatalogRequest request) {
        return products.stream()
                .map(product -> applyPricingRules(product, request))
                .map(this::applyCategoryRules)
                .map(this::applyInventoryRules)
                .collect(Collectors.toList());
    }
    
    private List<ProductListing> validateAndFilterProducts(List<ProductListing> products, List<String> errors) {
        return products.stream()
                .filter(product -> {
                    try {
                        validator.validate(product);
                        return true;
                    } catch (Exception e) {
                        errors.add("Product validation failed for " + product.name() + ": " + e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }
    
    private void initializeTemplates() {
        templates.put("electronics", new ProductTemplate("electronics", 
                "High-tech gadgets and devices", 50.0, 2000.0));
        templates.put("clothing", new ProductTemplate("clothing", 
                "Fashion and apparel items", 15.0, 500.0));
        templates.put("home", new ProductTemplate("home", 
                "Home and garden products", 25.0, 1000.0));
        templates.put("books", new ProductTemplate("books", 
                "Books and educational materials", 5.0, 100.0));
    }
    
    private String buildProductPrompt(ProductRequest request) {
        ProductTemplate template = templates.get(request.category().toLowerCase());
        if (template == null) {
            template = new ProductTemplate("general", "General product", 10.0, 100.0);
        }
        
        return String.format("""
                Create a detailed product listing for:
                Name: %s
                Category: %s
                Target Market: %s
                Price Range: $%.2f - $%.2f
                
                Include comprehensive features, specifications, and marketing copy.
                Ensure the product is commercially viable and appealing to the target market.
                """,
                request.name(),
                request.category(),
                request.market(),
                template.minPrice(),
                template.maxPrice()
        );
    }
    
    // Business logic helper methods
    
    private String enhanceProductName(String name, String category) {
        // Apply naming conventions and SEO optimization
        return name.trim().replaceAll("\\s+", " ");
    }
    
    private String enhanceDescription(String description) {
        // Apply marketing enhancements and SEO optimization
        return description;
    }
    
    private BigDecimal optimizePrice(BigDecimal basePrice, String market) {
        // Apply market-specific pricing strategies
        return priceOptimizer.optimize(basePrice, market);
    }
    
    private ProductListing applyPricingRules(ProductListing product, CatalogRequest request) {
        BigDecimal adjustedPrice = product.price();
        
        // Apply bulk discount for large catalogs
        if (request.productCount() > 100) {
            adjustedPrice = adjustedPrice.multiply(BigDecimal.valueOf(0.95));
        }
        
        return product.withPrice(adjustedPrice);
    }
    
    private ProductListing applyCategoryRules(ProductListing product) {
        // Apply category-specific business rules
        return product;
    }
    
    private ProductListing applyInventoryRules(ProductListing product) {
        // Apply inventory management rules
        return product;
    }
    
    private String generateProductId() {
        return "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private List<String> generateTags(AIGeneratedProduct product) {
        List<String> tags = new ArrayList<>();
        tags.addAll(product.features().stream()
                .map(String::toLowerCase)
                .map(feature -> feature.replaceAll("\\s+", "-"))
                .collect(Collectors.toList()));
        return tags;
    }
    
    private int calculateInventory(BigDecimal price) {
        // Calculate initial inventory based on price point
        if (price.compareTo(BigDecimal.valueOf(100)) > 0) {
            return 50;
        } else if (price.compareTo(BigDecimal.valueOf(50)) > 0) {
            return 100;
        } else {
            return 200;
        }
    }
    
    private List<String> generateImageUrls(String productName) {
        // Generate placeholder image URLs
        String slug = productName.toLowerCase().replaceAll("\\s+", "-");
        return List.of(
                "https://images.example.com/products/" + slug + "-main.jpg",
                "https://images.example.com/products/" + slug + "-detail1.jpg",
                "https://images.example.com/products/" + slug + "-detail2.jpg"
        );
    }
    
    private Map<String, String> generateSEOMetadata(String name, String description) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("title", name + " - Best Quality & Price");
        metadata.put("description", description.substring(0, Math.min(160, description.length())));
        metadata.put("keywords", String.join(", ", name.split("\\s+")));
        return metadata;
    }
    
    private CatalogMetadata generateCatalogMetadata(List<ProductListing> products, CatalogRequest request) {
        double avgPrice = products.stream()
                .mapToDouble(p -> p.price().doubleValue())
                .average()
                .orElse(0.0);
        
        Map<String, Long> categoryDistribution = products.stream()
                .collect(Collectors.groupingBy(
                        ProductListing::category,
                        Collectors.counting()
                ));
        
        return new CatalogMetadata(
                request.category(),
                products.size(),
                BigDecimal.valueOf(avgPrice),
                categoryDistribution,
                LocalDateTime.now()
        );
    }
    
    private int calculateOptimalBatchSize(int totalProducts) {
        if (totalProducts <= 10) return totalProducts;
        if (totalProducts <= 50) return 5;
        if (totalProducts <= 100) return 10;
        return 20;
    }
    
    // Inner classes and records
    
    public record CatalogRequest(
            String category,
            int productCount,
            double minPrice,
            double maxPrice,
            String targetMarket,
            String brandStyle
    ) {}
    
    public record ProductRequest(
            String name,
            String category,
            String market
    ) {}
    
    public record BasicProduct(
            String name,
            String category,
            BigDecimal price
    ) {}
    
    public record SEOParameters(
            List<String> targetKeywords,
            String competitionLevel,
            String targetAudience
    ) {}
    
    public record AIGeneratedProduct(
            @JsonPropertyDescription("Product name") String name,
            @JsonPropertyDescription("Detailed product description") String description,
            @JsonPropertyDescription("Product price in USD") BigDecimal price,
            @JsonPropertyDescription("List of key product features") List<String> features,
            @JsonPropertyDescription("Technical specifications") Map<String, String> specifications
    ) {}
    
    public record ProductBatch(
            @JsonPropertyDescription("List of generated products") List<AIGeneratedProduct> products
    ) {}
    
    public record ProductListing(
            String id,
            String name,
            String description,
            BigDecimal price,
            String category,
            List<String> features,
            Map<String, String> specifications,
            List<String> tags,
            ProductStatus status,
            LocalDateTime createdAt,
            int inventory,
            List<String> imageUrls,
            Map<String, String> seoMetadata
    ) {
        public ProductListing withPrice(BigDecimal newPrice) {
            return new ProductListing(id, name, description, newPrice, category, features,
                    specifications, tags, status, createdAt, inventory, imageUrls, seoMetadata);
        }
    }
    
    public record SEOOptimizedContent(
            @JsonPropertyDescription("SEO-optimized title") String title,
            @JsonPropertyDescription("Meta description for search engines") String metaDescription,
            @JsonPropertyDescription("Primary keywords") List<String> keywords,
            @JsonPropertyDescription("Optimized product description") String optimizedDescription,
            @JsonPropertyDescription("SEO-friendly URL slug") String urlSlug
    ) {}
    
    public record CompetitiveAnalysis(
            @JsonPropertyDescription("Market positioning insights") String positioning,
            @JsonPropertyDescription("Pricing recommendations") String pricingStrategy,
            @JsonPropertyDescription("Feature gaps and opportunities") List<String> opportunities,
            @JsonPropertyDescription("Competitive advantages") List<String> advantages,
            @JsonPropertyDescription("Market share estimation") String marketShare
    ) {}
    
    public record CatalogGenerationResult(
            List<ProductListing> products,
            int totalGenerated,
            List<String> errors,
            long generationTimeMs,
            CatalogMetadata metadata
    ) {}
    
    public record CatalogMetadata(
            String category,
            int productCount,
            BigDecimal averagePrice,
            Map<String, Long> categoryDistribution,
            LocalDateTime generatedAt
    ) {}
    
    public record ProductTemplate(
            String category,
            String description,
            double minPrice,
            double maxPrice
    ) {}
    
    public enum ProductStatus {
        DRAFT, PENDING_REVIEW, APPROVED, PUBLISHED, ARCHIVED
    }
    
    // Helper classes
    
    private static class ProductValidator {
        public ProductListing validate(ProductListing product) {
            if (product.name() == null || product.name().trim().isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty");
            }
            if (product.price() == null || product.price().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Product price must be positive");
            }
            if (product.description() == null || product.description().length() < 10) {
                throw new IllegalArgumentException("Product description must be at least 10 characters");
            }
            return product;
        }
    }
    
    private static class PriceOptimizer {
        public ProductListing optimizePrice(ProductListing product, String market) {
            BigDecimal optimizedPrice = optimize(product.price(), market);
            return product.withPrice(optimizedPrice);
        }
        
        public BigDecimal optimize(BigDecimal basePrice, String market) {
            // Apply market-specific pricing strategies
            return switch (market.toLowerCase()) {
                case "premium" -> basePrice.multiply(BigDecimal.valueOf(1.2));
                case "budget" -> basePrice.multiply(BigDecimal.valueOf(0.8));
                case "luxury" -> basePrice.multiply(BigDecimal.valueOf(1.5));
                default -> basePrice;
            };
        }
    }
    
    public static class CatalogGenerationException extends RuntimeException {
        public CatalogGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}