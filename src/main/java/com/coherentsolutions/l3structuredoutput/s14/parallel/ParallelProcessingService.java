package com.coherentsolutions.l3structuredoutput.s14.parallel;

import com.coherentsolutions.l3structuredoutput.s14.cache.StructuredOutputCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Service for parallel processing of structured output requests
 * with advanced features like rate limiting, circuit breaking, and priority queuing.
 */
@Service
public class ParallelProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(ParallelProcessingService.class);
    
    private final ChatClient chatClient;
    private final StructuredOutputCache cache;
    private final ThreadPoolExecutor executorService;
    private final Semaphore rateLimiter;
    private final CircuitBreaker circuitBreaker;
    
    // Configuration
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;
    private static final int RATE_LIMIT = 20; // requests per second
    
    public ParallelProcessingService(
            ChatClient.Builder chatClientBuilder,
            StructuredOutputCache cache) {
        
        this.chatClient = chatClientBuilder.build();
        this.cache = cache;
        
        // Configure thread pool with priority queue
        this.executorService = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                60L, TimeUnit.SECONDS,
                new PriorityBlockingQueue<>(QUEUE_CAPACITY, new TaskPriorityComparator()),
                new NamedThreadFactory("parallel-ai-")
        );
        
        this.rateLimiter = new Semaphore(RATE_LIMIT);
        this.circuitBreaker = new CircuitBreaker(5, Duration.ofMinutes(1));
        
        // Start rate limiter reset thread
        startRateLimiterReset();
    }

    /**
     * Process multiple requests in parallel with caching
     */
    public <T, R> ParallelResult<R> processParallel(
            List<T> requests,
            Function<T, String> promptGenerator,
            Class<R> responseType,
            ProcessingOptions options) {
        
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<ProcessedItem<T, R>>> futures = new ArrayList<>();
        
        for (int i = 0; i < requests.size(); i++) {
            T request = requests.get(i);
            int priority = options.priorityFunction().apply(request);
            
            CompletableFuture<ProcessedItem<T, R>> future = 
                    submitPrioritizedTask(
                            () -> processWithCaching(request, promptGenerator, responseType, options),
                            priority
                    );
            
            futures.add(future);
        }
        
        // Wait for completion with timeout
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(options.timeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            logger.warn("Parallel processing timed out after {}", options.timeout());
        } catch (Exception e) {
            logger.error("Error in parallel processing", e);
        }
        
        // Collect results
        List<ProcessedItem<T, R>> processedItems = futures.stream()
                .map(f -> {
                    try {
                        return f.getNow(null);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Convert to wildcard type for ParallelResult
        List<ProcessedItem<?, R>> wildcardItems = new ArrayList<>(processedItems);
        
        return new ParallelResult<>(
                wildcardItems,
                requests.size(),
                processedItems.size(),
                duration,
                calculateAverageLatency(wildcardItems),
                cache.getStats()
        );
    }

    /**
     * Submit prioritized task
     */
    private <R> CompletableFuture<R> submitPrioritizedTask(Supplier<R> task, int priority) {
        return CompletableFuture.supplyAsync(
                new PrioritizedTask<>(task, priority),
                executorService
        );
    }

    /**
     * Process with streaming for real-time results
     */
    public <T, R> void processStream(
            List<T> requests,
            Function<T, String> promptGenerator,
            Class<R> responseType,
            StreamingOptions options,
            StreamResultHandler<T, R> handler) {
        
        requests.forEach(request -> {
            executorService.execute(() -> {
                try {
                    ProcessedItem<T, R> result = processWithCaching(
                            request, promptGenerator, responseType, 
                            ProcessingOptions.defaults()
                    );
                    handler.onResult(result);
                } catch (Exception e) {
                    handler.onError(request, e);
                }
            });
        });
    }

    /**
     * Map-reduce style parallel processing
     */
    public <T, R, A> A mapReduce(
            List<T> requests,
            Function<T, String> promptGenerator,
            Class<R> responseType,
            Function<R, A> mapper,
            BinaryOperator<A> reducer,
            A identity) {
        
        return processParallel(requests, promptGenerator, responseType, ProcessingOptions.defaults())
                .items()
                .stream()
                .map(item -> mapper.apply(item.response()))
                .reduce(identity, reducer);
    }

    /**
     * Process single request with caching and circuit breaker
     */
    private <T, R> ProcessedItem<T, R> processWithCaching(
            T request,
            Function<T, String> promptGenerator,
            Class<R> responseType,
            ProcessingOptions options) {
        
        long itemStartTime = System.currentTimeMillis();
        String cacheKey = generateCacheKey(request, responseType);
        
        // Check cache first
        if (options.useCache()) {
            Optional<R> cached = cache.get(cacheKey, responseType);
            if (cached.isPresent()) {
                return new ProcessedItem<>(
                        request,
                        cached.get(),
                        System.currentTimeMillis() - itemStartTime,
                        true,
                        null
                );
            }
        }
        
        // Check circuit breaker
        if (!circuitBreaker.allowRequest()) {
            throw new RuntimeException("Circuit breaker is open");
        }
        
        try {
            // Apply rate limiting
            rateLimiter.acquire();
            
            // Generate prompt
            String prompt = promptGenerator.apply(request);
            
            // Create converter
            BeanOutputConverter<R> converter = new BeanOutputConverter<>(responseType);
            
            // Execute request
            R response = chatClient.prompt()
                    .user(prompt + "\n\n" + converter.getFormat())
                    .call()
                    .entity(converter);
            
            // Cache result
            if (options.useCache() && response != null) {
                cache.put(cacheKey, response, options.cacheTTL());
            }
            
            circuitBreaker.recordSuccess();
            
            return new ProcessedItem<>(
                    request,
                    response,
                    System.currentTimeMillis() - itemStartTime,
                    false,
                    null
            );
            
        } catch (Exception e) {
            circuitBreaker.recordFailure();
            return new ProcessedItem<>(
                    request,
                    null,
                    System.currentTimeMillis() - itemStartTime,
                    false,
                    e
            );
        } finally {
            rateLimiter.release();
        }
    }

    /**
     * Generate cache key for request
     */
    private <T> String generateCacheKey(T request, Class<?> responseType) {
        return responseType.getSimpleName() + ":" + request.hashCode();
    }

    /**
     * Calculate average latency
     */
    private <R> double calculateAverageLatency(List<ProcessedItem<?, R>> items) {
        if (items.isEmpty()) return 0.0;
        
        return items.stream()
                .mapToLong(ProcessedItem::processingTimeMs)
                .average()
                .orElse(0.0);
    }

    /**
     * Start rate limiter reset thread
     */
    private void startRateLimiterReset() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            int permits = RATE_LIMIT - rateLimiter.availablePermits();
            if (permits > 0) {
                rateLimiter.release(permits);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Shutdown service
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
     * Processing options
     */
    public record ProcessingOptions(
            boolean useCache,
            Duration cacheTTL,
            Duration timeout,
            Function<Object, Integer> priorityFunction
    ) {
        public static ProcessingOptions defaults() {
            return new ProcessingOptions(
                    true,
                    Duration.ofMinutes(5),
                    Duration.ofMinutes(2),
                    obj -> 0 // default priority
            );
        }
    }

    /**
     * Streaming options
     */
    public record StreamingOptions(
            int bufferSize,
            Duration flushInterval
    ) {
        public static StreamingOptions defaults() {
            return new StreamingOptions(10, Duration.ofSeconds(1));
        }
    }

    /**
     * Processed item result
     */
    public record ProcessedItem<T, R>(
            T request,
            R response,
            long processingTimeMs,
            boolean fromCache,
            Exception error
    ) {
        public boolean isSuccess() {
            return error == null && response != null;
        }
    }

    /**
     * Parallel processing result
     */
    public record ParallelResult<R>(
            List<ProcessedItem<?, R>> items,
            int totalRequests,
            int successfulRequests,
            long totalTimeMs,
            double averageLatencyMs,
            StructuredOutputCache.CacheStats cacheStats
    ) {
        public double throughput() {
            return totalTimeMs == 0 ? 0.0 : (totalRequests * 1000.0) / totalTimeMs;
        }
        
        public double successRate() {
            return totalRequests == 0 ? 0.0 : (double) successfulRequests / totalRequests;
        }
    }

    /**
     * Stream result handler
     */
    public interface StreamResultHandler<T, R> {
        void onResult(ProcessedItem<T, R> result);
        void onError(T request, Exception error);
    }

    /**
     * Prioritized task wrapper
     */
    private static class PrioritizedTask<T> implements Supplier<T>, Comparable<PrioritizedTask<T>> {
        private final Supplier<T> task;
        private final int priority;
        
        PrioritizedTask(Supplier<T> task, int priority) {
            this.task = task;
            this.priority = priority;
        }
        
        @Override
        public T get() {
            return task.get();
        }
        
        @Override
        public int compareTo(PrioritizedTask<T> other) {
            return Integer.compare(other.priority, this.priority); // Higher priority first
        }
    }

    /**
     * Task priority comparator
     */
    private static class TaskPriorityComparator implements Comparator<Runnable> {
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public int compare(Runnable r1, Runnable r2) {
            if (r1 instanceof PrioritizedTask && r2 instanceof PrioritizedTask) {
                // Raw type comparison is safe here as we only compare priorities
                return ((PrioritizedTask) r1).compareTo((PrioritizedTask) r2);
            }
            return 0;
        }
    }

    /**
     * Named thread factory
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger counter = new AtomicInteger(0);
        
        NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(prefix + counter.incrementAndGet());
            return thread;
        }
    }

    /**
     * Simple circuit breaker implementation
     */
    private static class CircuitBreaker {
        private final int failureThreshold;
        private final Duration resetTimeout;
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private volatile long lastFailureTime = 0;
        private volatile boolean open = false;
        
        CircuitBreaker(int failureThreshold, Duration resetTimeout) {
            this.failureThreshold = failureThreshold;
            this.resetTimeout = resetTimeout;
        }
        
        boolean allowRequest() {
            if (!open) {
                return true;
            }
            
            // Check if timeout has passed
            if (System.currentTimeMillis() - lastFailureTime > resetTimeout.toMillis()) {
                reset();
                return true;
            }
            
            return false;
        }
        
        void recordSuccess() {
            reset();
        }
        
        void recordFailure() {
            lastFailureTime = System.currentTimeMillis();
            if (failureCount.incrementAndGet() >= failureThreshold) {
                open = true;
            }
        }
        
        void reset() {
            failureCount.set(0);
            open = false;
        }
    }
}