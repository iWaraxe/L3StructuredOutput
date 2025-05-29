# S14: Performance and Optimization - Speed at Scale

## Why Performance Matters for AI Applications

The performance challenge with AI is unique:

```java
// Traditional API: Database query
long start = System.currentTimeMillis();
Product product = productRepository.findById(id); // 10ms
long duration = System.currentTimeMillis() - start;

// AI-powered API: Generate product description  
long start = System.currentTimeMillis();
ProductDescription desc = generateDescription(product); // 2000ms+
long duration = System.currentTimeMillis() - start;
```

**The 200x difference changes everything about how we architect systems.**

## Understanding the Performance Bottlenecks

### Token Economics

```java
@Service
public class TokenAnalyzer {
    
    public TokenMetrics analyzeUsage(String prompt, String response) {
        // Why analyze tokens?
        int promptTokens = tokenizer.countTokens(prompt);
        int responseTokens = tokenizer.countTokens(response);
        
        // Cost calculation - why this matters
        double promptCost = promptTokens * 0.00003;  // $0.03 per 1K tokens
        double responseCost = responseTokens * 0.00006; // $0.06 per 1K tokens
        
        // Time estimation - why latency matters
        int totalTokens = promptTokens + responseTokens;
        long estimatedLatency = calculateLatency(totalTokens);
        
        return new TokenMetrics(
            promptTokens,
            responseTokens,
            promptCost + responseCost,
            estimatedLatency,
            getOptimizationSuggestions(promptTokens, responseTokens)
        );
    }
    
    private List<String> getOptimizationSuggestions(int promptTokens, int responseTokens) {
        List<String> suggestions = new ArrayList<>();
        
        if (promptTokens > 1000) {
            suggestions.add("Consider prompt compression techniques");
        }
        
        if (responseTokens > 2000) {
            suggestions.add("Request more concise outputs");
        }
        
        if (promptTokens > responseTokens * 2) {
            suggestions.add("Prompt might be over-specified");
        }
        
        return suggestions;
    }
}
```

## Optimization Strategy #1: Intelligent Caching

### Multi-Level Cache Architecture

```java
@Configuration
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new CompositeCacheManager(
            localCacheManager(),    // L1: In-memory
            redisCacheManager(),    // L2: Distributed
            cdnCacheManager()       // L3: Edge cache
        );
    }
    
    @Bean
    public CacheManager localCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats()
        );
        return manager;
    }
}

@Service
public class CachedAIService {
    
    @Cacheable(value = "ai-responses", 
               key = "#request.generateCacheKey()",
               condition = "#request.isCacheable()",
               unless = "#result == null")
    public ProductDescription generateDescription(ProductRequest request) {
        return aiService.generate(request);
    }
    
    // Why custom cache key generation?
    public String generateCacheKey(ProductRequest request) {
        // Normalize request for better cache hits
        return DigestUtils.md5Hex(
            request.category().toLowerCase() + ":" +
            request.style().toLowerCase() + ":" +
            Math.round(request.priceRange() / 10) * 10  // Round to nearest 10
        );
    }
}
```

### Semantic Caching

```java
@Service
public class SemanticCacheService {
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    
    public Optional<String> findSimilarResponse(String prompt) {
        // Why embeddings for caching?
        float[] promptEmbedding = embeddingModel.embed(prompt);
        
        // Find semantically similar previous prompts
        List<SimilarityResult> similar = vectorStore.findSimilar(
            promptEmbedding, 
            0.95f,  // Why 95% similarity threshold?
            10      // Top 10 candidates
        );
        
        // Verify semantic match
        return similar.stream()
            .filter(result -> isSemanticMatch(prompt, result.getOriginalPrompt()))
            .map(SimilarityResult::getResponse)
            .findFirst();
    }
    
    private boolean isSemanticMatch(String prompt1, String prompt2) {
        // Why additional verification?
        // Embeddings can be fooled by surface similarity
        Set<String> keywords1 = extractKeywords(prompt1);
        Set<String> keywords2 = extractKeywords(prompt2);
        
        // Jaccard similarity for keyword overlap
        Set<String> intersection = new HashSet<>(keywords1);
        intersection.retainAll(keywords2);
        
        Set<String> union = new HashSet<>(keywords1);
        union.addAll(keywords2);
        
        double similarity = (double) intersection.size() / union.size();
        return similarity > 0.8;
    }
}
```

## Optimization Strategy #2: Batch Processing

### Dynamic Batch Optimization

```java
@Service
public class BatchOptimizationService {
    
    private static final int MIN_BATCH_SIZE = 5;
    private static final int MAX_BATCH_SIZE = 50;
    private static final int OPTIMAL_TOKENS_PER_REQUEST = 4000;
    
    public List<ProductDescription> processBatch(List<ProductRequest> requests) {
        // Why dynamic batch sizing?
        List<List<ProductRequest>> batches = createOptimalBatches(requests);
        
        return batches.parallelStream()
            .map(this::processSingleBatch)
            .flatMap(List::stream)
            .toList();
    }
    
    private List<List<ProductRequest>> createOptimalBatches(List<ProductRequest> requests) {
        List<List<ProductRequest>> batches = new ArrayList<>();
        List<ProductRequest> currentBatch = new ArrayList<>();
        int currentTokenCount = 0;
        
        for (ProductRequest request : requests) {
            int requestTokens = estimateTokens(request);
            
            // Why these conditions?
            if (!currentBatch.isEmpty() && 
                (currentBatch.size() >= MAX_BATCH_SIZE ||
                 currentTokenCount + requestTokens > OPTIMAL_TOKENS_PER_REQUEST)) {
                
                batches.add(new ArrayList<>(currentBatch));
                currentBatch.clear();
                currentTokenCount = 0;
            }
            
            currentBatch.add(request);
            currentTokenCount += requestTokens;
        }
        
        // Handle remaining
        if (!currentBatch.isEmpty()) {
            batches.add(currentBatch);
        }
        
        return batches;
    }
    
    private List<ProductDescription> processSingleBatch(List<ProductRequest> batch) {
        // Why combine into single prompt?
        String batchPrompt = createBatchPrompt(batch);
        
        var converter = new BeanOutputConverter<>(
            new ParameterizedTypeReference<List<ProductDescription>>() {}
        );
        
        // Single API call for entire batch
        return chatClient.prompt()
            .user(batchPrompt + "\n\n" + converter.getFormat())
            .call()
            .entity(converter);
    }
}
```

### Parallel Processing with Backpressure

```java
@Service
public class ParallelProcessingService {
    
    private final ExecutorService executorService;
    private final Semaphore rateLimiter;
    
    public ParallelProcessingService() {
        // Why these specific numbers?
        this.executorService = new ThreadPoolExecutor(
            10,     // Core pool size - baseline parallelism
            20,     // Max pool size - peak load handling
            60L, TimeUnit.SECONDS,  // Keep-alive for excess threads
            new LinkedBlockingQueue<>(100),  // Bounded queue
            new ThreadPoolExecutor.CallerRunsPolicy()  // Backpressure
        );
        
        // Why rate limiting?
        this.rateLimiter = new Semaphore(15);  // API rate limit
    }
    
    public CompletableFuture<List<Result>> processParallel(List<Request> requests) {
        List<CompletableFuture<Result>> futures = requests.stream()
            .map(request -> CompletableFuture.supplyAsync(() -> {
                try {
                    // Why acquire permit?
                    rateLimiter.acquire();
                    try {
                        return processRequest(request);
                    } finally {
                        rateLimiter.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }, executorService))
            .toList();
        
        // Why allOf instead of individual joins?
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .toList());
    }
}
```

## Optimization Strategy #3: Prompt Engineering

### Token-Efficient Prompts

```java
@Component
public class PromptOptimizer {
    
    public String optimizePrompt(String originalPrompt) {
        // Why these optimizations?
        
        // 1. Remove redundancy
        String optimized = removeRedundancy(originalPrompt);
        
        // 2. Use abbreviations
        optimized = applyAbbreviations(optimized);
        
        // 3. Compress instructions
        optimized = compressInstructions(optimized);
        
        // 4. Remove examples if too long
        if (countTokens(optimized) > 500) {
            optimized = removeExamples(optimized);
        }
        
        return optimized;
    }
    
    private String compressInstructions(String prompt) {
        Map<String, String> compressionRules = Map.of(
            "Please ensure that you", "Ensure",
            "Make sure to include", "Include",
            "It is important that", "Must",
            "You should generate", "Generate",
            "The response must contain", "Include"
        );
        
        String compressed = prompt;
        for (var entry : compressionRules.entrySet()) {
            compressed = compressed.replace(entry.getKey(), entry.getValue());
        }
        
        return compressed;
    }
    
    public String createEfficientBatchPrompt(List<String> items) {
        // Why JSON array format?
        // More token-efficient than natural language lists
        return String.format(
            "Process items: %s. Return array with same order.",
            objectMapper.writeValueAsString(items)
        );
    }
}
```

### Response Size Optimization

```java
@Service
public class ResponseOptimizationService {
    
    public <T> T getOptimizedResponse(Query query, Class<T> responseType) {
        // Why specify max tokens?
        int maxTokens = calculateOptimalTokens(responseType);
        
        var options = OpenAiChatOptions.builder()
            .withMaxTokens(maxTokens)
            .withTemperature(0.3f)  // Why lower temperature?
            .build();
        
        String prompt = buildPrompt(query) + 
                       "\n\nBe concise. Omit unnecessary details.";
        
        return chatModel.call(new Prompt(prompt, options))
            .getResult()
            .getOutput()
            .getEntity(responseType);
    }
    
    private int calculateOptimalTokens(Class<?> responseType) {
        // Why different limits for different types?
        return switch (responseType.getSimpleName()) {
            case "ProductSummary" -> 200;      // Brief descriptions
            case "ProductDetails" -> 500;       // Moderate detail
            case "ProductCatalog" -> 2000;      // Full catalog
            case "AnalysisReport" -> 3000;      // Comprehensive reports
            default -> 1000;                    // Safe default
        };
    }
}
```

## Optimization Strategy #4: Connection Pooling

### HTTP Client Optimization

```java
@Configuration
public class HttpClientConfig {
    
    @Bean
    public WebClient webClient() {
        // Why these specific settings?
        ConnectionProvider provider = ConnectionProvider.builder("ai-pool")
            .maxConnections(50)           // Total connections
            .maxIdleTime(Duration.ofSeconds(30))  // Reuse connections
            .maxLifeTime(Duration.ofMinutes(5))   // Prevent stale connections
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120))
            .build();
        
        HttpClient httpClient = HttpClient.create(provider)
            .responseTimeout(Duration.ofSeconds(30))  // API timeout
            .compress(true)  // Why compression?
            .keepAlive(true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(10 * 1024 * 1024))  // 10MB buffer
            .build();
    }
}
```

## Optimization Strategy #5: Monitoring and Profiling

### Performance Metrics Collection

```java
@Component
@Slf4j
public class PerformanceMonitor {
    private final MeterRegistry registry;
    
    @EventListener
    public void handleAIRequest(AIRequestEvent event) {
        Timer.Sample sample = Timer.start(registry);
        
        try {
            // Process request
            AIResponse response = processRequest(event.getRequest());
            
            // Why these specific metrics?
            registry.counter("ai.requests.total", 
                           "type", event.getType(),
                           "status", "success")
                   .increment();
            
            registry.gauge("ai.tokens.prompt.last", 
                         event.getPromptTokens());
            
            registry.gauge("ai.tokens.response.last", 
                         response.getTokenCount());
            
            // Why percentile histograms?
            registry.summary("ai.tokens.total")
                   .record(event.getPromptTokens() + response.getTokenCount());
            
        } catch (Exception e) {
            registry.counter("ai.requests.total",
                           "type", event.getType(),
                           "status", "error",
                           "error", e.getClass().getSimpleName())
                   .increment();
            throw e;
        } finally {
            sample.stop(registry.timer("ai.request.duration",
                                     "type", event.getType()));
        }
    }
    
    @Scheduled(fixedRate = 60000)  // Every minute
    public void logPerformanceStats() {
        // Why these specific stats?
        double avgResponseTime = registry.timer("ai.request.duration")
            .mean(TimeUnit.MILLISECONDS);
        
        double tokenCostPerMinute = calculateTokenCost();
        
        long cacheHitRate = calculateCacheHitRate();
        
        log.info("Performance stats - Avg response: {}ms, Cost/min: ${}, Cache hit: {}%",
                avgResponseTime, tokenCostPerMinute, cacheHitRate);
        
        // Why alert on thresholds?
        if (avgResponseTime > 3000) {
            alerting.sendAlert("High AI response time: " + avgResponseTime + "ms");
        }
        
        if (tokenCostPerMinute > 10) {
            alerting.sendAlert("High token cost: $" + tokenCostPerMinute + "/min");
        }
    }
}
```

## Real-World Performance Patterns

### The 80/20 Cache Pattern

```java
@Service
public class OptimizedProductService {
    
    // Why track popular items?
    private final LoadingCache<String, Integer> popularityCache = 
        Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(key -> 0);
    
    public ProductDescription getDescription(String productId) {
        // Track popularity
        popularityCache.put(productId, 
            popularityCache.get(productId) + 1);
        
        // Why different strategies?
        if (isPopular(productId)) {
            // Popular items: Aggressive caching, pre-generation
            return getFromCacheOrPreGenerated(productId);
        } else {
            // Long tail: Generate on demand, short cache
            return generateWithShortCache(productId);
        }
    }
    
    private boolean isPopular(String productId) {
        // Top 20% of products get 80% of views
        return popularityCache.get(productId) > 10;
    }
}
```

### Progressive Enhancement Pattern

```java
@RestController
public class ProgressiveAIController {
    
    @GetMapping("/product/{id}/description")
    public DeferredResult<ProductDescription> getDescription(@PathVariable String id) {
        DeferredResult<ProductDescription> result = new DeferredResult<>(30000L);
        
        // Why progressive enhancement?
        
        // 1. Return cached immediately if available
        getCached(id).ifPresent(result::setResult);
        
        if (!result.hasResult()) {
            // 2. Return basic version quickly
            CompletableFuture.runAsync(() -> {
                BasicDescription basic = generateBasic(id);
                if (!result.hasResult()) {
                    result.setResult(basic);
                }
                
                // 3. Generate enhanced version in background
                EnhancedDescription enhanced = generateEnhanced(id);
                cacheService.put(id, enhanced);
            });
        }
        
        return result;
    }
}
```

## Performance Best Practices

1. **Measure First, Optimize Second**
   ```java
   @Timed("product.generation")
   public Product generate() { /* ... */ }
   ```

2. **Cache Aggressively but Intelligently**
   - Cache by semantic similarity
   - Use multi-level caching
   - Track hit rates

3. **Batch When Possible**
   - Combine related requests
   - Optimize batch sizes
   - Handle partial failures

4. **Optimize Prompts**
   - Remove redundancy
   - Use compression techniques
   - Specify output size limits

5. **Monitor Everything**
   - Token usage
   - Response times
   - Cost per operation
   - Cache effectiveness

## Key Takeaways

1. **AI Performance is Different**: 100x slower than traditional APIs
2. **Caching is Critical**: Semantic caching can achieve 90%+ hit rates
3. **Batch Processing Saves Money**: 10x cost reduction possible
4. **Prompt Optimization Matters**: 50% token reduction achievable
5. **Monitor or Die**: Track metrics to avoid cost surprises
6. **Progressive Enhancement**: Serve fast, enhance in background

## Next Steps

With performance optimized, you're ready to deploy production systems using the patterns from [Section 16](s16-real-world.md).