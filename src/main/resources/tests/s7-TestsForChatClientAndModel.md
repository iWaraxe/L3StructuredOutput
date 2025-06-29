# Testing Spring AI Controllers with Postman

Here are Postman test requests for each of the endpoints:

## 1. Weather API Endpoints

### GET: Weather Info via ChatClient

**Request:**
- Method: GET
- URL: `http://localhost:8080/weather/client?city=Tokyo`

**Parameters:**
- `city`: Tokyo (or any city of your choice)

### GET: Weather Info via ChatModel

**Request:**
- Method: GET
- URL: `http://localhost:8080/weather/model?city=Paris`

**Parameters:**
- `city`: Paris (or any city of your choice)

## 2. Financial Advice API Endpoints

### GET: Budget Tips

**Request:**
- Method: GET
- URL: `http://localhost:8080/finance/budget-tips?monthlyIncome=3500`

**Parameters:**
- `monthlyIncome`: 3500 (or any other amount)

### POST: Investment Analysis

**Request:**
- Method: POST
- URL: `http://localhost:8080/finance/investment-analysis`
- Headers: `Content-Type: application/json`
- Body:
```json
{
  "name": "Tech Growth Fund",
  "type": "Mutual Fund",
  "price": 175.25,
  "history": "15% annual growth over the last 5 years with moderate volatility"
}
```

### GET: Financial Plan

**Request:**
- Method: GET
- URL: `http://localhost:8080/finance/plan`
- Parameters:
    - `goal`: retirement
    - `budget`: 2000
    - `riskTolerance`: moderate
    - `years`: 25

Full URL would be:
```
http://localhost:8080/finance/plan?goal=retirement&budget=2000&riskTolerance=moderate&years=25
```

### GET: Investment Options

**Request:**
- Method: GET
- URL: `http://localhost:8080/finance/investment-options`
- Parameters:
    - `type`: technology
    - `risk`: medium
    - `minReturn`: 8.5

Full URL would be:
```
http://localhost:8080/finance/investment-options?type=technology&risk=medium&minReturn=8.5
```

## Configuration Notes for Testing

Make sure your Spring Boot application has proper configuration for Spring AI in your `application.properties` or `application.yml` file. For OpenAI, you'd need something like:

```properties
# OpenAI API Key
spring.ai.openai.api-key=your_api_key_here

# Chat model configuration
spring.ai.openai.chat.options.model=gpt-4o
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.chat.options.max-tokens=1000
```

Replace `your_api_key_here` with your actual OpenAI API key.

## Testing Tips

1. **Start Small**: Begin with the simplest endpoints (like `/weather/client` or `/finance/budget-tips`) to verify your basic Spring AI integration works

2. **Check Headers**: Make sure to set the correct `Content-Type: application/json` header for POST requests

3. **Request Times**: OpenAI API responses might take a few seconds, so expect some latency in the responses

4. **Rate Limits**: Be aware of OpenAI API rate limits if you're on a free tier or have limited tokens

5. **Look at Network Logs**: In Postman, check the Console or Network tab to see detailed request/response information if you encounter issues

6. **Save Collection**: Consider saving these requests as a Postman Collection for easier repeated testing

7. **Environment Variables**: For convenience, you could set up Postman environment variables like `{{baseUrl}}` and reuse them across requests