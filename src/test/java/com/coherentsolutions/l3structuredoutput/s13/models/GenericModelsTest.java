package com.coherentsolutions.l3structuredoutput.s13.models;

import com.coherentsolutions.l3structuredoutput.s13.models.GenericModels.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GenericModelsTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void complexNestedStructure_ShouldSerializeAndDeserialize() throws Exception {
        // Given
        ComplexNestedStructure<Map<String, Object>, BaseEntity, BigDecimal> structure = 
            new ComplexNestedStructure<>(
                Map.of("version", "1.0", "created", "2024-01-01"),
                Arrays.asList(
                    Map.of("id", 1, "value", "sample"),
                    Map.of("id", 2, "value", "data")
                ),
                Map.of("category1", Map.of("rel1", 
                    new NumericEntity("num1", "Sample Number", BigDecimal.valueOf(42.5), "units", 2)
                )),
                new Analytics<>(
                    Map.of("avg", 25.5, "max", 100.0),
                    Map.of("group1", Arrays.asList(BigDecimal.valueOf(10.5))),
                    Arrays.asList(new TimeSeriesPoint<>(LocalDateTime.of(2024, 1, 1, 12, 0), BigDecimal.valueOf(15.5), 0.9)),
                    Map.of("future", new PredictionRange<>(BigDecimal.valueOf(10), BigDecimal.valueOf(30), BigDecimal.valueOf(20), 0.8))
                )
            );

        // When
        String json = objectMapper.writeValueAsString(structure);
        
        // Then
        assertThat(json).isNotBlank();
        assertThat(json).contains("\"version\":\"1.0\"");
        assertThat(json).contains("\"avg\":25.5");
    }

    @Test
    void recursiveNode_ShouldCreateHierarchy() throws Exception {
        // Given
        RecursiveNode<Map<String, Object>> node = new RecursiveNode<>(
                "Root Organization",
                "company",
                Arrays.asList(
                    new RecursiveNode<>("Engineering", "department", 
                        Arrays.asList(
                            new RecursiveNode<>("Backend Team", "team", null, Map.of("size", 5, "tech", "Java"))
                        ), 
                        Map.of("budget", 500000, "location", "Building A")
                    )
                ),
                Map.of("employees", 1000, "founded", 2010)
        );

        // When
        String json = objectMapper.writeValueAsString(node);

        // Then
        assertThat(json).contains("\"name\":\"Root Organization\"");
        assertThat(json).contains("\"type\":\"company\"");
        assertThat(json).contains("\"Backend Team\"");
        assertThat(json).contains("\"employees\":1000");
    }

    @Test
    void graphNode_ShouldSerializeConnections() throws Exception {
        // Given
        GraphNode<Double, Map<String, String>> node = new GraphNode<>(
                "node1", 
                "User Alice", 
                Map.of("node2", Map.of("relationship", "friend", "strength", "strong")), 
                8.5, 
                Map.of("age", "25", "location", "NYC")
        );

        // When
        String json = objectMapper.writeValueAsString(node);

        // Then
        assertThat(json).contains("\"id\":\"node1\"");
        assertThat(json).contains("\"label\":\"User Alice\"");
        assertThat(json).contains("\"weight\":8.5");
        assertThat(json).contains("\"relationship\":\"friend\"");
    }

    @Test
    void pagedResult_ShouldContainAllElements() throws Exception {
        // Given
        PagedResult<BaseEntity> result = new PagedResult<>(
                Arrays.asList(
                    new TextEntity("txt1", "Sample Text", "Hello World", "UTF-8", "en"),
                    new NumericEntity("num1", "Sample Number", BigDecimal.valueOf(42), "units", 0)
                ),
                new PaginationInfo(0, 5, 20L, 4, true, false),
                Map.of("type", "all", "status", "active"),
                Arrays.asList(new SortCriteria("name", "ASC", 1))
        );

        // When
        String json = objectMapper.writeValueAsString(result);

        // Then
        assertThat(json).contains("\"page\":0");
        assertThat(json).contains("\"totalElements\":20");
        assertThat(json).contains("\"hasNext\":true");
        assertThat(json).contains("\"type\":\"text\"");
        assertThat(json).contains("\"type\":\"numeric\"");
    }

    @Test
    void boundedGeneric_ShouldWorkWithDifferentTypes() throws Exception {
        // Given - BigDecimal range
        BoundedGeneric<BigDecimal> numericRange = new BoundedGeneric<>(
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(1000),
                Arrays.asList(BigDecimal.valueOf(10), BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                Arrays.asList("eq", "gt", "lt", "gte", "lte", "between")
        );

        // When
        String json = objectMapper.writeValueAsString(numericRange);

        // Then
        assertThat(json).contains("\"min\":0");
        assertThat(json).contains("\"max\":1000");
        assertThat(json).contains("\"values\":[10,50,100]");
        assertThat(json).contains("\"operators\":[\"eq\",\"gt\",\"lt\"");
    }

    @Test
    void boundedGeneric_ShouldWorkWithLocalDateTime() throws Exception {
        // Given - LocalDateTime range
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
        BoundedGeneric<LocalDateTime> dateRange = new BoundedGeneric<>(
                start,
                end,
                Arrays.asList(LocalDateTime.of(2024, 6, 15, 12, 0)),
                Arrays.asList("before", "after", "between", "during")
        );

        // When
        String json = objectMapper.writeValueAsString(dateRange);

        // Then
        assertThat(json).contains("\"min\":");
        assertThat(json).contains("\"max\":");
        assertThat(json).contains("\"operators\":[\"before\",\"after\"");
    }

    @Test
    void wildcardContainer_ShouldHandlePolymorphicTypes() throws Exception {
        // Given
        WildcardContainer container = new WildcardContainer(
                Arrays.asList(
                    new TextEntity("txt1", "Text", "content", "UTF-8", "en"),
                    new NumericEntity("num1", "Number", BigDecimal.valueOf(42), "units", 0)
                ),
                Arrays.asList(new TextEntity("txt2", "Consumer Text", "data", "UTF-8", "en")),
                Map.of("key1", "value1", "key2", 42, "key3", true)
        );

        // When
        String json = objectMapper.writeValueAsString(container);

        // Then
        assertThat(json).contains("\"producer\":");
        assertThat(json).contains("\"consumer\":");
        assertThat(json).contains("\"transformer\":");
        assertThat(json).contains("\"type\":\"text\"");
        assertThat(json).contains("\"type\":\"numeric\"");
    }

    @Test
    void polymorphicEntities_ShouldIncludeTypeInformation() throws Exception {
        // Given
        List<BaseEntity> entities = Arrays.asList(
                new NumericEntity("num1", "Number", BigDecimal.valueOf(42.5), "units", 2),
                new TextEntity("txt1", "Text", "Hello", "UTF-8", "en"),
                new DateEntity("date1", "Date", LocalDateTime.of(2024, 1, 1, 12, 0), "UTC", "ISO"),
                new BooleanEntity("bool1", "Boolean", true, false)
        );

        // When
        String json = objectMapper.writeValueAsString(entities);

        // Then
        assertThat(json).contains("\"type\":\"numeric\"");
        assertThat(json).contains("\"type\":\"text\"");
        assertThat(json).contains("\"type\":\"date\"");
        assertThat(json).contains("\"type\":\"boolean\"");
        assertThat(json).contains("\"value\":42.5");
        assertThat(json).contains("\"content\":\"Hello\"");
    }

    @Test
    void analytics_ShouldHandleGenericTypes() throws Exception {
        // Given
        Analytics<BigDecimal> analytics = new Analytics<>(
                Map.of("metric1", 25.5, "metric2", 30.0),
                Map.of("group1", Arrays.asList(BigDecimal.valueOf(10.5), BigDecimal.valueOf(20.0))),
                Arrays.asList(
                    new TimeSeriesPoint<>(LocalDateTime.of(2024, 1, 1, 12, 0), BigDecimal.valueOf(15.5), 0.9),
                    new TimeSeriesPoint<>(LocalDateTime.of(2024, 1, 2, 12, 0), BigDecimal.valueOf(16.5), 0.8)
                ),
                Map.of("prediction1", new PredictionRange<>(BigDecimal.valueOf(10), BigDecimal.valueOf(30), BigDecimal.valueOf(20), 0.85))
        );

        // When
        String json = objectMapper.writeValueAsString(analytics);

        // Then
        assertThat(json).contains("\"metrics\":");
        assertThat(json).contains("\"aggregations\":");
        assertThat(json).contains("\"timeSeries\":");
        assertThat(json).contains("\"predictions\":");
        assertThat(json).contains("\"metric1\":25.5");
        assertThat(json).contains("\"confidence\":0.9");
        assertThat(json).contains("\"probability\":0.85");
    }

    @Test
    void paginationInfo_ShouldCalculateCorrectly() {
        // Given
        PaginationInfo pagination = new PaginationInfo(2, 10, 95L, 10, true, true);

        // Then
        assertThat(pagination.page()).isEqualTo(2);
        assertThat(pagination.size()).isEqualTo(10);
        assertThat(pagination.totalElements()).isEqualTo(95L);
        assertThat(pagination.totalPages()).isEqualTo(10);
        assertThat(pagination.hasNext()).isTrue();
        assertThat(pagination.hasPrevious()).isTrue();
    }

    @Test
    void sortCriteria_ShouldMaintainOrder() {
        // Given
        List<SortCriteria> sorting = Arrays.asList(
                new SortCriteria("name", "ASC", 1),
                new SortCriteria("date", "DESC", 2),
                new SortCriteria("priority", "ASC", 3)
        );

        // Then
        assertThat(sorting).hasSize(3);
        assertThat(sorting.get(0).field()).isEqualTo("name");
        assertThat(sorting.get(0).direction()).isEqualTo("ASC");
        assertThat(sorting.get(0).priority()).isEqualTo(1);
    }
}