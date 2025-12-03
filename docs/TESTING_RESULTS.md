# Testing Results

## Summary
- Test run date: 2025-11-30 (Iteration 2)
- Frameworks: JUnit 5, Mockito, Spring Test, Postman/Newman
- Coverage: ≥80% branches and lines (JaCoCo) - Iteration 2 target achieved


## Unit Testing

### How to Run Unit Tests

```bash
mvn test
# Or via Docker:
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests
```

Unit tests are automatically executed during Maven `verify` phase and in CI pipelines.

### Equivalence Partitions and Boundary Testing

**Service Layer (`PersonServiceTest`):**

**BMI Calculation:**
- **Inputs Tested**: `weight` (kg), `height` (cm)
- **Equivalence Classes**:
  - Valid: Typical adult metrics (70kg, 175cm)
  - Boundary: Underweight threshold (BMI < 18.5)
  - Boundary: Normal weight threshold (BMI = 18.5-25.0)
  - Boundary: Overweight threshold (BMI = 25.0-30.0)
  - Boundary: Obese threshold (BMI ≥ 30.0)
  - Invalid: Null inputs
  - Invalid: Non-positive values (≤ 0)
  - Invalid: Unreasonably large values (> 635kg weight, > 272cm height)
- **Boundaries Tested**:
  - BMI = 18.5 (underweight/normal boundary)
  - BMI = 25.0 (normal/overweight boundary)
  - BMI = 30.0 (overweight/obese boundary)
  - Weight = 0, Height = 0 (zero boundary)
  - Weight = MAX_PLAUSIBLE_WEIGHT_KG, Height = MAX_PLAUSIBLE_HEIGHT_CM (upper bounds)

**BMR Calculation:**
- **Inputs Tested**: `weight`, `height`, `age`, `isMale` (boolean)
- **Equivalence Classes**:
  - Valid: Male with typical metrics
  - Valid: Female with typical metrics
  - Invalid: Null weight/height/age
- **Boundaries Tested**:
  - Age = 0 (newborn)
  - Age = 100+ (elderly)

**Daily Calorie Needs:**
- **Inputs Tested**: `bmr`, `weeklyTrainingFreq`
- **Equivalence Classes**:
  - Sedentary: 0 training days (factor 1.2)
  - Light activity: 1-2 training days (factor 1.375)
  - Moderate activity: 3-4 training days (factor 1.55)
  - Very active: 5-6 training days (factor 1.725)
  - Extra active: 7+ training days (factor 1.9)
- **Boundaries Tested**:
  - Training frequency = 0, 1, 2, 3, 4, 5, 6, 7 (activity level boundaries)

**Security Layer (`ClientIdInterceptorTest`):**
- **Inputs Tested**: `X-Client-ID` header values
- **Equivalence Classes**:
  - Valid: `mobile-abc123`, `research-xyz789`
  - Invalid: Missing header
  - Invalid: Empty header
  - Invalid: Wrong format (e.g., `invalid-123`)
- **Boundaries Tested**:
  - Header present vs. absent
  - Valid format vs. invalid format

### Test Grouping

**Setup/Teardown:**
- `@BeforeEach`: Initializes service instances and test data
- `@AfterEach`: Cleans up thread-local `ClientContext` to prevent test interference

**Mocks:**
- `@Mock`: Repository interfaces (`PersonRepository`, `ResearcherRepository`)
- `@InjectMocks`: Controllers and services under test
- `@ExtendWith(MockitoExtension.class)`: Enables Mockito annotations

**Test Doubles:**
- Mock repositories return controlled test data
- Thread-local `ClientContext` is manually set/cleared for isolation testing

**Test Organization:**
- Unit tests: `src/test/java/com/teamx/fitness/service/`, `src/test/java/com/teamx/fitness/security/`
- Controller tests: `src/test/java/com/teamx/fitness/controller/`
- Integration tests: `src/test/java/com/teamx/fitness/integration/`

### CI Execution

Unit tests are automatically executed in CI pipelines on every push and pull request. Test results are reported in CI logs and must pass for builds to succeed.

---

## API Testing

### How to Run API Tests

**Using Postman/Newman (Recommended):**
```bash
# Via Docker:
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm newman

# Or locally (requires Newman CLI):
newman run postman/fitness-api-tests.postman_collection.json \
  -e postman/fitness-api-tests.postman_environment.json \
  --reporters html,json \
  --reporter-html-export testresult/api/postman-report.html \
  --reporter-json-export testresult/api/postman-summary.json
```

**Using Maven (REST Assured):**
```bash
mvn verify
```

### API Endpoints and Test Coverage

**Personal Endpoints (`/api/persons`):**

1. **POST /api/persons** - Create Profile
   - **Input**: `name`, `weight`, `height`, `birthDate`, `gender`, `goal`
   - **Output**: `201 Created` with persisted record including `clientId`
   - **Equivalence Partitions**:
     - Normal: Valid profile data
     - Boundary: Minimum/maximum weight/height values
     - Invalid: Missing required fields, invalid date format, invalid enum values

2. **GET /api/persons/me** - Get Current Profile
   - **Input**: `X-Client-ID` header
   - **Output**: `200 OK` with profile; `404` if not found
   - **Equivalence Partitions**:
     - Valid: Existing profile for client ID
     - Invalid: Non-existent client ID, missing header

3. **PUT /api/persons/me** - Update Profile
   - **Input**: Same as POST, `X-Client-ID` header
   - **Output**: `200 OK` with updated record; `404` if not found
   - **Equivalence Partitions**:
     - Valid: Update existing profile
     - Invalid: Update non-existent profile, invalid data

4. **DELETE /api/persons/me** - Delete Profile
   - **Input**: `X-Client-ID` header
   - **Output**: `204 No Content`; `404` if not found
   - **Equivalence Partitions**:
     - Valid: Delete existing profile
     - Invalid: Delete non-existent profile

5. **GET /api/persons/bmi** - Calculate BMI
   - **Input**: Query params `weight`, `height`; `X-Client-ID` header
   - **Output**: `200 OK` with BMI and category; `400` for invalid inputs
   - **Equivalence Partitions**:
     - Valid: Typical values, boundary BMI categories
     - Invalid: Missing params, zero/negative values, extreme values

6. **GET /api/persons/calories** - Calculate Daily Calories
   - **Input**: Query params `weight`, `height`, `age`, `gender`, `weeklyTrainingFreq`; `X-Client-ID` header
   - **Output**: `200 OK` with BMR and daily calories; `400` for invalid inputs
   - **Equivalence Partitions**:
     - Valid: All activity levels (0, 1-2, 3-4, 5-6, 7+ training days)
     - Invalid: Missing params, invalid gender, negative training frequency

**Research Endpoints (`/api/research`):**

1. **POST /api/research/register** - Register Researcher
   - **Input**: `name`, `email`
   - **Output**: `201 Created` with `research-*` client ID
   - **Equivalence Partitions**:
     - Valid: Valid researcher data
     - Invalid: Missing fields, invalid email format

2. **GET /api/research/demographics** - Demographics Analytics
   - **Input**: `X-Client-ID: research-*` header; optional query params `ageRange`, `gender`, `objective`
   - **Output**: `200 OK` with anonymized demographics; `403` for mobile clients
   - **Equivalence Partitions**:
     - Valid: Research client with valid filters
     - Invalid: Mobile client (403), invalid filters

3. **GET /api/research/population-health** - Population Health
   - **Input**: `X-Client-ID: research-*` header
   - **Output**: `200 OK` with aggregate health metrics; `403` for mobile clients
   - **Equivalence Partitions**:
     - Valid: Research client access
     - Invalid: Mobile client (403)

### Valid & Invalid Test Cases

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

### API Tests Coverage

**Persistent Data:**
- Tests verify data is persisted to PostgreSQL
- Tests verify data isolation between clients
- Tests verify data retrieval after service restart

**Logging:**
- Confirmed end-to-end logging by invoking representative endpoints (mobile profile CRUD and research analytics) while tailing [`logs/fitness-app.log`](logs/fitness-app.log). Each request produced a structured JSON entry containing `clientId`, `method`, `path`, `status`, and latency.
- Newman suites ran against the dockerized stack; after the run we spot-checked the log file and verified that the `ClientIdInterceptor` correctly clears `ClientContext` between requests (no cross-talk between `mobile-*` and `research-*` entries).


## Integration Testing

### Definition of Integration

For this project, integration testing verifies:
- **Service + Repository Layer Interactions**: Controllers calling services, services calling repositories
- **Database Integration**: Real database operations (create, read, update, delete) with data persistence
- **Client Isolation**: Multi-client scenarios where different client IDs access the same database
- **Security Integration**: `ClientIdInterceptor` working with controllers and repositories
- **Cross-Layer Data Flow**: End-to-end request processing from HTTP request to database and back

### Integration Tests

**1. Client Isolation Integration (`ClientIsolationIntegrationTest`):**
- Tests that `PersonController` enforces client isolation using mocked repositories
- Verifies that repository queries filter by `clientId` from `ClientContext`
- Tests CRUD operations with different client IDs to ensure data isolation
- Uses `@ExtendWith(MockitoExtension.class)` with mocked repositories

**2. Research Controller Integration (`ResearchControllerTest`):**
- Tests `ResearchController` with mocked `PersonRepository` and `ResearcherRepository`
- Verifies research endpoints return aggregate data from multiple mobile clients
- Tests access control (mobile clients receive 403, research clients receive 200)
- Verifies anonymization of personal data in research responses

**3. Service + Repository Integration:**
- Tests `PersonService` calculations with real data flow
- Tests `HealthInsightService` building insights from repository data
- Verifies business logic correctly processes persisted data

**4. Database Integration:**
- Tests run against PostgreSQL database (via Docker Compose)
- Verifies schema migrations and data persistence
- Tests transaction boundaries and data consistency

### How to Run Integration Tests

```bash
# Ensure database is running:
docker compose up -d postgres

# Run integration tests:
mvn test -Dtest=*IntegrationTest

# Or via Docker:
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests
```

Integration tests are included in the standard `mvn test` execution and run automatically in CI.

### CI Execution

Integration tests are executed in CI pipelines alongside unit tests. The CI environment includes:
- PostgreSQL database container
- Application container with test execution
- Test results reported in CI logs

---
## Notable Findings
- Unauthorized access attempts now return structured JSON with status codes validated in both unit and system suites.
- Repository mocks confirmed that client IDs are written on create/update and enforced on retrieval.
- Logging configuration captured request traces during Newman runs; spot checks in `logs/fitness-app.log` verified context clearing.

## Manual Verification
- Swagger UI (`/swagger-ui.html`) spot-tested for BMI and health-check endpoints.
- PostgreSQL `fitnessdb` instance inspected via `psql` to confirm test-created records and automatic cleanup after deletions.

## Reports
- JaCoCo coverage report: [`testresult/unit-coverage/jacoco/index.html`](../testresult/unit-coverage/jacoco/index.html)
- Postman/Newman execution log: [`testresult/api/postman-summary.json`](../testresult/api/postman-summary.json)
- Postman/Newman HTML report: [`testresult/api/postman-report.html`](../testresult/api/postman-report.html)

## Iteration 2 Achievements
- Coverage improved to ≥80% (exceeding Iteration 1's 69% branches, 68% lines)
- PMD static analysis integrated and running in CI pipeline
- Enhanced integration tests for service-repository interactions
- Comprehensive end-to-end testing documentation completed
- Cloud deployment on GCP with frontend and backend accessible


