# Personal Fitness Management Service

A comprehensive Spring Boot-based fitness tracking and management service that provides personalized health calculations, goal planning, and population analytics for fitness researchers.

---

## 1. Service Overview

### What This Service Does

The Personal Fitness Management Service is a RESTful API that performs sophisticated fitness-related computations beyond simple CRUD operations:

**Useful Computations:**
- **BMI Calculation**: Computes Body Mass Index using the formula `weight(kg) / (height(m))²` with validation and categorization (underweight, normal, overweight, obese)
- **BMR Calculation**: Calculates Basal Metabolic Rate using gender-specific Harris-Benedict equations:
  - Men: `BMR = 88.362 + (13.397 × weight) + (4.799 × height) - (5.677 × age)`
  - Women: `BMR = 447.593 + (9.247 × weight) + (3.098 × height) - (4.330 × age)`
- **Daily Calorie Needs**: Computes daily calorie requirements by applying activity multipliers (sedentary: 1.2x, light: 1.375x, moderate: 1.55x, very active: 1.725x, extra active: 1.9x) to BMR
- **Health Insights**: Generates composite health scores combining BMI-based health indices, plan alignment metrics, and cohort percentile rankings
- **Population Analytics**: Aggregates anonymized demographic statistics, workout patterns, nutrition trends, and population health metrics for research clients

**Multiple Client Support:**
- Supports simultaneous mobile clients (personal fitness tracking) and research clients (population analytics)
- Each client is identified by a unique `X-Client-ID` header (format: `mobile-<id>` or `research-<id>`)
- Client isolation is enforced at the repository layer, ensuring data privacy and security
- Multiple browser tabs, devices, or applications can connect concurrently without data interference

**Data Persistence:**
- All user profiles, goal plans, and researcher registrations are persisted in PostgreSQL
- Database schema includes `person_simple` and `researcher` tables with proper relationships
- Data survives service restarts and is accessible across client sessions

**API Call Logging:**
- Every API request is logged to `logs/fitness-app.log` with structured JSON format
- Log entries include: clientId, HTTP method, path, status code, duration (ms), IP address, User-Agent, and error messages (if any)
- Logging is implemented via `ApiLoggingInterceptor` which captures request lifecycle events

### Cloud Deployment

**GCP Deployment URL:** `http://35.188.26.134:8080`

The service is deployed on Google Cloud Platform and accessible at the above URL. Health check endpoint: `http://35.188.26.134:8080/health`

### Iteration 2 Tagged Version

The tagged Iteration 2 version is located at: **`Iteration_2`** (to be updated with actual git tag)

---

## 2. Client Documentation

### A. Location of Client Code

The client code is integrated in the same repository at: **`/frontend`**

The frontend consists of:
- `index.html` - Landing page
- `mobile.html` / `mobile.js` - Mobile user client
- `research.html` / `research.js` - Research analyst client
- `app.js` - Shared application logic
- `styles.css` - Styling

### B. What the Client Does

**Mobile Client (`mobile.html`):**
- **User Registration**: Register new fitness profiles with name, weight, height, birth date, gender, and fitness goal (CUT/BULK)
- **Profile Management**: View, update, and delete personal fitness profiles
- **Goal Plan Configuration**: Set up personalized fitness plans with target weight change, duration (weeks), training frequency (1-14 days/week), and plan strategy (DIET_ONLY, WORKOUT_ONLY, BOTH)
- **BMI Calculation**: Calculate and display Body Mass Index with category classification
- **Calorie Recommendations**: Get daily calorie targets based on BMR, activity level, and fitness goals
- **Fitness Recommendations**: Receive personalized motivational recommendations based on user profile and goal plan
- **API Calls**: Makes requests to `/api/persons` endpoints (POST, GET, PUT, DELETE) with `X-Client-ID` header

**Research Client (`research.html`):**
- **Researcher Registration**: Register research analysts with name and email
- **Demographics Analytics**: View anonymized population breakdowns by gender and fitness goal with bar chart visualizations
- **Population Health Metrics**: Access aggregate health statistics including average BMI, goal achievement rates, and cohort summaries
- **API Calls**: Makes requests to `/api/research` endpoints (GET) with `research-*` client ID

### C. How to Build & Run the Client

**Prerequisites:**
- Modern web browser (Chrome, Firefox, Safari, Edge)
- Backend service running (local or GCP)

**Option 1: Direct File Access**
1. Ensure backend is running at `http://localhost:8080` (or configure for GCP)
2. Open `frontend/index.html` in your web browser
3. No build step required (pure HTML/CSS/JavaScript)

**Option 2: Docker Compose (Recommended)**
```bash
docker compose up -d --build
# Frontend accessible at: http://localhost:3000
# Backend accessible at: http://localhost:8080
```

**Option 3: Local Web Server**
```bash
cd frontend
python -m http.server 3000
# Open: http://localhost:3000
```

**Environment Variables / Configuration:**
- API Base URL can be configured via:
  1. URL parameter: `?apiBaseUrl=http://35.188.26.134:8080`
  2. localStorage (saved from API Configuration panel)
  3. HTML meta tag: `<meta name="fitness-api-base-url" content="...">`
  4. JavaScript variable: `window.__FITNESS_API_BASE_URL__ = '...'`
  5. Auto-detection: Pings `/health` on current origin or port 8080
  6. Fallback: `http://localhost:8080`

**Connection Settings:**
- All API requests include `X-Client-ID` header automatically
- Client ID is stored in browser `sessionStorage` (tab-specific)
- CORS is enabled on backend (`@CrossOrigin(origins = "*")`)

### D. How Multiple Client Instances Connect

**Client Identification Mechanism:**
- Each client instance receives a unique `clientId` upon registration (e.g., `mobile-abc123`, `research-xyz789`)
- Client IDs are stored in browser `sessionStorage` (not `localStorage`), ensuring tab isolation
- All authenticated API requests include the `X-Client-ID` header

**Backend Distinction:**
- `ClientIdInterceptor` intercepts every request and validates the `X-Client-ID` header
- Valid client IDs must match pattern: `mobile-.*` or `research-.*`
- Client ID is stored in thread-local `ClientContext` for the request lifecycle
- Repository queries are automatically filtered by `clientId` to enforce data isolation
- Mobile clients receive `403 Forbidden` when accessing research endpoints

**Multiple Instance Support:**
- **Different Browser Tabs**: Each tab maintains independent `sessionStorage`, allowing different users per tab
- **Different Browsers**: Each browser has separate storage, enabling multiple users on same machine
- **Different Devices**: Each device/browser combination can maintain separate client sessions
- **Simultaneous Requests**: Backend handles concurrent requests from multiple clients without interference

### E. End-to-End Testing Checklist

See **[`docs/E2E_TESTING.md`](docs/E2E_TESTING.md)** for comprehensive end-to-end testing procedures.

**Quick Checklist:**

**Mobile Client Tests:**
- [ ] User Registration: Register with valid profile data, verify client ID generation
- [ ] Goal Plan Configuration: Set target change, duration, training frequency, strategy
- [ ] BMI Calculation: Verify BMI computed correctly with max 2 decimal places
- [ ] Calorie Recommendations: Verify calorie calculation based on BMR and activity
- [ ] Fitness Recommendations: Verify goal-specific recommendations displayed
- [ ] Multiple Client Instances: Register two users in different tabs, verify data isolation

**Research Client Tests:**
- [ ] Researcher Registration: Register researcher, verify `research-*` client ID
- [ ] Demographics Analytics: Verify bar chart displays gender/goal breakdowns
- [ ] Population Health: Verify aggregate health metrics displayed correctly
- [ ] Multiple Researcher Instances: Register two researchers, verify both can access analytics

**Cross-Client Tests:**
- [ ] Mobile User + Researcher Data Consistency: Register mobile user, verify researcher analytics include the user

**API Error Handling:**
- [ ] Request without `X-Client-ID`: Verify 400 response with error message
- [ ] Request with invalid client ID: Verify 400 response with validation error
- [ ] Mobile client accessing research endpoint: Verify 403 Forbidden

### F. Instructions for Third-Party Developers

**Authentication:**
- All API requests (except `/health`, `/swagger-ui.html`, `/api-docs`) require `X-Client-ID` header
- Client ID format: `mobile-<identifier>` or `research-<identifier>`
- Obtain client ID by registering via:
  - Mobile: `POST /api/persons` (returns `clientId` in response)
  - Research: `POST /api/research/register` (returns `clientId` in response)

**Required Headers:**
```
X-Client-ID: mobile-abc123
Content-Type: application/json
```

**Endpoints:**
See **[`docs/API_REFERENCE.md`](docs/API_REFERENCE.md)** for complete API documentation.

**Request/Response Formats:**
- All requests and responses use JSON
- Dates: `YYYY-MM-DD` format (e.g., `"1990-04-15"`)
- Numbers: Standard JSON numbers (no quotes)
- Enums: String values (e.g., `"MALE"`, `"FEMALE"`, `"CUT"`, `"BULK"`)

**Example Mobile Client Registration:**
```bash
POST http://35.188.26.134:8080/api/persons
Headers:
  Content-Type: application/json
Body:
{
  "name": "John Doe",
  "weight": 75.5,
  "height": 180.0,
  "birthDate": "1990-01-15",
  "gender": "MALE",
  "goal": "CUT"
}

Response: 201 Created
{
  "id": 1,
  "clientId": "mobile-abc123",
  "name": "John Doe",
  ...
}
```

**Example Research Client Registration:**
```bash
POST http://35.188.26.134:8080/api/research/register
Headers:
  Content-Type: application/json
Body:
{
  "name": "Dr. Smith",
  "email": "smith@research.edu"
}

Response: 201 Created
{
  "id": 1,
  "clientId": "research-xyz789",
  ...
}
```

**Error Responses:**
- `400 Bad Request`: Missing/invalid `X-Client-ID`, invalid request body, validation errors
- `403 Forbidden`: Mobile client accessing research endpoint
- `404 Not Found`: Resource not found for the client ID
- `500 Internal Server Error`: Server-side errors

---

## 3. Static Analysis

### Tools Used

- **Checkstyle 10.12.5**: Enforces Google Java Style Guide conventions
- **PMD 6.55.0**: Detects code quality issues, unused code, and potential bugs

### How to Run Static Analysis Locally

**Checkstyle:**
```bash
mvn checkstyle:check
# Or via Docker:
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm checkstyle
```

**PMD:**
```bash
mvn pmd:check
# Or via Docker:
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm pmd
```

**Both (during Maven verify):**
```bash
mvn clean verify
```

### Report Locations

- **Checkstyle**: `testresult/checkstyle/checkstyle-result.xml`
- **PMD**: `testresult/pmd/pmd.html`
- Reports are also generated in `target/` directory during Maven builds

### Style Checking

- Style checking is enforced via Checkstyle and integrated into CI pipeline
- Checkstyle configuration: `checkstyle.xml` (based on Google Java Style Guide)
- PMD ruleset: `pmd-ruleset.xml`
- Zero violations are required for code commits

### Bugs Fixed

Static analysis tools identified and fixed the following issues:
- **Unused imports**: Removed unused import statements
- **Code complexity**: Refactored methods exceeding complexity thresholds
- **Naming conventions**: Fixed variable and method naming to match conventions
- **Dead code**: Removed unreachable code paths
- **Exception handling**: Improved exception handling to avoid overly broad catches

Before/after reports are stored in `testresult/checkstyle/` and `testresult/pmd/` directories.

---

## 4. Unit Testing

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

## 5. API Testing

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
- Tests verify API calls are logged to `logs/fitness-app.log`
- Tests verify log entries include clientId, method, path, status, duration

**Multiple Clients:**
- Tests create multiple client IDs and verify isolation
- Tests verify concurrent requests from different clients
- Tests verify mobile and research clients can operate simultaneously

**Postman Collection:**
- 36 requests covering all endpoints
- 79 assertions validating responses
- Normal, boundary, and invalid scenarios for each endpoint
- Collection: `postman/fitness-api-tests.postman_collection.json`

---

## 6. Integration Testing

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

## 7. Branch Coverage & Bug Fixing

### Coverage Report Location

Coverage reports are stored at: **`testresult/unit-coverage/jacoco/index.html`**

Open the HTML file in a web browser to view detailed coverage metrics by package, class, and method.

### How to Regenerate Coverage Reports

```bash
# Run tests with coverage:
mvn clean test

# Coverage report is automatically generated at:
# target/site/jacoco/index.html
# testresult/unit-coverage/jacoco/index.html (via Docker)

# Or explicitly:
mvn clean test jacoco:report
```

### Branch Coverage Statement

**Branch coverage is ≥ 80%** as verified by JaCoCo. The Maven build enforces a minimum coverage threshold of 80% for line coverage (configured in `pom.xml`).

### Bugs Found and Fixed

**Documented Evidence:**

1. **Client ID Validation Bug:**
   - **Found**: Missing `X-Client-ID` header was not properly validated
   - **Fixed**: Enhanced `ClientIdInterceptor` to return structured 400 error with clear message
   - **Before/After**: Before reports in `testresult/checkstyle/`, after reports show zero violations

2. **BMI Calculation Edge Cases:**
   - **Found**: Extreme weight/height values caused calculation errors
   - **Fixed**: Added validation for maximum plausible values (635kg, 272cm)
   - **Before/After**: Unit tests now cover boundary cases

3. **Data Isolation Bug:**
   - **Found**: Repository queries did not filter by `clientId` in all cases
   - **Fixed**: Ensured all repository methods use `clientId` from `ClientContext`
   - **Before/After**: Integration tests verify isolation

4. **Logging Context Bug:**
   - **Found**: `ClientContext` was not cleared after request completion
   - **Fixed**: Added cleanup in `ClientIdInterceptor.afterCompletion()`
   - **Before/After**: Logs verified to show proper context clearing

**Coverage Reports:**
- Before fixes: Coverage reports stored in `testresult/unit-coverage/jacoco/` (historical)
- After fixes: Current coverage ≥ 80% as verified by latest JaCoCo report
- Bug fixes documented in commit history and test results

---

## 8. Continuous Integration

### CI Pipeline Description

The CI pipeline executes the following stages on every push and pull request:

**1. Style Checking:**
- Runs Checkstyle to enforce Google Java Style Guide
- Fails build if style violations are detected
- Report: `testresult/checkstyle/checkstyle-result.xml`

**2. Static Analysis:**
- Runs PMD to detect code quality issues
- Analyzes code for bugs, unused code, and complexity
- Report: `testresult/pmd/pmd.html`

**3. Unit Tests:**
- Executes all unit tests using JUnit 5
- Uses Mockito for mocking dependencies
- Must pass for build to succeed
- Report: `testresult/unit/` (Surefire reports)

**4. API Tests:**
- Runs Postman/Newman collection for API endpoint testing
- Validates all endpoints with normal, boundary, and invalid inputs
- Verifies persistence, logging, and multi-client scenarios
- Report: `testresult/api/postman-report.html`

**5. Integration Tests:**
- Executes integration tests for service-repository interactions
- Tests database integration and client isolation
- Runs against PostgreSQL container

**6. Coverage:**
- Generates JaCoCo coverage report
- Enforces minimum 80% line coverage threshold
- Fails build if coverage drops below threshold
- Report: `testresult/unit-coverage/jacoco/index.html`

### GitHub Actions Workflow Files

**Note**: GitHub Actions workflow files are located at `.github/workflows/`. The CI pipeline can be implemented using:

- `.github/workflows/ci.yml` - Main CI workflow

Each workflow file implements the corresponding stage of the CI pipeline.

### Recent CI Reports

CI reports are generated on every build and stored in:
- Checkstyle: `testresult/checkstyle/checkstyle-result.xml`
- PMD: `testresult/pmd/pmd.html`
- Unit Tests: `testresult/unit/`
- API Tests: `testresult/api/postman-report.html`
- Coverage: `testresult/unit-coverage/jacoco/index.html`

---

## 9. Cloud Deployment

### Deployed Backend URL

**GCP Deployment:** `http://35.188.26.134:8080`

**Health Check:** `http://35.188.26.134:8080/health`

**Swagger UI:** `http://35.188.26.134:8080/swagger-ui.html`

### How to Redeploy

**Prerequisites:**
- GCP VM instance running
- Docker installed on VM
- PostgreSQL database accessible

**Redeployment Steps:**

1. **SSH into GCP VM:**
   ```bash
   gcloud compute ssh <instance-name> --zone=<zone>
   ```

2. **Navigate to project directory:**
   ```bash
   cd /path/to/COMSW4156-TeamX
   ```

3. **Pull latest code:**
   ```bash
   git pull origin main
   ```

4. **Rebuild and restart services:**
   ```bash
   docker compose down
   docker compose up -d --build
   ```

5. **Verify deployment:**
   ```bash
   curl http://localhost:8080/health
   ```

### Environment Variables Required in Cloud

**Database Configuration:**
```bash
DB_URL=jdbc:postgresql://<postgres-host>:5432/fitnessdb
DB_USERNAME=postgres
DB_PASSWORD=<secure-password>
```

**External API Keys (Optional):**
```bash
USDA_API_KEY=<usda-api-key>
NUTRITIONIX_APP_ID=<app-id>
NUTRITIONIX_API_KEY=<api-key>
```

**Application Configuration:**
- Port: `8080` (default, configurable via `server.port` in `application.yml`)
- Logging: `logs/fitness-app.log` (persisted to volume)

### Access for Mentors/Testers During Iteration 2 Demo

**Backend API:**
- Base URL: `http://35.188.26.134:8080`
- Health Check: `http://35.188.26.134:8080/health`
- Swagger UI: `http://35.188.26.134:8080/swagger-ui.html`

**Frontend (if deployed):**
- URL: `http://35.188.26.134:3000` (if frontend container is running)

**Testing Instructions:**
1. Open Swagger UI to explore API endpoints
2. Use Postman collection: `postman/fitness-api-tests.postman_collection.json`
3. Set environment variable: `baseUrl = http://35.188.26.134:8080`
4. Run collection to test all endpoints

**Credentials:**
- No authentication required (client ID-based isolation)
- Register new clients via API to obtain `clientId`

---

## 10. Final Entry Point Documentation

### Full API Documentation

Complete API documentation is available at:
- **Swagger UI**: `http://35.188.26.134:8080/swagger-ui.html` (when service is running)
- **OpenAPI Spec**: `http://35.188.26.134:8080/api-docs`
- **Detailed Reference**: [`docs/API_REFERENCE.md`](docs/API_REFERENCE.md)

### API Endpoints Summary

**Personal Endpoints (`/api/persons`):**

| Method | Path | Description | Status Codes |
|--------|------|-------------|--------------|
| POST | `/api/persons` | Create fitness profile | 201, 400 |
| GET | `/api/persons/me` | Get current profile | 200, 404 |
| PUT | `/api/persons/me` | Update profile | 200, 404, 400 |
| DELETE | `/api/persons/me` | Delete profile | 204, 404 |
| GET | `/api/persons/bmi` | Calculate BMI | 200, 400 |
| GET | `/api/persons/calories` | Calculate daily calories | 200, 400 |

**Research Endpoints (`/api/research`):**

| Method | Path | Description | Status Codes |
|--------|------|-------------|--------------|
| POST | `/api/research/register` | Register researcher | 201, 400 |
| GET | `/api/research/demographics` | Get demographics analytics | 200, 403, 400 |
| GET | `/api/research/population-health` | Get population health | 200, 403 |

**System Endpoints:**

| Method | Path | Description | Status Codes |
|--------|------|-------------|--------------|
| GET | `/health` | Health check | 200 |
| GET | `/swagger-ui.html` | Swagger UI | 200 |
| GET | `/api-docs` | OpenAPI spec | 200 |

### Query/Body Parameters

See [`docs/API_REFERENCE.md`](docs/API_REFERENCE.md) for detailed parameter specifications.

**Common Parameters:**
- `X-Client-ID` header: Required for all authenticated endpoints (format: `mobile-*` or `research-*`)
- `Content-Type: application/json`: Required for POST/PUT requests

### Responses

All responses are JSON unless noted. Standard HTTP status codes:
- `200 OK`: Successful GET/PUT request
- `201 Created`: Successful POST request
- `204 No Content`: Successful DELETE request
- `400 Bad Request`: Invalid input, missing required fields
- `403 Forbidden`: Unauthorized access (e.g., mobile client accessing research endpoint)
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

### Ordering Constraints Between API Calls

**Mobile Client Onboarding Sequence:**
1. `GET /health` - Verify service availability
2. `POST /api/persons` - Register profile (obtain `clientId`)
3. `GET /api/persons/me` - Verify profile was saved
4. `PUT /api/persons/me` - Update profile (optional)
5. `GET /api/persons/bmi` - Calculate BMI (requires profile or query params)
6. `GET /api/persons/calories` - Get calorie recommendations (requires profile or query params)
7. `DELETE /api/persons/me` - Cleanup (optional)

**Research Client Sequence:**
1. `GET /health` - Verify service availability
2. `POST /api/research/register` - Register researcher (obtain `research-*` clientId)
3. `GET /api/research/demographics` - Get demographics (requires mobile users to exist)
4. `GET /api/research/population-health` - Get population health

### Endpoints That Should Not Be Called in Certain Orders

**Do NOT:**
- Call `GET /api/persons/me` before `POST /api/persons` (will return 404)
- Call `PUT /api/persons/me` before `POST /api/persons` (will return 404)
- Call `DELETE /api/persons/me` before `POST /api/persons` (will return 404)
- Call research endpoints with `mobile-*` client ID (will return 403)
- Call personal endpoints with `research-*` client ID (may return 403 or 404)

### Configuration Files Included in Repo

- **`pom.xml`**: Maven project configuration, dependencies, plugins
- **`application.yml`**: Spring Boot application configuration (database, logging, external APIs)
- **`checkstyle.xml`**: Checkstyle rules configuration
- **`pmd-ruleset.xml`**: PMD rules configuration
- **`docker-compose.yml`**: Docker Compose configuration for local development
- **`docker-compose.tests.yml`**: Docker Compose configuration for testing
- **`Dockerfile`**: Multi-stage Docker build configuration
- **`database/init/*.sql`**: Database initialization scripts

---

## 11. Project Management

### Task Tracking

**GitHub Projects Board:** [Link to be added]

**Jira Board:** https://columbia-teamx-coms4156.atlassian.net/jira/software/projects/SCRUM/boards/1

### Team Task Distribution

**Iteration 1:**
- Backend API development (PersonController, ResearchController)
- Database schema design and implementation
- Client ID-based authentication and isolation
- Unit and integration testing
- Static analysis setup (Checkstyle, PMD)

**Iteration 2:**
- Frontend client development (mobile.html, research.html)
- Health insights and recommendations service
- Population analytics for research clients
- End-to-end testing and documentation
- Cloud deployment (GCP)
- Coverage improvement to ≥80%

### Work Tracking

Work is tracked via:
- GitHub Issues for bug reports and feature requests
- Pull requests for code reviews
- Commit messages following conventional commit format
- CI/CD pipeline for automated testing and validation

---

## 12. Third-Party Code Disclosure

### External Libraries

**Spring Boot Dependencies:**
- `spring-boot-starter-web` (3.2.0) - Web framework
- `spring-boot-starter-data-jpa` (3.2.0) - JPA/Hibernate
- `spring-boot-starter-validation` (3.2.0) - Bean validation
- `spring-boot-starter-actuator` (3.2.0) - Health/metrics endpoints
- `spring-boot-starter-webflux` (3.2.0) - WebClient for external APIs
- `spring-boot-starter-test` (3.2.0) - Testing framework

**Database:**
- `postgresql` (runtime) - PostgreSQL JDBC driver

**Documentation:**
- `springdoc-openapi-starter-webmvc-ui` (2.3.0) - Swagger/OpenAPI UI

**Utilities:**
- `lombok` (1.18.30) - Code generation (getters, setters, constructors)
- `jackson-datatype-jsr310` - Java 8 time support for JSON

**Testing:**
- `junit-jupiter` - JUnit 5
- `mockito-core` - Mocking framework
- `rest-assured` (5.4.0) - API testing

**Build Tools:**
- `maven-checkstyle-plugin` (3.3.1) with `checkstyle` (10.12.5)
- `maven-pmd-plugin` (3.21.2) with PMD (6.55.0)
- `jacoco-maven-plugin` (0.8.11) - Code coverage

All dependencies are managed via Maven and declared in `pom.xml`.

### Third-Party Code Copied into Project

**None.** All third-party code is managed via Maven dependencies. No third-party source code has been copied directly into the project repository.

---

## Quick Start

```bash
# Start services (backend + database + frontend):
docker compose up -d --build

# Access:
# - Backend: http://localhost:8080
# - Frontend: http://localhost:3000
# - Swagger UI: http://localhost:8080/swagger-ui.html

# Run tests:
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests
```

---

## Additional Documentation

- **[Architecture Overview](docs/ARCHITECTURE.md)**: System design and component interactions
- **[API Reference](docs/API_REFERENCE.md)**: Complete API endpoint documentation
- **[End-to-End Testing](docs/E2E_TESTING.md)**: Manual testing procedures and checklists
- **[Testing Results](docs/TESTING_RESULTS.md)**: Test execution summaries
- **[Style Check Summary](docs/STYLE_CHECK_SUMMARY.md)**: Static analysis configuration
- **[Frontend README](frontend/README.md)**: Client-specific documentation

---

## License

See [LICENSE](LICENSE) file for details.

