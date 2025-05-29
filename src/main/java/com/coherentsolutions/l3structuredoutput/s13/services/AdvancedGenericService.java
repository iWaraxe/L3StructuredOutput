package com.coherentsolutions.l3structuredoutput.s13.services;

import com.coherentsolutions.l3structuredoutput.s13.models.GenericModels.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AdvancedGenericService {

    private final ChatClient chatClient;

    public AdvancedGenericService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public ComplexNestedStructure<Map<String, Object>, BaseEntity, BigDecimal> analyzeComplexData(
            String dataType, String analysisScope, int itemCount) {

        var converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<ComplexNestedStructure<Map<String, Object>, BaseEntity, BigDecimal>>() {}
        );

        String prompt = String.format("""
                Analyze %s data with scope: %s for %d items.
                
                Create a complex nested structure containing:
                1. Metadata with general information about the analysis
                2. Data as a list of maps containing sample data points
                3. Relationships mapping categories to entity connections
                4. Analytics with metrics, aggregations, time series, and predictions
                
                For relationships, use BaseEntity polymorphic types (numeric, text, date, boolean).
                For analytics, use BigDecimal values for precise calculations.
                Include realistic data and meaningful relationships.
                
                %s
                """, dataType, analysisScope, itemCount, converter.getFormat());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(converter);
    }

    public RecursiveNode<Map<String, Object>> buildHierarchy(String domain, int maxDepth) {
        var converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<RecursiveNode<Map<String, Object>>>() {}
        );

        String prompt = String.format("""
                Build a recursive hierarchical structure for domain: %s with max depth: %d.
                
                Create a tree structure where:
                - Each node has a name, type, and properties (as a map)
                - Nodes can have children (recursive structure)
                - Properties should contain relevant domain-specific data
                - Include at least 3 levels of nesting where appropriate
                - Each level should have different types of nodes
                
                Example domains: organizational structure, file system, category taxonomy, etc.
                
                %s
                """, domain, maxDepth, converter.getFormat());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(converter);
    }

    public List<GraphNode<Double, Map<String, String>>> createGraph(String graphType, int nodeCount) {
        var converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<List<GraphNode<Double, Map<String, String>>>>() {}
        );

        String prompt = String.format("""
                Create a graph structure of type: %s with %d nodes.
                
                Generate a list of graph nodes where:
                - Each node has a unique ID and descriptive label
                - Weight is a Double representing importance/value
                - Connections map to other node IDs with edge metadata
                - Edge metadata includes relationship type and strength
                - Create realistic connections between nodes
                
                Graph types: social network, dependency graph, knowledge graph, etc.
                
                %s
                """, graphType, nodeCount, converter.getFormat());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(converter);
    }

    public PagedResult<BaseEntity> getPagedEntities(String entityType, int page, int size) {
        var converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<PagedResult<BaseEntity>>() {}
        );

        String prompt = String.format("""
                Generate a paged result for %s entities (page %d, size %d).
                
                Create a paged result containing:
                - Items: List of polymorphic BaseEntity objects (mix of numeric, text, date, boolean types)
                - Pagination: Complete pagination information
                - Filters: Sample filters that could be applied
                - Sorting: Multiple sort criteria
                
                Use realistic data for each entity type with proper values.
                Ensure pagination math is correct (totalElements, totalPages, hasNext, hasPrevious).
                
                %s
                """, entityType, page, size, converter.getFormat());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(converter);
    }

    public BoundedGeneric<BigDecimal> createBoundedNumericRange(String context, String unit) {
        var converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<BoundedGeneric<BigDecimal>>() {}
        );

        String prompt = String.format("""
                Create a bounded numeric range for %s measured in %s.
                
                Generate:
                - Realistic min and max bounds for the context
                - List of sample values within the bounds
                - Supported comparison operators (eq, gt, lt, gte, lte, between)
                
                Context examples: price range, temperature range, score range, etc.
                
                %s
                """, context, unit, converter.getFormat());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(converter);
    }

    public BoundedGeneric<LocalDateTime> createBoundedDateRange(String timeContext) {
        var converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<BoundedGeneric<LocalDateTime>>() {}
        );

        String prompt = String.format("""
                Create a bounded date/time range for %s.
                
                Generate:
                - Realistic min and max date bounds
                - List of sample timestamps within the range
                - Supported temporal operators (before, after, between, during)
                
                Use ISO 8601 format for dates.
                
                %s
                """, timeContext, converter.getFormat());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(converter);
    }

    public WildcardContainer demonstrateWildcards(String scenario) {
        var converter = new BeanOutputConverter<>(WildcardContainer.class);

        String prompt = String.format("""
                Create a wildcard container demonstration for scenario: %s.
                
                Generate:
                - Producer: List of BaseEntity subtypes (mix of all 4 types)
                - Consumer: List that can accept TextEntity or its supertypes
                - Transformer: Map with string keys and various object values
                
                Show how wildcards work with inheritance and type safety.
                
                %s
                """, scenario, converter.getFormat());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(converter);
    }

    public Map<String, List<? extends BaseEntity>> getPolymorphicCollections(String domain) {
        var converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<Map<String, List<? extends BaseEntity>>>() {}
        );

        String prompt = String.format("""
                Create polymorphic collections for domain: %s.
                
                Generate a map where:
                - Keys are category names
                - Values are lists of different BaseEntity types
                - Each category should contain mixed entity types
                - Show polymorphism in action with inheritance
                
                Include realistic data for each entity type.
                
                %s
                """, domain, converter.getFormat());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(converter);
    }
}