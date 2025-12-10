# API Reference

All endpoints are served over HTTP and consume or produce JSON unless noted. 

**Base URLs:**
- **Local Development**: `http://localhost:8080`
- **GCP Deployment**: `http://34.30.81.33:8080`

Every call (except `/`, `/swagger-ui.html`, `/health`, and `/actuator`) must include the `X-Client-ID` header. Client IDs follow the pattern `<type>-<identifier>` where the type is either `mobile` or `research`.

## 1. Personal Client Endpoints (`/api/persons`)

### 1.1 Create Person
- **Method and Path**: `POST /api/persons`
- **Input**: 
  - **Headers**: `Content-Type: application/json` (Note: This is the only personal endpoint that does NOT require `X-Client-ID` header)
  - **Request Body**:
    ```json
    {
      "name": "string",
      "weight": number,
      "height": number,
      "birthDate": "YYYY-MM-DD",
      "gender": "MALE" | "FEMALE",
      "goal": "CUT" | "BULK"
    }
    ```
- **Output**: `201 Created` with persisted record including `clientId`:
  ```json
  {
    "id": number,
    "clientId": "mobile-...",
    "name": "string",
    "weight": number,
    "height": number,
    "birthDate": "YYYY-MM-DD",
    "gender": "MALE" | "FEMALE",
    "goal": "CUT" | "BULK"
  }
  ```
- **Behaviour**: Stores a new person record and generates a unique `clientId`. The client ID must be used in subsequent requests via the `X-Client-ID` header.
- **Equivalence Partitions**:
  - Normal: Valid profile data
  - Boundary: Minimum/maximum weight/height values
  - Invalid: Missing required fields, invalid date format, invalid enum values

### 1.2 Get Current Profile
- **Method and Path**: `GET /api/persons/me`
- **Input**: `X-Client-ID` header
- **Output**: `200 OK` with profile; `404` if not found:
  ```json
  {
    "id": number,
    "clientId": "mobile-...",
    "name": "string",
    "weight": number,
    "height": number,
    "birthDate": "YYYY-MM-DD",
    "gender": "MALE" | "FEMALE",
    "goal": "CUT" | "BULK",
    "targetChangeKg": number,
    "targetDurationWeeks": number,
    "trainingFrequencyPerWeek": number,
    "planStrategy": "DIET_ONLY" | "WORKOUT_ONLY" | "BOTH"
  }
  ```
- **Behaviour**: Returns the authenticated mobile client's stored profile including all attributes (name, weight, height, birthDate, gender, goal, plan details).
- **Equivalence Partitions**:
  - Valid: Existing profile for client ID
  - Invalid: Non-existent client ID, missing header

### 1.3 Configure Goal Plan
- **Method and Path**: `POST /api/persons/plan`
- **Input**: 
  - **Headers**: `X-Client-ID: mobile-...`, `Content-Type: application/json`
  - **Request Body**:
    ```json
    {
      "targetChangeKg": number,
      "durationWeeks": number,
      "trainingFrequencyPerWeek": number (1-14),
      "planStrategy": "DIET_ONLY" | "WORKOUT_ONLY" | "BOTH"
    }
    ```
- **Output**: `200 OK` with updated record; `400` if validation fails; `404` if not found:
  ```json
  {
    "id": number,
    "clientId": "mobile-...",
    "name": "string",
    "weight": number,
    "height": number,
    "birthDate": "YYYY-MM-DD",
    "gender": "MALE" | "FEMALE",
    "goal": "CUT" | "BULK",
    "targetChangeKg": number,
    "targetDurationWeeks": number,
    "trainingFrequencyPerWeek": number,
    "planStrategy": "DIET_ONLY" | "WORKOUT_ONLY" | "BOTH"
  }
  ```
- **Behaviour**: Configures the goal plan for the authenticated client. Requires a goal (CUT/BULK) to be set first via profile creation or update.
- **Equivalence Partitions**:
  - Valid: Valid plan configuration with goal set
  - Invalid: Missing goal, invalid training frequency (outside 1-14), invalid plan strategy, non-existent profile

### 1.4 Update Person
- **Method and Path**: `PUT /api/persons/me`
- **Input**: 
  - **Headers**: `X-Client-ID: mobile-...`, `Content-Type: application/json`
  - **Request Body**: Same as POST (`name`, `weight`, `height`, `birthDate`, `gender`, `goal`); all fields required
    ```json
    {
      "name": "string",
      "weight": number,
      "height": number,
      "birthDate": "YYYY-MM-DD",
      "gender": "MALE" | "FEMALE",
      "goal": "CUT" | "BULK"
    }
    ```
- **Output**: `200 OK` with updated record; `404` if not found:
  ```json
  {
    "id": number,
    "clientId": "mobile-...",
    "name": "string",
    "weight": number,
    "height": number,
    "birthDate": "YYYY-MM-DD",
    "gender": "MALE" | "FEMALE",
    "goal": "CUT" | "BULK"
  }
  ```
- **Behaviour**: Updates the stored profile for the authenticated mobile client. Plan details are preserved unless explicitly changed via `/api/persons/plan`.
- **Equivalence Partitions**:
  - Valid: Update existing profile
  - Invalid: Update non-existent profile, invalid data

### 1.5 Delete Person
- **Method and Path**: `DELETE /api/persons/me`
- **Input**: `X-Client-ID` header
- **Output**: `204 No Content`; `404` if not found
- **Behaviour**: Removes the person record associated with the requesting client.
- **Equivalence Partitions**:
  - Valid: Delete existing profile
  - Invalid: Delete non-existent profile

### 1.6 Calculate BMI
- **Method and Path**: `GET /api/persons/bmi`
- **Input**: Query params `weight` (kg), `height` (cm); `X-Client-ID` header
- **Output**: `200 OK` with BMI and category; `400` for invalid inputs:
  ```json
  {
    "weight": number,
    "height": number,
    "bmi": number,
    "category": "underweight" | "normal" | "overweight" | "obese"
  }
  ```
- **Behaviour**: Computes BMI using the formula `weight(kg) / (height(m))²` with validation. Uses query params if provided, otherwise uses stored profile data. Does not persist data.
- **Equivalence Partitions**:
  - Valid: Typical values, boundary BMI categories
  - Invalid: Missing params, zero/negative values, extreme values

### 1.7 Calculate Daily Calories
- **Method and Path**: `GET /api/persons/calories`
- **Input**: Query params `weight` (kg), `height` (cm), `age`, `gender`, `weeklyTrainingFreq` (optional - uses stored profile if not provided); `X-Client-ID` header
- **Output**: `200 OK` with BMR and daily calories; `400` for invalid inputs:
  ```json
  {
    "bmr": number,
    "maintenanceCalories": number,
    "recommendedDailyCalories": number,
    "calorieAdjustmentPerDay": number,
    "goal": "CUT" | "BULK",
    "planStrategy": "DIET_ONLY" | "WORKOUT_ONLY" | "BOTH",
    "targetChangeKg": number,
    "targetDurationWeeks": number,
    "trainingFrequencyPerWeek": number
  }
  ```
- **Behaviour**: Calculates BMR using Harris-Benedict equation (gender-specific), applies activity multipliers based on training frequency, and adjusts for goal (CUT/BULK) if plan is configured. **Requires plan configuration via `/api/persons/plan` before calling.**
- **Equivalence Partitions**:
  - Valid: All activity levels (0, 1-2, 3-4, 5-6, 7+ training days)
  - Invalid: Missing params, invalid gender, negative training frequency

### 1.8 Get Fitness Recommendation
- **Method and Path**: `GET /api/persons/recommendation`
- **Input**: `X-Client-ID` header
- **Output**: `200 OK` with comprehensive health insights:
  ```json
  {
    "goal": "CUT" | "BULK",
    "message": "string (personalized recommendation)",
    "bmi": number,
    "bmiCategory": "string",
    "healthIndex": number,
    "planAlignmentIndex": number,
    "overallScore": number,
    "percentile": number,
    "planStrategy": "DIET_ONLY" | "WORKOUT_ONLY" | "BOTH",
    "targetChangeKg": number,
    "targetDurationWeeks": number,
    "trainingFrequencyPerWeek": number,
    "dietPlan": "string (if strategy includes DIET)",
    "workoutPlan": "string (if strategy includes WORKOUT)"
  }
  ```
- **Behaviour**: Generates personalized fitness recommendations based on profile, goal plan, and cohort analysis. Uses `HealthInsightService` to compute composite health scores. Enhanced when plan is configured.
- **Equivalence Partitions**:
  - Valid: Existing profile with or without plan configuration
  - Invalid: Non-existent client ID, missing header


## 2. Research Client Endpoints (`/api/research`)

All research endpoints (except registration) require a `research-*` client ID. Mobile clients receive `403 Forbidden`.

### 2.1 Register Researcher
- **Method and Path**: `POST /api/research`
- **Input**: 
  - **Headers**: `Content-Type: application/json` (Note: This is the only research endpoint that does NOT require `X-Client-ID` header)
  - **Request Body**: `name`, `email`
    ```json
    {
      "name": "string",
      "email": "string"
    }
    ```
- **Output**: `201 Created` with `research-*` client ID:
  ```json
  {
    "id": number,
    "clientId": "research-...",
    "name": "string",
    "email": "string"
  }
  ```
- **Behaviour**: Registers a new researcher profile and generates a unique `research-*` client ID. Email must be unique. The client ID must be used in subsequent requests via the `X-Client-ID` header.
- **Equivalence Partitions**:
  - Valid: Valid researcher data
  - Invalid: Missing fields, invalid email format, duplicate email

### 2.2 Demographic Statistics
- **Method and Path**: `GET /api/research/demographics`
- **Input**: `X-Client-ID: research-*` header; optional query params `ageRange`, `gender`, `objective`
- **Output**: `200 OK` with anonymized demographics; `403` for mobile clients:
  ```json
  {
    "cohortSummary": {
      "sampleSize": number,
      "averageAge": number,
      "averageWeight": number,
      "averageHeight": number
    },
    "breakdown": {
      "byGender": {"MALE": count, "FEMALE": count},
      "byGoal": {"CUT": count, "BULK": count}
    }
  }
  ```
- **Status Codes**: `200 OK`, `400 Bad Request` (if sample size < 3), `403 Forbidden` (for mobile clients)
- **Behaviour**: Provides anonymized demographic breakdowns. Requires at least 3 person profiles in the database.
- **Equivalence Partitions**:
  - Valid: Research client with valid filters, sufficient data (≥3 profiles)
  - Invalid: Mobile client (403), invalid filters, insufficient data (<3 profiles)

### 2.3 Population Health
- **Method and Path**: `GET /api/research/population-health`
- **Input**: `X-Client-ID: research-*` header
- **Output**: `200 OK` with aggregate health metrics; `403` for mobile clients:
  ```json
  {
    "totalProfiles": number,
    "goalSegments": {
      "CUT": {
        "count": number,
        "averageWeight": number,
        "averageBMI": number,
        "genderSplit": {"MALE": count, "FEMALE": count}
      },
      "BULK": {
        "count": number,
        "averageWeight": number,
        "averageBMI": number,
        "genderSplit": {"MALE": count, "FEMALE": count}
      }
    }
  }
  ```
- **Status Codes**: `200 OK`, `400 Bad Request` (if insufficient CUT/BULK data), `403 Forbidden` (for mobile clients)
- **Behaviour**: Summarizes population-wide health outcomes grouped by fitness goal (CUT/BULK). Requires at least one profile with CUT goal and one with BULK goal.
- **Equivalence Partitions**:
  - Valid: Research client access with sufficient data (≥1 CUT + ≥1 BULK profile)
  - Invalid: Mobile client (403), insufficient data (missing CUT or BULK profiles)

## 3. System Endpoints

These endpoints are provided by system controllers and do not require client authentication.

### 3.1 Health Check (HealthController)
- **Method and Path**: `GET /health`
- **Controller**: `HealthController`
- **Input**: None required
- **Output**: `200 OK` with JSON response:
  ```json
  {
    "status": "UP",
    "service": "Personal Fitness Management Service",
    "version": "1.0.0"
  }
  ```
- **Behaviour**: Service health check endpoint provided by `HealthController`. Does not require authentication or `X-Client-ID` header. Used to verify service availability.

### 3.2 Swagger UI
- **Method and Path**: `GET /swagger-ui.html`
- **Input**: None required
- **Output**: Interactive API documentation interface
- **Behaviour**: Provides interactive API exploration and testing interface.

### 3.3 OpenAPI Specification
- **Method and Path**: `GET /api-docs`
- **Input**: None required
- **Output**: OpenAPI 3.0 JSON specification
- **Behaviour**: Returns machine-readable API specification.

## 4. Recommended Call Sequences

### 4.1 Mobile Client Onboarding
1. `GET /health` - Verify service availability
2. `POST /api/persons` - Register profile and receive `clientId` (store this for subsequent requests)
3. `GET /api/persons/me` - Verify the stored profile
4. `POST /api/persons/plan` - Configure goal plan (target weight, duration, training frequency, strategy)
5. `GET /api/persons/bmi` - Calculate BMI (optional, can use query params or stored profile)
6. `GET /api/persons/calories` - Get calorie recommendations (uses stored profile and plan)
7. `GET /api/persons/recommendation` - Get personalized fitness recommendations
8. `PUT /api/persons/me` - Update profile (optional)
9. `DELETE /api/persons/me` - Cleanup (optional)

### 4.2 Research Analyst Workflow
1. `GET /health` - Verify service availability
2. `POST /api/research` - Register researcher and receive `research-*` clientId (store this for subsequent requests)
3. `GET /api/research/demographics` - Get demographic breakdowns (requires at least 3 person profiles)
4. `GET /api/research/population-health` - Get population health metrics (requires both CUT and BULK profiles)

**Note**: Research endpoints require mobile users to exist first. Mobile clients must create profiles before researchers can analyze the data.

All sequences may be repeated with different client IDs to validate isolation and authorization policies.

### 4.3 Dependency Graph

**Mobile Client Endpoints:**
```
GET /health (independent)
    ↓
POST /api/persons (independent - generates clientId)
    ↓
    ├─→ GET /api/persons/me (requires: POST /api/persons)
    ├─→ PUT /api/persons/me (requires: POST /api/persons)
    ├─→ DELETE /api/persons/me (requires: POST /api/persons)
    ├─→ POST /api/persons/plan (requires: POST /api/persons)
    │       ↓
    │       └─→ GET /api/persons/calories (requires: POST /api/persons + POST /api/persons/plan)
    │
    ├─→ GET /api/persons/bmi (optional: can use query params OR stored profile)
    └─→ GET /api/persons/recommendation (requires: POST /api/persons, enhanced with plan)
```

**Research Client Endpoints:**
```
GET /health (independent)
    ↓
POST /api/research (independent - generates research clientId)
    ↓
    ├─→ GET /api/research/demographics (requires: POST /api/research + ≥3 mobile profiles)
    └─→ GET /api/research/population-health (requires: POST /api/research + ≥1 CUT + ≥1 BULK profile)
```

## 5. Valid & Invalid Test Cases

**Valid Cases:**
- Normal user workflows (register → configure plan → get metrics)
- Boundary values (minimum/maximum weight, height, age)
- All activity levels (0-14 training days/week)
- All fitness goals (CUT, BULK)
- All genders (MALE, FEMALE)
- Multiple simultaneous clients

**Invalid Cases:**
- Missing `X-Client-ID` header (400)
- Invalid client ID format (400)
- Missing required fields (400)
- Invalid date format (400)
- Negative or zero weight/height (400)
- Extreme values exceeding limits (400)
- Mobile client accessing research endpoints (403)
- Accessing non-existent resources (404)

## 6. API Tests Coverage

**Persistent Data:**
- Tests verify data is persisted to PostgreSQL
- Tests verify data isolation between clients
- Tests verify data retrieval after service restart

**Logging:**
- Tests verify API calls are logged to [`logs/fitness-app.log`](logs/fitness-app.log)
- Tests verify log entries include clientId, method, path, status, duration

**Multiple Clients:**
- Tests create multiple client IDs and verify isolation
- Tests verify concurrent requests from different clients
- Tests verify mobile and research clients can operate simultaneously

**Postman Collection:**
- 36 requests covering all endpoints
- 79 assertions validating responses
- Normal, boundary, and invalid scenarios for each endpoint
- Collection: [`postman/fitness-api-tests.postman_collection.json`](postman/fitness-api-tests.postman_collection.json)
