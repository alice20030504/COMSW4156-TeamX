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
- Every API request is logged to [`logs/fitness-app.log`](logs/fitness-app.log) with structured JSON format
- Log entries include: clientId, HTTP method, path, status code, duration (ms), IP address, User-Agent, and error messages (if any)
- Logging is implemented via `ApiLoggingInterceptor` which captures request lifecycle events

### Cloud Deployment

**GCP Deployment URLs:**
- **Backend:** `http://34.30.81.33:8080`
- **Frontend:** `http://34.30.81.33:3000`

### Iteration 2 Tagged Version

The tagged Iteration 2 version is located at: **`Iteration_2`** (to be updated with actual git tag)

---

## 2. Client Program

A modern web-based client is available in the `frontend/` directory, providing user-friendly browser interfaces for both mobile users to manage fitness profiles and research analysts to access population health analytics. It supports simultaneous multi-client sessions with complete data isolation via `X-Client-ID` header authentication.

**For complete documentation on building, running, testing locally, and connecting to the GCP-deployed backend server, see [`frontend/README.md`](frontend/README.md).**

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

- **Checkstyle**: [`testresult/checkstyle/checkstyle-result.xml`](testresult/checkstyle/checkstyle-result.xml)
- **PMD**: [`testresult/pmd/pmd.html`](testresult/pmd/pmd.html)
- Reports are also generated in `target/` directory during Maven builds

### Style Checking

- Style checking is enforced via Checkstyle and integrated into CI pipeline
- Checkstyle configuration: [`checkstyle.xml`](checkstyle.xml) (based on Google Java Style Guide)
- PMD ruleset: [`pmd-ruleset.xml`](pmd-ruleset.xml)
- Zero violations are required for code commits

### Bugs Fixed

Static analysis tools identified and fixed the following issues:
- **Unused imports**: Removed unused import statements
- **Code complexity**: Refactored methods exceeding complexity thresholds
- **Naming conventions**: Fixed variable and method naming to match conventions
- **Dead code**: Removed unreachable code paths
- **Exception handling**: Improved exception handling to avoid overly broad catches

Before/after reports are stored in [`testresult/checkstyle/`](testresult/checkstyle/) and [`testresult/pmd/`](testresult/pmd/) directories.

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
For complete API documentation including all endpoints, request/response formats, parameters, status codes, and calling sequences, see **[`docs/API_REFERENCE.md`](docs/API_REFERENCE.md)**.

**Quick Access:**
- **Swagger UI**: `http://34.30.81.33:8080/swagger-ui.html` (when service is running)
- **OpenAPI Spec**: `http://34.30.81.33:8080/api-docs`

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

Coverage reports are stored at: **[`testresult/unit-coverage/jacoco/index.html`](testresult/unit-coverage/jacoco/index.html)**

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

**Branch coverage is ≥ 80%** as verified by JaCoCo. The Maven build enforces a minimum coverage threshold of 80% for line coverage (configured in [`pom.xml`](pom.xml)).

### Bugs Found and Fixed

**Documented Evidence:**

1. **Client ID Validation Bug:**
   - **Found**: Missing `X-Client-ID` header was not properly validated
   - **Fixed**: Enhanced `ClientIdInterceptor` to return structured 400 error with clear message
   - **Before/After**: Before reports in [`testresult/checkstyle/`](testresult/checkstyle/), after reports show zero violations

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
- Before fixes: Coverage reports stored in [`testresult/unit-coverage/jacoco/`](testresult/unit-coverage/jacoco/) (historical)
- After fixes: Current coverage ≥ 80% as verified by latest JaCoCo report
- Bug fixes documented in commit history and test results

---

## 8. Continuous Integration

### CI Pipeline Description

The CI pipeline executes the following stages on every push and pull request:

**1. Style Checking:**
- Runs Checkstyle to enforce Google Java Style Guide
- Fails build if style violations are detected
- Report: [`testresult/checkstyle/checkstyle-result.xml`](testresult/checkstyle/checkstyle-result.xml)

**2. Static Analysis:**
- Runs PMD to detect code quality issues
- Analyzes code for bugs, unused code, and complexity
- Report: [`testresult/pmd/pmd.html`](testresult/pmd/pmd.html)

**3. Unit Tests & API Tests:**
- Executes all unit tests using JUnit 5
- Uses Mockito for mocking dependencies
- Runs Postman/Newman collection for API endpoint testing
- Validates all endpoints with normal, boundary, and invalid inputs
- Verifies persistence, logging, and multi-client scenarios
- Must pass for build to succeed
- Report: [`testresult/api/postman-report.html`](testresult/api/postman-report.html) (includes both unit and API test results)

**5. Integration Tests:**
- Executes integration tests for service-repository interactions
- Tests database integration and client isolation
- Runs against PostgreSQL container

**6. Coverage:**
- Generates JaCoCo coverage report
- Enforces minimum 80% line coverage threshold
- Fails build if coverage drops below threshold
- Report: [`testresult/unit-coverage/jacoco/index.html`](testresult/unit-coverage/jacoco/index.html)

### GitHub Actions Workflow Files

**Note**: GitHub Actions workflow files are located at `.github/workflows/`. The CI pipeline can be implemented using:

- `.github/workflows/ci.yml` - Main CI workflow

Each workflow file implements the corresponding stage of the CI pipeline.

### Recent CI Reports

CI reports are generated on every build and stored in:
- Checkstyle: [`testresult/checkstyle/checkstyle-result.xml`](testresult/checkstyle/checkstyle-result.xml)
- PMD: [`testresult/pmd/pmd.html`](testresult/pmd/pmd.html)
- Unit Tests & API Tests: [`testresult/api/postman-report.html`](testresult/api/postman-report.html)
- Coverage: [`testresult/unit-coverage/jacoco/index.html`](testresult/unit-coverage/jacoco/index.html)

---

## 9. Cloud Deployment

### Deployed URLs

**GCP Deployment:**
- **Backend:** `http://34.30.81.33:8080`
- **Frontend:** `http://34.30.81.33:3000`

**Service Endpoints:**
- **Health Check:** `http://34.30.81.33:8080/health`
- **Swagger UI:** `http://34.30.81.33:8080/swagger-ui.html`

### Access for Mentors/Testers During Iteration 2 Demo

**Backend API:**
- Base URL: `http://34.30.81.33:8080`
- Health Check: `http://34.30.81.33:8080/health`
- Swagger UI: `http://34.30.81.33:8080/swagger-ui.html`

**Frontend:**
- URL: `http://34.30.81.33:3000`
---

## 10. Final Entry Point Documentation

### Full API Documentation

Complete API documentation is available at:
- **Swagger UI**: `http://34.30.81.33:8080/swagger-ui.html` (when service is running)
- **OpenAPI Spec**: `http://34.30.81.33:8080/api-docs`
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

- **[`pom.xml`](pom.xml)**: Maven project configuration, dependencies, plugins
- **[`application.yml`](src/main/resources/application.yml)**: Spring Boot application configuration (database, logging, external APIs)
- **[`checkstyle.xml`](checkstyle.xml)**: Checkstyle rules configuration
- **[`pmd-ruleset.xml`](pmd-ruleset.xml)**: PMD rules configuration
- **[`docker-compose.yml`](docker-compose.yml)**: Docker Compose configuration for local development
- **[`docker-compose.tests.yml`](docker-compose.tests.yml)**: Docker Compose configuration for testing
- **[`Dockerfile`](Dockerfile)**: Multi-stage Docker build configuration
- **[`database/init/`](database/init/)**: Database initialization scripts

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

All dependencies are managed via Maven and declared in [`pom.xml`](pom.xml).

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

## 13. Project Proposal Implementation Status

This section provides a comprehensive comparison between our original project proposal and the implemented features in Iteration 2.

### Core Functionality Status

| Proposed Feature | Status | Implementation Details |
|------------------|--------|------------------------|
| **Maintain structured records of users** | ✅ **Implemented** | User profiles stored in `person_simple` table with fields: name, weight, height, birthDate, gender, goal, targetChangeKg, targetDurationWeeks, trainingFrequencyPerWeek, planStrategy |
| **Store and manage nutritional data for foods** | 🔄 **Planned for Future** | See rationale below |
| **Generate personalised weekly recipes** | 🔄 **Explored, Deferred** | See rationale below |
| **Analytic computations (BMI, age, calories, macros)** | ✅ **Partially Implemented** | BMI, age, BMR, and daily calorie needs are fully implemented. Macronutrient balance calculations are planned for future iterations. |
| **RESTful API** | ✅ **Implemented** | Complete REST API with endpoints for personal and research clients. See [`docs/API_REFERENCE.md`](docs/API_REFERENCE.md) |
| **Data persistence** | ✅ **Implemented** | PostgreSQL database with JPA/Hibernate. All user profiles, goal plans, and researcher data are persisted. |
| **Comprehensive API call logging** | ✅ **Implemented** | All API requests logged to [`logs/fitness-app.log`](logs/fitness-app.log) with clientId, method, path, status, duration, IP, User-Agent |

### Core Computation Status

#### [Base] Workout Computation

| Proposed Feature | Status | Implementation Details |
|------------------|--------|------------------------|
| **Record exercises (type: aerobic/anaerobic, sets, duration)** | 🔄 **Planned for Future** | See rationale below |
| **Calculate calories burned using MET-based formulas** | 🔄 **Planned for Future** | See rationale below |

**Implementation Approach:**
- **Current Foundation**: We implemented training frequency tracking (weekly workouts) which provides sufficient data for calorie adjustment calculations. This serves as the foundation for future detailed exercise logging.
- **Future Enhancement**: Detailed exercise tracking with MET-based calculations is planned for future iterations. This would require:
  - A comprehensive exercise database with MET values for different activities
  - Time-series data storage for workout logs
  - Integration with activity tracking devices or manual entry systems
- **Scope Decision**: Given the time constraints of Iteration 1 and 2, we prioritized core user profile management, goal planning, and basic health calculations (BMI, BMR, calorie needs) which form the essential foundation of the service.

#### [Base] Nutrition Computation

| Proposed Feature | Status | Implementation Details |
|------------------|--------|------------------------|
| **Store food logs with nutritional breakdown** | 🔄 **Planned for Future** | See rationale below |
| **Fetch real-world data from external APIs (USDA, Nutritionix)** | ⚠️ **Infrastructure Ready** | API keys and endpoints are configured in [`application.yml`](src/main/resources/application.yml). Integration endpoints are planned for future iterations. |
| **Compute total intake and net calories** | 🔄 **Planned for Future** | See rationale below |

**Implementation Approach:**
- **Current Foundation**: We provide goal-based calorie recommendations (based on BMR and activity level) which delivers immediate value. The infrastructure for external API integration is already in place.
- **Future Enhancement**: Full nutrition tracking is planned for future iterations. This would include:
  - Food database schema (foods, meals, daily logs)
  - Macronutrient tracking (carbs, protein, fat) per food item
  - Time-series data for daily intake tracking
  - Integration with USDA and Nutritionix APIs (already configured)
- **Technical Considerations**: Full implementation would require handling API rate limits, security, error handling, and comprehensive testing infrastructure for external service mocking.

#### [Advanced] Goal and Progress Tracking

| Proposed Feature | Status | Implementation Details |
|------------------|--------|------------------------|
| **Support objectives (cut, bulk, recover)** | ✅ **Partially Implemented** | CUT and BULK objectives are fully supported. RECOVER objective is planned for future iterations. |
| **Store personal attributes (height, weight, age, gender)** | ✅ **Implemented** | All attributes stored in `PersonSimple` entity |
| **Body fat tracking** | 🔄 **Planned for Future** | Body fat percentage field can be added to the data model in future iterations |
| **Track weekly training frequency** | ✅ **Implemented** | `trainingFrequencyPerWeek` field with validation (1-14 days/week) |
| **Progress tracking over time** | 🔄 **Planned for Future** | Historical progress data and trend analysis planned for future iterations |

**Implementation Status:**
- **CUT/BULK Objectives**: Fully implemented with comprehensive goal planning, calorie adjustments, and strategy selection (DIET_ONLY, WORKOUT_ONLY, BOTH).
- **RECOVER Objective**: Planned for future iterations. Requires different calculation logic (maintenance/recovery calories) and was lower priority than CUT/BULK which cover the primary use cases.
- **Body Fat Tracking**: Can be added to the data model in future iterations. Currently, BMI serves as the primary body composition indicator.
- **Progress Tracking**: Historical tracking is planned for future iterations. Would require time-series data storage and trend analysis algorithms. Current implementation focuses on current state and goal planning.

#### [Advanced] Recipe and Recommendation System

| Proposed Feature | Status | Implementation Details |
|------------------|--------|------------------------|
| **Generate or assign weekly diet plans** | 🔄 **Explored, Deferred** | See rationale below |
| **Recipe database** | 🔄 **Planned for Future** | Recipe storage and management planned for future iterations |
| **Dynamic plan adaptation** | ⚠️ **Partially Implemented** | Plans adapt based on goal (CUT/BULK) and training frequency. Dynamic adaptation based on progress is planned for future iterations. |
| **Personalised nutrition recommendations** | ✅ **Implemented** | `HealthInsightService` provides goal-specific recommendations based on BMI, plan strategy, and training frequency |

**Implementation Exploration:**
- **AI-Powered Recipe Generation**: We explored using AI (FastAPI with Python) for weekly recipe generation. However, this approach presented significant technical challenges:
  - **Language Barrier**: Python libraries (e.g., FastAPI, specialized ML libraries) don't have direct Java equivalents, making integration complex
  - **Architecture Complexity**: Implementing this would require a dual-backend architecture (Java Spring Boot on port 8080 + Python FastAPI on port 5001), significantly increasing system complexity
  - **Time Constraints**: Setting up inter-service communication, API gateways, and managing two separate backends would require substantial additional development time beyond Iteration 2 scope
- **Current Approach**: We provide calorie targets and goal-specific recommendations through `HealthInsightService`, allowing clients to implement their own meal planning or integrate with third-party meal planning services.
- **Future Enhancement**: Recipe generation remains a high-priority feature for future iterations. Potential approaches include:
  - Java-native recipe generation algorithms
  - Integration with existing recipe APIs
  - Simplified meal planning based on macro targets

### Persistent Data Status

| Proposed Data Type | Status | Implementation Details |
|---------------------|--------|------------------------|
| **User profiles** | ✅ **Implemented** | Complete user profile with all essential attributes |
| **Workout records** | 🔄 **Planned for Future** | Individual exercise logs planned for future iterations |
| **Nutrition records** | 🔄 **Planned for Future** | Food logs and meal tracking planned for future iterations |
| **Goal and progress data** | ✅ **Partially Implemented** | Goal plans (target change, duration, strategy) are stored. Historical progress tracking is planned for future iterations. |
| **API call logs** | ✅ **Implemented** | Comprehensive logging with clientId, endpoint, timestamp, status, duration, IP, User-Agent |

### Client Programs Status

| Proposed Client | Status | Implementation Details |
|-----------------|--------|------------------------|
| **Mobile Fitness Tracker App** | ✅ **Implemented** | Web-based mobile client ([`frontend/mobile.html`](frontend/mobile.html)) with profile management, goal planning, BMI calculation, and calorie recommendations |
| **Analyser/Researcher Tool** | ✅ **Implemented** | Research client ([`frontend/research.html`](frontend/research.html)) with demographics analytics, population health metrics, and anonymized aggregate data |

**Note**: While the proposed clients mentioned specific endpoints like `log_workout` and `log_food` which are not implemented, our clients provide equivalent functionality through the implemented endpoints (goal planning, calorie recommendations, health insights).

### Development Tools Status

All proposed development tools have been successfully integrated:

| Tool | Status | Usage |
|------|--------|-------|
| **GitHub with GitHub Actions** | ✅ **Implemented** | Version control and CI/CD pipeline |
| **Maven** | ✅ **Implemented** | Build and dependency management ([`pom.xml`](pom.xml)) |
| **JUnit** | ✅ **Implemented** | Unit testing framework |
| **Postman/Newman** | ✅ **Implemented** | API testing with collection: [`postman/fitness-api-tests.postman_collection.json`](postman/fitness-api-tests.postman_collection.json) |
| **Mockito** | ✅ **Implemented** | Mocking framework for unit tests |
| **JaCoCo** | ✅ **Implemented** | Code coverage tracking (≥80% achieved) |
| **CheckStyle** | ✅ **Implemented** | Style checking ([`checkstyle.xml`](checkstyle.xml)) |
| **PMD** | ✅ **Implemented** | Static analysis ([`pmd-ruleset.xml`](pmd-ruleset.xml)) |
| **GitHub Projects** | ✅ **Implemented** | Task tracking (see Section 11) |
| **IntelliJ IDEA** | ✅ **Used** | Primary IDE for development |

### Summary

**Successfully Implemented (Core Features):**
- ✅ User profile management with comprehensive attributes
- ✅ BMI, BMR, and calorie calculation computations
- ✅ Goal-based planning (CUT/BULK) with strategy selection
- ✅ RESTful API with client isolation
- ✅ Data persistence in PostgreSQL
- ✅ Comprehensive API logging
- ✅ Mobile and research client applications
- ✅ Population analytics for researchers
- ✅ Health insights and recommendations

**Planned for Future Iterations:**
- 🔄 Individual exercise logging and MET-based calorie burn calculations
- 🔄 Food logging and nutrition tracking (infrastructure already configured)
- 🔄 Recipe database and AI-powered meal plan generation (explored, requires dual-backend architecture)
- 🔄 Historical progress tracking and trend analysis
- 🔄 RECOVER objective (CUT/BULK currently implemented)
- 🔄 Body fat percentage tracking

**Implementation Strategy:**
Our Iteration 2 implementation focused on delivering a **solid foundation** for a fitness management service with:
1. **Core health calculations** (BMI, BMR, calories) that provide immediate value
2. **Goal-based planning** that enables personalized fitness strategies
3. **Multi-client architecture** that supports both end-users and researchers
4. **Robust infrastructure** (logging, persistence, testing, CI/CD) that ensures quality

The features planned for future iterations represent **advanced functionality** that would require:
- Significant additional data modeling and storage
- External API integrations with complex error handling (USDA/Nutritionix infrastructure already in place)
- Advanced algorithms for meal planning and optimization
- Time-series data analysis capabilities
- Potential dual-backend architecture for AI-powered features (explored for recipe generation)

These features are excellent candidates for future iterations once the core service is proven stable and scalable. The current implementation provides a strong foundation that can be extended incrementally.

---

## License

See [`LICENSE`](LICENSE) file for details.

