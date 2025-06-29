package com.coherentsolutions.l3structuredoutput.s14.cache;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * High-performance in-memory cache for structured output responses
 * with TTL support and cache statistics.
 */
@Component
public class StructuredOutputCache {

    private final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong evictions = new AtomicLong(0);
    
    private final Duration defaultTTL = Duration.ofMinutes(5);

    public StructuredOutputCache() {
        // Schedule cleanup every minute
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Get a value from cache if present and not expired
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        CacheEntry<?> entry = cache.get(key);
        
        if (entry == null) {
            misses.incrementAndGet();
            return Optional.empty();
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            misses.incrementAndGet();
            evictions.incrementAndGet();
            return Optional.empty();
        }
        
        hits.incrementAndGet();
        return Optional.of((T) entry.value);
    }

    /**
     * Put a value in cache with default TTL
     */
    public <T> void put(String key, T value) {
        put(key, value, defaultTTL);
    }

    /**
     * Put a value in cache with custom TTL
     */
    public <T> void put(String key, T value, Duration ttl) {
        cache.put(key, new CacheEntry<>(value, Instant.now().plus(ttl)));
    }

    /**
     * Compute if absent with caching
     */
    @SuppressWarnings("unchecked")
    public <T> T computeIfAbsent(String key, CacheComputation<T> computation, Class<T> type) {
        return computeIfAbsent(key, computation, type, defaultTTL);
    }

    /**
     * Compute if absent with caching and custom TTL
     */
    @SuppressWarnings("unchecked")
    public <T> T computeIfAbsent(String key, CacheComputation<T> computation, Class<T> type, Duration ttl) {
        CacheEntry<?> entry = cache.get(key);
        
        if (entry != null && !entry.isExpired()) {
            hits.incrementAndGet();
            return (T) entry.value;
        }
        
        misses.incrementAndGet();
        T value = computation.compute();
        put(key, value, ttl);
        return value;
    }

    /**
     * Remove a specific key from cache
     */
    public void evict(String key) {
        if (cache.remove(key) != null) {
            evictions.incrementAndGet();
        }
    }

    /**
     * Clear all cache entries
     */
    public void clear() {
        int size = cache.size();
        cache.clear();
        evictions.addAndGet(size);
    }

    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        return new CacheStats(
            cache.size(),
            hits.get(),
            misses.get(),
            evictions.get(),
            getHitRate()
        );
    }

    /**
     * Calculate hit rate
     */
    private double getHitRate() {
        long totalRequests = hits.get() + misses.get();
        return totalRequests == 0 ? 0.0 : (double) hits.get() / totalRequests;
    }

    /**
     * Cleanup expired entries
     */
    private void cleanupExpiredEntries() {
        Instant now = Instant.now();
        cache.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().expiresAt.isBefore(now);
            if (expired) {
                evictions.incrementAndGet();
            }
            return expired;
        });
    }

    /**
     * Shutdown cleanup executor
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Cache entry with expiration
     */
    private static class CacheEntry<T> {
        final T value;
        final Instant expiresAt;

        CacheEntry(T value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    /**
     * Functional interface for cache computation
     */
    @FunctionalInterface
    public interface CacheComputation<T> {
        T compute();
    }

    /**
     * Cache statistics
     */
    public record CacheStats(
        int size,
        long hits,
        long misses,
        long evictions,
        double hitRate
    ) {}
}