# API Reference

All endpoints are served over HTTP from `http://localhost:8080` and consume or produce JSON unless noted. Every call (except `/`, `/swagger-ui.html`, `/health`, and `/actuator`) must include the `X-Client-ID` header. Client IDs follow the pattern `<type>-<identifier>` where the type is either `mobile` or `research`.

## 1. Personal Client Endpoints (`/api/persons`)

### 1.1 Create Person
- Method and Path: `POST /api/persons`
- Headers: `X-Client-ID: mobile-...`, `Content-Type: application/json`
- Input Body:
  ```json
  {
    "name": "string",
    "weight": number,
    "height": number,
    "birthDate": "YYYY-MM-DD"
  }
  ```
- Output: `201 Created` with the persisted record including generated `id` and `clientId`.
- Behaviour: Stores a new person record bound to the requesting client ID.

### 1.2 Get Current Profile
- Method and Path: `GET /api/persons/me`
- Headers: `X-Client-ID: mobile-...`
- Output: `200 OK` with the persisted profile for the header's client ID; `404` if no profile exists.
- Behaviour: Returns the authenticated mobile client's stored profile without requiring an id parameter.

### 1.3 Update Person
- Method and Path: `PUT /api/persons/me`
- Headers: `X-Client-ID: mobile-...`, `Content-Type: application/json`
- Input Body: Same shape as create; all fields required.
- Output: `200 OK` with the updated record; `404` if no profile exists for the calling client.
- Behaviour: Updates the stored profile for the authenticated mobile client without requiring an ID or birth-date challenge.

### 1.4 Delete Person
- Method and Path: `DELETE /api/persons/me`
- Headers: `X-Client-ID: mobile-...`
- Output: `204 No Content` on success; `404` if no profile exists for the calling client.
- Behaviour: Removes the lone person record associated with the requesting client.

### 1.5 Calculate BMI
- Method and Path: `GET /api/persons/bmi`
- Headers: `X-Client-ID: mobile-...`
- Query Parameters: `weight` (kg), `height` (cm)
- Output: `200 OK` with `weight`, `height`, `bmi`, and `category`; `400` if parameters are missing or invalid.
- Behaviour: Computes BMI using the provided values without persisting data.

### 1.6 Calculate Daily Calories
- Method and Path: `GET /api/persons/calories`
- Headers: `X-Client-ID: mobile-...`
- Query Parameters: `weight`, `height`, `age`, `gender` (`male` or `female`), `weeklyTrainingFreq`
- Output: `200 OK` with `bmr`, `dailyCalories`, `weeklyTrainingFreq`; `400` for missing or invalid inputs.
- Behaviour: Calculates basal metabolic rate and daily calorie needs using the Harris-Benedict formula plus activity multipliers.


## 2. Research Client Endpoints (`/api/research`)

All research endpoints require a `research-*` client ID. Mobile clients receive `403 Forbidden`.

### 2.1 Aggregated Persons
- Method and Path: `GET /api/research/persons`
- Headers: `X-Client-ID: research-...`
- Output: `200 OK` with anonymized counts and metadata.
- Behaviour: Returns total user counts and privacy indicators.

### 2.2 Demographic Statistics
- Method and Path: `GET /api/research/demographics`
- Headers: `X-Client-ID: research-...`
- Query Parameters (optional): `ageRange`, `gender`, `objective`
- Output: `200 OK` with cohort details and aggregate metrics; `403` for mobile client.
- Behaviour: Provides anonymized demographic breakdowns ensuring minimum cohort sizes.

### 2.3 Workout Patterns
- Method and Path: `GET /api/research/workout-patterns`
- Headers: `X-Client-ID: research-...`
- Query Parameters: `ageRange` (optional)
- Output: `200 OK` with workout distribution data.
- Behaviour: Returns anonymized workout frequencies, exercise type percentages, and privacy flags.

### 2.4 Nutrition Trends
- Method and Path: `GET /api/research/nutrition-trends`
- Headers: `X-Client-ID: research-...`
- Query Parameters: `objective` (`BULK`, `CUT`, `RECOVER`, or omitted for default)
- Output: `200 OK` with macro distribution and calorie guidance; `403` for mobile clients.
- Behaviour: Supplies aggregated nutrition plans per fitness objective.

### 2.5 Population Health
- Method and Path: `GET /api/research/population-health`
- Headers: `X-Client-ID: research-...`
- Output: `200 OK` with population health indicators, goal achievement rates, and BMI distribution; `403` for mobile clients.
- Behaviour: Summarizes population-wide health outcomes and goal progress.

## 3. Recommended Call Sequences

### 3.1 Mobile Client Onboarding
1. `GET /health` (confirm service availability)
2. `POST /api/persons` (seed record and receive client ID)
3. `GET /api/persons/me` (verify the stored profile)
4. `PUT /api/persons/me` (update detail)
5. `GET /api/persons/bmi` and `GET /api/persons/calories` (derive metrics)
6. `DELETE /api/persons/me` (cleanup when needed)

### 3.2 Research Analyst Workflow
1. `GET /health` (shared health check)
2. `GET /api/research/persons` (validate access and totals)
3. `GET /api/research/demographics` (select cohort)
4. `GET /api/research/workout-patterns` (activity insight)
5. `GET /api/research/nutrition-trends` (objective-specific data)
6. `GET /api/research/population-health` (overall summary)

All sequences may be repeated with different client IDs to validate isolation and authorization policies.

