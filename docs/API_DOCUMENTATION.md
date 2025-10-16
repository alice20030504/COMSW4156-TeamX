# API Documentation

## Overview

This document provides detailed documentation for all API endpoints in the Personal Fitness Management Service. The service is a RESTful API that provides fitness calculation and research data endpoints.

**Base URL**: `http://localhost:8080`

**API Documentation UI**: `http://localhost:8080/swagger-ui.html`

## Authentication

Currently, no authentication is required (this will be added in future iterations).

## Response Format

All responses are in JSON format with appropriate HTTP status codes.

### Success Responses
- `200 OK` - Request successful
- `201 Created` - Resource created successfully

### Error Responses
- `400 Bad Request` - Invalid input parameters
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## API Endpoints

### Person Controller

Handles fitness-related calculations for individuals.

#### 1. Calculate BMI

Calculates Body Mass Index based on weight and height.

**Endpoint**: `GET /api/persons/bmi`

**Parameters**:
- `weight` (required, Double): Weight in kilograms
- `height` (required, Double): Height in centimeters

**Example Request**:
```http
GET /api/persons/bmi?weight=70&height=175
```

**Example Response**:
```json
{
  "weight": 70.0,
  "height": 175.0,
  "bmi": 22.86,
  "category": "Normal weight"
}
```

**BMI Categories**:
- `< 18.5`: Underweight
- `18.5 - 24.9`: Normal weight
- `25.0 - 29.9`: Overweight
- `≥ 30.0`: Obese

**Status Codes**:
- `200 OK`: Success
- `400 Bad Request`: Invalid parameters (negative values, null)

---

#### 2. Calculate Age

Calculates age from birth date.

**Endpoint**: `GET /api/persons/age`

**Parameters**:
- `birthDate` (required, String): Birth date in ISO format (YYYY-MM-DD)

**Example Request**:
```http
GET /api/persons/age?birthDate=1990-05-15
```

**Example Response**:
```json
{
  "birthDate": "1990-05-15",
  "age": 35
}
```

**Status Codes**:
- `200 OK`: Success
- `400 Bad Request`: Invalid date format

---

#### 3. Calculate Daily Calorie Needs

Calculates daily calorie requirements based on BMR and activity level.

**Endpoint**: `GET /api/persons/calories`

**Parameters**:
- `weight` (required, Double): Weight in kilograms
- `height` (required, Double): Height in centimeters
- `age` (required, Integer): Age in years
- `gender` (required, String): "male" or "female"
- `weeklyTrainingFreq` (required, Integer): Training frequency (0-7 days per week)

**Example Request**:
```http
GET /api/persons/calories?weight=70&height=175&age=30&gender=male&weeklyTrainingFreq=4
```

**Example Response**:
```json
{
  "bmr": 1680.5,
  "dailyCalories": 2520.75,
  "weeklyTrainingFreq": 4
}
```

**Activity Level Multipliers**:
- 0-1 days: 1.2 (Sedentary)
- 2-3 days: 1.375 (Lightly active)
- 4-5 days: 1.55 (Moderately active)
- 6-7 days: 1.725 (Very active)
- 7+ days: 1.9 (Extra active)

**Status Codes**:
- `200 OK`: Success
- `400 Bad Request`: Invalid parameters

---

#### 4. Health Check

Service health check endpoint.

**Endpoint**: `GET /api/persons/health`

**Parameters**: None

**Example Request**:
```http
GET /api/persons/health
```

**Example Response**:
```json
{
  "status": "UP",
  "service": "Personal Fitness Management Service",
  "version": "1.0.0"
}
```

**Status Codes**:
- `200 OK`: Service is running

---

### Research Controller

Provides anonymized research data and analytics (mock data for demonstration).

#### 5. Get Workout Patterns

Returns aggregated workout pattern data.

**Endpoint**: `GET /api/research/workout-patterns`

**Parameters**: None

**Example Request**:
```http
GET /api/research/workout-patterns
```

**Example Response**:
```json
{
  "totalSampleSize": 156,
  "averageWorkoutDuration": 52.0,
  "averageWeeklyFrequency": 3.8,
  "mostPopularExerciseType": "Cardio",
  "patterns": {
    "avgBMI": 24.5,
    "avgWeight": 72.3,
    "avgHeight": 171.2,
    "avgBodyFat": 18.5,
    "avgWeeklyWorkouts": 4.2
  },
  "nutritionStats": {
    "avgDailyCalories": 2450.0,
    "avgProteinGrams": 120.0,
    "avgCarbsGrams": 280.0,
    "avgFatGrams": 85.0
  }
}
```

**Status Codes**:
- `200 OK`: Success
- `403 Forbidden`: Insufficient sample size for anonymization

---

#### 6. Get Population Health Metrics

Returns aggregated health metrics for a population.

**Endpoint**: `GET /api/research/population-health`

**Parameters**: None

**Example Request**:
```http
GET /api/research/population-health
```

**Example Response**:
```json
{
  "totalPopulation": 89,
  "ageGroups": {
    "18-30": 45,
    "31-45": 30,
    "46-60": 25,
    "avgCaloriesByAge": 3200
  },
  "genderBreakdown": {
    "male": 35,
    "female": 40,
    "other": 25,
    "avgCaloriesByGender": 2000
  },
  "activityLevels": {
    "sedentary": 40,
    "moderate": 30,
    "active": 30,
    "avgCaloriesByActivity": 2500
  },
  "totalRecords": 234
}
```

**Status Codes**:
- `200 OK`: Success
- `403 Forbidden`: Insufficient sample size

---

#### 7. Get Nutrition Trends

Returns nutrition consumption trends.

**Endpoint**: `GET /api/research/nutrition-trends`

**Parameters**: None

**Example Request**:
```http
GET /api/research/nutrition-trends
```

**Example Response**:
```json
{
  "macronutrientTrends": {
    "proteinPercentage": 5.2,
    "carbsPercentage": 48.3,
    "fatsPercentage": 32.1,
    "fiberGrams": 14.4
  },
  "calorieDistribution": {
    "breakfast": 67.8,
    "lunch": 62.3,
    "dinner": 71.5,
    "snacks": 82.1
  },
  "sampleSize": 1523
}
```

**Status Codes**:
- `200 OK`: Success

---

#### 8. Get Demographics

Returns demographic information (mock data).

**Endpoint**: `GET /api/research/demographics`

**Parameters**: None

**Example Request**:
```http
GET /api/research/demographics
```

**Example Response**:
```json
{
  "message": "Demographics data would be aggregated here",
  "sampleSize": 0
}
```

**Status Codes**:
- `200 OK`: Success

---

## Error Handling

All endpoints return appropriate HTTP status codes and error messages.

### Error Response Format

```json
{
  "timestamp": "2025-10-16T18:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid parameter: weight must be positive",
  "path": "/api/persons/bmi"
}
```

---

## Rate Limiting

**Note**: Rate limiting is configured but not enforced in the current iteration.

- General endpoints: 60 requests/minute
- Research endpoints: 30 requests/minute

---

## Data Privacy

Research endpoints return only aggregated, anonymized data:
- Minimum cohort size: 10 participants
- No personally identifiable information (PII)
- Data retention: 365 days

---

## API Usage Examples

### Using cURL

```bash
# Calculate BMI
curl "http://localhost:8080/api/persons/bmi?weight=70&height=175"

# Calculate daily calories
curl "http://localhost:8080/api/persons/calories?weight=70&height=175&age=30&gender=male&weeklyTrainingFreq=4"

# Get workout patterns
curl "http://localhost:8080/api/research/workout-patterns"
```

### Using JavaScript (Fetch API)

```javascript
// Calculate BMI
fetch('http://localhost:8080/api/persons/bmi?weight=70&height=175')
  .then(response => response.json())
  .then(data => console.log(data));

// Calculate daily calories
const params = new URLSearchParams({
  weight: 70,
  height: 175,
  age: 30,
  gender: 'male',
  weeklyTrainingFreq: 4
});

fetch(`http://localhost:8080/api/persons/calories?${params}`)
  .then(response => response.json())
  .then(data => console.log(data));
```

### Using Python (requests)

```python
import requests

# Calculate BMI
response = requests.get('http://localhost:8080/api/persons/bmi',
                       params={'weight': 70, 'height': 175})
print(response.json())

# Calculate daily calories
params = {
    'weight': 70,
    'height': 175,
    'age': 30,
    'gender': 'male',
    'weeklyTrainingFreq': 4
}
response = requests.get('http://localhost:8080/api/persons/calories',
                       params=params)
print(response.json())
```

---

## Interactive API Testing

For interactive testing and exploration of all endpoints, visit:

**Swagger UI**: http://localhost:8080/swagger-ui.html

The Swagger UI provides:
- Complete API documentation
- Interactive testing interface
- Request/response schemas
- Example values
- "Try it out" functionality for all endpoints

---

## Database Schema

### PersonSimple Entity

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | Primary Key, Auto-increment | Unique identifier |
| name | String | Not null | Person's name |
| birthDate | LocalDate | Not null | Date of birth |
| weight | Double | Not null | Weight in kg |
| height | Double | Not null | Height in cm |

### ApiLog Entity

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | Primary Key, Auto-increment | Unique identifier |
| clientId | String | Not null | Client identifier |
| endpoint | String | Not null | API endpoint called |
| httpMethod | String | Not null | HTTP method (GET, POST, etc.) |
| timestamp | LocalDateTime | Not null, Auto-generated | Request timestamp |
| requestPayload | String | Optional | Request body (if any) |
| responseStatus | Integer | Optional | HTTP response status |
| responseTime | Long | Optional | Response time in ms |
| ipAddress | String | Optional | Client IP address |
| userAgent | String | Optional | Client user agent |
| errorMessage | String | Optional | Error message (if any) |

---

## API Versioning

Current version: **v1.0.0**

Future versions will be accessible via URL path versioning:
- v1: `/api/v1/...`
- v2: `/api/v2/...`

---

## Changelog

### Version 1.0.0 (2025-10-16)
- Initial release
- Person Controller: BMI, Age, Calorie calculations
- Research Controller: Workout patterns, Population health, Nutrition trends
- Health check endpoint
- Swagger UI documentation

---

## Support

For issues, questions, or feature requests:
- GitHub Issues: [Repository Issues Page]
- Team Contact: See README.md

---

## Future Enhancements

Planned for upcoming iterations:
- User authentication and authorization
- CRUD operations for Person entities
- Exercise tracking endpoints
- Meal planning endpoints
- Goal setting and tracking
- Real-time data aggregation for research endpoints
- WebSocket support for live updates
