# Spring AI Structured Output - Automated Testing Guide

## 🎯 Overview

This guide covers the automated testing capabilities built into the Postman collection for Spring AI structured output demonstrations. Each request includes comprehensive validation scripts to ensure consistent behavior during live presentations.

## 🧪 Test Categories

### 1. Health Check Validation
```javascript
pm.test("Application is healthy", function () {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().status).to.eql("UP");
});
```
**Purpose:** Ensures the Spring Boot application is running and responsive

### 2. Structured Output Validation
```javascript
pm.test("Weather forecast returned", function () {
    pm.response.to.have.status(200);
    const response = pm.response.json();
    pm.expect(response).to.have.property('city');
    pm.expect(response).to.have.property('temperature');
    pm.expect(response).to.have.property('description');
});
```
**Purpose:** Validates that structured objects contain expected fields

### 3. Data Type Validation
```javascript
pm.test("Recipe generated with structured data", function () {
    pm.response.to.have.status(200);
    const response = pm.response.json();
    pm.expect(response).to.have.property('name');
    pm.expect(response).to.have.property('ingredients');
    pm.expect(response).to.have.property('instructions');
    pm.expect(response.ingredients).to.be.an('array');
});
```
**Purpose:** Ensures correct data types for complex nested structures

### 4. Business Logic Validation
```javascript
pm.test("Invoice data extracted", function () {
    pm.response.to.have.status(200);
    const response = pm.response.json();
    pm.expect(response).to.have.property('invoiceNumber');
    pm.expect(response).to.have.property('totalAmount');
    pm.expect(response).to.have.property('vendor');
    pm.expect(response).to.have.property('lineItems');
});
```
**Purpose:** Validates business-specific data extraction accuracy

## 🔧 Enhanced Test Scripts

### Global Pre-request Script
```javascript
// Set dynamic variables for timestamps
pm.globals.set('timestamp', new Date().toISOString());

// Generate session ID for tracking related requests
if (!pm.globals.get('sessionId')) {
    pm.globals.set('sessionId', 'session_' + Date.now());
}

// Log request details for debugging
console.log('Executing:', pm.info.requestName);
console.log('URL:', pm.request.url.toString());
```

### Global Test Script
```javascript
// Global test for all requests
pm.test('Response time is reasonable', function () {
    pm.expect(pm.response.responseTime).to.be.below(30000);
});

// Log response for debugging
if (pm.response.code !== 200) {
    console.log('Request failed:', pm.request.url);
    console.log('Response:', pm.response.text());
}

// Validate common response structure
pm.test('Response is valid JSON', function () {
    pm.response.to.be.json;
});

// Store metrics for performance tracking
pm.globals.set('lastResponseTime', pm.response.responseTime);
pm.globals.set('lastRequestStatus', pm.response.code);
```

## 📊 Advanced Validation Scripts

### JSON Schema Validation
```javascript
// Advanced schema validation for structured output
pm.test("Response matches expected schema", function () {
    const response = pm.response.json();
    
    // Define expected schema based on endpoint
    const weatherSchema = {
        type: "object",
        required: ["city", "temperature", "description"],
        properties: {
            city: { type: "string" },
            temperature: { type: "number" },
            description: { type: "string" },
            humidity: { type: "number" }
        }
    };
    
    // Validate schema (simplified - would use ajv in real implementation)
    pm.expect(response).to.have.property('city');
    pm.expect(typeof response.city).to.eql('string');
    pm.expect(response).to.have.property('temperature');
    pm.expect(typeof response.temperature).to.eql('number');
});
```

### Performance Tracking
```javascript
// Track performance metrics across requests
pm.test("Performance within acceptable limits", function () {
    const responseTime = pm.response.responseTime;
    
    // Set different thresholds based on endpoint complexity
    let threshold = 5000; // Default 5 seconds
    
    if (pm.info.requestName.includes('Comprehensive Demo')) {
        threshold = 15000; // 15 seconds for complex operations
    } else if (pm.info.requestName.includes('Catalog')) {
        threshold = 10000; // 10 seconds for catalog generation
    }
    
    pm.expect(responseTime).to.be.below(threshold);
    
    // Store metrics for analysis
    const metrics = pm.globals.get('performanceMetrics') || [];
    metrics.push({
        request: pm.info.requestName,
        responseTime: responseTime,
        timestamp: new Date().toISOString()
    });
    pm.globals.set('performanceMetrics', metrics);
});
```

### Error Handling Validation
```javascript
// Validate error responses are well-formed
pm.test("Error responses are properly formatted", function () {
    if (pm.response.code >= 400) {
        const response = pm.response.json();
        pm.expect(response).to.have.property('error');
        pm.expect(response).to.have.property('message');
        pm.expect(response).to.have.property('timestamp');
    }
});
```

### Business Rule Validation
```javascript
// Validate business-specific rules
pm.test("Business rules are enforced", function () {
    if (pm.info.requestName.includes('Product Catalog')) {
        const response = pm.response.json();
        
        // Validate product count matches request
        const requestBody = JSON.parse(pm.request.body.raw);
        if (response.products) {
            pm.expect(response.products.length).to.be.at.most(requestBody.productCount);
        }
        
        // Validate price ranges
        if (response.products && response.products.length > 0) {
            response.products.forEach(product => {
                if (product.price) {
                    pm.expect(product.price).to.be.at.least(requestBody.minPrice);
                    pm.expect(product.price).to.be.at.most(requestBody.maxPrice);
                }
            });
        }
    }
});
```

## 🎪 Demo-Specific Test Scripts

### Phase 1: Foundation Tests
```javascript
// Foundation phase validation
pm.test("Foundation concepts demonstrated", function () {
    const response = pm.response.json();
    
    // Validate structured output conversion
    pm.expect(response).to.be.an('object');
    pm.expect(Object.keys(response).length).to.be.greaterThan(0);
    
    // Ensure no raw text responses
    pm.expect(typeof response).to.not.eql('string');
});
```

### Phase 2: Converter Tests
```javascript
// Converter-specific validation
pm.test("Converter type working correctly", function () {
    const response = pm.response.json();
    
    if (pm.info.requestName.includes('BeanConverter')) {
        // Validate object structure
        pm.expect(response).to.be.an('object');
        pm.expect(response).to.not.be.an('array');
        
    } else if (pm.info.requestName.includes('MapConverter')) {
        // Validate map structure
        pm.expect(response).to.be.an('object');
        pm.expect(Object.keys(response).length).to.be.greaterThan(0);
        
    } else if (pm.info.requestName.includes('ListConverter')) {
        // Validate array structure
        pm.expect(response).to.be.an('array');
        pm.expect(response.length).to.be.greaterThan(0);
    }
});
```

### Phase 3: Production Tests
```javascript
// Production-ready validation
pm.test("Production quality standards met", function () {
    const response = pm.response.json();
    
    // Validate metadata presence
    if (response.metadata || response.generationStats) {
        pm.expect(response).to.have.property('metadata').or.have.property('generationStats');
    }
    
    // Validate error handling
    if (response.errors) {
        pm.expect(response.errors).to.be.an('array');
    }
    
    // Validate processing time tracking
    if (response.processingTime) {
        pm.expect(response.processingTime).to.be.a('number');
        pm.expect(response.processingTime).to.be.greaterThan(0);
    }
});
```

### Phase 4: Advanced Feature Tests
```javascript
// Advanced feature validation
pm.test("Advanced features working", function () {
    const response = pm.response.json();
    
    if (pm.info.requestName.includes('JSON Mode')) {
        // Validate JSON mode enforcement
        pm.expect(response).to.be.an('object');
        pm.expect(response).to.have.property('productName').or.have.property('recommendation');
    }
    
    if (pm.info.requestName.includes('Performance')) {
        // Validate performance features
        pm.expect(response).to.have.property('cacheHit').or.have.property('responseTime');
    }
    
    if (pm.info.requestName.includes('Comprehensive')) {
        // Validate comprehensive demo results
        pm.expect(response).to.have.property('success', true);
        pm.expect(response).to.have.property('results');
        pm.expect(Object.keys(response.results).length).to.be.greaterThan(0);
    }
});
```

## 📈 Metrics Collection

### Response Time Tracking
```javascript
// Collect response time metrics
const responseTime = pm.response.responseTime;
const endpoint = pm.info.requestName;

// Store in collection variables for analysis
const metrics = pm.collectionVariables.get('responseTimeMetrics') || {};
if (!metrics[endpoint]) {
    metrics[endpoint] = [];
}
metrics[endpoint].push(responseTime);
pm.collectionVariables.set('responseTimeMetrics', metrics);

// Calculate running average
const average = metrics[endpoint].reduce((a, b) => a + b, 0) / metrics[endpoint].length;
pm.collectionVariables.set(`avg_${endpoint}`, average);
```

### Success Rate Tracking
```javascript
// Track success rates across requests
const success = pm.response.code === 200;
const endpoint = pm.info.requestName;

const successMetrics = pm.collectionVariables.get('successMetrics') || {};
if (!successMetrics[endpoint]) {
    successMetrics[endpoint] = { total: 0, successes: 0 };
}

successMetrics[endpoint].total++;
if (success) {
    successMetrics[endpoint].successes++;
}

successMetrics[endpoint].rate = 
    successMetrics[endpoint].successes / successMetrics[endpoint].total;

pm.collectionVariables.set('successMetrics', successMetrics);
```

## 🚨 Error Detection and Reporting

### Automated Error Detection
```javascript
// Comprehensive error detection
pm.test("No critical errors detected", function () {
    const response = pm.response.text();
    
    // Check for common error indicators
    const errorPatterns = [
        /NullPointerException/i,
        /ClassCastException/i,
        /JSON parse error/i,
        /API key invalid/i,
        /Rate limit exceeded/i
    ];
    
    errorPatterns.forEach(pattern => {
        pm.expect(response).to.not.match(pattern);
    });
});
```

### Live Demo Safety Net
```javascript
// Safety net for live demonstrations
pm.test("Demo-safe response", function () {
    // Ensure response is suitable for live presentation
    const response = pm.response.json();
    
    // Check for inappropriate content indicators
    if (typeof response === 'string') {
        const inappropriateWords = ['error', 'failed', 'exception'];
        inappropriateWords.forEach(word => {
            pm.expect(response.toLowerCase()).to.not.include(word);
        });
    }
    
    // Ensure response time is demo-friendly
    pm.expect(pm.response.responseTime).to.be.below(15000); // 15 seconds max
});
```

## 🎯 Teaching Validation

### Learning Objective Validation
```javascript
// Validate learning objectives are met
pm.test("Learning objectives demonstrated", function () {
    const response = pm.response.json();
    const endpoint = pm.info.requestName;
    
    if (endpoint.includes('Foundation')) {
        // Foundation objectives
        pm.expect(response).to.be.an('object'); // Type safety demonstrated
        
    } else if (endpoint.includes('Converter')) {
        // Converter objectives
        pm.expect(response).to.satisfy(r => 
            typeof r === 'object' || Array.isArray(r)); // Converter flexibility
            
    } else if (endpoint.includes('Production')) {
        // Production objectives
        pm.expect(response).to.have.property('metadata')
            .or.have.property('stats')
            .or.have.property('performance'); // Production readiness
    }
});
```

---

**🎓 Teaching Tip:** Use the test results tab in Postman to show live validation during demonstrations. Green checkmarks provide visual confirmation that structured output is working correctly!