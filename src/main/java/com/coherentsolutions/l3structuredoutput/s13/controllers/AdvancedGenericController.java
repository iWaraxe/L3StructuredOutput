package com.coherentsolutions.l3structuredoutput.s13.controllers;

import com.coherentsolutions.l3structuredoutput.s13.models.GenericModels.*;
import com.coherentsolutions.l3structuredoutput.s13.services.AdvancedGenericService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advanced-generics")
public class AdvancedGenericController {

    private final AdvancedGenericService genericService;

    public AdvancedGenericController(AdvancedGenericService genericService) {
        this.genericService = genericService;
    }

    @GetMapping("/complex-analysis")
    public ComplexNestedStructure<Map<String, Object>, BaseEntity, BigDecimal> analyzeComplexData(
            @RequestParam(defaultValue = "financial") String dataType,
            @RequestParam(defaultValue = "quarterly") String analysisScope,
            @RequestParam(defaultValue = "10") int itemCount) {
        
        return genericService.analyzeComplexData(dataType, analysisScope, itemCount);
    }

    @GetMapping("/hierarchy")
    public RecursiveNode<Map<String, Object>> buildHierarchy(
            @RequestParam(defaultValue = "organization") String domain,
            @RequestParam(defaultValue = "4") int maxDepth) {
        
        return genericService.buildHierarchy(domain, maxDepth);
    }

    @GetMapping("/graph")
    public List<GraphNode<Double, Map<String, String>>> createGraph(
            @RequestParam(defaultValue = "social") String graphType,
            @RequestParam(defaultValue = "8") int nodeCount) {
        
        return genericService.createGraph(graphType, nodeCount);
    }

    @GetMapping("/paged-entities")
    public PagedResult<BaseEntity> getPagedEntities(
            @RequestParam(defaultValue = "user") String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        return genericService.getPagedEntities(entityType, page, size);
    }

    @GetMapping("/bounded-numeric")
    public BoundedGeneric<BigDecimal> createBoundedNumericRange(
            @RequestParam(defaultValue = "product_price") String context,
            @RequestParam(defaultValue = "USD") String unit) {
        
        return genericService.createBoundedNumericRange(context, unit);
    }

    @GetMapping("/bounded-date")
    public BoundedGeneric<LocalDateTime> createBoundedDateRange(
            @RequestParam(defaultValue = "project_timeline") String timeContext) {
        
        return genericService.createBoundedDateRange(timeContext);
    }

    @GetMapping("/wildcards")
    public WildcardContainer demonstrateWildcards(
            @RequestParam(defaultValue = "data_processing") String scenario) {
        
        return genericService.demonstrateWildcards(scenario);
    }

    @GetMapping("/polymorphic-collections")
    public Map<String, List<? extends BaseEntity>> getPolymorphicCollections(
            @RequestParam(defaultValue = "ecommerce") String domain) {
        
        return genericService.getPolymorphicCollections(domain);
    }

    @PostMapping("/batch-analysis")
    public Map<String, ComplexNestedStructure<Map<String, Object>, BaseEntity, BigDecimal>> batchAnalysis(
            @RequestBody Map<String, String> analysisRequests) {
        
        return analysisRequests.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> genericService.analyzeComplexData(
                                entry.getValue(),
                                "batch",
                                5
                        )
                ));
    }

    @PostMapping("/recursive-comparison")
    public Map<String, RecursiveNode<Map<String, Object>>> compareHierarchies(
            @RequestBody List<String> domains) {
        
        return domains.stream()
                .collect(java.util.stream.Collectors.toMap(
                        domain -> domain,
                        domain -> genericService.buildHierarchy(domain, 3)
                ));
    }

    @GetMapping("/type-demonstration")
    public Map<String, Object> demonstrateTypeComplexity() {
        
        return Map.of(
                "complex_nested", genericService.analyzeComplexData("sample", "demo", 3),
                "recursive_tree", genericService.buildHierarchy("demo", 2),
                "graph_nodes", genericService.createGraph("demo", 4),
                "paged_data", genericService.getPagedEntities("demo", 0, 3),
                "bounded_range", genericService.createBoundedNumericRange("demo", "units"),
                "wildcard_demo", genericService.demonstrateWildcards("demo"),
                "polymorphic_data", genericService.getPolymorphicCollections("demo")
        );
    }
}