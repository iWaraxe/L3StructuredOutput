package com.coherentsolutions.l3structuredoutput.s15.benchmarks;

import com.coherentsolutions.l3structuredoutput.s15.fixtures.TestDataFixtures;
import com.coherentsolutions.l3structuredoutput.s15.mocks.MockAIResponseGenerator;
import com.coherentsolutions.l3structuredoutput.s15.mocks.MockChatModel;
import com.coherentsolutions.l3structuredoutput.s15.testing.TestingUtilities;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for Spring AI structured output converters
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class PerformanceBenchmarks {

    private MockAIResponseGenerator responseGenerator;
    private BeanOutputConverter<TestDataFixtures.PersonData> beanConverter;
    private ListOutputConverter listConverter;
    private MapOutputConverter mapConverter;
    
    private String smallJsonResponse;
    private String mediumJsonResponse;
    private String largeJsonResponse;
    private String listResponse;
    private String mapResponse;
    
    @Setup
    public void setup() {
        responseGenerator = new MockAIResponseGenerator();
        beanConverter = new BeanOutputConverter<>(TestDataFixtures.PersonData.class);
        listConverter = new ListOutputConverter();
        mapConverter = new MapOutputConverter();
        
        // Prepare test data
        smallJsonResponse = """
            {
                "name": "John Doe",
                "age": 30,
                "email": "john@example.com"
            }
            """;
        
        mediumJsonResponse = responseGenerator.generateMockResponse(
                TestDataFixtures.PersonData.class,
                MockAIResponseGenerator.MockConfig.defaults()
        );
        
        largeJsonResponse = generateLargeResponse();
        listResponse = "item1, item2, item3, item4, item5, item6, item7, item8, item9, item10";
        mapResponse = """
            {
                "key1": "value1",
                "key2": "value2",
                "key3": "value3",
                "nested": {
                    "innerKey1": "innerValue1",
                    "innerKey2": "innerValue2"
                }
            }
            """;
    }
    
    @Benchmark
    public void benchmarkBeanConverterSmall(Blackhole blackhole) {
        TestDataFixtures.PersonData result = beanConverter.convert(smallJsonResponse);
        blackhole.consume(result);
    }
    
    @Benchmark
    public void benchmarkBeanConverterMedium(Blackhole blackhole) {
        TestDataFixtures.PersonData result = beanConverter.convert(mediumJsonResponse);
        blackhole.consume(result);
    }
    
    @Benchmark
    public void benchmarkBeanConverterLarge(Blackhole blackhole) {
        TestDataFixtures.PersonData result = beanConverter.convert(largeJsonResponse);
        blackhole.consume(result);
    }
    
    @Benchmark
    public void benchmarkListConverter(Blackhole blackhole) {
        List<String> result = listConverter.convert(listResponse);
        blackhole.consume(result);
    }
    
    @Benchmark
    public void benchmarkMapConverter(Blackhole blackhole) {
        Map<String, Object> result = mapConverter.convert(mapResponse);
        blackhole.consume(result);
    }
    
    @Benchmark
    public void benchmarkConverterCreation(Blackhole blackhole) {
        BeanOutputConverter<TestDataFixtures.PersonData> converter = 
                new BeanOutputConverter<>(TestDataFixtures.PersonData.class);
        blackhole.consume(converter);
    }
    
    @Benchmark
    public void benchmarkFormatGeneration(Blackhole blackhole) {
        String format = beanConverter.getFormat();
        blackhole.consume(format);
    }
    
    @Benchmark
    @Threads(4)
    public void benchmarkConcurrentConversion(Blackhole blackhole) {
        TestDataFixtures.PersonData result = beanConverter.convert(mediumJsonResponse);
        blackhole.consume(result);
    }
    
    @Benchmark
    public void benchmarkEndToEndChatClient(Blackhole blackhole) {
        MockChatModel mockModel = new MockChatModel()
                .withTypedResponse(TestDataFixtures.PersonData.class, 
                        MockAIResponseGenerator.MockConfig.defaults());
        
        ChatClient chatClient = ChatClient.builder(mockModel).build();
        
        TestDataFixtures.PersonData result = chatClient.prompt()
                .user("Generate a person")
                .call()
                .entity(beanConverter);
        
        blackhole.consume(result);
    }
    
    /**
     * Comparison benchmarks for different converter types
     */
    @State(Scope.Benchmark)
    public static class ComparisonState {
        
        BeanOutputConverter<SimpleData> simpleBeanConverter;
        BeanOutputConverter<ComplexData> complexBeanConverter;
        ListOutputConverter listConverter;
        MapOutputConverter mapConverter;
        
        String simpleResponse = """
            {"id": "123", "value": "test"}
            """;
        
        String complexResponse = """
            {
                "id": "123",
                "items": ["a", "b", "c"],
                "metadata": {"key": "value"},
                "nested": {
                    "level1": {
                        "level2": {
                            "data": "deep"
                        }
                    }
                }
            }
            """;
        
        @Setup
        public void setup() {
            simpleBeanConverter = new BeanOutputConverter<>(SimpleData.class);
            complexBeanConverter = new BeanOutputConverter<>(ComplexData.class);
            listConverter = new ListOutputConverter();
            mapConverter = new MapOutputConverter();
        }
        
        public record SimpleData(String id, String value) {}
        
        public record ComplexData(
                String id,
                List<String> items,
                Map<String, String> metadata,
                Map<String, Object> nested
        ) {}
    }
    
    @Benchmark
    public void compareSimpleBean(ComparisonState state, Blackhole blackhole) {
        var result = state.simpleBeanConverter.convert(state.simpleResponse);
        blackhole.consume(result);
    }
    
    @Benchmark
    public void compareComplexBean(ComparisonState state, Blackhole blackhole) {
        var result = state.complexBeanConverter.convert(state.complexResponse);
        blackhole.consume(result);
    }
    
    @Benchmark
    public void compareMapForSimple(ComparisonState state, Blackhole blackhole) {
        var result = state.mapConverter.convert(state.simpleResponse);
        blackhole.consume(result);
    }
    
    @Benchmark
    public void compareMapForComplex(ComparisonState state, Blackhole blackhole) {
        var result = state.mapConverter.convert(state.complexResponse);
        blackhole.consume(result);
    }
    
    /**
     * Memory allocation benchmarks
     */
    @State(Scope.Thread)
    public static class MemoryState {
        private static final int ITERATIONS = 1000;
        
        @Benchmark
        public void memoryAllocationBeanConverter(Blackhole blackhole) {
            for (int i = 0; i < ITERATIONS; i++) {
                BeanOutputConverter<TestDataFixtures.PersonData> converter = 
                        new BeanOutputConverter<>(TestDataFixtures.PersonData.class);
                blackhole.consume(converter);
            }
        }
        
        @Benchmark
        public void memoryAllocationConversion(Blackhole blackhole) {
            BeanOutputConverter<TestDataFixtures.PersonData> converter = 
                    new BeanOutputConverter<>(TestDataFixtures.PersonData.class);
            
            String response = """
                {"name": "Test", "age": 25, "email": "test@example.com"}
                """;
            
            for (int i = 0; i < ITERATIONS; i++) {
                TestDataFixtures.PersonData result = converter.convert(response);
                blackhole.consume(result);
            }
        }
    }
    
    // Helper methods
    
    private String generateLargeResponse() {
        return """
            {
                "name": "Very Long Name That Contains Many Characters",
                "age": 35,
                "email": "verylongemailaddress@extremelylongdomainname.com",
                "phone": "+1-234-567-8901-234567",
                "address": {
                    "street": "123 Very Long Street Name That Goes On And On",
                    "city": "San Francisco Metropolitan Area",
                    "state": "California",
                    "zipCode": "94105-1234",
                    "country": "United States of America"
                }
            }
            """;
    }
    
    /**
     * Main method to run benchmarks
     */
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(PerformanceBenchmarks.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("benchmark-results.json")
                .build();
        
        new Runner(options).run();
    }
}