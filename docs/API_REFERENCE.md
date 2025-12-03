# API Reference

All endpoints are served over HTTP and consume or produce JSON unless noted. 

**Base URLs:**
- **Local Development**: `http://localhost:8080`
- **GCP Deployment**: `http://34.30.81.33:8080`

Every call (except `/`, `/swagger-ui.html`, `/health`, and `/actuator`) must include the `X-Client-ID` header. Client IDs follow the pattern `<type>-<identifier>` where the type is either `mobile` or `research`.

## 1. Personal Client Endpoints (`/api/persons`)

### 1.1 Create Person
- Method and Path: `POST /api/persons`
- Headers: `Content-Type: application/json` (Note: This is the only personal endpoint that does NOT require `X-Client-ID` header)
- Input Body:
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
- Output: `201 Created` with `{"clientId": "mobile-..."}` containing the generated client identifier.
- Behaviour: Stores a new person record and generates a unique `clientId`. The client ID must be used in subsequent requests via the `X-Client-ID` header.

### 1.2 Get Current Profile
- Method and Path: `GET /api/persons/me`
- Headers: `X-Client-ID: mobile-...` (required)
- Output: `200 OK` with the persisted profile for the header's client ID; `404` if no profile exists.
- Behaviour: Returns the authenticated mobile client's stored profile including all attributes (name, weight, height, birthDate, gender, goal, plan details).

### 1.3 Configure Goal Plan
- Method and Path: `POST /api/persons/plan`
- Headers: `X-Client-ID: mobile-...`, `Content-Type: application/json`
- Input Body:
  ```json
  {
    "targetChangeKg": number,
    "durationWeeks": number,
    "trainingFrequencyPerWeek": number (1-14),
    "planStrategy": "DIET_ONLY" | "WORKOUT_ONLY" | "BOTH"
  }
  ```
- Output: `200 OK` with the updated profile including plan details; `400` if plan validation fails; `404` if no profile exists.
- Behaviour: Configures the goal plan for the authenticated client. Requires a goal (CUT/BULK) to be set first via profile creation or update.

### 1.4 Update Person
- Method and Path: `PUT /api/persons/me`
- Headers: `X-Client-ID: mobile-...`, `Content-Type: application/json`
- Input Body: Same shape as create (name, weight, height, birthDate, gender, goal); all fields required.
- Output: `200 OK` with the updated record; `404` if no profile exists for the calling client.
- Behaviour: Updates the stored profile for the authenticated mobile client. Plan details are preserved unless explicitly changed via `/api/persons/plan`.

### 1.5 Delete Person
- Method and Path: `DELETE /api/persons/me`
- Headers: `X-Client-ID: mobile-...`
- Output: `204 No Content` on success; `404` if no profile exists for the calling client.
- Behaviour: Removes the person record associated with the requesting client.

### 1.6 Calculate BMI
- Method and Path: `GET /api/persons/bmi`
- Headers: `X-Client-ID: mobile-...`
- Query Parameters: `weight` (kg), `height` (cm)
- Output: `200 OK` with `weight`, `height`, `bmi`, and `category` (underweight/normal/overweight/obese); `400` if parameters are missing or invalid.
- Behaviour: Computes BMI using the formula `weight(kg) / (height(m))²` with validation. Does not persist data.

### 1.7 Calculate Daily Calories
- Method and Path: `GET /api/persons/calories`
- Headers: `X-Client-ID: mobile-...`
- Query Parameters: Optional `weight`, `height`, `age`, `gender`, `weeklyTrainingFreq`. If not provided, uses stored profile data.
- Output: `200 OK` with `bmr`, `dailyCalories`, `targetCalories` (adjusted for goal), `maintenanceCalories`, `weeklyTrainingFreq`; `400` for missing or invalid inputs.
- Behaviour: Calculates BMR using Harris-Benedict equation (gender-specific), applies activity multipliers based on training frequency, and adjusts for goal (CUT/BULK) if plan is configured. Requires profile to exist or all query parameters provided.

### 1.8 Get Fitness Recommendation
- Method and Path: `GET /api/persons/recommendation`
- Headers: `X-Client-ID: mobile-...`
- Output: `200 OK` with comprehensive health insights including:
  - `goal`, `message` (personalized recommendation)
  - `bmi`, `bmiCategory`
  - `healthIndex`, `planAlignmentIndex`, `overallScore`
  - `percentile` (cohort comparison)
  - `planStrategy`, `targetChangeKg`, `targetDurationWeeks`, `trainingFrequencyPerWeek`
  - `dietPlan`, `workoutPlan` (if strategy includes them)
- Behaviour: Generates personalized fitness recommendations based on profile, goal plan, and cohort analysis. Uses `HealthInsightService` to compute composite health scores.


## 2. Research Client Endpoints (`/api/research`)

All research endpoints (except registration) require a `research-*` client ID. Mobile clients receive `403 Forbidden`.

### 2.1 Register Researcher
- Method and Path: `POST /api/research`
- Headers: `Content-Type: application/json` (Note: This is the only research endpoint that does NOT require `X-Client-ID` header)
- Input Body:
  ```json
  {
    "name": "string",
    "email": "string"
  }
  ```
- Output: `201 Created` with `{"clientId": "research-..."}` containing the generated client identifier.
- Behaviour: Registers a new researcher profile and generates a unique `research-*` client ID. Email must be unique. The client ID must be used in subsequent requests via the `X-Client-ID` header.

### 2.2 Demographic Statistics
- Method and Path: `GET /api/research/demographics`
- Headers: `X-Client-ID: research-...` (required)
- Query Parameters: None (all person data is aggregated)
- Output: `200 OK` with:
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
- Status Codes: `200 OK`, `400 Bad Request` (if sample size < 3), `403 Forbidden` (for mobile clients)
- Behaviour: Provides anonymized demographic breakdowns. Requires at least 3 person profiles in the database.

### 2.3 Population Health
- Method and Path: `GET /api/research/population-health`
- Headers: `X-Client-ID: research-...` (required)
- Query Parameters: None
- Output: `200 OK` with:
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
- Status Codes: `200 OK`, `400 Bad Request` (if insufficient CUT/BULK data), `403 Forbidden` (for mobile clients)
- Behaviour: Summarizes population-wide health outcomes grouped by fitness goal (CUT/BULK). Requires at least one profile with CUT goal and one with BULK goal.

## 3. System Endpoints

These endpoints are provided by system controllers and do not require client authentication.

### 3.1 Health Check (HealthController)
- Method and Path: `GET /health`
- Controller: `HealthController`
- Headers: None required
- Output: `200 OK` with JSON response:
  ```json
  {
    "status": "UP",
    "service": "Personal Fitness Management Service",
    "version": "1.0.0"
  }
  ```
- Behaviour: Service health check endpoint provided by `HealthController`. Does not require authentication or `X-Client-ID` header. Used to verify service availability.

### 3.2 Swagger UI
- Method and Path: `GET /swagger-ui.html`
- Headers: None required
- Output: Interactive API documentation interface
- Behaviour: Provides interactive API exploration and testing interface.

### 3.3 OpenAPI Specification
- Method and Path: `GET /api-docs`
- Headers: None required
- Output: OpenAPI 3.0 JSON specification
- Behaviour: Returns machine-readable API specification.

## 4. Recommended Call Sequences

### 4.1 Mobile Client Onboarding
1. `GET /health` - Verify service availability
2. `POST /api/persons` - Register profile and receive `clientId` (store this for subsequent requests)
3. `GET /api/persons/me` - Verify the stored profile
4. `POST /api/persons/plan` - Configure goal plan (target change, duration, training frequency, strategy)
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

