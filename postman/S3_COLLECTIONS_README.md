# S3 Movie Recommendation Collections

## Overview

I've created comprehensive Postman collections for the S3 movie recommendation endpoints that were missing from the original collection.

## What's Been Added

### 1. Separate S3-Specific Collection
**File:** `S3-Movie-Recommendations.postman_collection.json`

**Features:**
- 🎬 **Single Movie Recommendation** - `/api/movies/recommend`
- 🎭 **Comedy Movie Recommendation** - Genre-specific example
- 🎬 **Multiple Movie Recommendations** - `/api/movies/recommend/multiple`
- 🎪 **Diverse Genre Collection** - Quality drama selections
- 🌟 **Classic Movie Request** - Vintage thriller example

**Test Coverage:**
- ✅ Complete response structure validation
- ✅ Movie data type verification
- ✅ Rating range validation (0-10)
- ✅ Genre filtering accuracy
- ✅ Array response validation for multiple recommendations
- ✅ Streaming platform availability checks

### 2. Updated Main Collection
**File:** `Spring-AI-Structured-Output-Course.postman_collection.json`

**New Additions in Phase 1:**
- 🎬 **S3: Single Movie Recommendation**
- 🎬 **S3: Multiple Movie Recommendations**

**Updated Description:**
- Extended timing to 47+ minutes total
- Added movie recommendation system examples
- Updated phase descriptions

## Endpoint Details

### Single Movie Recommendation
```http
POST /api/movies/recommend
Content-Type: application/json

{
  "genre": "Action",
  "releaseYearAfter": 2010,
  "mood": "exciting",
  "maxResults": 1
}
```

**Response Structure:**
```json
{
  "title": "Mad Max: Fury Road",
  "year": 2015,
  "director": "George Miller",
  "genre": "Action",
  "rating": 8.1,
  "summary": "In a post-apocalyptic wasteland...",
  "streamingPlatforms": ["HBO Max", "Amazon Prime Video"]
}
```

### Multiple Movie Recommendations
```http
POST /api/movies/recommend/multiple
Content-Type: application/json

{
  "genre": "Sci-Fi",
  "releaseYearAfter": 2015,
  "mood": "thoughtful",
  "maxResults": 3
}
```

**Response Structure:**
```json
[
  {
    "title": "Arrival",
    "year": 2016,
    "director": "Denis Villeneuve",
    "genre": "Sci-Fi",
    "rating": 7.9,
    "summary": "A linguist works with the military...",
    "streamingPlatforms": ["Amazon Prime", "Hulu"]
  },
  // ... more movies
]
```

## Request Parameters

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `genre` | String | Movie genre filter | "Action", "Comedy", "Sci-Fi", "Drama" |
| `releaseYearAfter` | Integer | Minimum release year | 2010, 2015, 1990 |
| `mood` | String | Desired mood/tone | "exciting", "thoughtful", "lighthearted" |
| `maxResults` | Integer | Max number of recommendations | 1-5 |

## Test Scenarios Included

### Diverse Genre Testing
- **Action Movies** - Exciting, modern blockbusters
- **Comedy Movies** - Lighthearted entertainment
- **Sci-Fi Movies** - Thoughtful, intelligent films
- **Drama Movies** - Inspiring, quality cinema
- **Thriller Movies** - Classic suspenseful films

### Validation Testing
- Response structure completeness
- Data type accuracy
- Array handling for multiple results
- Genre filtering effectiveness
- Rating range validation
- Streaming platform data presence

## Usage Instructions

### For Separate Collection
1. Import `S3-Movie-Recommendations.postman_collection.json`
2. Set environment variable `baseUrl` to `http://localhost:8080`
3. Run individual requests or entire collection
4. Review test results for validation

### For Updated Main Collection
1. Re-import `Spring-AI-Structured-Output-Course.postman_collection.json`
2. The S3 endpoints are now included in **Phase 1: Foundation & Setup**
3. Run the full progressive learning sequence
4. S3 endpoints demonstrate movie recommendation use cases

## Testing Status

✅ **All endpoints tested and working**
- Single recommendation: Returns complete movie object
- Multiple recommendations: Returns array of movie objects
- All test validations passing
- Response times: < 5 seconds per request

## Learning Objectives Covered

### For Instructors
- **Structured Data Extraction**: How AI can generate complex, nested objects
- **Industry Use Cases**: Real-world entertainment recommendation systems
- **Array vs Object Responses**: When to use single vs multiple result endpoints
- **Data Validation**: Ensuring AI responses meet business requirements

### For Students
- Understanding structured output for recommendation systems
- Seeing practical applications of Spring AI in entertainment
- Learning to handle both single and multiple result scenarios
- Observing proper JSON structure for complex domain objects

## Architecture Demonstrated

```
MoviePreferenceRequest (Input)
    ↓
Spring AI + OpenAI
    ↓
BeanOutputConverter<MovieRecommendation>
    ↓
Structured Movie Data (Output)
```

The S3 endpoints showcase:
- **Complex Domain Objects**: Movies with multiple properties
- **Business Logic Integration**: Genre, year, and mood filtering
- **Real-world Data Structures**: Streaming platforms, ratings, summaries
- **Scalable Patterns**: Single vs batch recommendation modes

---

**Ready for immediate use in Spring AI structured output demonstrations! 🎬✨**