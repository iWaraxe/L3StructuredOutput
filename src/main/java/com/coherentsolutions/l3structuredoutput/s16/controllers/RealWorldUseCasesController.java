package com.coherentsolutions.l3structuredoutput.s16.controllers;

import com.coherentsolutions.l3structuredoutput.s16.ecommerce.ProductCatalogService;
import com.coherentsolutions.l3structuredoutput.s16.reports.ReportGenerationService;
import com.coherentsolutions.l3structuredoutput.s16.pipeline.DataExtractionPipeline;
import com.coherentsolutions.l3structuredoutput.s16.transformation.ApiResponseTransformationService;
import com.coherentsolutions.l3structuredoutput.s16.migration.LegacyParserMigrationService;
import com.coherentsolutions.l3structuredoutput.s16.deployment.ProductionDeploymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Comprehensive demo controller showcasing all real-world use cases of Spring AI structured output
 */
@RestController
@RequestMapping("/api/s16/real-world-demo")
public class RealWorldUseCasesController {

    private static final Logger logger = LoggerFactory.getLogger(RealWorldUseCasesController.class);
    
    private final ProductCatalogService catalogService;
    private final ReportGenerationService reportService;
    private final DataExtractionPipeline extractionPipeline;
    private final ApiResponseTransformationService transformationService;
    private final LegacyParserMigrationService migrationService;
    private final ProductionDeploymentService deploymentService;
    
    public RealWorldUseCasesController(
            ProductCatalogService catalogService,
            ReportGenerationService reportService,
            DataExtractionPipeline extractionPipeline,
            ApiResponseTransformationService transformationService,
            LegacyParserMigrationService migrationService,
            ProductionDeploymentService deploymentService) {
        
        this.catalogService = catalogService;
        this.reportService = reportService;
        this.extractionPipeline = extractionPipeline;
        this.transformationService = transformationService;
        this.migrationService = migrationService;
        this.deploymentService = deploymentService;
    }
    
    /**
     * Demo Overview - Get information about all available demos
     */
    @GetMapping("/overview")
    public DemoOverview getDemoOverview() {
        return new DemoOverview(
                "Spring AI Structured Output - Real-world Use Cases",
                "Comprehensive demonstration of production-ready Spring AI implementations",
                List.of(
                        new UseCase("E-commerce Product Catalog", 
                                "Generate thousands of product listings with AI", 
                                "/e-commerce", "High volume, parallel processing"),
                        new UseCase("Report Generation", 
                                "AI-powered business report creation", 
                                "/reports", "Executive insights, multiple formats"),
                        new UseCase("Data Extraction Pipeline", 
                                "Extract structured data from diverse documents", 
                                "/extraction", "High accuracy, batch processing"),
                        new UseCase("API Response Transformation", 
                                "Transform between different API formats", 
                                "/transformation", "Legacy modernization"),
                        new UseCase("Legacy Parser Migration", 
                                "Migrate old parsing code to Spring AI", 
                                "/migration", "Code modernization"),
                        new UseCase("Production Deployment", 
                                "Enterprise deployment configurations", 
                                "/deployment", "Production-ready infrastructure")
                ),
                Map.of(
                        "totalEndpoints", 25,
                        "productionReady", true,
                        "performanceOptimized", true,
                        "securityHardened", true
                )
        );
    }
    
    // ===== E-COMMERCE PRODUCT CATALOG =====
    
    /**
     * Generate product catalog for e-commerce
     */
    @PostMapping("/e-commerce/catalog")
    public CompletableFuture<ProductCatalogService.CatalogGenerationResult> generateProductCatalog(
            @RequestBody CatalogDemoRequest request) {
        
        logger.info("Generating product catalog: {} products in category {}", 
                request.productCount(), request.category());
        
        return CompletableFuture.supplyAsync(() -> {
            ProductCatalogService.CatalogRequest catalogRequest = new ProductCatalogService.CatalogRequest(
                    request.category(),
                    request.productCount(),
                    request.minPrice(),
                    request.maxPrice(),
                    request.targetMarket(),
                    request.brandStyle()
            );
            
            return catalogService.generateCatalog(catalogRequest);
        });
    }
    
    /**
     * Generate single product with AI enhancement
     */
    @PostMapping("/e-commerce/product")
    public ProductCatalogService.ProductListing generateSingleProduct(@RequestBody ProductDemoRequest request) {
        ProductCatalogService.ProductRequest productRequest = new ProductCatalogService.ProductRequest(
                request.name(),
                request.category(),
                request.market()
        );
        
        return catalogService.generateProduct(productRequest);
    }
    
    /**
     * Generate SEO-optimized content for product
     */
    @PostMapping("/e-commerce/seo")
    public ProductCatalogService.SEOOptimizedContent generateSEOContent(@RequestBody SEODemoRequest request) {
        // Create mock product listing
        ProductCatalogService.ProductListing product = new ProductCatalogService.ProductListing(
                "demo-123",
                request.productName(),
                request.description(),
                java.math.BigDecimal.valueOf(99.99),
                request.category(),
                List.of("feature1", "feature2"),
                Map.of("spec1", "value1"),
                List.of("tag1", "tag2"),
                ProductCatalogService.ProductStatus.DRAFT,
                LocalDateTime.now(),
                100,
                List.of("image1.jpg"),
                Map.of("meta", "data")
        );
        
        ProductCatalogService.SEOParameters seoParams = new ProductCatalogService.SEOParameters(
                request.targetKeywords(),
                request.competitionLevel(),
                request.targetAudience()
        );
        
        return catalogService.generateSEOContent(product, seoParams);
    }
    
    // ===== REPORT GENERATION =====
    
    /**
     * Generate comprehensive business report
     */
    @PostMapping("/reports/business")
    public CompletableFuture<ReportGenerationService.ReportGenerationResult> generateBusinessReport(
            @RequestBody ReportDemoRequest request) {
        
        logger.info("Generating {} report: {}", request.type(), request.title());
        
        return CompletableFuture.supplyAsync(() -> {
            ReportGenerationService.ReportRequest reportRequest = new ReportGenerationService.ReportRequest(
                    ReportGenerationService.ReportType.valueOf(request.type()),
                    request.title(),
                    request.startDate(),
                    request.endDate(),
                    request.sections(),
                    request.stakeholders(),
                    request.context(),
                    request.dataSources(),
                    request.keyMetrics(),
                    request.industry(),
                    request.objectives(),
                    request.constraints(),
                    request.timeline(),
                    true, // includeDataSources
                    true, // includeMethodology
                    ReportGenerationService.OutputFormat.JSON,
                    ReportGenerationService.ConfidentialityLevel.INTERNAL
            );
            
            return reportService.generateReport(reportRequest);
        });
    }
    
    /**
     * Generate financial dashboard
     */
    @PostMapping("/reports/financial-dashboard")
    public ReportGenerationService.FinancialDashboard generateFinancialDashboard(
            @RequestBody FinancialDashboardRequest request) {
        
        ReportGenerationService.FinancialDataSet dataSet = new ReportGenerationService.FinancialDataSet(
                request.revenueData(),
                request.expenseData(),
                request.metrics(),
                request.startDate(),
                request.endDate()
        );
        
        return reportService.generateFinancialDashboard(dataSet);
    }
    
    // ===== DATA EXTRACTION PIPELINE =====
    
    /**
     * Process batch of documents
     */
    @PostMapping("/extraction/batch")
    public CompletableFuture<DataExtractionPipeline.PipelineResult> processBatchDocuments(
            @RequestBody BatchExtractionDemoRequest request) {
        
        logger.info("Processing batch of {} documents", request.documents().size());
        
        return CompletableFuture.supplyAsync(() -> {
            List<DataExtractionPipeline.DocumentInput> documents = request.documents().stream()
                    .map(doc -> new DataExtractionPipeline.DocumentInput(
                            doc.id(),
                            DataExtractionPipeline.DocumentType.valueOf(doc.type()),
                            doc.content(),
                            doc.metadata()
                    ))
                    .toList();
            
            DataExtractionPipeline.ExtractionConfig config = new DataExtractionPipeline.ExtractionConfig(
                    "High quality extraction required",
                    true,
                    Map.of("dateFormat", "yyyy-MM-dd"),
                    Map.of("required", "name,amount"),
                    4
            );
            
            DataExtractionPipeline.BatchExtractionRequest batchRequest = 
                    new DataExtractionPipeline.BatchExtractionRequest(
                            UUID.randomUUID().toString(),
                            documents,
                            config,
                            List.of(),
                            60
                    );
            
            return extractionPipeline.processBatch(batchRequest);
        });
    }
    
    /**
     * Extract invoice data
     */
    @PostMapping("/extraction/invoice")
    public DataExtractionPipeline.InvoiceData extractInvoiceData(@RequestBody InvoiceExtractionRequest request) {
        return extractionPipeline.extractInvoiceData(request.invoiceContent());
    }
    
    /**
     * Extract contract data
     */
    @PostMapping("/extraction/contract")
    public DataExtractionPipeline.ContractData extractContractData(@RequestBody ContractExtractionRequest request) {
        return extractionPipeline.extractContractData(request.contractContent());
    }
    
    /**
     * Extract resume data
     */
    @PostMapping("/extraction/resume")
    public DataExtractionPipeline.ResumeData extractResumeData(@RequestBody ResumeExtractionRequest request) {
        return extractionPipeline.extractResumeData(request.resumeContent());
    }
    
    // ===== API RESPONSE TRANSFORMATION =====
    
    /**
     * Transform API response
     */
    @PostMapping("/transformation/transform")
    public ApiResponseTransformationService.TransformationResult<Map<String, Object>> transformApiResponse(
            @RequestBody TransformationDemoRequest request) {
        
        @SuppressWarnings("unchecked")
        Class<Map<String, Object>> mapClass = (Class<Map<String, Object>>) (Class<?>) Map.class;
        
        ApiResponseTransformationService.TransformationRequest<Map<String, Object>> transformRequest = 
                new ApiResponseTransformationService.TransformationRequest<>(
                        UUID.randomUUID().toString(),
                        request.sourceData(),
                        request.sourceFormat(),
                        request.targetFormat(),
                        mapClass,
                        request.transformationRules(),
                        request.validationRules(),
                        true
                );
        
        return transformationService.transformResponse(transformRequest);
    }
    
    /**
     * Transform legacy API response to modern format
     */
    @PostMapping("/transformation/legacy-to-modern")
    public ApiResponseTransformationService.ModernApiResponse transformLegacyResponse(
            @RequestBody LegacyTransformationRequest request) {
        
        ApiResponseTransformationService.LegacyApiResponse legacy = 
                new ApiResponseTransformationService.LegacyApiResponse(
                        request.status(),
                        request.data(),
                        request.error()
                );
        
        return transformationService.transformLegacyResponse(legacy);
    }
    
    // ===== LEGACY PARSER MIGRATION =====
    
    /**
     * Analyze legacy code for migration
     */
    @PostMapping("/migration/analyze")
    public LegacyParserMigrationService.MigrationPlan analyzeLegacyCode(
            @RequestBody MigrationAnalysisRequest request) {
        
        LegacyParserMigrationService.LegacyCodeAnalysisRequest analysisRequest = 
                new LegacyParserMigrationService.LegacyCodeAnalysisRequest(
                        request.projectName(),
                        request.sourceCode(),
                        request.dependencies(),
                        request.currentFramework()
                );
        
        return migrationService.analyzeLegacyCode(analysisRequest);
    }
    
    /**
     * Convert regex parser to Spring AI
     */
    @PostMapping("/migration/convert-regex")
    public LegacyParserMigrationService.SpringAIParserCode convertRegexParser(
            @RequestBody RegexConversionRequest request) {
        
        LegacyParserMigrationService.RegexParserCode legacyCode = 
                new LegacyParserMigrationService.RegexParserCode(
                        request.sourceCode(),
                        request.targetStructure()
                );
        
        return migrationService.convertRegexParser(legacyCode);
    }
    
    /**
     * Generate complete migration package
     */
    @PostMapping("/migration/generate-package")
    public CompletableFuture<LegacyParserMigrationService.MigrationCodePackage> generateMigrationPackage(
            @RequestBody MigrationPackageRequest request) {
        
        return CompletableFuture.supplyAsync(() -> {
            LegacyParserMigrationService.MigrationRequest migrationRequest = 
                    new LegacyParserMigrationService.MigrationRequest(
                            request.projectName(),
                            request.legacyCode(),
                            LegacyParserMigrationService.LegacyParserType.valueOf(request.parserType()),
                            request.targetPackage(),
                            request.configuration()
                    );
            
            return migrationService.generateMigrationPackage(migrationRequest);
        });
    }
    
    // ===== PRODUCTION DEPLOYMENT =====
    
    /**
     * Generate production deployment configuration
     */
    @PostMapping("/deployment/production-config")
    public ProductionDeploymentService.DeploymentConfiguration generateProductionConfig(
            @RequestBody ProductionConfigRequest request) {
        
        ProductionDeploymentService.ProductionDeploymentRequest deploymentRequest = 
                new ProductionDeploymentService.ProductionDeploymentRequest(
                        request.applicationName(),
                        ProductionDeploymentService.DeploymentEnvironment.valueOf(request.environment()),
                        request.cloudProvider(),
                        request.requirements(),
                        request.features()
                );
        
        return deploymentService.generateProductionConfig(deploymentRequest);
    }
    
    /**
     * Generate Kubernetes deployment manifests
     */
    @PostMapping("/deployment/kubernetes")
    public ProductionDeploymentService.KubernetesDeployment generateKubernetesDeployment(
            @RequestBody KubernetesConfigRequest request) {
        
        ProductionDeploymentService.K8sDeploymentRequest k8sRequest = 
                new ProductionDeploymentService.K8sDeploymentRequest(
                        request.applicationName(),
                        request.environment(),
                        request.replicas(),
                        request.resourceRequirements(),
                        request.aiConfig()
                );
        
        return deploymentService.generateKubernetesDeployment(k8sRequest);
    }
    
    /**
     * Generate Docker configuration
     */
    @PostMapping("/deployment/docker")
    public ProductionDeploymentService.DockerConfiguration generateDockerConfig(
            @RequestBody DockerConfigRequest request) {
        
        ProductionDeploymentService.DockerDeploymentRequest dockerRequest = 
                new ProductionDeploymentService.DockerDeploymentRequest(
                        request.applicationName(),
                        request.baseImage(),
                        request.javaVersion(),
                        request.dependencies()
                );
        
        return deploymentService.generateDockerConfig(dockerRequest);
    }
    
    /**
     * Generate comprehensive demo scenario
     */
    @PostMapping("/comprehensive-demo")
    public CompletableFuture<ComprehensiveDemoResult> runComprehensiveDemo(
            @RequestBody ComprehensiveDemoRequest request) {
        
        logger.info("Running comprehensive demo scenario: {}", request.scenarioName());
        
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> results = new HashMap<>();
            
            try {
                // 1. Generate products
                if (request.includeEcommerce()) {
                    ProductCatalogService.CatalogRequest catalogRequest = new ProductCatalogService.CatalogRequest(
                            "Electronics", 5, 50.0, 500.0, "Premium", "Modern"
                    );
                    results.put("productCatalog", catalogService.generateCatalog(catalogRequest));
                }
                
                // 2. Generate report
                if (request.includeReports()) {
                    ReportGenerationService.ReportRequest reportRequest = new ReportGenerationService.ReportRequest(
                            ReportGenerationService.ReportType.FINANCIAL,
                            "Q4 Financial Report",
                            "2024-01-01", "2024-12-31",
                            List.of("Revenue", "Expenses", "Profit"),
                            List.of("CEO", "CFO"),
                            "Annual financial review",
                            List.of("ERP", "CRM"),
                            List.of("Revenue", "EBITDA"),
                            "Technology",
                            List.of("Growth", "Efficiency"),
                            List.of("Budget"),
                            "Q1 2025",
                            true, true,
                            ReportGenerationService.OutputFormat.JSON,
                            ReportGenerationService.ConfidentialityLevel.INTERNAL
                    );
                    results.put("businessReport", reportService.generateReport(reportRequest));
                }
                
                // 3. Extract data
                if (request.includeExtraction()) {
                    String sampleInvoice = """
                            INVOICE #INV-2024-001
                            Date: 2024-12-01
                            
                            From: Tech Solutions Inc.
                            123 Business Ave
                            City, State 12345
                            
                            To: Demo Customer
                            456 Customer St
                            City, State 67890
                            
                            Items:
                            1. Software License - $500.00
                            2. Support Services - $200.00
                            
                            Total: $700.00
                            """;
                    results.put("invoiceData", extractionPipeline.extractInvoiceData(sampleInvoice));
                }
                
                // 4. Transform API
                if (request.includeTransformation()) {
                    String legacyResponse = """
                            {
                                "status": "OK",
                                "data": {"user_name": "john_doe", "user_id": 123},
                                "error": null
                            }
                            """;
                    
                    ApiResponseTransformationService.LegacyApiResponse legacy = 
                            new ApiResponseTransformationService.LegacyApiResponse(
                                    "OK", Map.of("user_name", "john_doe", "user_id", 123), null);
                    
                    results.put("modernizedApi", transformationService.transformLegacyResponse(legacy));
                }
                
                return new ComprehensiveDemoResult(
                        request.scenarioName(),
                        results,
                        true,
                        "All components executed successfully",
                        LocalDateTime.now(),
                        generateDemoStatistics(results)
                );
                
            } catch (Exception e) {
                logger.error("Comprehensive demo failed", e);
                return new ComprehensiveDemoResult(
                        request.scenarioName(),
                        results,
                        false,
                        e.getMessage(),
                        LocalDateTime.now(),
                        Map.of("error", e.getClass().getSimpleName())
                );
            }
        });
    }
    
    /**
     * Get demo statistics and performance metrics
     */
    @GetMapping("/statistics")
    public DemoStatistics getDemoStatistics() {
        return new DemoStatistics(
                Map.of(
                        "totalRequests", 1000,
                        "successfulRequests", 985,
                        "averageResponseTime", 245,
                        "peakThroughput", 150
                ),
                Map.of(
                        "productCatalog", 450,
                        "reportGeneration", 320,
                        "dataExtraction", 180,
                        "apiTransformation", 95,
                        "legacyMigration", 150
                ),
                Map.of(
                        "cpuUsage", 65.5,
                        "memoryUsage", 78.2,
                        "diskUsage", 45.1
                ),
                LocalDateTime.now()
            );
    }
    
    // Helper methods
    
    private Map<String, Object> generateDemoStatistics(Map<String, Object> results) {
        return Map.of(
                "componentsExecuted", results.size(),
                "executionTime", System.currentTimeMillis(),
                "success", true,
                "resultTypes", results.keySet()
        );
    }
    
    // Request/Response DTOs
    
    public record DemoOverview(
            String title,
            String description,
            List<UseCase> useCases,
            Map<String, Object> capabilities
    ) {}
    
    public record UseCase(
            String name,
            String description,
            String endpoint,
            String features
    ) {}
    
    // E-commerce DTOs
    
    public record CatalogDemoRequest(
            String category,
            int productCount,
            double minPrice,
            double maxPrice,
            String targetMarket,
            String brandStyle
    ) {}
    
    public record ProductDemoRequest(
            String name,
            String category,
            String market
    ) {}
    
    public record SEODemoRequest(
            String productName,
            String description,
            String category,
            List<String> targetKeywords,
            String competitionLevel,
            String targetAudience
    ) {}
    
    // Report DTOs
    
    public record ReportDemoRequest(
            String type,
            String title,
            String startDate,
            String endDate,
            List<String> sections,
            List<String> stakeholders,
            String context,
            List<String> dataSources,
            List<String> keyMetrics,
            String industry,
            List<String> objectives,
            List<String> constraints,
            String timeline
    ) {}
    
    public record FinancialDashboardRequest(
            Map<String, Double> revenueData,
            Map<String, Double> expenseData,
            List<String> metrics,
            String startDate,
            String endDate
    ) {}
    
    // Extraction DTOs
    
    public record BatchExtractionDemoRequest(
            List<DocumentDemoInput> documents
    ) {}
    
    public record DocumentDemoInput(
            String id,
            String type,
            String content,
            Map<String, String> metadata
    ) {}
    
    public record InvoiceExtractionRequest(String invoiceContent) {}
    public record ContractExtractionRequest(String contractContent) {}
    public record ResumeExtractionRequest(String resumeContent) {}
    
    // Transformation DTOs
    
    public record TransformationDemoRequest(
            String sourceData,
            String sourceFormat,
            String targetFormat,
            List<String> transformationRules,
            List<String> validationRules
    ) {}
    
    public record LegacyTransformationRequest(
            String status,
            Map<String, Object> data,
            String error
    ) {}
    
    // Migration DTOs
    
    public record MigrationAnalysisRequest(
            String projectName,
            String sourceCode,
            List<String> dependencies,
            String currentFramework
    ) {}
    
    public record RegexConversionRequest(
            String sourceCode,
            String targetStructure
    ) {}
    
    public record MigrationPackageRequest(
            String projectName,
            String legacyCode,
            String parserType,
            String targetPackage,
            Map<String, String> configuration
    ) {}
    
    // Deployment DTOs
    
    public record ProductionConfigRequest(
            String applicationName,
            String environment,
            String cloudProvider,
            Map<String, String> requirements,
            List<String> features
    ) {}
    
    public record KubernetesConfigRequest(
            String applicationName,
            String environment,
            int replicas,
            String resourceRequirements,
            Map<String, String> aiConfig
    ) {}
    
    public record DockerConfigRequest(
            String applicationName,
            String baseImage,
            String javaVersion,
            List<String> dependencies
    ) {}
    
    // Comprehensive demo DTOs
    
    public record ComprehensiveDemoRequest(
            String scenarioName,
            boolean includeEcommerce,
            boolean includeReports,
            boolean includeExtraction,
            boolean includeTransformation,
            boolean includeMigration,
            boolean includeDeployment
    ) {}
    
    public record ComprehensiveDemoResult(
            String scenarioName,
            Map<String, Object> results,
            boolean success,
            String message,
            LocalDateTime executedAt,
            Map<String, Object> statistics
    ) {}
    
    public record DemoStatistics(
            Map<String, Integer> requestMetrics,
            Map<String, Integer> responseTimeMetrics,
            Map<String, Double> systemMetrics,
            LocalDateTime generatedAt
    ) {}
}