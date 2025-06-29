package com.coherentsolutions.l3structuredoutput.s14;

import com.coherentsolutions.l3structuredoutput.s14.optimization.TokenOptimizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Benchmark tests for performance optimization features.
 * These tests measure and log performance metrics.
 */
class BenchmarkTest {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkTest.class);
    
    private TokenOptimizationService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenOptimizationService();
    }

    @Test
    void benchmarkTokenOptimization() {
        List<String> testPrompts = List.of(
                "Please analyze this document and provide a comprehensive summary.",
                "What is the weather forecast for tomorrow in New York City?",
                "Explain the concept of quantum computing in simple terms.",
                generateLongPrompt(500),
                generateLongPrompt(1000)
        );

        logger.info("=== Token Optimization Benchmark ===");
        
        for (String prompt : testPrompts) {
            benchmarkSinglePrompt(prompt);
        }
    }

    @Test
    void benchmarkParallelVsSequential() throws InterruptedException, ExecutionException {
        int[] itemCounts = {10, 50, 100, 200};
        
        logger.info("=== Parallel vs Sequential Processing Benchmark ===");
        
        for (int count : itemCounts) {
            List<String> items = IntStream.range(0, count)
                    .mapToObj(i -> "Process item " + i)
                    .toList();
            
            // Sequential benchmark
            long seqStart = System.currentTimeMillis();
            List<String> seqResults = new ArrayList<>();
            for (String item : items) {
                seqResults.add(processItem(item));
            }
            long seqDuration = System.currentTimeMillis() - seqStart;
            
            // Parallel benchmark
            ExecutorService executor = Executors.newFixedThreadPool(4);
            long parStart = System.currentTimeMillis();
            
            List<Future<String>> futures = items.stream()
                    .map(item -> executor.submit(() -> processItem(item)))
                    .toList();
            
            List<String> parResults = new ArrayList<>();
            for (Future<String> future : futures) {
                parResults.add(future.get());
            }
            long parDuration = System.currentTimeMillis() - parStart;
            
            executor.shutdown();
            
            double speedup = (double) seqDuration / parDuration;
            
            logger.info("Items: {}, Sequential: {}ms, Parallel: {}ms, Speedup: {:.2f}x",
                    count, seqDuration, parDuration, speedup);
            
            assertThat(parResults).hasSameSizeAs(seqResults);
        }
    }

    @Test
    void benchmarkBatchSizes() {
        int totalItems = 100;
        int[] batchSizes = {1, 5, 10, 20, 50};
        
        logger.info("=== Batch Size Optimization Benchmark ===");
        
        for (int batchSize : batchSizes) {
            long duration = benchmarkBatchProcessing(totalItems, batchSize);
            double itemsPerSecond = (totalItems * 1000.0) / duration;
            
            logger.info("Batch size: {}, Duration: {}ms, Throughput: {:.2f} items/sec",
                    batchSize, duration, itemsPerSecond);
        }
    }

    @Test
    void benchmarkMemoryUsage() {
        int[] datasetSizes = {100, 1000, 10000};
        
        logger.info("=== Memory Usage Benchmark ===");
        
        for (int size : datasetSizes) {
            long memoryBefore = getUsedMemory();
            
            // Process dataset
            List<String> dataset = IntStream.range(0, size)
                    .mapToObj(i -> "Data item " + i + ": " + generateLongPrompt(100))
                    .toList();
            
            // Simulate processing
            dataset.forEach(this::processItem);
            
            long memoryAfter = getUsedMemory();
            long memoryUsed = memoryAfter - memoryBefore;
            double memoryPerItem = (double) memoryUsed / size;
            
            logger.info("Dataset size: {}, Memory used: {} KB, Per item: {:.2f} bytes",
                    size, memoryUsed / 1024, memoryPerItem);
        }
    }

    // Helper methods

    private void benchmarkSinglePrompt(String prompt) {
        long startTime = System.nanoTime();
        
        var minimal = tokenService.optimizePrompt(prompt, TokenOptimizationService.OptimizationLevel.MINIMAL);
        var moderate = tokenService.optimizePrompt(prompt, TokenOptimizationService.OptimizationLevel.MODERATE);
        var aggressive = tokenService.optimizePrompt(prompt, TokenOptimizationService.OptimizationLevel.AGGRESSIVE);
        
        long duration = System.nanoTime() - startTime;
        
        logger.info("Prompt length: {} chars", prompt.length());
        logger.info("  Original tokens: {}", minimal.originalTokens());
        logger.info("  Minimal: {} tokens ({:.1f}% saved)", 
                minimal.optimizedTokens(), minimal.savingsPercentage());
        logger.info("  Moderate: {} tokens ({:.1f}% saved)", 
                moderate.optimizedTokens(), moderate.savingsPercentage());
        logger.info("  Aggressive: {} tokens ({:.1f}% saved)", 
                aggressive.optimizedTokens(), aggressive.savingsPercentage());
        logger.info("  Processing time: {} μs\n", duration / 1000);
    }

    private String generateLongPrompt(int words) {
        return IntStream.range(0, words)
                .mapToObj(i -> "word" + i)
                .reduce("", (a, b) -> a + " " + b);
    }

    private String processItem(String item) {
        // Simulate processing delay
        try {
            Thread.sleep(10); // 10ms processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Processed: " + item;
    }

    private long benchmarkBatchProcessing(int totalItems, int batchSize) {
        long start = System.currentTimeMillis();
        
        for (int i = 0; i < totalItems; i += batchSize) {
            int end = Math.min(i + batchSize, totalItems);
            List<String> batch = IntStream.range(i, end)
                    .mapToObj(j -> "Item " + j)
                    .toList();
            
            // Simulate batch processing
            processBatch(batch);
        }
        
        return System.currentTimeMillis() - start;
    }

    private void processBatch(List<String> batch) {
        // Batch processing is more efficient
        try {
            Thread.sleep(5 + batch.size() * 2); // Base overhead + per-item cost
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private long getUsedMemory() {
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}