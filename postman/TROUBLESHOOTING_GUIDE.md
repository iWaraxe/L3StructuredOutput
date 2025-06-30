# Spring AI Structured Output - Troubleshooting Guide

## 🚨 Quick Diagnosis Flowchart

```
Application Not Responding?
├── YES → Check Application Health Section
└── NO → Continue

API Calls Failing?
├── YES → Check API Configuration Section
└── NO → Continue

Responses Not Structured?
├── YES → Check Converter Configuration Section
└── NO → Continue

Performance Issues?
├── YES → Check Performance Optimization Section
└── NO → Check Advanced Troubleshooting
```

## 🏥 Application Health Issues

### Problem: Application Won't Start

#### Symptoms
- `curl http://localhost:8080/actuator/health` returns connection refused
- Spring Boot application logs show startup errors
- Port 8080 already in use

#### Diagnostic Steps
```bash
# Check if port is in use
lsof -i :8080

# Check application logs
./mvnw spring-boot:run

# Verify Java version
java -version
```

#### Solutions
```yaml
Port Conflict:
  - Kill existing process: kill -9 <PID>
  - Use different port: --server.port=8081
  - Check Docker containers: docker ps

Java Version Issues:
  - Verify Java 21+ installed
  - Check JAVA_HOME environment variable
  - Update Maven wrapper if needed

Dependency Issues:
  - Clean Maven cache: ./mvnw clean
  - Force dependency update: ./mvnw dependency:resolve
  - Check internet connectivity for downloads
```

### Problem: Application Starts but Health Check Fails

#### Symptoms
- Application logs show "Started Application" message
- Health endpoint returns 503 Service Unavailable
- Actuator endpoints not accessible

#### Diagnostic Steps
```bash
# Check actuator endpoints
curl http://localhost:8080/actuator
curl http://localhost:8080/actuator/health

# Check application properties
cat src/main/resources/application.properties
```

#### Solutions
```yaml
Actuator Configuration:
  - Add management.endpoints.web.exposure.include=health,info
  - Verify management.endpoint.health.show-details=always
  - Check security configuration

Spring Boot Version:
  - Ensure Spring Boot 3.x compatibility
  - Verify Spring AI BOM version matches
  - Check for conflicting dependencies
```

## 🔑 API Configuration Issues

### Problem: OpenAI API Key Not Working

#### Symptoms
- Requests return 401 Unauthorized
- Error: "Invalid API key provided"
- API calls timeout or fail

#### Diagnostic Steps
```bash
# Verify API key is set
echo $OPENAI_API_KEY

# Test API key directly
curl https://api.openai.com/v1/models \
  -H "Authorization: Bearer $OPENAI_API_KEY"

# Check application logs for API errors
tail -f logs/application.log
```

#### Solutions
```yaml
API Key Issues:
  - Verify key format: sk-...
  - Check key permissions and rate limits
  - Regenerate key from OpenAI dashboard
  - Ensure key is not expired

Environment Configuration:
  - Set in shell: export OPENAI_API_KEY=your-key
  - Add to application.properties: spring.ai.openai.api-key=${OPENAI_API_KEY}
  - Verify no trailing spaces or quotes

Network Issues:
  - Check firewall settings
  - Verify corporate proxy configuration
  - Test direct internet connectivity
```

### Problem: Rate Limit Exceeded

#### Symptoms
- Error: "Rate limit exceeded"
- HTTP 429 responses
- Requests queued or delayed

#### Diagnostic Steps
```bash
# Check current usage
curl https://api.openai.com/v1/usage \
  -H "Authorization: Bearer $OPENAI_API_KEY"

# Monitor request frequency
grep "OpenAI" logs/application.log | tail -20
```

#### Solutions
```yaml
Immediate Fixes:
  - Wait for rate limit reset
  - Reduce request frequency
  - Use smaller test datasets

Long-term Solutions:
  - Implement exponential backoff
  - Add request queuing
  - Cache frequent responses
  - Upgrade OpenAI plan if needed
```

## 🔧 Converter Configuration Issues

### Problem: Responses Not Following Schema

#### Symptoms
- JSON parsing errors
- Missing required fields
- Incorrect data types
- Validation failures

#### Diagnostic Steps
```bash
# Check raw AI responses in logs
grep "AI Response" logs/application.log

# Verify JSON Schema generation
curl http://localhost:8080/api/s4/weather \
  -H "Content-Type: application/json" \
  -d '{"city": "Test"}' -v

# Validate converter configuration
```

#### Solutions
```yaml
Schema Issues:
  - Review Java DTO annotations
  - Check JSON Schema in response headers
  - Verify required vs optional fields
  - Test with simpler schemas first

AI Model Issues:
  - Try different model (gpt-4 vs gpt-3.5)
  - Adjust temperature settings
  - Improve prompt engineering
  - Add more explicit instructions

Converter Configuration:
  - Verify converter type selection
  - Check Jackson annotations
  - Review error handling configuration
```

### Problem: BeanConverter Failures

#### Symptoms
- ClassCastException errors
- Deserialization failures
- Missing constructor errors
- Field mapping issues

#### Diagnostic Steps
```java
// Check DTO class structure
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherForecast {
    private String city;
    private Double temperature;
    private String description;
}

// Verify Jackson configuration
@JsonProperty("cityName")
private String city;
```

#### Solutions
```yaml
DTO Class Issues:
  - Add @NoArgsConstructor
  - Ensure all fields have getters/setters
  - Check field names match JSON
  - Add @JsonProperty for name mismatches

Type Conversion:
  - Use wrapper types (Double vs double)
  - Handle null values appropriately
  - Add custom deserializers if needed
  - Validate data types in schema

Constructor Issues:
  - Provide default constructor
  - Use @Builder pattern for complex objects
  - Check field initialization order
```

### Problem: MapConverter Type Issues

#### Symptoms
- ClassCastException when accessing values
- Unexpected null values
- Type conversion errors
- Generic type warnings

#### Diagnostic Steps
```java
// Check map access patterns
Map<String, Object> result = mapConverter.convert(response);
Object value = result.get("temperature");

// Type checking
if (value instanceof Number) {
    Double temp = ((Number) value).doubleValue();
}
```

#### Solutions
```yaml
Type Safety:
  - Always check instance type before casting
  - Use type-safe accessors
  - Handle null values explicitly
  - Consider BeanConverter for known schemas

Map Structure:
  - Validate key existence before access
  - Handle nested map structures
  - Use utility methods for common conversions
  - Log map contents for debugging
```

## ⚡ Performance Issues

### Problem: Slow Response Times

#### Symptoms
- Requests taking >30 seconds
- Timeout errors
- High memory usage
- CPU spikes

#### Diagnostic Steps
```bash
# Monitor response times
curl -w "%{time_total}\n" http://localhost:8080/api/s16/real-world-demo/overview

# Check memory usage
jcmd <pid> VM.memory_pools

# Monitor thread usage
jstack <pid>
```

#### Solutions
```yaml
AI Model Optimization:
  - Use faster models (gpt-3.5-turbo vs gpt-4)
  - Reduce token count in requests
  - Implement response caching
  - Use parallel processing for batch requests

Application Optimization:
  - Increase heap size: -Xmx2g
  - Use connection pooling
  - Implement async processing
  - Add performance monitoring

Network Optimization:
  - Check network latency to OpenAI
  - Configure appropriate timeouts
  - Use CDN for static content
  - Optimize request/response sizes
```

### Problem: Memory Leaks

#### Symptoms
- OutOfMemoryError exceptions
- Gradual memory increase
- GC pressure
- Application becomes unresponsive

#### Diagnostic Steps
```bash
# Monitor memory usage
jcmd <pid> GC.run_finalization
jcmd <pid> VM.memory_usage

# Generate heap dump
jcmd <pid> GC.heap_dump /tmp/heapdump.hprof

# Analyze with tools like VisualVM or Eclipse MAT
```

#### Solutions
```yaml
Memory Management:
  - Implement proper response caching with TTL
  - Clear converter caches periodically
  - Use streaming for large responses
  - Implement circuit breakers

Resource Cleanup:
  - Close HTTP connections properly
  - Clean up thread pools
  - Manage converter instances
  - Implement proper disposal patterns
```

## 🔍 Advanced Troubleshooting

### Problem: JSON Schema Validation Failures

#### Symptoms
- Schema generation errors
- Invalid JSON Schema warnings
- Validation bypassed unexpectedly
- Complex object handling issues

#### Diagnostic Steps
```bash
# Check generated schema
curl -v http://localhost:8080/api/s4/weather \
  -H "Content-Type: application/json" \
  -d '{"city": "Test"}' 2>&1 | grep -i schema

# Validate schema manually
node -e "const schema = require('./schema.json'); console.log(JSON.stringify(schema, null, 2));"
```

#### Solutions
```yaml
Schema Generation:
  - Verify Jackson annotations
  - Check for circular references
  - Review inheritance structures
  - Test with simple objects first

Validation Configuration:
  - Enable detailed validation logging
  - Review validation error messages
  - Check schema compliance tools
  - Test with online JSON Schema validators
```

### Problem: Concurrent Access Issues

#### Symptoms
- Race conditions in converters
- Inconsistent results
- Thread safety exceptions
- Data corruption

#### Diagnostic Steps
```bash
# Check thread usage
jstack <pid> | grep -A 5 -B 5 "converter"

# Monitor concurrent requests
curl -v http://localhost:8080/api/s4/weather & \
curl -v http://localhost:8080/api/s4/profile & \
wait
```

#### Solutions
```yaml
Thread Safety:
  - Use thread-safe converter instances
  - Implement proper synchronization
  - Consider immutable data structures
  - Review shared state access

Concurrency Control:
  - Implement rate limiting
  - Use connection pooling
  - Add circuit breakers
  - Monitor resource contention
```

### Problem: Integration Test Failures

#### Symptoms
- Tests pass individually but fail in suite
- Inconsistent test results
- Mock vs real API discrepancies
- Environment-specific failures

#### Diagnostic Steps
```bash
# Run individual tests
./mvnw test -Dtest=ConverterTest

# Run with different profiles
./mvnw test -Dspring.profiles.active=test

# Check test logs
tail -f target/surefire-reports/*.txt
```

#### Solutions
```yaml
Test Configuration:
  - Use proper test profiles
  - Implement test data isolation
  - Mock external dependencies
  - Reset state between tests

Environment Issues:
  - Check test environment variables
  - Verify test database configuration
  - Review CI/CD pipeline setup
  - Test with production-like data
```

## 🛡️ Security Issues

### Problem: API Key Exposure

#### Symptoms
- API keys in logs
- Keys in version control
- Unauthorized API usage
- Security scan failures

#### Diagnostic Steps
```bash
# Check for exposed keys
grep -r "sk-" --include="*.java" --include="*.properties" .
git log --grep="api.key" --oneline

# Review log files
grep -i "key\|token\|secret" logs/*.log
```

#### Solutions
```yaml
Key Protection:
  - Use environment variables only
  - Add sensitive files to .gitignore
  - Implement key rotation procedures
  - Use secret management systems

Logging Security:
  - Filter sensitive data from logs
  - Use structured logging
  - Implement log sanitization
  - Review log retention policies
```

## 📱 Postman-Specific Issues

### Problem: Postman Collection Import Failures

#### Symptoms
- Collection import errors
- Missing environment variables
- Request format issues
- Test script failures

#### Diagnostic Steps
```bash
# Validate JSON format
cat Spring-AI-Structured-Output-Course.postman_collection.json | jq .

# Check environment variables
cat Spring-AI-Course-Environment.postman_environment.json | jq .values
```

#### Solutions
```yaml
Collection Issues:
  - Verify JSON format validity
  - Check Postman version compatibility
  - Review collection schema version
  - Test with minimal collection first

Environment Setup:
  - Import environment file separately
  - Verify variable scope settings
  - Check variable name consistency
  - Test variable interpolation
```

### Problem: Test Script Failures

#### Symptoms
- JavaScript errors in tests
- Assertion failures
- Variable access issues
- Pre-request script problems

#### Diagnostic Steps
```javascript
// Debug in Postman console
console.log("Request:", pm.request.url.toString());
console.log("Response:", pm.response.text());
console.log("Variables:", pm.environment.toObject());
```

#### Solutions
```yaml
Script Debugging:
  - Use console.log for debugging
  - Check JavaScript syntax
  - Verify API method availability
  - Test scripts in isolation

Variable Issues:
  - Check variable scope (global vs environment)
  - Verify variable name spelling
  - Review variable initialization
  - Test variable persistence
```

## 🚀 Recovery Procedures

### Emergency Demo Recovery

```yaml
Level 1 - Quick Fixes (30 seconds):
  - Restart application: Ctrl+C, ./mvnw spring-boot:run
  - Switch to backup environment
  - Use pre-prepared response examples
  - Continue with next working endpoint

Level 2 - Moderate Recovery (2 minutes):
  - Check API key configuration
  - Clear application cache
  - Restart with debug logging
  - Use mock mode if available

Level 3 - Full Recovery (5 minutes):
  - Rebuild application: ./mvnw clean package
  - Reset environment variables
  - Import backup Postman collection
  - Switch to local mock server
```

### Preventive Measures

```yaml
Pre-Demo Checklist:
  - ✅ Test all requests 30 minutes before
  - ✅ Verify API key balance and limits
  - ✅ Check network connectivity
  - ✅ Prepare backup response examples
  - ✅ Have mock mode ready
  - ✅ Test recovery procedures

Monitoring Setup:
  - Enable detailed logging
  - Monitor API usage dashboard
  - Set up health check alerts
  - Track performance metrics
  - Monitor error rates
```

---

**🛠️ Pro Tip:** Keep a troubleshooting checklist handy during live demonstrations. Most issues have simple solutions if you know where to look first!