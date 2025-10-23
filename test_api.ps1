# Fitness Management Service API Test Script (PowerShell)
# Base URL
$BASE_URL = "http://localhost:8080"

Write-Host "🏃‍♂️ Testing Fitness Management Service API" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green

# Test 1: Health Check (Personal Controller)
Write-Host ""
Write-Host "1. Testing Health Check..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/persons/health" -Method GET -Headers @{"X-Client-ID"="mobile-app1"} -ErrorAction Stop
    Write-Host "✅ Success: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Create User Account
Write-Host ""
Write-Host "2. Testing Create User Account..." -ForegroundColor Yellow
$userData = @{
    name = "John Doe"
    weight = 75.5
    height = 180.0
    birthDate = "1990-05-15"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/persons" -Method POST -Body $userData -ContentType "application/json" -Headers @{"X-Client-ID"="mobile-app1"} -ErrorAction Stop
    Write-Host "✅ Success: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Calculate BMI
Write-Host ""
Write-Host "3. Testing BMI Calculation..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/persons/bmi?weight=75.5&height=180.0" -Method GET -Headers @{"X-Client-ID"="mobile-app1"} -ErrorAction Stop
    Write-Host "✅ Success: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Calculate Daily Calories
Write-Host ""
Write-Host "4. Testing Daily Calories Calculation..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/persons/calories?weight=75.5&height=180.0&age=34&gender=male&weeklyTrainingFreq=4" -Method GET -Headers @{"X-Client-ID"="mobile-app1"} -ErrorAction Stop
    Write-Host "✅ Success: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Get User Record (requires authentication)
Write-Host ""
Write-Host "5. Testing Get User Record (with authentication)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/persons/1?birthDate=1990-05-15" -Method GET -Headers @{"X-Client-ID"="mobile-app1"} -ErrorAction Stop
    Write-Host "✅ Success: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Research Endpoints (should work with research client)
Write-Host ""
Write-Host "6. Testing Research - Get All Users Anonymized..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/research/persons" -Method GET -Headers @{"X-Client-ID"="research-tool1"} -ErrorAction Stop
    Write-Host "✅ Success: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 7: Research - Get Demographic Statistics
Write-Host ""
Write-Host "7. Testing Research - Get Demographic Statistics..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/research/demographics?ageRange=25-34&gender=male&objective=BULK" -Method GET -Headers @{"X-Client-ID"="research-tool1"} -ErrorAction Stop
    Write-Host "✅ Success: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 8: Research - Get Workout Patterns
Write-Host ""
Write-Host "8. Testing Research - Get Workout Patterns..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/research/workout-patterns?ageRange=25-34" -Method GET -Headers @{"X-Client-ID"="research-tool1"} -ErrorAction Stop
    Write-Host "✅ Success: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 9: Research - Get Nutrition Trends
Write-Host ""
Write-Host "9. Testing Research - Get Nutrition Trends..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/research/nutrition-trends?objective=BULK" -Method GET -Headers @{"X-Client-ID"="research-tool1"} -ErrorAction Stop
    Write-Host "✅ Success: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 10: Research - Get Population Health Metrics
Write-Host ""
Write-Host "10. Testing Research - Get Population Health Metrics..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/research/population-health" -Method GET -Headers @{"X-Client-ID"="research-tool1"} -ErrorAction Stop
    Write-Host "✅ Success: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 11: Test Access Control (Mobile client trying to access research endpoint)
Write-Host ""
Write-Host "11. Testing Access Control (Mobile client accessing research endpoint - should fail)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/research/persons" -Method GET -Headers @{"X-Client-ID"="mobile-app1"} -ErrorAction Stop
    Write-Host "✅ Success: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ Expected Error (403 Forbidden): $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Green
Write-Host "✅ API Testing Complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Expected Results:" -ForegroundColor Cyan
Write-Host "- Health Check: 200 OK" -ForegroundColor White
Write-Host "- Create User: 201 Created" -ForegroundColor White
Write-Host "- BMI Calculation: 200 OK" -ForegroundColor White
Write-Host "- Calories Calculation: 200 OK" -ForegroundColor White
Write-Host "- Get User Record: 200 OK (if user exists) or 404 Not Found" -ForegroundColor White
Write-Host "- Research endpoints with research-tool1: 200 OK" -ForegroundColor White
Write-Host "- Research endpoint with mobile-app1: 403 Forbidden" -ForegroundColor White
