package com.coherentsolutions.l3structuredoutput.s14.memory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Service for memory-efficient processing of large structured outputs
 * using streaming, pagination, and file-based buffering.
 */
@Service
public class MemoryEfficientService {

    private static final Logger logger = LoggerFactory.getLogger(MemoryEfficientService.class);
    
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final JsonFactory jsonFactory;
    
    // Configuration
    private static final int BUFFER_SIZE = 8192; // 8KB buffer
    private static final int QUEUE_SIZE = 100;
    private static final long MAX_MEMORY_THRESHOLD = 50 * 1024 * 1024; // 50MB
    
    public MemoryEfficientService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
        this.jsonFactory = new JsonFactory();
    }

    /**
     * Process large dataset with streaming to avoid memory overflow
     */
    public <T, R> StreamingResult processLargeDataset(
            Stream<T> dataStream,
            StreamProcessor<T, R> processor,
            Class<R> responseType,
            StreamingOptions options) throws IOException {
        
        Path tempFile = Files.createTempFile("stream-output-", ".json");
        AtomicLong processedCount = new AtomicLong(0);
        AtomicLong bytesWritten = new AtomicLong(0);
        
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
            writer.write("[");
            
            final boolean[] first = {true};
            
            dataStream.forEach(item -> {
                try {
                    R result = processItem(item, processor, responseType);
                    
                    if (!first[0]) {
                        writer.write(",");
                    }
                    first[0] = false;
                    
                    String json = objectMapper.writeValueAsString(result);
                    writer.write(json);
                    
                    processedCount.incrementAndGet();
                    bytesWritten.addAndGet(json.getBytes(StandardCharsets.UTF_8).length);
                    
                    // Flush periodically to prevent memory buildup
                    if (processedCount.get() % options.flushInterval() == 0) {
                        writer.flush();
                    }
                    
                } catch (Exception e) {
                    logger.error("Error processing item", e);
                }
            });
            
            writer.write("]");
            writer.flush();
        }
        
        return new StreamingResult(
                tempFile,
                processedCount.get(),
                bytesWritten.get(),
                Files.size(tempFile)
        );
    }

    /**
     * Process with pagination to handle large result sets
     */
    public <T, R> void processPaginated(
            PaginatedRequest<T> request,
            StreamProcessor<T, R> processor,
            Class<R> responseType,
            Consumer<Page<R>> pageConsumer) {
        
        int currentPage = 0;
        boolean hasMore = true;
        
        while (hasMore) {
            List<T> pageItems = request.getPage(currentPage, request.pageSize());
            
            if (pageItems.isEmpty()) {
                hasMore = false;
                continue;
            }
            
            List<R> results = new ArrayList<>();
            for (T item : pageItems) {
                try {
                    R result = processItem(item, processor, responseType);
                    results.add(result);
                } catch (Exception e) {
                    logger.error("Error processing item in page {}", currentPage, e);
                }
            }
            
            Page<R> page = new Page<>(
                    results,
                    currentPage,
                    request.pageSize(),
                    hasMore
            );
            
            pageConsumer.accept(page);
            currentPage++;
            
            // Check memory usage and pause if needed
            checkMemoryUsage();
        }
    }

    /**
     * Process with producer-consumer pattern for efficient memory usage
     */
    public <T, R> void processProducerConsumer(
            Iterator<T> producer,
            StreamProcessor<T, R> processor,
            Class<R> responseType,
            Consumer<R> consumer,
            int numWorkers) throws InterruptedException {
        
        BlockingQueue<Optional<T>> queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
        List<Thread> workers = new ArrayList<>();
        
        // Start producer thread
        Thread producerThread = new Thread(() -> {
            try {
                while (producer.hasNext()) {
                    queue.put(Optional.of(producer.next()));
                }
                // Signal end of stream
                for (int i = 0; i < numWorkers; i++) {
                    queue.put(Optional.empty());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        producerThread.start();
        
        // Start worker threads
        for (int i = 0; i < numWorkers; i++) {
            Thread worker = new Thread(() -> {
                try {
                    while (true) {
                        Optional<T> item = queue.take();
                        if (item.isEmpty()) {
                            break;
                        }
                        
                        R result = processItem(item.get(), processor, responseType);
                        consumer.accept(result);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            workers.add(worker);
            worker.start();
        }
        
        // Wait for completion
        producerThread.join();
        for (Thread worker : workers) {
            worker.join();
        }
    }

    /**
     * Stream JSON parsing for memory-efficient large file processing
     */
    public <R> void parseJsonStream(
            InputStream inputStream,
            Class<R> itemType,
            Consumer<R> itemConsumer) throws IOException {
        
        try (JsonParser parser = jsonFactory.createParser(inputStream)) {
            
            // Skip START_ARRAY
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected start of array");
            }
            
            // Process each item
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                R item = objectMapper.readValue(parser, itemType);
                itemConsumer.accept(item);
            }
        }
    }

    /**
     * Memory-mapped file processing for very large outputs
     */
    public void processWithMemoryMappedFile(
            Path inputFile,
            Path outputFile,
            FileProcessor processor) throws IOException {
        
        try (RandomAccessFile input = new RandomAccessFile(inputFile.toFile(), "r");
             RandomAccessFile output = new RandomAccessFile(outputFile.toFile(), "rw");
             FileChannel inputChannel = input.getChannel();
             FileChannel outputChannel = output.getChannel()) {
            
            long fileSize = inputChannel.size();
            long position = 0;
            
            while (position < fileSize) {
                long remaining = fileSize - position;
                long size = Math.min(remaining, Integer.MAX_VALUE);
                
                ByteBuffer buffer = inputChannel.map(
                        FileChannel.MapMode.READ_ONLY,
                        position,
                        size
                );
                
                ByteBuffer processed = processor.process(buffer);
                outputChannel.write(processed);
                
                position += size;
            }
        }
    }

    /**
     * Process single item
     */
    private <T, R> R processItem(
            T item,
            StreamProcessor<T, R> processor,
            Class<R> responseType) {
        
        String prompt = processor.createPrompt(item);
        BeanOutputConverter<R> converter = new BeanOutputConverter<>(responseType);
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }

    /**
     * Check memory usage and trigger GC if needed
     */
    private void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        
        if (usedMemory > MAX_MEMORY_THRESHOLD) {
            logger.info("Memory usage high: {}MB, triggering GC", usedMemory / 1024 / 1024);
            System.gc();
        }
    }

    /**
     * Stream processor interface
     */
    @FunctionalInterface
    public interface StreamProcessor<T, R> {
        String createPrompt(T item);
    }

    /**
     * File processor interface
     */
    @FunctionalInterface
    public interface FileProcessor {
        ByteBuffer process(ByteBuffer input);
    }

    /**
     * Paginated request interface
     */
    public interface PaginatedRequest<T> {
        List<T> getPage(int pageNumber, int pageSize);
        int pageSize();
    }

    /**
     * Streaming options
     */
    public record StreamingOptions(
            int flushInterval,
            boolean compressOutput
    ) {
        public static StreamingOptions defaults() {
            return new StreamingOptions(100, false);
        }
    }

    /**
     * Streaming result
     */
    public record StreamingResult(
            Path outputFile,
            long itemsProcessed,
            long bytesProcessed,
            long outputFileSize
    ) {
        public double compressionRatio() {
            return bytesProcessed == 0 ? 0.0 : (double) outputFileSize / bytesProcessed;
        }
    }

    /**
     * Page of results
     */
    public record Page<T>(
            List<T> items,
            int pageNumber,
            int pageSize,
            boolean hasNext
    ) {
        public int itemCount() {
            return items.size();
        }
    }

    /**
     * Memory usage stats
     */
    public static class MemoryStats {
        public static MemoryInfo getCurrentMemoryInfo() {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            return new MemoryInfo(
                    maxMemory,
                    totalMemory,
                    freeMemory,
                    usedMemory,
                    (double) usedMemory / maxMemory
            );
        }
    }

    /**
     * Memory information
     */
    public record MemoryInfo(
            long maxMemory,
            long totalMemory,
            long freeMemory,
            long usedMemory,
            double usagePercentage
    ) {
        public String toHumanReadable() {
            return String.format(
                    "Memory: Used=%dMB, Free=%dMB, Total=%dMB, Max=%dMB (%.1f%%)",
                    usedMemory / 1024 / 1024,
                    freeMemory / 1024 / 1024,
                    totalMemory / 1024 / 1024,
                    maxMemory / 1024 / 1024,
                    usagePercentage * 100
            );
        }
    }
}