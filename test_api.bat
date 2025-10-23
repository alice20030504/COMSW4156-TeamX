@echo off
REM Fitness Management Service API Test Script (Windows Batch)
REM Base URL
set BASE_URL=http://localhost:8080

echo 🏃‍♂️ Testing Fitness Management Service API
echo ==============================================

REM Test 1: Health Check
echo.
echo 1. Testing Health Check...
curl -X GET -H "X-Client-ID: mobile-app1" "%BASE_URL%/api/persons/health" -w "\nHTTP Status: %%{http_code}\n" -s

REM Test 2: Calculate BMI
echo.
echo 2. Testing BMI Calculation...
curl -X GET -H "X-Client-ID: mobile-app1" "%BASE_URL%/api/persons/bmi?weight=75.5&height=180.0" -w "\nHTTP Status: %%{http_code}\n" -s

REM Test 3: Calculate Daily Calories
echo.
echo 3. Testing Daily Calories Calculation...
curl -X GET -H "X-Client-ID: mobile-app1" "%BASE_URL%/api/persons/calories?weight=75.5&height=180.0&age=34&gender=male&weeklyTrainingFreq=4" -w "\nHTTP Status: %%{http_code}\n" -s

REM Test 4: Research - Get All Users Anonymized
echo.
echo 4. Testing Research - Get All Users Anonymized...
curl -X GET -H "X-Client-ID: research-tool1" "%BASE_URL%/api/research/persons" -w "\nHTTP Status: %%{http_code}\n" -s

REM Test 5: Research - Get Demographic Statistics
echo.
echo 5. Testing Research - Get Demographic Statistics...
curl -X GET -H "X-Client-ID: research-tool1" "%BASE_URL%/api/research/demographics?ageRange=25-34&gender=male&objective=BULK" -w "\nHTTP Status: %%{http_code}\n" -s

REM Test 6: Test Access Control (should fail)
echo.
echo 6. Testing Access Control (Mobile client accessing research endpoint - should fail)...
curl -X GET -H "X-Client-ID: mobile-app1" "%BASE_URL%/api/research/persons" -w "\nHTTP Status: %%{http_code}\n" -s

echo.
echo ==============================================
echo ✅ API Testing Complete!
echo.
echo Expected Results:
echo - Health Check: 200 OK
echo - BMI Calculation: 200 OK  
echo - Calories Calculation: 200 OK
echo - Research endpoints with research-tool1: 200 OK
echo - Research endpoint with mobile-app1: 403 Forbidden
pause
