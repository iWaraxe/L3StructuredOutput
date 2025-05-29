# Section 12: Custom FormatProvider and ConversionService Integration Tests

## Overview
This section demonstrates advanced type conversion and formatting capabilities in Spring AI applications.

## Test Coverage

### 1. MoneyConverterTest (7 tests)
- ✅ Convert with amount and currency
- ✅ Convert with currency symbol
- ✅ Convert with currency first
- ✅ Convert with only amount (defaults to USD)
- ✅ Convert null returns null
- ✅ Convert empty string returns null
- ✅ Convert invalid format throws exception

### 2. DurationConverterTest (9 tests)
- ✅ Convert ISO 8601 format (PT2H30M)
- ✅ Convert human-readable hours
- ✅ Convert human-readable minutes
- ✅ Convert human-readable mixed format
- ✅ Convert full words
- ✅ Convert days
- ✅ Convert null returns null
- ✅ Convert empty string returns null
- ✅ Convert invalid format throws exception

### 3. CustomConversionServiceTest (5 tests)
- ✅ Generate invoice with custom types
- ✅ Generate scheduled event with custom types
- ✅ Convert value with Money type
- ✅ Convert value with Duration type
- ✅ Convert unsupported type throws exception

### 4. CustomConversionControllerTest (6 tests)
- ✅ Generate invoice endpoint
- ✅ Generate event endpoint
- ✅ Convert value with valid conversion
- ✅ Convert value with invalid conversion
- ✅ Get examples endpoint
- ✅ Demonstrate custom types endpoint

## Key Features Tested

### Custom Types
1. **Money** - Currency-aware monetary values
2. **CustomDate** - Date with format and timezone
3. **Duration** - Time durations with flexible parsing
4. **Complex Models** - Invoice, ScheduledEvent with nested types

### Converters
1. **MoneyConverter** - Handles various money formats
2. **CustomDateConverter** - Parses multiple date formats
3. **DurationConverter** - Converts human-readable durations

### Formatters
1. **MoneyFormatter** - Locale-aware money formatting
2. **CustomDateFormatter** - Locale-aware date formatting

### Integration
1. **CustomFormatProvider** - Generates AI-friendly schemas
2. **ConversionService** - Spring's type conversion integration
3. **Custom ObjectMapper** - JSON serialization with custom types

## Test Results
- Total Tests: 27
- Passed: 27
- Failed: 0
- Test Coverage: Comprehensive unit and integration tests

## Usage Examples

### Generate Invoice
```bash
curl -X POST http://localhost:8080/api/custom-conversion/invoice \
  -H "Content-Type: application/json" \
  -d '{"description": "Web development services for January 2024, 40 hours at $150/hour"}'
```

### Generate Event
```bash
curl -X POST http://localhost:8080/api/custom-conversion/event \
  -H "Content-Type: application/json" \
  -d '{"description": "Team meeting next Friday 2-5 PM at Central Park, $50 per person"}'
```

### Convert Value
```bash
curl -X POST http://localhost:8080/api/custom-conversion/convert \
  -H "Content-Type: application/json" \
  -d '{"value": "100.50 USD", "targetType": "Money"}'
```

### Get Examples
```bash
curl http://localhost:8080/api/custom-conversion/examples
```

### Demo Custom Types
```bash
curl http://localhost:8080/api/custom-conversion/demo
```