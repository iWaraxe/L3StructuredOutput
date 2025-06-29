# Test Cases for BeanConverterDemoController

Here are comprehensive test cases for BeanConverterDemoController that you can use in Postman:

## 1. Get Book Recommendation

**Request URL:**
```
GET http://localhost:8080/api/books/recommend
```

**Query Parameters:**
- `genre`: fantasy
- `mood`: suspenseful
- `theme`: redemption

**Full URL:**
```
http://localhost:8080/api/books/recommend?genre=fantasy&mood=suspenseful&theme=redemption
```

**Expected Response (Sample):**
```json
{
  "title": "The Shadow of the Wind",
  "author": "Carlos Ruiz Zafón",
  "genre": "Fantasy",
  "publicationYear": 2001,
  "summary": "In post-war Barcelona, a young boy discovers a mysterious book in the Cemetery of Forgotten Books, leading him into a dark labyrinth of secrets and buried memories."
}
```

## 2. Get Book Recommendations by Mood

**Request URL:**
```
GET http://localhost:8080/api/books/recommend-by-mood
```

**Query Parameters:**
- `genre`: science fiction
- `moods`: inspiring,melancholic,thrilling

**Full URL:**
```
http://localhost:8080/api/books/recommend-by-mood?genre=science fiction&moods=inspiring&moods=melancholic&moods=thrilling
```

**Expected Response (Sample):**
```json
{
  "inspiring": [
    {
      "title": "Project Hail Mary",
      "author": "Andy Weir",
      "genre": "Science Fiction",
      "publicationYear": 2021,
      "summary": "A lone astronaut must save humanity from extinction through scientific ingenuity and unexpected cooperation."
    },
    {
      "title": "The Martian",
      "author": "Andy Weir",
      "genre": "Science Fiction",
      "publicationYear": 2011,
      "summary": "An astronaut stranded on Mars uses his scientific knowledge and determination to survive against all odds."
    }
  ],
  "melancholic": [
    {
      "title": "Never Let Me Go",
      "author": "Kazuo Ishiguro",
      "genre": "Science Fiction",
      "publicationYear": 2005,
      "summary": "A haunting story of three friends who gradually discover the truth about their special upbringing and their predetermined fate."
    },
    {
      "title": "Station Eleven",
      "author": "Emily St. John Mandel",
      "genre": "Science Fiction",
      "publicationYear": 2014,
      "summary": "A post-apocalyptic novel following a traveling Shakespeare company in a world devastated by a pandemic."
    }
  ],
  "thrilling": [
    {
      "title": "Dark Matter",
      "author": "Blake Crouch",
      "genre": "Science Fiction",
      "publicationYear": 2016,
      "summary": "A physicist is kidnapped and sent into a multiverse where he confronts alternate versions of his life and must find his way back to his reality."
    },
    {
      "title": "Recursion",
      "author": "Blake Crouch",
      "genre": "Science Fiction",
      "publicationYear": 2019,
      "summary": "A detective and a neuroscientist work together to investigate a memory-altering phenomenon that's causing chaos and reshaping reality."
    }
  ]
}
```

## 3. Get Capital Info

**Request URL:**
```
GET http://localhost:8080/api/capitals/{country}
```

**Path Variable:**
- `country`: france

**Full URL:**
```
http://localhost:8080/api/capitals/france
```

**Expected Response (Sample):**
```json
{
  "city": "Paris",
  "population": 2.16,
  "region": "Île-de-France",
  "language": "French",
  "currency": "Euro",
  "landmarks": [
    "Eiffel Tower",
    "Louvre Museum",
    "Notre-Dame Cathedral",
    "Arc de Triomphe",
    "Sacré-Cœur Basilica"
  ]
}
```

**Additional Test Cases:**
```
http://localhost:8080/api/capitals/japan
http://localhost:8080/api/capitals/brazil
http://localhost:8080/api/capitals/egypt
```

## 4. Compare Capitals

**Request URL:**
```
GET http://localhost:8080/api/capitals/compare
```

**Query Parameters:**
- `countries`: usa,uk,australia

**Full URL:**
```
http://localhost:8080/api/capitals/compare?countries=usa&countries=uk&countries=australia
```

**Expected Response (Sample):**
```json
{
  "usa": {
    "city": "Washington, D.C.",
    "population": 0.69,
    "region": "District of Columbia",
    "language": "English",
    "currency": "US Dollar",
    "landmarks": [
      "White House",
      "United States Capitol",
      "Lincoln Memorial",
      "Washington Monument"
    ]
  },
  "uk": {
    "city": "London",
    "population": 8.98,
    "region": "Greater London",
    "language": "English",
    "currency": "Pound Sterling",
    "landmarks": [
      "Buckingham Palace",
      "Tower of London",
      "Big Ben",
      "London Eye"
    ]
  },
  "australia": {
    "city": "Canberra",
    "population": 0.43,
    "region": "Australian Capital Territory",
    "language": "English",
    "currency": "Australian Dollar",
    "landmarks": [
      "Parliament House",
      "Australian War Memorial",
      "National Gallery of Australia",
      "Lake Burley Griffin"
    ]
  }
}
```

**Additional Test Cases:**
```
http://localhost:8080/api/capitals/compare?countries=germany&countries=france&countries=italy
http://localhost:8080/api/capitals/compare?countries=china&countries=japan&countries=india
```

## 5. Edge Case Tests

### 5.1 Book Recommendation with Unusual Parameters

**Request URL:**
```
http://localhost:8080/api/books/recommend?genre=cyberpunk&mood=philosophical&theme=consciousness
```

### 5.2 Book Recommendations with Single Mood

**Request URL:**
```
http://localhost:8080/api/books/recommend-by-mood?genre=historical fiction&moods=educational
```

### 5.3 Capital Info for Non-Existent Country

**Request URL:**
```
http://localhost:8080/api/capitals/atlantis
```

### 5.4 Compare Capitals with Invalid Country

**Request URL:**
```
http://localhost:8080/api/capitals/compare?countries=usa&countries=narnia
```

### 5.5 Book Recommendation with Empty Parameters

**Request URL:**
```
http://localhost:8080/api/books/recommend?genre=&mood=&theme=
```

## Testing Instructions

1. Create a new Postman collection named "Bean Converter Demo API"
2. Add a folder for each endpoint type: "Book Recommendations", "Capital Info", "Edge Cases"
3. Create requests for each test case listed above
4. For the multiple parameters (like in "recommend-by-mood" endpoint), make sure to use the same parameter name multiple times (Postman handles this correctly)
5. Execute the requests and verify the responses match the expected format
6. Save the responses for reference

These tests thoroughly cover BeanConverterDemoController functionality. The expected responses are samples - the actual content will be generated by your AI service based on the prompt templates in services.