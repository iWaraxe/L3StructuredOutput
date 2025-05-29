package com.coherentsolutions.l3structuredoutput.s16.reports;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Production-ready report generation service using AI for intelligent content creation
 */
@Service
public class ReportGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerationService.class);
    
    private final ChatClient chatClient;
    private final Map<ReportType, ReportTemplate> templates = new EnumMap<>(ReportType.class);
    private final ReportFormatter formatter = new ReportFormatter();
    private final ReportValidator validator = new ReportValidator();
    
    public ReportGenerationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        initializeTemplates();
    }
    
    /**
     * Generate comprehensive business report
     */
    public ReportGenerationResult generateReport(ReportRequest request) {
        logger.info("Generating {} report for period: {} to {}", 
                request.type(), request.startDate(), request.endDate());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Generate report sections in parallel
            CompletableFuture<ExecutiveSummary> summaryFuture = 
                    CompletableFuture.supplyAsync(() -> generateExecutiveSummary(request));
            
            CompletableFuture<List<ReportSection>> sectionsFuture = 
                    CompletableFuture.supplyAsync(() -> generateReportSections(request));
            
            CompletableFuture<ReportInsights> insightsFuture = 
                    CompletableFuture.supplyAsync(() -> generateInsights(request));
            
            CompletableFuture<List<Recommendation>> recommendationsFuture = 
                    CompletableFuture.supplyAsync(() -> generateRecommendations(request));
            
            // Wait for all sections to complete
            CompletableFuture.allOf(summaryFuture, sectionsFuture, insightsFuture, recommendationsFuture).join();
            
            // Assemble final report
            BusinessReport report = new BusinessReport(
                    generateReportId(),
                    request.type(),
                    request.title(),
                    summaryFuture.get(),
                    sectionsFuture.get(),
                    insightsFuture.get(),
                    recommendationsFuture.get(),
                    generateAppendices(request),
                    LocalDateTime.now(),
                    request.confidentialityLevel()
            );
            
            // Validate and format
            report = validator.validate(report);
            String formattedReport = formatter.format(report, request.outputFormat());
            
            long duration = System.currentTimeMillis() - startTime;
            
            return new ReportGenerationResult(
                    report,
                    formattedReport,
                    duration,
                    generateMetadata(report, request)
            );
            
        } catch (Exception e) {
            logger.error("Failed to generate report", e);
            throw new ReportGenerationException("Report generation failed", e);
        }
    }
    
    /**
     * Generate financial dashboard report
     */
    public FinancialDashboard generateFinancialDashboard(FinancialDataSet dataSet) {
        logger.info("Generating financial dashboard for {} metrics", dataSet.metrics().size());
        
        BeanOutputConverter<FinancialDashboard> converter = 
                new BeanOutputConverter<>(FinancialDashboard.class);
        
        String prompt = String.format("""
                Analyze this financial data and create a comprehensive dashboard:
                
                Revenue Data: %s
                Expense Data: %s
                Key Metrics: %s
                Period: %s to %s
                
                Generate insights, trends, and actionable recommendations.
                Include key performance indicators and variance analysis.
                """,
                formatFinancialData(dataSet.revenueData()),
                formatFinancialData(dataSet.expenseData()),
                String.join(", ", dataSet.metrics()),
                dataSet.startDate(),
                dataSet.endDate()
        );
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    /**
     * Generate operational performance report
     */
    public OperationalReport generateOperationalReport(OperationalData data) {
        BeanOutputConverter<OperationalReport> converter = 
                new BeanOutputConverter<>(OperationalReport.class);
        
        String prompt = String.format("""
                Create an operational performance report based on:
                
                Key Performance Indicators:
                %s
                
                Operational Metrics:
                %s
                
                Process Efficiency Data:
                %s
                
                Provide analysis of operational efficiency, bottlenecks, and improvement opportunities.
                """,
                formatKPIs(data.kpis()),
                formatMetrics(data.metrics()),
                formatProcessData(data.processData())
        );
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    /**
     * Generate market analysis report
     */
    public MarketAnalysisReport generateMarketAnalysis(MarketDataRequest request) {
        BeanOutputConverter<MarketAnalysisReport> converter = 
                new BeanOutputConverter<>(MarketAnalysisReport.class);
        
        String prompt = String.format("""
                Conduct comprehensive market analysis for:
                
                Industry: %s
                Geographic Region: %s
                Time Period: %s
                Market Segments: %s
                Competitive Landscape: %s
                
                Provide market sizing, growth trends, competitive positioning, 
                opportunities, and strategic recommendations.
                """,
                request.industry(),
                request.region(),
                request.timePeriod(),
                String.join(", ", request.segments()),
                String.join(", ", request.competitors())
        );
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    /**
     * Generate custom report from template
     */
    public CustomReport generateCustomReport(CustomReportRequest request) {
        logger.info("Generating custom report: {}", request.templateName());
        
        BeanOutputConverter<CustomReport> converter = 
                new BeanOutputConverter<>(CustomReport.class);
        
        String prompt = buildCustomPrompt(request);
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    /**
     * Export report to various formats
     */
    public ExportResult exportReport(BusinessReport report, ExportFormat format) {
        logger.info("Exporting report {} to format: {}", report.id(), format);
        
        try {
            String exportedContent = switch (format) {
                case PDF -> formatter.toPDF(report);
                case WORD -> formatter.toWord(report);
                case EXCEL -> formatter.toExcel(report);
                case HTML -> formatter.toHTML(report);
                case JSON -> formatter.toJSON(report);
                case CSV -> formatter.toCSV(report);
            };
            
            return new ExportResult(
                    report.id(),
                    format,
                    exportedContent,
                    generateFileName(report, format),
                    LocalDateTime.now()
            );
            
        } catch (Exception e) {
            logger.error("Failed to export report to {}", format, e);
            throw new ReportExportException("Export failed", e);
        }
    }
    
    // Private helper methods
    
    private ExecutiveSummary generateExecutiveSummary(ReportRequest request) {
        BeanOutputConverter<ExecutiveSummary> converter = 
                new BeanOutputConverter<>(ExecutiveSummary.class);
        
        String prompt = String.format("""
                Create an executive summary for a %s report covering:
                Period: %s to %s
                Key Areas: %s
                Stakeholders: %s
                
                Provide high-level insights, key findings, and critical recommendations
                suitable for C-level executives.
                """,
                request.type(),
                request.startDate(),
                request.endDate(),
                String.join(", ", request.sections()),
                String.join(", ", request.stakeholders())
        );
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    private List<ReportSection> generateReportSections(ReportRequest request) {
        return request.sections().parallelStream()
                .map(sectionName -> generateReportSection(sectionName, request))
                .collect(Collectors.toList());
    }
    
    private ReportSection generateReportSection(String sectionName, ReportRequest request) {
        BeanOutputConverter<ReportSection> converter = 
                new BeanOutputConverter<>(ReportSection.class);
        
        String prompt = String.format("""
                Generate detailed content for report section: %s
                
                Report Type: %s
                Context: %s
                Data Sources: %s
                
                Provide comprehensive analysis with supporting data and visualizations.
                """,
                sectionName,
                request.type(),
                request.context(),
                String.join(", ", request.dataSources())
        );
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    private ReportInsights generateInsights(ReportRequest request) {
        BeanOutputConverter<ReportInsights> converter = 
                new BeanOutputConverter<>(ReportInsights.class);
        
        String prompt = String.format("""
                Generate strategic insights for %s analysis:
                
                Business Context: %s
                Key Metrics: %s
                Industry: %s
                
                Identify trends, patterns, risks, and opportunities.
                """,
                request.type(),
                request.context(),
                String.join(", ", request.keyMetrics()),
                request.industry()
        );
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    private List<Recommendation> generateRecommendations(ReportRequest request) {
        BeanOutputConverter<RecommendationSet> converter = 
                new BeanOutputConverter<>(RecommendationSet.class);
        
        String prompt = String.format("""
                Generate actionable recommendations based on:
                
                Report Type: %s
                Business Objectives: %s
                Constraints: %s
                Timeline: %s
                
                Provide specific, measurable, and time-bound recommendations.
                """,
                request.type(),
                String.join(", ", request.objectives()),
                String.join(", ", request.constraints()),
                request.timeline()
        );
        
        RecommendationSet recommendationSet = chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
        
        return recommendationSet.recommendations();
    }
    
    private List<ReportAppendix> generateAppendices(ReportRequest request) {
        // Generate supporting appendices
        List<ReportAppendix> appendices = new ArrayList<>();
        
        if (request.includeDataSources()) {
            appendices.add(new ReportAppendix("Data Sources", 
                    "Detailed information about data sources used in this report",
                    generateDataSourcesContent(request.dataSources())));
        }
        
        if (request.includeMethodology()) {
            appendices.add(new ReportAppendix("Methodology", 
                    "Analysis methodology and assumptions",
                    generateMethodologyContent(request)));
        }
        
        return appendices;
    }
    
    private void initializeTemplates() {
        templates.put(ReportType.FINANCIAL, new ReportTemplate(
                ReportType.FINANCIAL,
                "Financial Performance Report",
                List.of("Revenue Analysis", "Cost Analysis", "Profitability", "Cash Flow"),
                "financial-template.json"
        ));
        
        templates.put(ReportType.OPERATIONAL, new ReportTemplate(
                ReportType.OPERATIONAL,
                "Operational Performance Report",
                List.of("Efficiency Metrics", "Process Performance", "Quality Indicators"),
                "operational-template.json"
        ));
        
        templates.put(ReportType.MARKET_ANALYSIS, new ReportTemplate(
                ReportType.MARKET_ANALYSIS,
                "Market Analysis Report",
                List.of("Market Size", "Competition", "Trends", "Opportunities"),
                "market-analysis-template.json"
        ));
    }
    
    private String buildCustomPrompt(CustomReportRequest request) {
        return String.format("""
                Generate a custom report using template: %s
                
                Parameters: %s
                Data Context: %s
                Output Requirements: %s
                
                Follow the template structure while incorporating the provided data.
                """,
                request.templateName(),
                formatParameters(request.parameters()),
                request.dataContext(),
                String.join(", ", request.outputRequirements())
        );
    }
    
    // Formatting helper methods
    
    private String formatFinancialData(Map<String, Double> data) {
        return data.entrySet().stream()
                .map(entry -> entry.getKey() + ": $" + String.format("%.2f", entry.getValue()))
                .collect(Collectors.joining(", "));
    }
    
    private String formatKPIs(Map<String, Object> kpis) {
        return kpis.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
    }
    
    private String formatMetrics(List<String> metrics) {
        return String.join(", ", metrics);
    }
    
    private String formatProcessData(Map<String, Object> processData) {
        return processData.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
    }
    
    private String formatParameters(Map<String, Object> parameters) {
        return parameters.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
    }
    
    private String generateReportId() {
        return "RPT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) +
                "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
    
    private ReportMetadata generateMetadata(BusinessReport report, ReportRequest request) {
        return new ReportMetadata(
                report.id(),
                request.type(),
                report.sections().size(),
                report.insights().keyInsights().size(),
                report.recommendations().size(),
                LocalDateTime.now(),
                request.confidentialityLevel()
        );
    }
    
    private String generateDataSourcesContent(List<String> dataSources) {
        return "Data sources used: " + String.join(", ", dataSources);
    }
    
    private String generateMethodologyContent(ReportRequest request) {
        return "Methodology: AI-powered analysis using Spring AI structured output for " + request.type();
    }
    
    private String generateFileName(BusinessReport report, ExportFormat format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = format.toString().toLowerCase();
        return String.format("%s_%s.%s", report.title().replaceAll("\\s+", "_"), timestamp, extension);
    }
    
    // Data models and records
    
    public record ReportRequest(
            ReportType type,
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
            String timeline,
            boolean includeDataSources,
            boolean includeMethodology,
            OutputFormat outputFormat,
            ConfidentialityLevel confidentialityLevel
    ) {}
    
    public record FinancialDataSet(
            Map<String, Double> revenueData,
            Map<String, Double> expenseData,
            List<String> metrics,
            String startDate,
            String endDate
    ) {}
    
    public record OperationalData(
            Map<String, Object> kpis,
            List<String> metrics,
            Map<String, Object> processData
    ) {}
    
    public record MarketDataRequest(
            String industry,
            String region,
            String timePeriod,
            List<String> segments,
            List<String> competitors
    ) {}
    
    public record CustomReportRequest(
            String templateName,
            Map<String, Object> parameters,
            String dataContext,
            List<String> outputRequirements
    ) {}
    
    public record BusinessReport(
            String id,
            ReportType type,
            String title,
            ExecutiveSummary executiveSummary,
            List<ReportSection> sections,
            ReportInsights insights,
            List<Recommendation> recommendations,
            List<ReportAppendix> appendices,
            LocalDateTime generatedAt,
            ConfidentialityLevel confidentiality
    ) {}
    
    public record ExecutiveSummary(
            @JsonPropertyDescription("High-level overview") String overview,
            @JsonPropertyDescription("Key findings") List<String> keyFindings,
            @JsonPropertyDescription("Critical recommendations") List<String> criticalRecommendations,
            @JsonPropertyDescription("Business impact assessment") String businessImpact
    ) {}
    
    public record ReportSection(
            @JsonPropertyDescription("Section title") String title,
            @JsonPropertyDescription("Section content") String content,
            @JsonPropertyDescription("Key data points") List<String> dataPoints,
            @JsonPropertyDescription("Charts and visualizations") List<String> visualizations
    ) {}
    
    public record ReportInsights(
            @JsonPropertyDescription("Primary insights") List<String> keyInsights,
            @JsonPropertyDescription("Trend analysis") String trendAnalysis,
            @JsonPropertyDescription("Risk assessment") List<String> risks,
            @JsonPropertyDescription("Opportunities identified") List<String> opportunities
    ) {}
    
    public record Recommendation(
            @JsonPropertyDescription("Recommendation title") String title,
            @JsonPropertyDescription("Detailed description") String description,
            @JsonPropertyDescription("Priority level") String priority,
            @JsonPropertyDescription("Implementation timeline") String timeline,
            @JsonPropertyDescription("Expected impact") String expectedImpact
    ) {}
    
    public record RecommendationSet(
            @JsonPropertyDescription("List of recommendations") List<Recommendation> recommendations
    ) {}
    
    public record ReportAppendix(
            String title,
            String description,
            String content
    ) {}
    
    public record FinancialDashboard(
            @JsonPropertyDescription("Revenue analysis") String revenueAnalysis,
            @JsonPropertyDescription("Expense breakdown") String expenseAnalysis,
            @JsonPropertyDescription("Profitability metrics") List<String> profitabilityMetrics,
            @JsonPropertyDescription("Financial trends") String trends,
            @JsonPropertyDescription("Key performance indicators") Map<String, String> kpis
    ) {}
    
    public record OperationalReport(
            @JsonPropertyDescription("Efficiency analysis") String efficiencyAnalysis,
            @JsonPropertyDescription("Process performance") String processPerformance,
            @JsonPropertyDescription("Improvement recommendations") List<String> improvements,
            @JsonPropertyDescription("Operational metrics") Map<String, String> metrics
    ) {}
    
    public record MarketAnalysisReport(
            @JsonPropertyDescription("Market size and growth") String marketSize,
            @JsonPropertyDescription("Competitive landscape") String competitiveLandscape,
            @JsonPropertyDescription("Market trends") List<String> trends,
            @JsonPropertyDescription("Strategic opportunities") List<String> opportunities,
            @JsonPropertyDescription("Market positioning") String positioning
    ) {}
    
    public record CustomReport(
            @JsonPropertyDescription("Report title") String title,
            @JsonPropertyDescription("Executive summary") String summary,
            @JsonPropertyDescription("Main content sections") List<String> sections,
            @JsonPropertyDescription("Key insights") List<String> insights,
            @JsonPropertyDescription("Recommendations") List<String> recommendations
    ) {}
    
    public record ReportGenerationResult(
            BusinessReport report,
            String formattedContent,
            long generationTimeMs,
            ReportMetadata metadata
    ) {}
    
    public record ReportMetadata(
            String reportId,
            ReportType type,
            int sectionCount,
            int insightCount,
            int recommendationCount,
            LocalDateTime generatedAt,
            ConfidentialityLevel confidentiality
    ) {}
    
    public record ExportResult(
            String reportId,
            ExportFormat format,
            String content,
            String fileName,
            LocalDateTime exportedAt
    ) {}
    
    public record ReportTemplate(
            ReportType type,
            String name,
            List<String> defaultSections,
            String templateFile
    ) {}
    
    public enum ReportType {
        FINANCIAL, OPERATIONAL, MARKET_ANALYSIS, CUSTOM, DASHBOARD
    }
    
    public enum OutputFormat {
        JSON, XML, HTML, MARKDOWN, PLAIN_TEXT
    }
    
    public enum ExportFormat {
        PDF, WORD, EXCEL, HTML, JSON, CSV
    }
    
    public enum ConfidentialityLevel {
        PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    }
    
    // Helper classes
    
    private static class ReportFormatter {
        public String format(BusinessReport report, OutputFormat format) {
            return switch (format) {
                case JSON -> toJSON(report);
                case XML -> toXML(report);
                case HTML -> toHTML(report);
                case MARKDOWN -> toMarkdown(report);
                case PLAIN_TEXT -> toPlainText(report);
            };
        }
        
        public String toPDF(BusinessReport report) { return "PDF content"; }
        public String toWord(BusinessReport report) { return "Word content"; }
        public String toExcel(BusinessReport report) { return "Excel content"; }
        public String toHTML(BusinessReport report) { return "HTML content"; }
        public String toJSON(BusinessReport report) { return "JSON content"; }
        public String toCSV(BusinessReport report) { return "CSV content"; }
        private String toXML(BusinessReport report) { return "XML content"; }
        private String toMarkdown(BusinessReport report) { return "Markdown content"; }
        private String toPlainText(BusinessReport report) { return "Plain text content"; }
    }
    
    private static class ReportValidator {
        public BusinessReport validate(BusinessReport report) {
            if (report.title() == null || report.title().trim().isEmpty()) {
                throw new IllegalArgumentException("Report title cannot be empty");
            }
            if (report.sections().isEmpty()) {
                throw new IllegalArgumentException("Report must have at least one section");
            }
            return report;
        }
    }
    
    public static class ReportGenerationException extends RuntimeException {
        public ReportGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class ReportExportException extends RuntimeException {
        public ReportExportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}