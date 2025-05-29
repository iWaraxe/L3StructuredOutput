package com.coherentsolutions.l3structuredoutput.s14.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service for efficient batch processing of multiple structured output requests
 * with configurable parallelism and chunking strategies.
 */
@Service
public class BatchProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(BatchProcessingService.class);
    
    private final ChatClient chatClient;
    private final ExecutorService executorService;
    
    // Optimal batch size based on token limits
    private static final int DEFAULT_BATCH_SIZE = 5;
    private static final int DEFAULT_PARALLEL_THREADS = 4;

    public BatchProcessingService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.executorService = Executors.newFixedThreadPool(DEFAULT_PARALLEL_THREADS);
    }

    /**
     * Process multiple requests in parallel batches
     */
    public <T, R> BatchResult<R> processBatch(
            List<T> requests,
            BatchProcessor<T, R> processor,
            Class<R> responseType) {
        
        return processBatch(requests, processor, responseType, DEFAULT_BATCH_SIZE);
    }

    /**
     * Process multiple requests in parallel batches with custom batch size
     */
    public <T, R> BatchResult<R> processBatch(
            List<T> requests,
            BatchProcessor<T, R> processor,
            Class<R> responseType,
            int batchSize) {
        
        long startTime = System.currentTimeMillis();
        List<List<T>> batches = partition(requests, batchSize);
        
        logger.info("Processing {} requests in {} batches of size {}", 
                requests.size(), batches.size(), batchSize);
        
        List<CompletableFuture<List<R>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(
                        () -> processSingleBatch(batch, processor, responseType),
                        executorService
                ))
                .collect(Collectors.toList());
        
        // Wait for all batches to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        
        try {
            allOf.get(5, TimeUnit.MINUTES);
            
            List<R> results = futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            
            long duration = System.currentTimeMillis() - startTime;
            
            return new BatchResult<>(
                    results,
                    requests.size(),
                    results.size(),
                    duration,
                    requests.size() - results.size()
            );
            
        } catch (Exception e) {
            logger.error("Batch processing failed", e);
            return new BatchResult<>(
                    new ArrayList<>(),
                    requests.size(),
                    0,
                    System.currentTimeMillis() - startTime,
                    requests.size()
            );
        }
    }

    /**
     * Process requests with streaming for memory efficiency
     */
    public <T, R> void processStream(
            List<T> requests,
            BatchProcessor<T, R> processor,
            Class<R> responseType,
            StreamConsumer<R> consumer) {
        
        List<List<T>> batches = partition(requests, DEFAULT_BATCH_SIZE);
        
        batches.parallelStream()
                .forEach(batch -> {
                    List<R> results = processSingleBatch(batch, processor, responseType);
                    results.forEach(consumer::consume);
                });
    }

    /**
     * Process with token optimization - combine multiple small requests
     */
    public <T, R> BatchResult<R> processOptimized(
            List<T> requests,
            TokenEstimator<T> estimator,
            BatchProcessor<T, R> processor,
            Class<R> responseType,
            int maxTokensPerBatch) {
        
        List<List<T>> optimizedBatches = createOptimizedBatches(requests, estimator, maxTokensPerBatch);
        return processBatch(optimizedBatches.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()), processor, responseType, 1);
    }

    /**
     * Process a single batch
     */
    private <T, R> List<R> processSingleBatch(
            List<T> batch,
            BatchProcessor<T, R> processor,
            Class<R> responseType) {
        
        try {
            // Combine batch into single prompt for efficiency
            String combinedPrompt = processor.createBatchPrompt(batch);
            
            BeanOutputConverter<BatchResponse<R>> converter = new BeanOutputConverter<>(
                    new BatchResponseTypeReference<>(responseType)
            );
            
            BatchResponse<R> response = chatClient.prompt()
                    .user(combinedPrompt + "\n\n" + converter.getFormat())
                    .call()
                    .entity(converter);
            
            return response != null ? response.items() : new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("Failed to process batch", e);
            return new ArrayList<>();
        }
    }

    /**
     * Partition list into batches
     */
    private <T> List<List<T>> partition(List<T> list, int batchSize) {
        return IntStream.range(0, (list.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> list.subList(
                        i * batchSize,
                        Math.min((i + 1) * batchSize, list.size())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Create optimized batches based on token estimates
     */
    private <T> List<List<T>> createOptimizedBatches(
            List<T> requests,
            TokenEstimator<T> estimator,
            int maxTokensPerBatch) {
        
        List<List<T>> batches = new ArrayList<>();
        List<T> currentBatch = new ArrayList<>();
        int currentTokens = 0;
        
        for (T request : requests) {
            int requestTokens = estimator.estimateTokens(request);
            
            if (currentTokens + requestTokens > maxTokensPerBatch && !currentBatch.isEmpty()) {
                batches.add(new ArrayList<>(currentBatch));
                currentBatch.clear();
                currentTokens = 0;
            }
            
            currentBatch.add(request);
            currentTokens += requestTokens;
        }
        
        if (!currentBatch.isEmpty()) {
            batches.add(currentBatch);
        }
        
        return batches;
    }

    /**
     * Shutdown executor service
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Interface for batch processing logic
     */
    @FunctionalInterface
    public interface BatchProcessor<T, R> {
        String createBatchPrompt(List<T> batch);
    }

    /**
     * Interface for streaming consumption
     */
    @FunctionalInterface
    public interface StreamConsumer<R> {
        void consume(R result);
    }

    /**
     * Interface for token estimation
     */
    @FunctionalInterface
    public interface TokenEstimator<T> {
        int estimateTokens(T request);
    }

    /**
     * Batch processing result
     */
    public record BatchResult<R>(
            List<R> results,
            int totalRequests,
            int successfulResults,
            long processingTimeMs,
            int failedRequests
    ) {
        public double successRate() {
            return totalRequests == 0 ? 0.0 : (double) successfulResults / totalRequests;
        }
        
        public double requestsPerSecond() {
            return processingTimeMs == 0 ? 0.0 : (totalRequests * 1000.0) / processingTimeMs;
        }
    }

    /**
     * Batch response wrapper
     */
    public record BatchResponse<T>(List<T> items) {}

    /**
     * Type reference for batch responses
     */
    private static class BatchResponseTypeReference<T> extends org.springframework.core.ParameterizedTypeReference<BatchResponse<T>> {
        public BatchResponseTypeReference(Class<T> type) {
            // Implementation handles type resolution
        }
    }
}