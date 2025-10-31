# Demo 1 Procedure

This document outlines the steps to demonstrate the API endpoints and data persistence.

## API 1: Create a Person

```powershell
# Using curl
$body = '{\"name\":\"Alice\",\"weight\":65,\"height\":170,\"birthDate\":\"1992-02-01\"}'; curl.exe -X POST 'http://localhost:8080/api/persons' -H 'Content-Type: application/json' -H 'X-Client-ID: mobile-app1' -d $body
```

Expected output:
```json
{"id":45,"name":"Alice","weight":65.0,"height":170.0,"birthDate":"1992-02-01","clientId":"mobile-app1"}
```

## API 2: Calculate BMI

```powershell
curl.exe -i -X GET "http://localhost:8080/api/persons/bmi?weight=75&height=180" -H "X-Client-ID: mobile-iphone15"
```

Expected output:
```http
HTTP/1.1 200 
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 30 Oct 2025 21:35:12 GMT

{"weight":75.0,"category":"Normal weight","height":180.0,"bmi":23.148148148148145}
```

## API 3: Data Persistence Test

1. Create a new person:
```powershell
$headers = @{ 'Content-Type'='application/json'; 'X-Client-ID'='mobile-app1' }; $body = '{"name":"Peter","weight":55,"height":110,"birthDate":"1992-02-01"}'; Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/persons" -Headers $headers -Body $body
```

2. Read the person's data before restart:
```powershell
curl.exe -s -X GET "http://localhost:8080/api/persons/61?birthDate=1992-02-01" -H "X-Client-ID: mobile-app1"
```

3. Restart the application:
```powershell
docker compose restart app; Start-Sleep -Seconds 3
```

4. Verify data persistence after restart:
```powershell
curl.exe -s -X GET "http://localhost:8080/api/persons/61?birthDate=1992-02-01" -H "X-Client-ID: mobile-app1"
```

## API 4: Research Demographics

```powershell
curl.exe -i -X GET "http://localhost:8080/api/research/demographics" -H "X-Client-ID: research-b"
```

Expected output:
```json
{
    "dataType": "AGGREGATED",
    "containsPII": false,
    "macroDistribution": {
        "averageCalories": 2500,
        "carbs": 40,
        "protein": 30,
        "fat": 30
    },
    "sampleSize": 234,
    "objective": "ALL"
}
```

## API 5: (Mobile tries to access researcher's endpoint)
```powershell
curl.exe -i -X GET "http://localhost:8080/api/research/demographics" -H "X-Client-ID: mobile-b" 
```

Expected ouput:
```json
{"error":"Forbidden","message":"Mobile clients are not authorized to access research endpoints. Research endpoints are restricted to research clients only.","status":403}
```

## TODO - Next Iteration Improvements

### Research Controller Enhancements
1. Replace hardcoded metrics with actual database computations:
   - `demographics`: Compute real age distributions and gender ratios from person records
   - `workout-patterns`: Analyze actual workout frequency data
   - `nutrition-trends`: Calculate real average caloric intakes and macro distributions
   - `population-health`: Generate health statistics from actual BMI distributions

2. Add data aggregation features:
   - Time-based trending (weekly, monthly, yearly stats)
   - Geographic distribution of health metrics
   - Correlation analysis between different health indicators

### Input Validation Improvements
1. Enhanced error handling for illogical inputs:
   - Throw specific exceptions for negative weights/heights with meaningful messages
   - Add warnings for unusual but possible values (e.g., extreme heights/weights)
   - Implement maximum value thresholds for realistic human measurements

2. Additional validation for date-related inputs:
   - Validate birthDate is not in future
   - Add age range restrictions (e.g., 0-150 years)
   - Handle timezone considerations for date comparisons

### New Features Planned
1. Enhanced BMI calculations:
   - Add age-adjusted BMI interpretations
   - Include different BMI standards (e.g., Asian BMI scale)
   - Add body fat percentage estimations

2. Improved research data granularity:
   - Add confidence intervals to statistical calculations
   - Include data quality indicators
   - Support for excluding outliers in research calculations

### Security Enhancements
1. Rate limiting:
   - Implement per-client rate limits
   - Add graduated throttling for heavy users
   - Track and alert on suspicious patterns

2. Enhanced client validation:
   - Implement client registration/verification system
   - Add purpose-specific client types (e.g., `academic-research-`, `clinical-`)
   - Support for temporary research access tokens

### Testing Improvements
1. Edge case coverage:
   - Test extreme value combinations
   - Validate all error messages are helpful
   - Test concurrent access patterns

2. Performance testing:
   - Add load tests for research endpoints
   - Measure and optimize database query performance
   - Test with large datasets
