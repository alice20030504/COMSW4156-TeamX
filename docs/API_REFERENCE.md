# API Reference

All endpoints are served over HTTP from `http://localhost:8080` and consume or produce JSON unless noted. Every call (except `/`, `/swagger-ui.html`, and `/actuator`) must include the `X-Client-ID` header. Client IDs follow the pattern `<type>-<identifier>` where the type is either `mobile` or `research`.

## 1. Personal Client Endpoints (`/api/persons`)

### 1.1 Create Person
- **Method and Path:** `POST /api/persons`
- **Headers:** `X-Client-ID: mobile-...`, `Content-Type: application/json`
- **Input Body:**
  ```json
  {
    "name": "string",
    "weight": number,
    "height": number,
    "birthDate": "YYYY-MM-DD"
  }
  ```
- **Output:** `201 Created` with the persisted record including generated `id` and `clientId`.
- **Behaviour:** Stores a new person record bound to the requesting client ID.
- **Typical Call Sequence:** Used after authentication to seed client-specific data before any read operations.

### 1.2 Get Person by Id
- **Method and Path:** `GET /api/persons/{id}`
- **Headers:** `X-Client-ID: mobile-...`
- **Query Parameters:** `birthDate=YYYY-MM-DD` (required for access validation)
- **Output:** `200 OK` with person record if the id belongs to the caller; `404` if the record belongs to another client; `401` if birth date verification fails.
- **Behaviour:** Retrieves a single person scoped to the caller.
- **Typical Call Sequence:** Invoke after creating a record or before updating or deleting it.

### 1.3 Update Person
- **Method and Path:** `PUT /api/persons/{id}`
- **Headers:** `X-Client-ID: mobile-...`, `Content-Type: application/json`
- **Query Parameters:** `birthDate=YYYY-MM-DD`
- **Input Body:** Same shape as create; all fields required.
- **Output:** `200 OK` with the updated record; `404` if record belongs to another client; `401` if birth date check fails.
- **Behaviour:** Updates a person’s name, weight, height, and birth date while preserving client ownership.
- **Typical Call Sequence:** Fetch existing record, then submit updated fields. Always call GET first to confirm ownership.

### 1.4 Delete Person
- **Method and Path:** `DELETE /api/persons/{id}`
- **Headers:** `X-Client-ID: mobile-...`
- **Query Parameters:** `birthDate=YYYY-MM-DD`
- **Output:** `204 No Content` on success; `404` or `401` for same reasons as update.
- **Behaviour:** Removes the person if owned by the requester.
- **Typical Call Sequence:** Use after verifying the record with GET. Follow with a GET to confirm deletion if desired.

### 1.5 List Persons
- **Method and Path:** `GET /api/persons`
- **Headers:** `X-Client-ID: mobile-...`
- **Output:** `200 OK` with an array of the caller’s person records.
- **Behaviour:** Returns all records for the authenticated mobile client.
- **Typical Call Sequence:** Call after create operations or during dashboard initialization.

### 1.6 Calculate BMI
- **Method and Path:** `GET /api/persons/bmi`
- **Headers:** `X-Client-ID: mobile-...`
- **Query Parameters:** `weight` (kg), `height` (cm)
- **Output:** `200 OK` with `weight`, `height`, `bmi`, and `category`; `400` if parameters are missing or invalid.
- **Behaviour:** Computes BMI using the provided values without persisting data.
- **Typical Call Sequence:** May be called independently. Often follows a list or fetch operation that supplies the values.

### 1.7 Calculate Daily Calories
- **Method and Path:** `GET /api/persons/calories`
- **Headers:** `X-Client-ID: mobile-...`
- **Query Parameters:** `weight`, `height`, `age`, `gender` (`male` or `female`), `weeklyTrainingFreq`
- **Output:** `200 OK` with `bmr`, `dailyCalories`, `weeklyTrainingFreq`; `400` for missing or invalid inputs.
- **Behaviour:** Calculates basal metabolic rate and daily calorie needs using the Harris-Benedict formula plus activity multipliers.
- **Typical Call Sequence:** Similar to BMI; call whenever caloric advice is needed.

### 1.8 Health Check
- **Method and Path:** `GET /api/persons/health`
- **Headers:** `X-Client-ID: mobile-...` or `research-...`
- **Output:** `200 OK` with `status`, `service`, `version`; `400` if the client header is missing.
- **Behaviour:** Indicates service availability.
- **Typical Call Sequence:** Recommended first call after deployment or before integration tests.

## 2. Research Client Endpoints (`/api/research`)

All research endpoints require a `research-*` client ID. Mobile clients receive `403 Forbidden`.

### 2.1 Aggregated Persons
- **Method and Path:** `GET /api/research/persons`
- **Headers:** `X-Client-ID: research-...`
- **Output:** `200 OK` with anonymized counts and metadata.
- **Behaviour:** Returns total user counts and privacy indicators.
- **Typical Call Sequence:** Invoked after health check to validate research access before deeper analytics.

### 2.2 Demographic Statistics
- **Method and Path:** `GET /api/research/demographics`
- **Headers:** `X-Client-ID: research-...`
- **Query Parameters (optional):** `ageRange`, `gender`
- **Output:** `200 OK` with cohort details and aggregate metrics; `403` for mobile client.
- **Behaviour:** Provides anonymized demographic breakdowns ensuring minimum cohort sizes.
- **Typical Call Sequence:** Often follows `/research/persons` to drill into specific cohorts.

### 2.3 Workout Patterns
- **Method and Path:** `GET /api/research/workout-patterns`
- **Headers:** `X-Client-ID: research-...`
- **Query Parameters:** `ageRange` (optional)
- **Output:** `200 OK` with workout distribution data.
- **Behaviour:** Returns anonymized workout frequencies, exercise type percentages, and privacy flags.
- **Typical Call Sequence:** Used after confirming cohort eligibility to analyze activity trends.

### 2.4 Nutrition Trends
- **Method and Path:** `GET /api/research/nutrition-trends`
- **Headers:** `X-Client-ID: research-...`
- **Query Parameters:** `objective` (`BULK`, `CUT`, `RECOVER`, or omitted for default)
- **Output:** `200 OK` with macro distribution and calorie guidance; `403` for mobile clients.
- **Behaviour:** Supplies aggregated nutrition plans per fitness objective.
- **Typical Call Sequence:** Combine with demographics to compare nutritional objectives across cohorts.

### 2.5 Population Health
- **Method and Path:** `GET /api/research/population-health`
- **Headers:** `X-Client-ID: research-...`
- **Output:** `200 OK` with population health indicators, goal achievement rates, and BMI distribution; `403` for mobile clients.
- **Behaviour:** Summarizes population-wide health outcomes and goal progress.
- **Typical Call Sequence:** Use after other analytics to provide executive summaries or dashboards.

## 3. Recommended Call Sequences

### 3.1 Mobile Client Onboarding
1. `GET /api/persons/health` (confirm service)
2. `POST /api/persons` (seed record)
3. `GET /api/persons` (list records)
4. `GET /api/persons/{id}` (view detail)
5. `PUT /api/persons/{id}` (update detail)
6. `GET /api/persons/bmi` and `GET /api/persons/calories` (derive metrics)
7. `DELETE /api/persons/{id}` (cleanup when needed)

### 3.2 Research Analyst Workflow
1. `GET /api/persons/health` (shared health check)
2. `GET /api/research/persons` (validate access and totals)
3. `GET /api/research/demographics` (select cohort)
4. `GET /api/research/workout-patterns` (activity insight)
5. `GET /api/research/nutrition-trends` (objective-specific data)
6. `GET /api/research/population-health` (overall summary)

All sequences may be repeated with different client IDs to validate isolation and authorization policies.
