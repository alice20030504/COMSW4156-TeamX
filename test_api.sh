#!/bin/bash

# Fitness Management Service API Test Script
# Base URL
BASE_URL="http://localhost:8080"

echo "🏃‍♂️ Testing Fitness Management Service API"
echo "=============================================="

# Test 1: Health Check (Personal Controller)
echo ""
echo "1. Testing Health Check..."
curl -X GET \
  -H "X-Client-ID: mobile-app1" \
  "$BASE_URL/api/persons/health" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# Test 2: Create User Account
echo ""
echo "2. Testing Create User Account..."
curl -X POST \
  -H "X-Client-ID: mobile-app1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "weight": 75.5,
    "height": 180.0,
    "birthDate": "1990-05-15"
  }' \
  "$BASE_URL/api/persons" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# Test 3: Calculate BMI
echo ""
echo "3. Testing BMI Calculation..."
curl -X GET \
  -H "X-Client-ID: mobile-app1" \
  "$BASE_URL/api/persons/bmi?weight=75.5&height=180.0" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# Test 4: Calculate Daily Calories
echo ""
echo "4. Testing Daily Calories Calculation..."
curl -X GET \
  -H "X-Client-ID: mobile-app1" \
  "$BASE_URL/api/persons/calories?weight=75.5&height=180.0&age=34&gender=male&weeklyTrainingFreq=4" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# Test 5: Get User Record (requires authentication)
echo ""
echo "5. Testing Get User Record (with authentication)..."
curl -X GET \
  -H "X-Client-ID: mobile-app1" \
  "$BASE_URL/api/persons/1?birthDate=1990-05-15" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# Test 6: Research Endpoints (should work with research client)
echo ""
echo "6. Testing Research - Get All Users Anonymized..."
curl -X GET \
  -H "X-Client-ID: research-tool1" \
  "$BASE_URL/api/research/persons" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# Test 7: Research - Get Demographic Statistics
echo ""
echo "7. Testing Research - Get Demographic Statistics..."
curl -X GET \
  -H "X-Client-ID: research-tool1" \
  "$BASE_URL/api/research/demographics?ageRange=25-34&gender=male&objective=BULK" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# Test 8: Research - Get Workout Patterns
echo ""
echo "8. Testing Research - Get Workout Patterns..."
curl -X GET \
  -H "X-Client-ID: research-tool1" \
  "$BASE_URL/api/research/workout-patterns?ageRange=25-34" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# Test 9: Research - Get Nutrition Trends
echo ""
echo "9. Testing Research - Get Nutrition Trends..."
curl -X GET \
  -H "X-Client-ID: research-tool1" \
  "$BASE_URL/api/research/nutrition-trends?objective=BULK" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# Test 10: Research - Get Population Health Metrics
echo ""
echo "10. Testing Research - Get Population Health Metrics..."
curl -X GET \
  -H "X-Client-ID: research-tool1" \
  "$BASE_URL/api/research/population-health" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# Test 11: Test Access Control (Mobile client trying to access research endpoint)
echo ""
echo "11. Testing Access Control (Mobile client accessing research endpoint - should fail)..."
curl -X GET \
  -H "X-Client-ID: mobile-app1" \
  "$BASE_URL/api/research/persons" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo ""
echo "=============================================="
echo "✅ API Testing Complete!"
echo ""
echo "Expected Results:"
echo "- Health Check: 200 OK"
echo "- Create User: 201 Created"
echo "- BMI Calculation: 200 OK"
echo "- Calories Calculation: 200 OK"
echo "- Get User Record: 200 OK (if user exists) or 404 Not Found"
echo "- Research endpoints with research-tool1: 200 OK"
echo "- Research endpoint with mobile-app1: 403 Forbidden"
