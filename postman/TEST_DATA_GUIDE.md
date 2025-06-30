# Spring AI Structured Output - Test Data Guide

## 🎯 Overview

This guide provides comprehensive test data scenarios for all Spring AI structured output demonstrations. Each test case is designed to showcase specific converter features and real-world use cases.

## 📊 Test Data Categories

### Phase 1: Foundation & Setup

#### S2: Weather Forecast (BeanConverter)
```json
{
  "city": "Seattle",
  "forecastType": "detailed",
  "includeHourly": true
}
```
**Purpose:** Demonstrates basic BeanOutputConverter with simple object structure
**Expected Output:** WeatherForecast object with city, temperature, description, humidity

#### S2: Recipe Generation (Complex Object)
```json
{
  "cuisine": "Italian",
  "dietaryRestrictions": ["vegetarian"],
  "cookingTime": 30,
  "servings": 4,
  "difficulty": "intermediate"
}
```
**Purpose:** Shows complex nested object conversion with arrays and validation
**Expected Output:** Recipe object with ingredients list, step-by-step instructions

#### S2: Sentiment Analysis (Few-shot)
```json
{
  "text": "I absolutely love this Spring AI framework! It makes structured output so easy and reliable.",
  "includeConfidence": true,
  "includeKeyPhrases": true
}
```
**Purpose:** Demonstrates few-shot prompting with structured classification
**Expected Output:** SentimentAnalysis with sentiment, confidence score, key phrases

### Phase 2: Converter Deep Dive

#### S4: Weather with BeanConverter (Enhanced)
```json
{
  "city": "Tokyo",
  "includeExtendedForecast": true,
  "units": "metric"
}
```
**Purpose:** Advanced BeanConverter with optional fields and validation
**Expected Output:** Detailed weather object with extended forecast data

#### S4: Profile with MapConverter
```json
{
  "profession": "Software Engineer",
  "experience": "5 years",
  "technologies": ["Java", "Spring", "AI"]
}
```
**Purpose:** MapConverter for flexible, dynamic data structures
**Expected Output:** Map<String, Object> with variable key-value pairs

#### S4: Tags with ListConverter
```json
{
  "content": "This is an article about Spring AI structured output and enterprise integration patterns",
  "maxTags": 8,
  "includeCategories": true
}
```
**Purpose:** ListConverter for simple collections and arrays
**Expected Output:** List<String> of relevant tags

### Phase 3: Production Patterns

#### S16: E-commerce Product Catalog
```json
{
  "category": "Electronics",
  "productCount": 5,
  "minPrice": 50.0,
  "maxPrice": 500.0,
  "targetMarket": "Premium",
  "brandStyle": "Modern"
}
```
**Purpose:** High-volume product generation for e-commerce
**Expected Output:** ProductCatalogResult with generated products and statistics

#### S16: Business Report Generation
```json
{
  "type": "FINANCIAL",
  "title": "Q4 2024 Financial Performance Report",
  "startDate": "2024-10-01",
  "endDate": "2024-12-31",
  "sections": ["Executive Summary", "Revenue Analysis", "Cost Analysis", "Profitability"],
  "stakeholders": ["CEO", "CFO", "Board of Directors"],
  "context": "Quarterly financial review for stakeholder presentation",
  "dataSources": ["ERP System", "CRM Database", "Financial Systems"],
  "keyMetrics": ["Revenue Growth", "EBITDA", "Operating Margin"],
  "industry": "Technology",
  "objectives": ["Growth Assessment", "Cost Optimization"],
  "constraints": ["Confidential Data"],
  "timeline": "End of Q1 2025"
}
```
**Purpose:** Complex business report generation with multiple parameters
**Expected Output:** Comprehensive business report with structured sections

#### S16: Invoice Data Extraction
```json
{
  "invoiceContent": "INVOICE #INV-2024-001\nDate: 2024-12-01\n\nFrom: Tech Solutions Inc.\n123 Business Ave\nSeattle, WA 98101\n\nTo: Demo Customer Corp\n456 Customer St\nPortland, OR 97201\n\nItems:\n1. Spring AI Consulting - $2,500.00\n2. Development Services - $1,500.00\n3. Training Workshop - $800.00\n\nSubtotal: $4,800.00\nTax (8.5%): $408.00\nTotal: $5,208.00\n\nPayment Terms: Net 30"
}
```
**Purpose:** Document parsing and data extraction from unstructured text
**Expected Output:** InvoiceData with parsed fields, line items, amounts

### Phase 4: Advanced Features

#### S16: Comprehensive Demo Scenario
```json
{
  "scenarioName": "Complete Spring AI Showcase",
  "includeEcommerce": true,
  "includeReports": true,
  "includeExtraction": true,
  "includeTransformation": true,
  "includeMigration": false,
  "includeDeployment": false
}
```
**Purpose:** End-to-end demonstration of all major features
**Expected Output:** ComprehensiveDemoResult with results from all enabled components

## 🔄 Alternative Test Scenarios

### Weather Variations
- **Tropical Location:** `{"city": "Hawaii", "includeUVIndex": true}`
- **Cold Climate:** `{"city": "Anchorage", "units": "imperial"}`
- **International:** `{"city": "London", "language": "en"}`

### Recipe Variations
- **Dietary Restrictions:** `{"cuisine": "Asian", "dietaryRestrictions": ["vegan", "gluten-free"]}`
- **Quick Meals:** `{"cookingTime": 15, "difficulty": "easy"}`
- **Large Groups:** `{"servings": 20, "occasion": "party"}`

### E-commerce Variations
- **Budget Products:** `{"minPrice": 10, "maxPrice": 50, "targetMarket": "Budget"}`
- **Luxury Items:** `{"minPrice": 1000, "maxPrice": 5000, "targetMarket": "Luxury"}`
- **Different Categories:** Fashion, Home & Garden, Sports, Books

### Business Report Variations
- **Marketing Report:** `{"type": "MARKETING", "industry": "Retail"}`
- **Operational Report:** `{"type": "OPERATIONAL", "focus": "Efficiency"}`
- **Strategic Report:** `{"type": "STRATEGIC", "timeframe": "Annual"}`

## 🧪 Testing Strategies

### Mock vs Real API Testing
- **Development Phase:** Use mock responses for rapid iteration
- **Integration Testing:** Real API calls with small data sets
- **Performance Testing:** Cached responses for consistent measurements

### Error Scenario Testing
- **Invalid JSON:** Malformed request bodies
- **Missing Required Fields:** Incomplete data structures
- **API Rate Limits:** High-volume testing scenarios
- **Network Timeouts:** Slow response simulation

### Validation Testing
- **Schema Compliance:** Ensure responses match expected structure
- **Data Type Validation:** Verify correct data types in responses
- **Business Logic Validation:** Check calculated fields and derived values

## 📈 Performance Test Data

### Small Scale Tests (Development)
- Product Catalog: 1-5 products
- Report Sections: 2-3 sections
- Document Batch: 1-3 documents

### Medium Scale Tests (Staging)
- Product Catalog: 10-20 products
- Report Sections: 5-10 sections
- Document Batch: 5-15 documents

### Large Scale Tests (Production Simulation)
- Product Catalog: 50-100 products
- Report Sections: 10-20 sections
- Document Batch: 20-50 documents

## 🎯 Teaching Scenarios

### Beginner Demonstrations
Focus on simple, clear examples that demonstrate core concepts:
- Single-field weather requests
- Basic recipe generation
- Simple sentiment analysis

### Intermediate Demonstrations
Show real-world complexity with multiple parameters:
- Multi-field product catalog generation
- Business reports with multiple sections
- Document extraction with validation

### Advanced Demonstrations
Showcase production-ready patterns:
- Batch processing capabilities
- Error handling and recovery
- Performance optimization techniques

## 🔍 Debugging Data

### Success Case Examples
Use these for demonstrating normal operation:
- Well-formatted invoices with clear structure
- Complete business data with all required fields
- Standard product descriptions with clear categories

### Edge Case Examples
Use these for demonstrating robustness:
- Invoices with missing or ambiguous data
- Business reports with unusual date ranges
- Product descriptions with special characters or multiple languages

### Error Case Examples
Use these for demonstrating error handling:
- Completely malformed text input
- Empty or null requests
- Requests that exceed API limits

## 📝 Best Practices

### Data Consistency
- Use consistent naming conventions across all test scenarios
- Maintain realistic data relationships (e.g., dates, amounts)
- Keep test data updated with current information

### Demonstration Flow
- Start with simple examples to build understanding
- Progress to complex scenarios gradually
- Always include error handling demonstrations

### Audience Adaptation
- **Executive Audiences:** Focus on business value and ROI examples
- **Developer Audiences:** Include technical edge cases and error scenarios
- **Mixed Audiences:** Balance business context with technical depth

---

**💡 Pro Tip:** Always have backup test data ready for live demonstrations, as AI responses can vary even with identical inputs.