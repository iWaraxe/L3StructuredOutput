package com.coherentsolutions.l3structuredoutput.s16.pipeline;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Production-ready data extraction pipeline using AI for intelligent document processing
 */
@Service
public class DataExtractionPipeline {

    private static final Logger logger = LoggerFactory.getLogger(DataExtractionPipeline.class);
    
    private final ChatClient chatClient;
    private final ExecutorService extractionExecutor;
    private final Map<DocumentType, ExtractionTemplate> templates = new EnumMap<>(DocumentType.class);
    private final DataValidator validator = new DataValidator();
    private final QualityAssessment qualityAssessment = new QualityAssessment();
    
    public DataExtractionPipeline(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.extractionExecutor = Executors.newFixedThreadPool(10);
        initializeTemplates();
    }
    
    /**
     * Process batch of documents with parallel extraction
     */
    public PipelineResult processBatch(BatchExtractionRequest request) {
        logger.info("Processing batch of {} documents", request.documents().size());
        
        long startTime = System.currentTimeMillis();
        List<ExtractionResult> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            // Process documents in parallel
            List<CompletableFuture<ExtractionResult>> futures = request.documents().stream()
                    .map(doc -> CompletableFuture.supplyAsync(() -> 
                            processDocument(doc, request.extractionConfig()), extractionExecutor))
                    .collect(Collectors.toList());
            
            // Collect results with timeout
            for (CompletableFuture<ExtractionResult> future : futures) {
                try {
                    ExtractionResult result = future.get(request.timeoutSeconds(), TimeUnit.SECONDS);
                    results.add(result);
                } catch (Exception e) {
                    logger.error("Document processing failed", e);
                    errors.add("Processing failed: " + e.getMessage());
                }
            }
            
            // Apply post-processing
            results = applyPostProcessing(results, request.postProcessingRules());
            
            long duration = System.currentTimeMillis() - startTime;
            
            return new PipelineResult(
                    results,
                    results.size(),
                    errors.size(),
                    duration,
                    calculateQualityMetrics(results),
                    generatePipelineMetadata(request, results)
            );
            
        } catch (Exception e) {
            logger.error("Critical error in batch processing", e);
            throw new PipelineException("Batch processing failed", e);
        }
    }
    
    /**
     * Extract structured data from single document
     */
    public ExtractionResult processDocument(DocumentInput document, ExtractionConfig config) {
        logger.debug("Processing document: {} (type: {})", document.id(), document.type());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Pre-process document
            String processedContent = preProcessDocument(document);
            
            // Extract data based on document type
            StructuredData extractedData = extractStructuredData(processedContent, document.type(), config);
            
            // Validate extraction quality
            QualityMetrics quality = qualityAssessment.assess(extractedData, document);
            
            // Post-process if needed
            if (config.enablePostProcessing()) {
                extractedData = postProcessExtractedData(extractedData, config);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            return new ExtractionResult(
                    document.id(),
                    document.type(),
                    extractedData,
                    quality,
                    duration,
                    ExtractionStatus.SUCCESS,
                    null
            );
            
        } catch (Exception e) {
            logger.error("Failed to process document: {}", document.id(), e);
            long duration = System.currentTimeMillis() - startTime;
            
            return new ExtractionResult(
                    document.id(),
                    document.type(),
                    null,
                    new QualityMetrics(0.0, 0.0, 0.0, List.of("Processing failed")),
                    duration,
                    ExtractionStatus.FAILED,
                    e.getMessage()
            );
        }
    }
    
    /**
     * Extract invoice data
     */
    public InvoiceData extractInvoiceData(String invoiceContent) {
        BeanOutputConverter<InvoiceData> converter = new BeanOutputConverter<>(InvoiceData.class);
        
        String prompt = String.format("""
                Extract structured data from this invoice:
                
                %s
                
                Extract all relevant invoice information including vendor details,
                line items, amounts, dates, and payment terms.
                """, invoiceContent);
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    /**
     * Extract contract data
     */
    public ContractData extractContractData(String contractContent) {
        BeanOutputConverter<ContractData> converter = new BeanOutputConverter<>(ContractData.class);
        
        String prompt = String.format("""
                Extract key contract information from this document:
                
                %s
                
                Focus on parties involved, key terms, obligations, dates,
                payment terms, and termination clauses.
                """, contractContent);
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    /**
     * Extract resume data
     */
    public ResumeData extractResumeData(String resumeContent) {
        BeanOutputConverter<ResumeData> converter = new BeanOutputConverter<>(ResumeData.class);
        
        String prompt = String.format("""
                Extract structured information from this resume:
                
                %s
                
                Extract personal information, work experience, education,
                skills, and other relevant details.
                """, resumeContent);
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    /**
     * Extract research paper data
     */
    public ResearchPaperData extractResearchData(String paperContent) {
        BeanOutputConverter<ResearchPaperData> converter = new BeanOutputConverter<>(ResearchPaperData.class);
        
        String prompt = String.format("""
                Extract key information from this research paper:
                
                %s
                
                Focus on title, authors, abstract, methodology, findings,
                conclusions, and references.
                """, paperContent);
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    /**
     * Process streaming data
     */
    public StreamingExtractionResult processStream(StreamingDataSource dataSource, ExtractionConfig config) {
        logger.info("Starting streaming extraction from: {}", dataSource.sourceId());
        
        BlockingQueue<DocumentInput> inputQueue = new LinkedBlockingQueue<>();
        BlockingQueue<ExtractionResult> outputQueue = new LinkedBlockingQueue<>();
        
        // Start producer thread
        CompletableFuture<Void> producer = CompletableFuture.runAsync(() -> {
            try {
                dataSource.streamDocuments(inputQueue::offer);
            } catch (Exception e) {
                logger.error("Error in streaming producer", e);
            }
        });
        
        // Start consumer threads
        List<CompletableFuture<Void>> consumers = java.util.stream.IntStream.range(0, config.parallelism())
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            DocumentInput doc = inputQueue.poll(1, TimeUnit.SECONDS);
                            if (doc == null) continue;
                            
                            ExtractionResult result = processDocument(doc, config);
                            outputQueue.offer(result);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        logger.error("Error in streaming consumer", e);
                    }
                }))
                .collect(Collectors.toList());
        
        return new StreamingExtractionResult(producer, consumers, outputQueue, dataSource.sourceId());
    }
    
    // Private helper methods
    
    private StructuredData extractStructuredData(String content, DocumentType type, ExtractionConfig config) {
        ExtractionTemplate template = templates.get(type);
        if (template == null) {
            throw new IllegalArgumentException("No template found for document type: " + type);
        }
        
        BeanOutputConverter<StructuredData> converter = new BeanOutputConverter<>(StructuredData.class);
        
        String prompt = String.format("""
                Extract structured data from this %s document:
                
                %s
                
                Template: %s
                Focus Areas: %s
                Quality Requirements: %s
                """,
                type.toString().toLowerCase(),
                content,
                template.description(),
                String.join(", ", template.focusAreas()),
                config.qualityRequirements()
        );
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    private String preProcessDocument(DocumentInput document) {
        // Apply pre-processing based on document type
        String content = document.content();
        
        // Clean up common issues
        content = content.replaceAll("\\s+", " ").trim();
        
        // Apply document-type specific preprocessing
        return switch (document.type()) {
            case INVOICE -> preprocessInvoice(content);
            case CONTRACT -> preprocessContract(content);
            case RESUME -> preprocessResume(content);
            case RESEARCH_PAPER -> preprocessResearchPaper(content);
            case EMAIL -> preprocessEmail(content);
            case LEGAL_DOCUMENT -> preprocessLegalDocument(content);
        };
    }
    
    private StructuredData postProcessExtractedData(StructuredData data, ExtractionConfig config) {
        // Apply post-processing rules
        Map<String, Object> processedFields = new HashMap<>(data.fields());
        
        // Apply data transformations
        config.transformationRules().forEach((field, transformation) -> {
            if (processedFields.containsKey(field)) {
                processedFields.put(field, applyTransformation(processedFields.get(field), transformation));
            }
        });
        
        // Apply validation rules
        validator.validate(processedFields, config.validationRules());
        
        return new StructuredData(
                data.documentType(),
                processedFields,
                data.confidence(),
                data.metadata()
        );
    }
    
    private List<ExtractionResult> applyPostProcessing(List<ExtractionResult> results, 
                                                       List<PostProcessingRule> rules) {
        return results.stream()
                .map(result -> applyPostProcessingRules(result, rules))
                .collect(Collectors.toList());
    }
    
    private ExtractionResult applyPostProcessingRules(ExtractionResult result, List<PostProcessingRule> rules) {
        if (result.extractedData() == null) return result;
        
        StructuredData processedData = result.extractedData();
        for (PostProcessingRule rule : rules) {
            processedData = rule.apply(processedData);
        }
        
        return new ExtractionResult(
                result.documentId(),
                result.documentType(),
                processedData,
                result.qualityMetrics(),
                result.processingTimeMs(),
                result.status(),
                result.errorMessage()
        );
    }
    
    private QualityMetrics calculateQualityMetrics(List<ExtractionResult> results) {
        double avgAccuracy = results.stream()
                .filter(r -> r.qualityMetrics() != null)
                .mapToDouble(r -> r.qualityMetrics().accuracy())
                .average()
                .orElse(0.0);
        
        double avgCompleteness = results.stream()
                .filter(r -> r.qualityMetrics() != null)
                .mapToDouble(r -> r.qualityMetrics().completeness())
                .average()
                .orElse(0.0);
        
        double avgConfidence = results.stream()
                .filter(r -> r.qualityMetrics() != null)
                .mapToDouble(r -> r.qualityMetrics().confidence())
                .average()
                .orElse(0.0);
        
        List<String> aggregatedIssues = results.stream()
                .filter(r -> r.qualityMetrics() != null)
                .flatMap(r -> r.qualityMetrics().issues().stream())
                .distinct()
                .collect(Collectors.toList());
        
        return new QualityMetrics(avgAccuracy, avgCompleteness, avgConfidence, aggregatedIssues);
    }
    
    private PipelineMetadata generatePipelineMetadata(BatchExtractionRequest request, List<ExtractionResult> results) {
        Map<DocumentType, Long> typeDistribution = results.stream()
                .collect(Collectors.groupingBy(
                        ExtractionResult::documentType,
                        Collectors.counting()
                ));
        
        Map<ExtractionStatus, Long> statusDistribution = results.stream()
                .collect(Collectors.groupingBy(
                        ExtractionResult::status,
                        Collectors.counting()
                ));
        
        return new PipelineMetadata(
                request.batchId(),
                request.documents().size(),
                results.size(),
                typeDistribution,
                statusDistribution,
                LocalDateTime.now()
        );
    }
    
    private void initializeTemplates() {
        templates.put(DocumentType.INVOICE, new ExtractionTemplate(
                DocumentType.INVOICE,
                "Invoice data extraction",
                List.of("vendor", "amounts", "line items", "dates", "payment terms"),
                "invoice-template.json"
        ));
        
        templates.put(DocumentType.CONTRACT, new ExtractionTemplate(
                DocumentType.CONTRACT,
                "Contract terms extraction",
                List.of("parties", "terms", "obligations", "dates", "clauses"),
                "contract-template.json"
        ));
        
        templates.put(DocumentType.RESUME, new ExtractionTemplate(
                DocumentType.RESUME,
                "Resume information extraction",
                List.of("personal info", "experience", "education", "skills"),
                "resume-template.json"
        ));
    }
    
    // Document type specific preprocessing
    private String preprocessInvoice(String content) { return content; }
    private String preprocessContract(String content) { return content; }
    private String preprocessResume(String content) { return content; }
    private String preprocessResearchPaper(String content) { return content; }
    private String preprocessEmail(String content) { return content; }
    private String preprocessLegalDocument(String content) { return content; }
    
    private Object applyTransformation(Object value, String transformation) {
        // Apply transformation logic
        return value;
    }
    
    // Data models and records
    
    public record BatchExtractionRequest(
            String batchId,
            List<DocumentInput> documents,
            ExtractionConfig extractionConfig,
            List<PostProcessingRule> postProcessingRules,
            long timeoutSeconds
    ) {}
    
    public record DocumentInput(
            String id,
            DocumentType type,
            String content,
            Map<String, String> metadata
    ) {}
    
    public record ExtractionConfig(
            String qualityRequirements,
            boolean enablePostProcessing,
            Map<String, String> transformationRules,
            Map<String, String> validationRules,
            int parallelism
    ) {}
    
    public record ExtractionResult(
            String documentId,
            DocumentType documentType,
            StructuredData extractedData,
            QualityMetrics qualityMetrics,
            long processingTimeMs,
            ExtractionStatus status,
            String errorMessage
    ) {}
    
    public record StructuredData(
            @JsonPropertyDescription("Type of document") DocumentType documentType,
            @JsonPropertyDescription("Extracted data fields") Map<String, Object> fields,
            @JsonPropertyDescription("Extraction confidence score") double confidence,
            @JsonPropertyDescription("Extraction metadata") Map<String, String> metadata
    ) {}
    
    public record QualityMetrics(
            double accuracy,
            double completeness,
            double confidence,
            List<String> issues
    ) {}
    
    public record PipelineResult(
            List<ExtractionResult> results,
            int successCount,
            int errorCount,
            long totalProcessingTimeMs,
            QualityMetrics overallQuality,
            PipelineMetadata metadata
    ) {}
    
    public record PipelineMetadata(
            String batchId,
            int totalDocuments,
            int processedDocuments,
            Map<DocumentType, Long> typeDistribution,
            Map<ExtractionStatus, Long> statusDistribution,
            LocalDateTime processedAt
    ) {}
    
    public record StreamingExtractionResult(
            CompletableFuture<Void> producer,
            List<CompletableFuture<Void>> consumers,
            BlockingQueue<ExtractionResult> outputQueue,
            String sourceId
    ) {}
    
    public record ExtractionTemplate(
            DocumentType type,
            String description,
            List<String> focusAreas,
            String templateFile
    ) {}
    
    // Specific data extraction models
    
    public record InvoiceData(
            @JsonPropertyDescription("Vendor information") VendorInfo vendor,
            @JsonPropertyDescription("Invoice details") InvoiceDetails details,
            @JsonPropertyDescription("Line items") List<LineItem> lineItems,
            @JsonPropertyDescription("Payment information") PaymentInfo payment
    ) {}
    
    public record VendorInfo(
            @JsonPropertyDescription("Vendor name") String name,
            @JsonPropertyDescription("Vendor address") String address,
            @JsonPropertyDescription("Contact information") String contact
    ) {}
    
    public record InvoiceDetails(
            @JsonPropertyDescription("Invoice number") String invoiceNumber,
            @JsonPropertyDescription("Invoice date") String date,
            @JsonPropertyDescription("Due date") String dueDate,
            @JsonPropertyDescription("Total amount") double totalAmount
    ) {}
    
    public record LineItem(
            @JsonPropertyDescription("Item description") String description,
            @JsonPropertyDescription("Quantity") int quantity,
            @JsonPropertyDescription("Unit price") double unitPrice,
            @JsonPropertyDescription("Total price") double totalPrice
    ) {}
    
    public record PaymentInfo(
            @JsonPropertyDescription("Payment terms") String terms,
            @JsonPropertyDescription("Payment method") String method,
            @JsonPropertyDescription("Account details") String accountDetails
    ) {}
    
    public record ContractData(
            @JsonPropertyDescription("Contract parties") List<String> parties,
            @JsonPropertyDescription("Contract terms") Map<String, String> terms,
            @JsonPropertyDescription("Key obligations") List<String> obligations,
            @JsonPropertyDescription("Important dates") Map<String, String> dates,
            @JsonPropertyDescription("Termination clauses") List<String> terminationClauses
    ) {}
    
    public record ResumeData(
            @JsonPropertyDescription("Personal information") PersonalInfo personal,
            @JsonPropertyDescription("Work experience") List<Experience> experience,
            @JsonPropertyDescription("Education background") List<Education> education,
            @JsonPropertyDescription("Skills and competencies") List<String> skills
    ) {}
    
    public record PersonalInfo(
            @JsonPropertyDescription("Full name") String name,
            @JsonPropertyDescription("Email address") String email,
            @JsonPropertyDescription("Phone number") String phone,
            @JsonPropertyDescription("Address") String address
    ) {}
    
    public record Experience(
            @JsonPropertyDescription("Company name") String company,
            @JsonPropertyDescription("Job title") String title,
            @JsonPropertyDescription("Duration") String duration,
            @JsonPropertyDescription("Key responsibilities") List<String> responsibilities
    ) {}
    
    public record Education(
            @JsonPropertyDescription("Institution name") String institution,
            @JsonPropertyDescription("Degree") String degree,
            @JsonPropertyDescription("Field of study") String field,
            @JsonPropertyDescription("Graduation year") String year
    ) {}
    
    public record ResearchPaperData(
            @JsonPropertyDescription("Paper title") String title,
            @JsonPropertyDescription("Authors") List<String> authors,
            @JsonPropertyDescription("Abstract") String abstractText,
            @JsonPropertyDescription("Methodology") String methodology,
            @JsonPropertyDescription("Key findings") List<String> findings,
            @JsonPropertyDescription("Conclusions") String conclusions
    ) {}
    
    public enum DocumentType {
        INVOICE, CONTRACT, RESUME, RESEARCH_PAPER, EMAIL, LEGAL_DOCUMENT
    }
    
    public enum ExtractionStatus {
        SUCCESS, FAILED, PARTIAL, PENDING
    }
    
    public interface PostProcessingRule {
        StructuredData apply(StructuredData data);
    }
    
    public interface StreamingDataSource {
        String sourceId();
        void streamDocuments(Function<DocumentInput, Boolean> consumer);
    }
    
    // Helper classes
    
    private static class DataValidator {
        public void validate(Map<String, Object> fields, Map<String, String> rules) {
            // Implement validation logic
        }
    }
    
    private static class QualityAssessment {
        public QualityMetrics assess(StructuredData data, DocumentInput document) {
            // Implement quality assessment logic
            return new QualityMetrics(0.95, 0.90, 0.85, List.of());
        }
    }
    
    public static class PipelineException extends RuntimeException {
        public PipelineException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}