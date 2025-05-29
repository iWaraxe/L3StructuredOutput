package com.coherentsolutions.l3structuredoutput.s13.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class GenericModels {

    @JsonPropertyOrder({"metadata", "data", "relationships", "analytics"})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ComplexNestedStructure<T, U, V>(
            @JsonPropertyDescription("Metadata information about the structure")
            Map<String, Object> metadata,

            @JsonPropertyDescription("Primary data payload with generic type T")
            List<T> data,

            @JsonPropertyDescription("Relationships between entities of type U mapped by string keys")
            Map<String, Map<String, U>> relationships,

            @JsonPropertyDescription("Analytics data with nested generic structure V")
            Analytics<V> analytics
    ) {}

    @JsonPropertyOrder({"metrics", "aggregations", "timeSeries", "predictions"})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Analytics<T>(
            @JsonPropertyDescription("Basic metrics as key-value pairs")
            Map<String, Double> metrics,

            @JsonPropertyDescription("Aggregated data grouped by categories")
            Map<String, List<T>> aggregations,

            @JsonPropertyDescription("Time-based data series")
            List<TimeSeriesPoint<T>> timeSeries,

            @JsonPropertyDescription("Future predictions with confidence intervals")
            Map<String, PredictionRange<T>> predictions
    ) {}

    @JsonPropertyOrder({"timestamp", "value", "confidence"})
    public record TimeSeriesPoint<T>(
            @JsonPropertyDescription("Timestamp of the data point")
            LocalDateTime timestamp,

            @JsonPropertyDescription("Value at this time point")
            T value,

            @JsonPropertyDescription("Confidence level (0.0 to 1.0)")
            Double confidence
    ) {}

    @JsonPropertyOrder({"lower", "upper", "expected", "probability"})
    public record PredictionRange<T>(
            @JsonPropertyDescription("Lower bound of the prediction")
            T lower,

            @JsonPropertyDescription("Upper bound of the prediction")
            T upper,

            @JsonPropertyDescription("Expected/most likely value")
            T expected,

            @JsonPropertyDescription("Probability of this prediction")
            Double probability
    ) {}

    @JsonPropertyOrder({"name", "type", "children", "properties"})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RecursiveNode<T>(
            @JsonPropertyDescription("Name of the node")
            String name,

            @JsonPropertyDescription("Type classification of the node")
            String type,

            @JsonPropertyDescription("Child nodes in the recursive structure")
            List<RecursiveNode<T>> children,

            @JsonPropertyDescription("Properties associated with this node")
            T properties
    ) {}

    @JsonPropertyOrder({"id", "label", "connections", "weight", "metadata"})
    public record GraphNode<T, U>(
            @JsonPropertyDescription("Unique identifier for the node")
            String id,

            @JsonPropertyDescription("Human-readable label")
            String label,

            @JsonPropertyDescription("Connections to other nodes with edge data")
            Map<String, U> connections,

            @JsonPropertyDescription("Weight or importance of this node")
            T weight,

            @JsonPropertyDescription("Additional metadata")
            Map<String, Object> metadata
    ) {}

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = NumericEntity.class, name = "numeric"),
            @JsonSubTypes.Type(value = TextEntity.class, name = "text"),
            @JsonSubTypes.Type(value = DateEntity.class, name = "date"),
            @JsonSubTypes.Type(value = BooleanEntity.class, name = "boolean")
    })
    @JsonPropertyOrder({"id", "name", "type"})
    public abstract static class BaseEntity {
        @JsonPropertyDescription("Unique identifier")
        public final String id;

        @JsonPropertyDescription("Display name")
        public final String name;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        public abstract String getType();

        protected BaseEntity(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @JsonPropertyOrder({"id", "name", "type", "value", "unit", "precision"})
    public static class NumericEntity extends BaseEntity {
        @JsonPropertyDescription("Numeric value")
        public final BigDecimal value;

        @JsonPropertyDescription("Unit of measurement")
        public final String unit;

        @JsonPropertyDescription("Decimal precision")
        public final Integer precision;

        public NumericEntity(String id, String name, BigDecimal value, String unit, Integer precision) {
            super(id, name);
            this.value = value;
            this.unit = unit;
            this.precision = precision;
        }

        @Override
        public String getType() {
            return "numeric";
        }
    }

    @JsonPropertyOrder({"id", "name", "type", "content", "encoding", "language"})
    public static class TextEntity extends BaseEntity {
        @JsonPropertyDescription("Text content")
        public final String content;

        @JsonPropertyDescription("Character encoding")
        public final String encoding;

        @JsonPropertyDescription("Language code")
        public final String language;

        public TextEntity(String id, String name, String content, String encoding, String language) {
            super(id, name);
            this.content = content;
            this.encoding = encoding;
            this.language = language;
        }

        @Override
        public String getType() {
            return "text";
        }
    }

    @JsonPropertyOrder({"id", "name", "type", "timestamp", "timezone", "format"})
    public static class DateEntity extends BaseEntity {
        @JsonPropertyDescription("Date/time value")
        public final LocalDateTime timestamp;

        @JsonPropertyDescription("Timezone identifier")
        public final String timezone;

        @JsonPropertyDescription("Date format pattern")
        public final String format;

        public DateEntity(String id, String name, LocalDateTime timestamp, String timezone, String format) {
            super(id, name);
            this.timestamp = timestamp;
            this.timezone = timezone;
            this.format = format;
        }

        @Override
        public String getType() {
            return "date";
        }
    }

    @JsonPropertyOrder({"id", "name", "type", "value", "defaultValue"})
    public static class BooleanEntity extends BaseEntity {
        @JsonPropertyDescription("Boolean value")
        public final Boolean value;

        @JsonPropertyDescription("Default value when null")
        public final Boolean defaultValue;

        public BooleanEntity(String id, String name, Boolean value, Boolean defaultValue) {
            super(id, name);
            this.value = value;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getType() {
            return "boolean";
        }
    }

    @JsonPropertyOrder({"items", "pagination", "filters", "sorting"})
    public record PagedResult<T>(
            @JsonPropertyDescription("List of items for current page")
            List<T> items,

            @JsonPropertyDescription("Pagination information")
            PaginationInfo pagination,

            @JsonPropertyDescription("Applied filters")
            Map<String, Object> filters,

            @JsonPropertyDescription("Sorting criteria")
            List<SortCriteria> sorting
    ) {}

    @JsonPropertyOrder({"page", "size", "totalElements", "totalPages", "hasNext", "hasPrevious"})
    public record PaginationInfo(
            @JsonPropertyDescription("Current page number (0-based)")
            Integer page,

            @JsonPropertyDescription("Number of items per page")
            Integer size,

            @JsonPropertyDescription("Total number of elements")
            Long totalElements,

            @JsonPropertyDescription("Total number of pages")
            Integer totalPages,

            @JsonPropertyDescription("Whether there is a next page")
            Boolean hasNext,

            @JsonPropertyDescription("Whether there is a previous page")
            Boolean hasPrevious
    ) {}

    @JsonPropertyOrder({"field", "direction", "priority"})
    public record SortCriteria(
            @JsonPropertyDescription("Field name to sort by")
            String field,

            @JsonPropertyDescription("Sort direction (ASC or DESC)")
            String direction,

            @JsonPropertyDescription("Sort priority (lower numbers first)")
            Integer priority
    ) {}

    @JsonPropertyOrder({"min", "max", "values", "operators"})
    public record BoundedGeneric<T extends Comparable<? super T>>(
            @JsonPropertyDescription("Minimum allowed value")
            T min,

            @JsonPropertyDescription("Maximum allowed value")
            T max,

            @JsonPropertyDescription("List of values within bounds")
            List<T> values,

            @JsonPropertyDescription("Supported comparison operators")
            List<String> operators
    ) {}

    @JsonPropertyOrder({"producer", "consumer", "transformer"})
    public record WildcardContainer(
            @JsonPropertyDescription("Producer that extends BaseEntity")
            List<? extends BaseEntity> producer,

            @JsonPropertyDescription("Consumer that accepts BaseEntity supertypes")
            List<? super TextEntity> consumer,

            @JsonPropertyDescription("Transformer with unbounded wildcard")
            Map<String, ?> transformer
    ) {}
}