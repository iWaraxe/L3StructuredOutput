package com.coherentsolutions.l3structuredoutput.s14;

import com.coherentsolutions.l3structuredoutput.s14.cache.StructuredOutputCache;
import com.coherentsolutions.l3structuredoutput.s14.optimization.TokenOptimizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceTest {

    private StructuredOutputCache cache;
    private TokenOptimizationService tokenService;

    @BeforeEach
    void setUp() {
        cache = new StructuredOutputCache();
        tokenService = new TokenOptimizationService();
    }

    @Test
    @Timeout(5)
    void cache_ShouldImprovePerformance() {
        String key = "test-key";
        String value = "test-value";
        
        // Measure cache miss
        long missDuration = measureDuration(() -> cache.get(key, String.class));
        
        // Put value in cache
        cache.put(key, value);
        
        // Measure cache hit
        long hitDuration = measureDuration(() -> cache.get(key, String.class));
        
        // Cache hit should be significantly faster
        assertThat(hitDuration).isLessThan(missDuration);
        
        // Verify cache stats
        StructuredOutputCache.CacheStats stats = cache.getStats();
        assertThat(stats.hits()).isEqualTo(1);
        assertThat(stats.misses()).isEqualTo(1);
        assertThat(stats.size()).isEqualTo(1);
    }

    @Test
    void cache_ShouldHandleConcurrentAccess() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "key-" + (threadId * operationsPerThread + j) % 50;
                        cache.computeIfAbsent(key, () -> "value-" + key, String.class);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();
        
        StructuredOutputCache.CacheStats stats = cache.getStats();
        assertThat(stats.size()).isLessThanOrEqualTo(50);
        assertThat(stats.hits() + stats.misses()).isEqualTo(threadCount * operationsPerThread);
    }

    @Test
    void cache_ShouldEvictExpiredEntries() throws InterruptedException {
        String key = "expire-key";
        Duration ttl = Duration.ofMillis(100);
        
        cache.put(key, "value", ttl);
        assertThat(cache.get(key, String.class)).isPresent();
        
        // Wait for expiration
        Thread.sleep(150);
        
        assertThat(cache.get(key, String.class)).isEmpty();
        assertThat(cache.getStats().evictions()).isGreaterThan(0);
    }

    @Test
    void tokenOptimization_ShouldReduceTokenCount() {
        String originalPrompt = "This is a very   long    prompt with       excessive whitespace and really unnecessary filler words.";
        
        // Test different optimization levels
        var minimal = tokenService.optimizePrompt(originalPrompt, TokenOptimizationService.OptimizationLevel.MINIMAL);
        var moderate = tokenService.optimizePrompt(originalPrompt, TokenOptimizationService.OptimizationLevel.MODERATE);
        var aggressive = tokenService.optimizePrompt(originalPrompt, TokenOptimizationService.OptimizationLevel.AGGRESSIVE);
        
        // Each level should provide more optimization
        assertThat(minimal.optimizedTokens()).isLessThan(minimal.originalTokens());
        assertThat(moderate.optimizedTokens()).isLessThanOrEqualTo(minimal.optimizedTokens());
        assertThat(aggressive.optimizedTokens()).isLessThanOrEqualTo(moderate.optimizedTokens());
        
        // Verify savings percentage
        assertThat(minimal.savingsPercentage()).isGreaterThan(0);
        assertThat(aggressive.savingsPercentage()).isGreaterThan(minimal.savingsPercentage());
    }

    @Test
    void tokenEstimation_ShouldBeConsistent() {
        String text = "This is a sample text for token estimation.";
        
        int estimate1 = tokenService.estimateTokens(text);
        int estimate2 = tokenService.estimateTokens(text);
        
        assertThat(estimate1).isEqualTo(estimate2);
        assertThat(estimate1).isGreaterThan(0);
        
        // Longer text should have more tokens
        String longerText = text + " " + text;
        int longerEstimate = tokenService.estimateTokens(longerText);
        assertThat(longerEstimate).isGreaterThan(estimate1);
    }

    @Test
    void batchProcessing_ShouldBeMoreEfficient() {
        List<String> items = IntStream.range(0, 100)
                .mapToObj(i -> "Item " + i)
                .toList();
        
        // Measure sequential processing
        long sequentialDuration = measureDuration(() -> {
            items.forEach(item -> simulateProcessing(item, 10));
        });
        
        // Measure batch processing
        long batchDuration = measureDuration(() -> {
            List<List<String>> batches = partition(items, 10);
            batches.forEach(batch -> simulateProcessing(String.join(",", batch), 50));
        });
        
        // Batch processing should be faster
        assertThat(batchDuration).isLessThan(sequentialDuration);
    }

    @Test
    void parallelProcessing_ShouldImproveThrough() throws InterruptedException {
        int itemCount = 20;
        int processingTimeMs = 100;
        
        List<CompletableFuture<String>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < itemCount; i++) {
            final int index = i;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                simulateProcessing("Item " + index, processingTimeMs);
                return "Processed " + index;
            }, executor);
            futures.add(future);
        }
        
        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long duration = System.currentTimeMillis() - startTime;
        executor.shutdown();
        
        // Parallel processing should be much faster than sequential
        long expectedSequentialTime = itemCount * processingTimeMs;
        assertThat(duration).isLessThan(expectedSequentialTime / 2);
    }

    @Test
    void memoryEfficiency_LargeDatasetHandling() {
        // Get initial memory
        System.gc();
        long initialMemory = getUsedMemory();
        
        // Process large dataset in chunks
        int totalItems = 10000;
        int chunkSize = 100;
        
        for (int i = 0; i < totalItems; i += chunkSize) {
            List<String> chunk = IntStream.range(i, Math.min(i + chunkSize, totalItems))
                    .mapToObj(j -> "Item " + j)
                    .toList();
            
            // Process chunk
            chunk.forEach(item -> simulateProcessing(item, 1));
            
            // Clear chunk to free memory
            chunk = null;
        }
        
        System.gc();
        long finalMemory = getUsedMemory();
        
        // Memory usage should not grow linearly with dataset size
        long memoryIncrease = finalMemory - initialMemory;
        long expectedMaxIncrease = chunkSize * 1000; // Rough estimate
        
        assertThat(memoryIncrease).isLessThan(expectedMaxIncrease);
    }

    @Test
    void optimalBatchSize_ShouldBeCalculatedCorrectly() {
        int maxTokens = 4000;
        int avgTokensPerItem = 100;
        int responseOverhead = 500;
        
        int optimalBatchSize = tokenService.calculateOptimalBatchSize(
                maxTokens, avgTokensPerItem, responseOverhead
        );
        
        // Should fit within token limits
        int expectedTokens = (optimalBatchSize * avgTokensPerItem) + responseOverhead + 50; // schema overhead
        assertThat(expectedTokens).isLessThanOrEqualTo(maxTokens);
        
        // Should maximize batch size
        int largerBatchTokens = ((optimalBatchSize + 1) * avgTokensPerItem) + responseOverhead + 50;
        assertThat(largerBatchTokens).isGreaterThan(maxTokens);
    }

    // Helper methods

    private long measureDuration(Runnable action) {
        long start = System.nanoTime();
        action.run();
        return System.nanoTime() - start;
    }

    private void simulateProcessing(String item, int delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private List<List<String>> partition(List<String> list, int size) {
        List<List<String>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}