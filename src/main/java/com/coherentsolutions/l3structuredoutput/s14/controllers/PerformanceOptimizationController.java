package com.coherentsolutions.l3structuredoutput.s14.controllers;

import com.coherentsolutions.l3structuredoutput.s14.batch.BatchProcessingService;
import com.coherentsolutions.l3structuredoutput.s14.cache.StructuredOutputCache;
import com.coherentsolutions.l3structuredoutput.s14.memory.MemoryEfficientService;
import com.coherentsolutions.l3structuredoutput.s14.optimization.TokenOptimizationService;
import com.coherentsolutions.l3structuredoutput.s14.parallel.ParallelProcessingService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/performance")
public class PerformanceOptimizationController {

    private final StructuredOutputCache cache;
    private final BatchProcessingService batchService;
    private final TokenOptimizationService tokenService;
    private final ParallelProcessingService parallelService;
    private final MemoryEfficientService memoryService;

    public PerformanceOptimizationController(
            StructuredOutputCache cache,
            BatchProcessingService batchService,
            TokenOptimizationService tokenService,
            ParallelProcessingService parallelService,
            MemoryEfficientService memoryService) {
        
        this.cache = cache;
        this.batchService = batchService;
        this.tokenService = tokenService;
        this.parallelService = parallelService;
        this.memoryService = memoryService;
    }

    /**
     * Demonstrate cache performance
     */
    @GetMapping("/cache/demo")
    public CacheDemo demonstrateCache(@RequestParam(defaultValue = "test-key") String key) {
        // First call - cache miss
        long startMiss = System.currentTimeMillis();
        Optional<String> missResult = cache.get(key, String.class);
        long missDuration = System.currentTimeMillis() - startMiss;
        
        // Store in cache
        String value = "Generated value at " + System.currentTimeMillis();
        cache.put(key, value);
        
        // Second call - cache hit
        long startHit = System.currentTimeMillis();
        Optional<String> hitResult = cache.get(key, String.class);
        long hitDuration = System.currentTimeMillis() - startHit;
        
        return new CacheDemo(
                missResult.isEmpty(),
                hitResult.isPresent(),
                missDuration,
                hitDuration,
                hitDuration > 0 ? (double) missDuration / hitDuration : 0,
                cache.getStats()
        );
    }

    /**
     * Get cache statistics
     */
    @GetMapping("/cache/stats")
    public StructuredOutputCache.CacheStats getCacheStats() {
        return cache.getStats();
    }

    /**
     * Clear cache
     */
    @DeleteMapping("/cache/clear")
    public Map<String, Object> clearCache() {
        StructuredOutputCache.CacheStats beforeStats = cache.getStats();
        cache.clear();
        
        return Map.of(
                "cleared", true,
                "itemsCleared", beforeStats.size(),
                "statsBeforeClear", beforeStats
        );
    }

    /**
     * Batch processing example
     */
    @PostMapping("/batch/process")
    public BatchProcessingService.BatchResult<AnalysisResult> processBatch(
            @RequestBody BatchRequest request) {
        
        List<String> items = IntStream.range(0, request.itemCount())
                .mapToObj(i -> "Item " + i + ": " + request.basePrompt())
                .collect(Collectors.toList());
        
        return batchService.processBatch(
                items,
                batch -> String.format("Analyze these items:\n%s", String.join("\n", batch)),
                AnalysisResult.class,
                request.batchSize()
        );
    }

    /**
     * Token optimization demonstration
     */
    @PostMapping("/tokens/optimize")
    public TokenOptimizationService.OptimizedPrompt optimizePrompt(
            @RequestBody TokenOptimizationRequest request) {
        
        return tokenService.optimizePrompt(
                request.prompt(),
                request.level()
        );
    }

    /**
     * Estimate tokens for text
     */
    @PostMapping("/tokens/estimate")
    public Map<String, Object> estimateTokens(@RequestBody String text) {
        int tokens = tokenService.estimateTokens(text);
        
        return Map.of(
                "text", text,
                "estimatedTokens", tokens,
                "characterCount", text.length(),
                "avgCharsPerToken", text.isEmpty() ? 0 : (double) text.length() / tokens
        );
    }

    /**
     * Parallel processing example
     */
    @PostMapping("/parallel/process")
    public ParallelProcessingService.ParallelResult<SummaryResult> processParallel(
            @RequestBody ParallelRequest request) {
        
        List<String> documents = IntStream.range(0, request.documentCount())
                .mapToObj(i -> "Document " + i + ": " + generateRandomText(request.documentSize()))
                .collect(Collectors.toList());
        
        return parallelService.processParallel(
                documents,
                doc -> "Summarize this document: " + doc,
                SummaryResult.class,
                ParallelProcessingService.ProcessingOptions.defaults()
        );
    }

    /**
     * Map-reduce example
     */
    @PostMapping("/parallel/map-reduce")
    public Map<String, Object> mapReduceExample(@RequestBody MapReduceRequest request) {
        List<String> items = IntStream.range(0, request.itemCount())
                .mapToObj(i -> "Analyze sentiment of: " + request.texts().get(i % request.texts().size()))
                .collect(Collectors.toList());
        
        Double averageSentiment = parallelService.mapReduce(
                items,
                item -> item,
                SentimentResult.class,
                result -> result.score(),
                Double::sum,
                0.0
        );
        
        return Map.of(
                "itemsProcessed", items.size(),
                "totalSentimentScore", averageSentiment,
                "averageSentiment", items.isEmpty() ? 0 : averageSentiment / items.size()
        );
    }

    /**
     * Memory-efficient streaming example
     */
    @PostMapping("/memory/stream")
    public Map<String, Object> streamLargeDataset(@RequestParam(defaultValue = "1000") int itemCount) {
        try {
            Stream<String> dataStream = IntStream.range(0, itemCount)
                    .mapToObj(i -> "Process item " + i);
            
            MemoryEfficientService.StreamingResult result = memoryService.processLargeDataset(
                    dataStream,
                    item -> "Analyze: " + item,
                    SimpleResult.class,
                    MemoryEfficientService.StreamingOptions.defaults()
            );
            
            // Read file size
            long fileSize = Files.size(result.outputFile());
            
            // Clean up temp file
            Files.deleteIfExists(result.outputFile());
            
            return Map.of(
                    "itemsProcessed", result.itemsProcessed(),
                    "bytesProcessed", result.bytesProcessed(),
                    "outputFileSize", fileSize,
                    "compressionRatio", result.compressionRatio(),
                    "avgBytesPerItem", result.itemsProcessed() > 0 ? 
                            result.bytesProcessed() / result.itemsProcessed() : 0
            );
            
        } catch (IOException e) {
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * Get current memory usage
     */
    @GetMapping("/memory/stats")
    public MemoryEfficientService.MemoryInfo getMemoryStats() {
        return MemoryEfficientService.MemoryStats.getCurrentMemoryInfo();
    }

    /**
     * Performance comparison endpoint
     */
    @PostMapping("/compare")
    public CompletableFuture<PerformanceComparison> compareApproaches(
            @RequestParam(defaultValue = "10") int itemCount) {
        
        List<String> items = IntStream.range(0, itemCount)
                .mapToObj(i -> "Analyze item " + i)
                .collect(Collectors.toList());
        
        return CompletableFuture.supplyAsync(() -> {
            // Sequential processing
            long sequentialStart = System.currentTimeMillis();
            List<SimpleResult> sequentialResults = new ArrayList<>();
            for (String item : items) {
                // Simulate processing
                sequentialResults.add(new SimpleResult(item, "processed"));
            }
            long sequentialDuration = System.currentTimeMillis() - sequentialStart;
            
            // Batch processing
            long batchStart = System.currentTimeMillis();
            BatchProcessingService.BatchResult<SimpleResult> batchResult = batchService.processBatch(
                    items,
                    batch -> String.join("\n", batch),
                    SimpleResult.class,
                    5
            );
            long batchDuration = System.currentTimeMillis() - batchStart;
            
            // Parallel processing
            long parallelStart = System.currentTimeMillis();
            ParallelProcessingService.ParallelResult<SimpleResult> parallelResult = 
                    parallelService.processParallel(
                            items,
                            item -> item,
                            SimpleResult.class,
                            ParallelProcessingService.ProcessingOptions.defaults()
                    );
            long parallelDuration = System.currentTimeMillis() - parallelStart;
            
            return new PerformanceComparison(
                    itemCount,
                    new ApproachResult("Sequential", sequentialDuration, itemCount, 
                            calculateThroughput(itemCount, sequentialDuration)),
                    new ApproachResult("Batch", batchDuration, batchResult.successfulResults(), 
                            batchResult.requestsPerSecond()),
                    new ApproachResult("Parallel", parallelDuration, parallelResult.successfulRequests(), 
                            parallelResult.throughput()),
                    cache.getStats()
            );
        });
    }

    // Helper methods

    private String generateRandomText(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> "word" + i)
                .collect(Collectors.joining(" "));
    }

    private double calculateThroughput(int items, long durationMs) {
        return durationMs == 0 ? 0 : (items * 1000.0) / durationMs;
    }

    // Request/Response DTOs

    public record CacheDemo(
            boolean firstCallMiss,
            boolean secondCallHit,
            long missDurationMs,
            long hitDurationMs,
            double speedupFactor,
            StructuredOutputCache.CacheStats stats
    ) {}

    public record BatchRequest(
            String basePrompt,
            int itemCount,
            int batchSize
    ) {
        public BatchRequest {
            if (itemCount <= 0) itemCount = 10;
            if (batchSize <= 0) batchSize = 5;
        }
    }

    public record TokenOptimizationRequest(
            String prompt,
            TokenOptimizationService.OptimizationLevel level
    ) {}

    public record ParallelRequest(
            int documentCount,
            int documentSize
    ) {
        public ParallelRequest {
            if (documentCount <= 0) documentCount = 10;
            if (documentSize <= 0) documentSize = 100;
        }
    }

    public record MapReduceRequest(
            List<String> texts,
            int itemCount
    ) {}

    public record AnalysisResult(
            String id,
            String analysis,
            double score
    ) {}

    public record SummaryResult(
            String summary,
            int wordCount,
            List<String> keyPoints
    ) {}

    public record SentimentResult(
            String text,
            double score,
            String sentiment
    ) {}

    public record SimpleResult(
            String input,
            String output
    ) {}

    public record PerformanceComparison(
            int totalItems,
            ApproachResult sequential,
            ApproachResult batch,
            ApproachResult parallel,
            StructuredOutputCache.CacheStats cacheStats
    ) {}

    public record ApproachResult(
            String approach,
            long durationMs,
            int itemsProcessed,
            double throughput
    ) {}
}