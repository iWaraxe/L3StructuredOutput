# Spring AI Structured Output Fundamentals

## Test Case 1: Getting a Single Movie Recommendation (Action Genre)

**Endpoint**: POST http://localhost:8080/api/movies/recommend

**Request Body**:
```json
{
  "genre": "Action",
  "releaseYearAfter": 2010,
  "mood": "Exciting",
  "maxResults": 1
}
```

This will test getting a single action movie recommendation released after 2010 with an exciting mood.

## Test Case 2: Getting Multiple Movie Recommendations (Comedy Genre)

**Endpoint**: POST http://localhost:8080/api/movies/recommend/multiple

**Request Body**:
```json
{
  "genre": "Comedy",
  "releaseYearAfter": 2015,
  "mood": "Lighthearted",
  "maxResults": 3
}
```

This will test getting multiple comedy movie recommendations released after 2015 with a lighthearted mood.

## Test Case 3: Getting a Single Sci-Fi Movie Recommendation

**Endpoint**: POST http://localhost:8080/api/movies/recommend

**Request Body**:
```json
{
  "genre": "Science Fiction",
  "releaseYearAfter": 2000,
  "mood": "Thought-provoking",
  "maxResults": 1
}
```

This tests getting a single sci-fi movie that's thought-provoking and released after 2000.

## Test Case 4: Getting Multiple Drama Movies

**Endpoint**: POST http://localhost:8080/api/movies/recommend/multiple

**Request Body**:
```json
{
  "genre": "Drama",
  "releaseYearAfter": 2020,
  "mood": "Emotional",
  "maxResults": 4
}
```

This tests getting multiple drama movies released after 2020 with an emotional mood.

## Test Case 5: Getting a Classic Horror Movie

**Endpoint**: POST http://localhost:8080/api/movies/recommend

**Request Body**:
```json
{
  "genre": "Horror",
  "releaseYearAfter": 1980,
  "mood": "Suspenseful",
  "maxResults": 1
}
```

This tests getting a horror movie recommendation with an older release date constraint.

## Test Case 6: Getting Multiple Movies with Mixed Criteria

**Endpoint**: POST http://localhost:8080/api/movies/recommend/multiple

**Request Body**:
```json
{
  "genre": "Any",
  "releaseYearAfter": 2022,
  "mood": "Uplifting",
  "maxResults": 5
}
```

This tests getting the most recent releases with an uplifting mood across any genre.

## Test Case 7: Handling an Edge Case (Very Old Movies)

**Endpoint**: POST http://localhost:8080/api/movies/recommend

**Request Body**:
```json
{
  "genre": "Western",
  "releaseYearAfter": 1930,
  "mood": "Nostalgic",
  "maxResults": 1
}
```

This tests how the system handles older film requirements.

## Test Case 8: Very Specific Request

**Endpoint**: POST http://localhost:8080/api/movies/recommend/multiple

**Request Body**:
```json
{
  "genre": "Animation",
  "releaseYearAfter": 2010,
  "mood": "Family-friendly",
  "maxResults": 3
}
```

This tests how well the system can recommend family-friendly animated movies.

## Setting Up Postman

1. Create a new Postman Collection named "Movie Recommendation API Tests"
2. Add request for each test case
3. Set the request type to POST for all requests
4. Set the Content-Type header to application/json
5. Add the JSON body according to each test case
6. Run the tests and examine the structured outputs

## Expected Response Format

The responses should be structured as either a single `MovieRecommendation` object or an array of such objects, each containing:
- title
- year
- director
- genre
- rating
- summary
- streamingPlatforms (as an array)

The responses will demonstrate how well Spring AI is handling the structured output conversion through the BeanOutputConverter.