## Test Cases for TravelController

Here are comprehensive test cases for your `TravelController` that you can use:

### 1. Recommend a Single Destination

**Request:**
- Method: POST
- URL: `http://localhost:8080/api/travel/destinations/recommend`
- Headers: Content-Type: application/json
- Body:
```json
{
  "season": "summer",
  "budget": "medium",
  "travelStyle": "adventure",
  "region": "Europe"
}
```

**Expected Response:**
A JSON object with details about a recommended destination, including:
- name
- country
- bestTimeToVisit
- estimatedCost
- highlights
- weather
- localCuisine

### 2. Suggest Multiple Destinations

**Request:**
- Method: POST
- URL: `http://localhost:8080/api/travel/destinations/suggest?count=3`
- Headers: Content-Type: application/json
- Body:
```json
{
  "season": "winter",
  "budget": "luxury",
  "travelStyle": "relaxation",
  "region": "Asia"
}
```

**Expected Response:**
A JSON object with:
- destinations (array of destination objects)
- summary (overview of the recommendations)

### 3. Suggest Activities for a Destination

**Request:**
- Method: GET
- URL: `http://localhost:8080/api/travel/activities?destination=Paris&travelStyle=cultural&count=5`

**Expected Response:**
An array of strings, each representing an activity in Paris suitable for a cultural travel style. For example:
```json
[
  "Visit the Louvre Museum",
  "Explore Notre-Dame Cathedral",
  "Take a walking tour of Montmartre",
  "Attend a classical music concert",
  "Browse specialty bookshops in Latin Quarter"
]
```

### 4. Get a Daily Itinerary

**Request:**
- Method: GET
- URL: `http://localhost:8080/api/travel/itinerary?destination=New York&travelStyle=family`

**Expected Response:**
An array of strings representing a chronological schedule of activities, such as:
```json
[
  "Morning: Visit the American Museum of Natural History",
  "Late Morning: Explore Central Park Zoo",
  "Lunch: Enjoy pizza at a family-friendly restaurant",
  "Afternoon: Take a ferry to the Statue of Liberty",
  "Evening: Watch a Broadway show suitable for children",
  "Dinner: Dine at Times Square restaurant"
]
```

### 5. Get a Packing List

**Request:**
- Method: GET
- URL: `http://localhost:8080/api/travel/packing-list?destination=Iceland&season=winter`

**Expected Response:**
An array of strings listing recommended items to pack, such as:
```json
[
  "Thermal base layers",
  "Waterproof winter jacket",
  "Insulated snow pants",
  "Waterproof hiking boots",
  "Wool socks (multiple pairs)",
  "Warm hat, scarf, and gloves",
  "Swimwear for hot springs",
  "Camera with extra batteries",
  "Power adapter for Iceland",
  "Moisturizer and lip balm",
  "Passport and travel documents",
  "Credit cards and local currency"
]
```

## Additional Test Cases

### 6. Recommend a Destination with Different Parameters

**Request:**
- Method: POST
- URL: `http://localhost:8080/api/travel/destinations/recommend`
- Body:
```json
{
  "season": "fall",
  "budget": "budget",
  "travelStyle": "foodie",
  "region": "North America"
}
```

### 7. Suggest Activities for a Remote Location

**Request:**
- Method: GET
- URL: `http://localhost:8080/api/travel/activities?destination=Patagonia&travelStyle=outdoor&count=7`

### 8. Get a Packing List for Tropical Climate

**Request:**
- Method: GET
- URL: `http://localhost:8080/api/travel/packing-list?destination=Bali&season=rainy`

### 9. Get an Itinerary for Business Travel

**Request:**
- Method: GET
- URL: `http://localhost:8080/api/travel/itinerary?destination=Singapore&travelStyle=business`

### 10. Suggest Multiple Destinations with Higher Count

**Request:**
- Method: POST
- URL: `http://localhost:8080/api/travel/destinations/suggest?count=5`
- Body:
```json
{
  "season": "spring",
  "budget": "high",
  "travelStyle": "luxury",
  "region": "Mediterranean"
}
```

## Testing Instructions

1. Create a new Postman collection named "Travel API"
2. Add folders for each endpoint type: "Destinations", "Activities", "Itineraries", "Packing Lists"
3. Create requests for each test case described above
4. For POST requests, set the body type to "raw" and format to "JSON"
5. For GET requests with query parameters, make sure they're properly encoded
6. Run each request and verify that the responses match the expected format
7. Save successful responses as examples for future reference

These tests should cover all endpoints of your TravelController and verify that both the MapOutputConverter and ListOutputConverter are working correctly with different input parameters.