# Personal Fitness Management Service

## Service Overview

The Personal Fitness Management Service is a Spring Boot REST API that provides comprehensive fitness tracking and analytics capabilities. The service performs meaningful computation beyond basic CRUD operations, including:

- **Fitness Calculations**: BMI (Body Mass Index), BMR (Basal Metabolic Rate), and daily calorie recommendations using the Harris-Benedict formula with activity multipliers
- **Goal Planning**: Personalized fitness plans with target weight changes, duration, and training frequency
- **Research Analytics**: Anonymized population-level analytics for demographic trends, workout patterns, nutrition insights, and population health metrics
- **Multi-Client Support**: Simultaneous support for multiple mobile and research clients with complete data isolation via `X-Client-ID` header authentication
- **Persistent Storage**: All data is persisted in PostgreSQL database with client-scoped isolation
- **Comprehensive Logging**: All API calls are logged with request/response details, client context, and execution traces in `logs/fitness-app.log`

### Cloud Deployment

**Production URL**: https://teamx-backend-118279583185.us-central1.run.app

The service is deployed on Google Cloud Platform (GCP) Cloud Run. See the [Cloud Deployment](#cloud-deployment) section for redeployment instructions and environment configuration.

### Iteration 2 Tagged Version

The Iteration 2 release is tagged in the repository. Check the git tags for the specific Iteration 2 version tag.

---

## Client Documentation

### Client Location

The web-based client is integrated in the same repository at: **`frontend/`**

The client consists of:
- `index.html` - Landing page with client selection
- `mobile.html` - Mobile user interface
- `research.html` - Research analyst interface
- `app.js` - Application logic and API integration
- `mobile.js` - Mobile-specific functionality
- `research.js` - Research-specific functionality
- `styles.css` - Styling and responsive design

### What the Client Does

The frontend web client provides a user-friendly browser interface with the following capabilities:

**Mobile Client Features:**
- **User Registration**: Register new fitness profiles with name, weight, height, birth date, gender, and fitness goal
- **Profile Management**: View and update stored fitness profiles
- **Goal Plan Configuration**: Set up personalized fitness plans with target weight change, duration (weeks), training frequency (days/week), and strategy (Diet only, Workout only, or Both)
- **Fitness Metrics**:
  - Calculate and display BMI with category classification
  - Get daily calorie recommendations based on BMR, goal, and activity level
  - Receive personalized fitness recommendations tailored to the user's goal (CUT/BULK)
- **Profile Retrieval**: View stored profile data for the authenticated client

**Research Client Features:**
- **Researcher Registration**: Register research analyst accounts
- **Demographics Analytics**: View anonymized demographic breakdowns by gender and goal with bar chart visualizations
- **Population Health Metrics**: Access aggregate population health indicators, goal achievement rates, and BMI distributions

**API Integration:**
- All client actions make HTTP requests to the backend service
- Requests include the `X-Client-ID` header for authentication
- Client IDs are stored in browser sessionStorage for persistence during the session
- Error handling displays user-friendly messages for API failures

### How to Build and Run the Client

**Prerequisites:**
- Modern web browser (Chrome, Firefox, Safari, Edge)
- Backend service running (see [Build, Test, and Run](#build-test-and-run) section)

**Option 1: Direct File Access**
1. Ensure the backend service is running on `http://localhost:8080`
2. Open `frontend/index.html` in your web browser
   - **Windows**: Double-click `index.html` or right-click → "Open with" → your browser
   - **Mac/Linux**: Open from file manager or use `open frontend/index.html` (Mac) / `xdg-open frontend/index.html` (Linux)

**Option 2: Local Web Server (Recommended)**
```bash
# Using Python 3
cd frontend
python -m http.server 3000
# Then open: http://localhost:3000

# Or using Node.js http-server
cd frontend
npx http-server -p 3000
# Then open: http://localhost:3000
```

**Environment Variables / Configuration:**
- The client automatically detects the backend API URL
- Default API URL: `http://localhost:8080`
- To connect to a remote backend, append `?apiBaseUrl=<url>` to the HTML file URL (e.g., `mobile.html?apiBaseUrl=https://teamx-backend-118279583185.us-central1.run.app`)
- API URL can also be configured via the "API Configuration" panel in the client UI
- Client IDs and API URLs are stored in browser sessionStorage (tab-specific)

**Connection Settings:**
- The client uses the Fetch API to make HTTP requests
- All authenticated requests include the `X-Client-ID` header
- CORS is enabled on the backend (`@CrossOrigin(origins = "*")`)
- The client handles errors gracefully and displays user-friendly messages

### How Multiple Client Instances Connect

The service supports multiple simultaneous client instances through the `X-Client-ID` header mechanism:

**Client Identification:**
- Each client instance is identified by a unique `X-Client-ID` header (e.g., `mobile-id1`, `mobile-id2`, `research-xyz789`)
- Client IDs are generated by the backend during registration and follow the pattern `<type>-<identifier>` where type is `mobile` or `research`
- Client IDs are stored in browser sessionStorage (tab-specific), ensuring each browser tab maintains its own independent session

**Backend Distinction:**
- The backend's `ClientIdInterceptor` intercepts every API request (except open endpoints like registration)
- The client ID is validated and stored in `ClientContext` (thread-local) for the request lifecycle
- Database queries are filtered by client ID, ensuring each client only sees/modifies their own data
- Multiple clients can run simultaneously without interference

**Testing Multiple Clients:**
- **Different Browser Tabs**: Each tab can have a different client ID in sessionStorage
- **Different Browsers**: Each browser maintains separate sessionStorage
- **Different Machines**: Multiple machines can connect to the same backend simultaneously
- **Concurrent Requests**: All instances can make simultaneous requests without data leakage

**Example:**
- Tab 1: Client ID `mobile-id1` → sees only profiles created with `mobile-id1`
- Tab 2: Client ID `mobile-id2` → sees only profiles created with `mobile-id2`
- Both tabs can run simultaneously, making requests to the same backend, and data remains isolated

### End-to-End Testing Checklist

Complete end-to-end (E2E) test documentation is available in **`docs/E2E_TESTING.md`**. The following checklist covers all client → service API workflows:

**Mobile Client E2E Tests:**
1. ✅ **User Registration and Profile Creation**
   - Register a new user with valid profile data
   - Verify client ID is generated and stored
   - Verify profile persists in backend

2. ✅ **Goal Plan Configuration**
   - Configure a personalized fitness plan
   - Verify plan is saved with correct client ID
   - Verify plan data persists after page refresh

3. ✅ **BMI Calculation**
   - Calculate BMI from registered metrics
   - Verify BMI is displayed with proper formatting (max 2 decimal places)
   - Verify result displays in card format (not raw JSON)

4. ✅ **Calorie Recommendations**
   - Get daily calorie recommendations
   - Verify calories are calculated based on BMR, goal, and activity level
   - Verify result displays in card format with breakdown

5. ✅ **Fitness Recommendations**
   - Get goal-specific recommendations
   - Verify recommendations match user's goal (CUT/BULK)
   - Verify result displays in readable format

6. ✅ **Multiple Mobile Client Instances**
   - Register two users in different browser tabs
   - Verify each tab shows only its own profile data
   - Verify simultaneous requests work correctly

**Research Client E2E Tests:**
7. ✅ **Researcher Registration and Access**
   - Register a researcher account
   - Verify researcher client ID is generated
   - Verify research dashboard loads

8. ✅ **Demographics Analytics**
   - Access demographics breakdown
   - Verify data displays as bar chart (not JSON)
   - Verify gender and goal breakdowns are correct

9. ✅ **Population Health Metrics**
   - Access population health data
   - Verify health metrics are displayed in formatted output
   - Verify numbers are properly formatted

10. ✅ **Multiple Researcher Instances**
    - Register two researchers in different tabs
    - Verify each tab maintains independent session
    - Verify both can access aggregate analytics simultaneously

**Cross-Client E2E Tests:**
11. ✅ **Mobile User + Researcher Data Consistency**
    - Register a mobile user
    - Access demographics as researcher
    - Verify researcher analytics include mobile user data in aggregate

**API Integration Tests:**
12. ✅ **API Error Responses**
    - Test request without client ID (expects 400)
    - Test request with invalid client ID (expects 400)
    - Verify error messages are user-friendly

For detailed step-by-step instructions, expected outcomes, and verification checklists, see **`docs/E2E_TESTING.md`**.

### Instructions for Third-Party Developers

Third-party developers can implement their own clients to interact with the Personal Fitness Management Service API.

**Authentication:**
- All authenticated endpoints require the `X-Client-ID` header
- Client IDs are obtained by registering a new profile via `POST /api/persons` (open endpoint)
- Client ID format: `<type>-<identifier>` where type is `mobile` or `research`
- Mobile clients can only access `/api/persons/*` endpoints
- Research clients can only access `/api/research/*` endpoints (mobile clients receive 403 Forbidden)

**Required Headers:**
```
X-Client-ID: <client-id>
Content-Type: application/json
```

**Base URL:**
- Local development: `http://localhost:8080`
- Production: `https://teamx-backend-118279583185.us-central1.run.app`

**Key Endpoints:**

**Mobile Client Endpoints:**
- `POST /api/persons` - Register new profile (open, returns client ID)
- `GET /api/persons/me` - Get current profile
- `PUT /api/persons/me` - Update profile
- `DELETE /api/persons/me` - Delete profile
- `GET /api/persons/bmi?weight=<kg>&height=<cm>` - Calculate BMI
- `GET /api/persons/calories?weight=<kg>&height=<cm>&age=<years>&gender=<MALE|FEMALE>&weeklyTrainingFreq=<1-14>` - Calculate daily calories
- `GET /api/persons/recommendation` - Get personalized recommendations

**Research Client Endpoints:**
- `GET /api/research/persons` - Get aggregated person counts
- `GET /api/research/demographics?ageRange=<optional>&gender=<optional>&objective=<optional>` - Get demographic statistics
- `GET /api/research/workout-patterns?ageRange=<optional>` - Get workout patterns
- `GET /api/research/nutrition-trends?objective=<optional>` - Get nutrition trends
- `GET /api/research/population-health` - Get population health metrics

**Request/Response Formats:**
- All requests and responses use JSON
- Date format: `YYYY-MM-DD` (e.g., `1995-03-18`)
- Gender values: `MALE`, `FEMALE`
- Goal values: `CUT`, `BULK`, `RECOVER`
- Status codes: 200 (OK), 201 (Created), 204 (No Content), 400 (Bad Request), 403 (Forbidden), 404 (Not Found), 500 (Internal Server Error)

**Error Responses:**
- All errors return JSON with `status`, `message`, and optional `errors` fields
- Missing `X-Client-ID` header returns 400 with error message
- Invalid client ID format returns 400
- Unauthorized access (mobile client accessing research endpoints) returns 403

**Complete API Documentation:**
- See **`docs/API_REFERENCE.md`** for full endpoint documentation
- Interactive API documentation available at `/swagger-ui/index.html` when service is running
- OpenAPI specification available at `/api-docs`

**Example Request (Register Profile):**
```bash
curl -X POST http://localhost:8080/api/persons \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Smith",
    "weight": 65.0,
    "height": 172.0,
    "birthDate": "1995-03-18",
    "gender": "FEMALE",
    "goal": "CUT"
  }'
```

**Example Response:**
```json
{
  "id": 1,
  "clientId": "mobile-abc123",
  "name": "Alice Smith",
  "weight": 65.0,
  "height": 172.0,
  "birthDate": "1995-03-18",
  "gender": "FEMALE",
  "goal": "CUT"
}
```

**Example Request (Get Profile - Authenticated):**
```bash
curl -X GET http://localhost:8080/api/persons/me \
  -H "X-Client-ID: mobile-abc123"
```

---

## Static Analysis

### Tools Used

The project uses the following static analysis tools:

- **Checkstyle 10.12.5**: Code style and formatting enforcement
  - Configuration: `checkstyle.xml` (based on Google Java Style)
  - Checks: Naming conventions, Javadoc requirements, code complexity, whitespace, imports, etc.

- **PMD 6.55.0**: Code quality and bug detection
  - Configuration: `pmd-ruleset.xml`
  - Rule categories: Best practices, code style, design, documentation, error-prone patterns, multithreading, performance

### How to Run Static Analysis Locally

**Checkstyle:**
```bash
mvn checkstyle:check
```

**PMD:**
```bash
mvn pmd:check
```

**Both (with tests):**
```bash
mvn clean test checkstyle:check pmd:check
```

**Using Docker:**
```bash
# Checkstyle only
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests /bin/sh -lc "mvn -e -B checkstyle:check && mkdir -p testresult/checkstyle && cp -f target/checkstyle-result.xml testresult/checkstyle/checkstyle-result.xml 2>/dev/null || true"

# PMD only
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests /bin/sh -lc "mvn -e -B pmd:check && mkdir -p testresult/pmd && { cp target/site/pmd.xml testresult/pmd/ 2>/dev/null || true; } && { cp target/site/pmd.html testresult/pmd/ 2>/dev/null || true; }"
```

### Report Locations

**Checkstyle Reports:**
- XML report: `target/checkstyle-result.xml`
- Archived report: `reports/checkstyle-result.xml`
- PDF summary: `reports/Personal Fitness Management Service – Checkstyle Results.pdf`

**PMD Reports:**
- XML report: `target/site/pmd.xml`
- HTML report: `target/site/pmd.html`
- Docker output: `testresult/pmd/` (when run via Docker)

**Before/After Reports:**
- Static analysis reports are stored in the `reports/` directory
- CI-generated reports are available in GitHub Actions artifacts

### Style Checking and CI

- **Checkstyle** is configured to run during the Maven `validate` phase
- **PMD** is configured to run during the Maven `verify` phase
- Both tools are executed automatically in the CI pipeline (see [Continuous Integration](#continuous-integration))
- Style checking is enforced via Checkstyle and CI - builds will show violations but do not fail (configured with `failsOnError: false` for iterative improvement)
- IDE formatters (IntelliJ IDEA with Google Java Style profile) help maintain consistency during development

### Bugs Fixed

Static analysis tools have identified and helped fix various code quality issues:

- **Checkstyle violations**: Fixed naming convention issues, added missing Javadoc, corrected import statements, fixed whitespace and formatting
- **PMD violations**: Addressed unused variables, simplified boolean expressions, fixed potential null pointer issues, improved code design patterns

**Evidence:**
- Checkstyle reports show zero violations for Iteration 2 codebase
- PMD reports document resolved issues and remaining warnings
- See `docs/STYLE_CHECK_SUMMARY.md` for detailed style check summary

---

## Unit Testing

### How to Run Unit Tests

**Local Execution:**
```bash
mvn clean test
```

**With Coverage Report:**
```bash
mvn clean test jacoco:report
# View report: target/site/jacoco/index.html
```

**Using Docker:**
```bash
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests
# Reports available in: testresult/unit/ and testresult/unit-coverage/jacoco/
```

### Test Frameworks

- **JUnit 5**: Test execution and assertions
- **Mockito**: Mocking dependencies and test doubles
- **Spring Test**: Integration with Spring Boot context
- **JaCoCo**: Code coverage measurement

### Equivalence Partitions and Boundary Testing

**Service Layer Tests (`PersonServiceTest`):**

**BMI Calculation:**
- **Inputs tested**: Weight (kg), Height (cm)
- **Equivalence classes**:
  - Valid positive numbers (normal range: 40-200 kg, 100-250 cm)
  - Boundary values: Minimum valid (0.1 kg, 1 cm), Maximum reasonable (500 kg, 300 cm)
  - Invalid: Zero or negative values, null values
- **Boundaries tested**:
  - Weight = 0 (invalid)
  - Weight = 0.1 (minimum valid)
  - Height = 0 (invalid)
  - Height = 1 (minimum valid)
  - Very large values (500 kg, 300 cm) for overflow handling

**BMR Calculation:**
- **Inputs tested**: Weight (kg), Height (cm), Age (years), Gender (MALE/FEMALE)
- **Equivalence classes**:
  - Valid age ranges: 1-120 years
  - Gender: MALE, FEMALE
  - Valid weight/height ranges
- **Boundaries tested**:
  - Age = 0 (invalid)
  - Age = 1 (minimum valid)
  - Age = 120 (maximum reasonable)
  - Age = 150 (extreme, should handle gracefully)

**Calorie Calculation:**
- **Inputs tested**: BMR, Weekly training frequency (1-14 days)
- **Equivalence classes**:
  - Sedentary (1 day/week)
  - Light activity (2-3 days/week)
  - Moderate activity (4-5 days/week)
  - High activity (6-7 days/week)
  - Very high activity (8-14 days/week)
- **Boundaries tested**:
  - Frequency = 0 (invalid)
  - Frequency = 1 (minimum)
  - Frequency = 14 (maximum)
  - Frequency = 15 (invalid, exceeds maximum)

**Security Layer Tests (`ClientIdInterceptorTest`):**

**Client ID Validation:**
- **Inputs tested**: Client ID header values
- **Equivalence classes**:
  - Valid format: `mobile-*`, `research-*`
  - Invalid format: Missing header, empty string, wrong prefix, malformed
- **Boundaries tested**:
  - Missing header (null)
  - Empty string
  - Invalid prefix (not `mobile-` or `research-`)
  - Valid format with various identifier lengths

**Controller Tests (`PersonControllerTest`):**

**CRUD Operations:**
- **Inputs tested**: Person creation/update requests, client IDs
- **Equivalence classes**:
  - Valid requests with all required fields
  - Invalid requests: Missing fields, invalid data types, out-of-range values
  - Client isolation: Different client IDs accessing same resource IDs
- **Boundaries tested**:
  - Empty name string
  - Negative weight/height
  - Future birth dates
  - Very old birth dates (age > 150)

**Integration Tests (`ClientIsolationIntegrationTest`):**

**Client Isolation:**
- **Inputs tested**: Multiple client IDs, shared resource IDs
- **Equivalence classes**:
  - Same client ID accessing own resources (allowed)
  - Different client ID accessing other's resources (forbidden)
  - Missing client ID (forbidden)
- **Boundaries tested**:
  - Concurrent requests from different clients
  - Client ID format validation

### Test Grouping

**Setup/Teardown:**
- `@BeforeEach` / `@AfterEach`: Initialize and clean up test data, reset mocks
- `@BeforeAll` / `@AfterAll`: One-time setup for expensive operations

**Mocks and Test Doubles:**
- **Repository mocks**: `@Mock PersonRepository` - Simulate database operations without actual DB
- **Service mocks**: Mock `PersonService` for controller tests
- **Context mocks**: Mock `ClientContext` for security tests

**Test Organization:**
- Unit tests: `src/test/java/com/teamx/fitness/service/`, `src/test/java/com/teamx/fitness/security/`
- Controller tests: `src/test/java/com/teamx/fitness/controller/`
- Integration tests: `src/test/java/com/teamx/fitness/integration/`
- Model tests: `src/test/java/com/teamx/fitness/model/`

**Test Categories:**
- `PersonServiceTest`: Business logic calculations
- `ClientIdInterceptorTest`: Security and validation
- `PersonControllerTest`: HTTP endpoint behavior
- `ClientIsolationIntegrationTest`: Multi-client scenarios
- `ResearchControllerTest`: Research endpoint access control

### CI Execution

Unit tests are automatically executed in the CI pipeline:
- **GitHub Actions workflow**: `.github/workflows/ci.yml`
- **Execution**: `mvn -B clean test` runs on every push and pull request
- **Coverage**: JaCoCo generates coverage reports during test execution
- **Reports**: Test results and coverage reports are available as CI artifacts

---

## API Testing

### How to Run API Tests

**Using Postman (GUI):**
1. Import collection: `postman/fitness-api-tests.postman_collection.json`
2. Import environment: `postman/fitness-api-tests.postman_environment.json`
3. Set `baseUrl` variable to `http://localhost:8080` (or your backend URL)
4. Run the collection

**Using Newman (CLI):**
```bash
# Install Newman (if not installed)
npm install -g newman

# Run collection
newman run postman/fitness-api-tests.postman_collection.json \
  -e postman/fitness-api-tests.postman_environment.json \
  --env-var baseUrl=http://localhost:8080 \
  --reporters cli,html,json \
  --reporter-html-export testresult/api/postman-report.html \
  --reporter-json-export testresult/api/postman-summary.json
```

**Using Docker:**
```bash
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm newman
# Report available at: testresult/api/postman-report.html
```

**Via Maven (with Docker):**
```bash
mvn verify
# This triggers Dockerized Newman execution
```

### API Endpoints Tested

**Personal Endpoints (`/api/persons`):**

1. **POST /api/persons** - Create Profile
   - **Input parameters**: `name` (string), `weight` (number), `height` (number), `birthDate` (YYYY-MM-DD), `gender` (MALE/FEMALE), `goal` (CUT/BULK/RECOVER)
   - **Expected output**: 201 Created with profile data including generated `clientId`
   - **Equivalence partitions**:
     - Normal: Valid data with all required fields
     - Boundary: Minimum/maximum valid values, edge dates
     - Invalid: Missing fields, invalid types, negative values, future dates

2. **GET /api/persons/me** - Get Current Profile
   - **Input parameters**: `X-Client-ID` header (required)
   - **Expected output**: 200 OK with profile data, 404 if no profile exists
   - **Equivalence partitions**:
     - Valid: Existing client ID with profile
     - Invalid: Missing header, invalid client ID, non-existent profile

3. **PUT /api/persons/me** - Update Profile
   - **Input parameters**: Same as POST, `X-Client-ID` header
   - **Expected output**: 200 OK with updated profile, 404 if no profile exists
   - **Equivalence partitions**:
     - Valid: All fields updated, partial updates
     - Invalid: Missing header, invalid data, non-existent profile

4. **DELETE /api/persons/me** - Delete Profile
   - **Input parameters**: `X-Client-ID` header
   - **Expected output**: 204 No Content, 404 if no profile exists
   - **Equivalence partitions**:
     - Valid: Existing profile deletion
     - Invalid: Missing header, non-existent profile

5. **GET /api/persons/bmi** - Calculate BMI
   - **Input parameters**: `weight` (kg), `height` (cm) as query parameters
   - **Expected output**: 200 OK with BMI value and category
   - **Equivalence partitions**:
     - Normal: Valid weight/height combinations
     - Boundary: Minimum (0.1 kg, 1 cm), maximum reasonable values
     - Invalid: Missing parameters, zero/negative values, non-numeric

6. **GET /api/persons/calories** - Calculate Daily Calories
   - **Input parameters**: `weight`, `height`, `age`, `gender`, `weeklyTrainingFreq` (query parameters)
   - **Expected output**: 200 OK with BMR and daily calories
   - **Equivalence partitions**:
     - Normal: Valid combinations
     - Boundary: Age 1-120, frequency 1-14, edge gender values
     - Invalid: Missing parameters, out-of-range values

7. **GET /api/persons/recommendation** - Get Recommendations
   - **Input parameters**: `X-Client-ID` header
   - **Expected output**: 200 OK with personalized recommendations
   - **Equivalence partitions**:
     - Valid: Client with profile and goal plan
     - Invalid: Missing header, no profile, no goal plan

**Research Endpoints (`/api/research`):**

8. **GET /api/research/persons** - Aggregated Persons
   - **Input parameters**: `X-Client-ID` header (must be `research-*`)
   - **Expected output**: 200 OK with anonymized counts, 403 for mobile clients
   - **Equivalence partitions**:
     - Valid: Research client ID
     - Invalid: Mobile client ID (403), missing header (400)

9. **GET /api/research/demographics** - Demographic Statistics
   - **Input parameters**: `X-Client-ID` header, optional `ageRange`, `gender`, `objective` query parameters
   - **Expected output**: 200 OK with demographic breakdowns, 403 for mobile clients
   - **Equivalence partitions**:
     - Valid: Research client with/without filters
     - Invalid: Mobile client (403), invalid filter values

10. **GET /api/research/workout-patterns** - Workout Patterns
    - **Input parameters**: `X-Client-ID` header, optional `ageRange` query parameter
    - **Expected output**: 200 OK with workout distribution data, 403 for mobile clients
    - **Equivalence partitions**: Same as demographics

11. **GET /api/research/nutrition-trends** - Nutrition Trends
    - **Input parameters**: `X-Client-ID` header, optional `objective` query parameter
    - **Expected output**: 200 OK with nutrition data, 403 for mobile clients
    - **Equivalence partitions**: Same as demographics

12. **GET /api/research/population-health** - Population Health
    - **Input parameters**: `X-Client-ID` header
    - **Expected output**: 200 OK with population health metrics, 403 for mobile clients
    - **Equivalence partitions**: Same as demographics

### Valid and Invalid Test Cases

**Valid Test Cases:**
- Normal inputs with all required fields
- Boundary values (minimum/maximum valid)
- Optional parameters omitted (where allowed)
- Multiple clients accessing their own data simultaneously
- Research clients accessing aggregate data

**Invalid Test Cases (Boundary and Error Handling):**
- Missing required headers (`X-Client-ID`)
- Invalid client ID format
- Missing required request body fields
- Invalid data types (string instead of number, etc.)
- Out-of-range values (negative weight, age > 150, etc.)
- Mobile clients accessing research endpoints (403)
- Research clients accessing personal endpoints (403)
- Non-existent resources (404)
- Future birth dates
- Invalid enum values (wrong gender/goal strings)

### API Tests Coverage

**Persistent Data:**
- Tests verify that created profiles persist in the database
- Tests verify that updates modify stored data
- Tests verify that deletions remove data from the database
- Tests verify client isolation (data created by one client is not accessible by another)

**Logging:**
- All API requests are logged with client ID, endpoint, method, and timestamp
- Tests verify that logging occurs for successful and failed requests
- Log files are checked to confirm request traces: `logs/fitness-app.log`

**Multiple Clients:**
- Tests create multiple client IDs and verify data isolation
- Tests verify concurrent requests from different clients
- Tests verify that research endpoints return aggregate data from all mobile clients
- Tests verify that mobile clients cannot access other mobile clients' data

**Test Statistics:**
- **Total requests**: 36
- **Total assertions**: 79
- **Coverage**: All active endpoints with normal, boundary, and invalid scenarios
- **Status**: All tests passing

---

## Integration Testing

### Definition of Integration

For this project, "integration" refers to testing the interaction between multiple components of the system:

1. **Service + Repository Layer**: Testing business logic with actual or mocked database interactions
2. **Controller + Service Integration**: Testing HTTP endpoints with service layer logic
3. **Database Integration**: Testing persistence and data retrieval with PostgreSQL
4. **Security Integration**: Testing client isolation and authentication across layers
5. **Cross-Component Integration**: Testing interactions between controllers, services, repositories, and security components

### Integration Tests

**1. Client Isolation Integration Tests (`ClientIsolationIntegrationTest`):**
- **Purpose**: Verify that client isolation is enforced across controller, service, and repository layers
- **Components tested**: `PersonController`, `PersonService`, `PersonRepository`, `ClientContext`
- **Scenarios**:
  - Same client ID accessing own resources (allowed)
  - Different client ID accessing other's resources (forbidden)
  - Client ID validation and context management
  - Concurrent requests from different clients

**2. Research Controller Integration Tests (`ResearchControllerTest`):**
- **Purpose**: Verify research endpoint access control and data aggregation
- **Components tested**: `ResearchController`, `PersonRepository`, `ClientContext`
- **Scenarios**:
  - Research clients accessing research endpoints (allowed)
  - Mobile clients accessing research endpoints (403 Forbidden)
  - Aggregate data calculation from multiple mobile client records
  - Demographic filtering and cohort size validation

**3. Database Integration:**
- **Purpose**: Verify data persistence and retrieval with PostgreSQL
- **Components tested**: `PersonRepository`, JPA entities, database schema
- **Scenarios**:
  - Create, read, update, delete operations
  - Client-scoped queries (filtering by client ID)
  - Data integrity and constraints
  - Transaction handling

**4. Service Layer Integration:**
- **Purpose**: Verify business logic with repository interactions
- **Components tested**: `PersonService`, `PersonRepository`
- **Scenarios**:
  - BMI calculation with persisted data
  - Calorie calculation with user profile and goal plan
  - Recommendation generation with multiple data sources

### How to Run Integration Tests

**Local Execution:**
```bash
# Run all tests (unit + integration)
mvn clean test

# Run specific integration test class
mvn test -Dtest=ClientIsolationIntegrationTest
```

**Using Docker:**
```bash
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests
# Integration tests are included in the test suite
```

**With Database:**
Integration tests use mocked repositories by default. For database integration testing:
1. Start PostgreSQL: `docker compose up -d` (from `database/` directory)
2. Set environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
3. Run tests: `mvn clean test -Dspring.profiles.active=postgres`

### CI Execution

Integration tests are automatically executed in the CI pipeline:
- **GitHub Actions workflow**: `.github/workflows/ci.yml`
- **Execution**: `mvn -B clean test` runs all tests including integration tests
- **Test isolation**: Each test run uses a clean database state
- **Reports**: Integration test results are included in Surefire reports: `testresult/unit/`

---

## Branch Coverage & Bug Fixing

### Coverage Report Locations

**Local Reports:**
- HTML report: `target/site/jacoco/index.html`
- XML report: `target/site/jacoco/jacoco.xml`
- CSV report: `target/site/jacoco/jacoco.csv`

**Docker-Generated Reports:**
- HTML report: `testresult/unit-coverage/jacoco/index.html`
- Reports are copied to `testresult/unit-coverage/` after Docker test execution

**CI Reports:**
- Coverage reports are generated during CI test execution
- Reports are available as GitHub Actions artifacts
- Coverage data is also stored in `ci-reports/coverage/` directory

### How to Regenerate Coverage Reports Locally

**Standard Maven:**
```bash
mvn clean test jacoco:report
# View report: open target/site/jacoco/index.html
```

**With Coverage Check:**
```bash
mvn clean test jacoco:check
# This will fail if coverage is below 80% line coverage
```

**Using Docker:**
```bash
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests
# Reports available at: testresult/unit-coverage/jacoco/index.html
```

**Coverage Threshold:**
- **Line Coverage**: Minimum 80% (configured in `pom.xml` JaCoCo plugin)
- **Branch Coverage**: Monitored and reported (target: ≥80%)

### Branch Coverage Statement

**Current Coverage Status:**
- **Line Coverage**: ≥80% (meets requirement)
- **Branch Coverage**: ≥80% (meets requirement)
- Coverage is enforced via JaCoCo Maven plugin with build failure if threshold is not met

**Coverage Configuration:**
- JaCoCo plugin configured in `pom.xml` with `minimum: 0.80` for line coverage
- Coverage check runs automatically during `mvn test` phase
- Reports include line, branch, instruction, and method coverage metrics

### Bugs Found and Fixed

**Documented Evidence:**

1. **Client Isolation Bug:**
   - **Found**: Initial implementation allowed clients to access other clients' data by resource ID
   - **Fixed**: Implemented client-scoped queries in repository layer, enforced in service and controller layers
   - **Evidence**: `ClientIsolationIntegrationTest` now passes, verifying data isolation

2. **BMI Calculation Edge Cases:**
   - **Found**: Division by zero not handled for zero height
   - **Fixed**: Added validation for zero/negative weight and height
   - **Evidence**: `PersonServiceTest` includes boundary tests for zero values

3. **Research Endpoint Access Control:**
   - **Found**: Mobile clients could access research endpoints
   - **Fixed**: Added client type validation in `ResearchController`
   - **Evidence**: `ResearchControllerTest` verifies 403 responses for mobile clients

4. **Null Pointer Exceptions:**
   - **Found**: Missing null checks in service layer calculations
   - **Fixed**: Added null validation and proper error handling
   - **Evidence**: Unit tests include null value test cases

5. **Date Validation:**
   - **Found**: Future birth dates were accepted
   - **Fixed**: Added birth date validation (must be in the past)
   - **Evidence**: API tests include invalid date scenarios

**Before/After Reports:**
- **Before reports**: Initial coverage was below 80%, multiple bugs identified via static analysis and testing
- **After reports**: Coverage meets ≥80% threshold, all identified bugs fixed
- **Reports location**: Coverage reports in `target/site/jacoco/` and `testresult/unit-coverage/jacoco/`
- **Test results**: See `docs/TESTING_RESULTS.md` for detailed testing outcomes

**Coverage Improvement:**
- Added unit tests for edge cases and boundary conditions
- Added integration tests for client isolation scenarios
- Improved test coverage for error handling paths
- Coverage increased from initial ~60% to current ≥80%

---

## Continuous Integration

### CI Pipeline Description

The project uses GitHub Actions for continuous integration. The CI pipeline executes the following steps on every push and pull request:

**Pipeline Steps:**

1. **Checkout Code**
   - Checks out the repository code

2. **Set up JDK 17**
   - Configures Java 17 using Temurin distribution
   - Sets up Maven cache for faster builds

3. **Compile & Unit Tests**
   - Runs `mvn -B clean test`
   - Executes all unit and integration tests
   - Generates JaCoCo coverage reports
   - Fails build if tests fail or coverage is below threshold

4. **Checkstyle**
   - Runs `mvn -B checkstyle:check`
   - Validates code style and formatting
   - Reports violations (non-blocking)

5. **PMD**
   - Runs `mvn -B pmd:check`
   - Performs static code analysis
   - Reports code quality issues (non-blocking)

6. **API Tests (Dockerized Newman)**
   - Starts application and database via Docker Compose
   - Runs Postman/Newman collection against the service
   - Validates all API endpoints with various scenarios
   - Generates HTML and JSON test reports

7. **Clean up Docker Services**
   - Always runs (even if previous steps fail)
   - Stops and removes Docker containers
   - Ensures clean environment for next run

### GitHub Actions Workflow Files

**Main CI Workflow:**
- **File**: `.github/workflows/ci.yml`
- **Triggers**: Push to any branch, pull requests to `main`
- **Jobs**: `build` (runs on `ubuntu-latest`)

**Workflow Configuration:**
```yaml
name: CI - Build, Test, Lint
on:
  pull_request:
    branches: ["main"]
  push:
    branches: ["**"]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - Checkout code
      - Set up JDK 17
      - Compile & unit tests
      - Checkstyle
      - PMD
      - API tests (Dockerized Newman)
      - Clean up Docker services
```

### CI Reports

**Recent CI Reports:**
- CI execution logs and artifacts are available in GitHub Actions
- Navigate to: Repository → Actions → Select workflow run → Artifacts
- Reports include:
  - Test results (Surefire reports)
  - Coverage reports (JaCoCo HTML)
  - Checkstyle reports (XML)
  - PMD reports (XML/HTML)
  - API test reports (Newman HTML)

**Local CI Reports:**
- Coverage reports: `ci-reports/coverage/` (if generated locally)
- Test results: `ci-reports/test/` (if generated locally)

**CI Status Badge:**
- Add a CI status badge to your README to show build status (optional):
  ```markdown
  ![CI](https://github.com/your-org/your-repo/workflows/CI/badge.svg)
  ```

---

## Cloud Deployment

### Deployed Backend URL

**Production URL**: https://teamx-backend-118279583185.us-central1.run.app

The service is deployed on Google Cloud Platform (GCP) Cloud Run, providing:
- Automatic scaling based on traffic
- HTTPS endpoint
- Managed infrastructure
- Health monitoring

### How to Redeploy

**Using Cloud Build (Recommended):**

1. **Prerequisites:**
   - Google Cloud SDK (`gcloud`) installed and authenticated
   - Cloud Build API enabled
   - Artifact Registry repository created
   - Cloud Run service configured

2. **Deploy Command:**
   ```bash
   gcloud builds submit --config cloudbuild.yaml \
     --substitutions=_REGION="us-central1",_REPO="fitness-repo",_SERVICE_NAME="fitness-service",_DB_URL="jdbc:postgresql://<DB_IP>:5432/fitnessdb",_DB_USERNAME="fitnessuser",_DB_PASSWORD="your-pass"
   ```

3. **Cloud Build Process:**
   - Builds Docker image from `Dockerfile`
   - Pushes image to Artifact Registry
   - Deploys to Cloud Run
   - Runs Postman/Newman collection against deployed URL
   - Reports deployment status

**Manual Deployment:**

1. **Build Docker Image:**
   ```bash
   docker build -t gcr.io/PROJECT_ID/fitness-service:latest .
   ```

2. **Push to Container Registry:**
   ```bash
   docker push gcr.io/PROJECT_ID/fitness-service:latest
   ```

3. **Deploy to Cloud Run:**
   ```bash
   gcloud run deploy fitness-service \
     --image gcr.io/PROJECT_ID/fitness-service:latest \
     --platform managed \
     --region us-central1 \
     --allow-unauthenticated
   ```

### Environment Variables Required in Cloud

**Database Configuration:**
- `DB_URL`: PostgreSQL connection string (e.g., `jdbc:postgresql://<DB_IP>:5432/fitnessdb`)
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password

**Application Configuration:**
- `SPRING_PROFILES_ACTIVE`: Set to `postgres` for production
- `SERVER_PORT`: Port for Cloud Run (typically 8080)

**Optional Configuration:**
- `USDA_API_KEY`: For external nutrition API (if used)
- `NUTRITIONIX_APP_ID`: For external nutrition API (if used)
- `NUTRITIONIX_API_KEY`: For external nutrition API (if used)

**Setting Environment Variables in Cloud Run:**
```bash
gcloud run services update fitness-service \
  --set-env-vars DB_URL="jdbc:postgresql://...",DB_USERNAME="...",DB_PASSWORD="..." \
  --region us-central1
```

**Security Note:**
- For production, use Google Secret Manager for sensitive credentials
- Reference secrets in Cloud Run: `--set-secrets DB_PASSWORD=db-password:latest`

### Access for Mentors/Testers During Iteration 2 Demo

**Backend Service:**
- **URL**: https://teamx-backend-118279583185.us-central1.run.app
- **Health Check**: https://teamx-backend-118279583185.us-central1.run.app/health
- **Swagger UI**: https://teamx-backend-118279583185.us-central1.run.app/swagger-ui/index.html
- **API Docs**: https://teamx-backend-118279583185.us-central1.run.app/api-docs

**Frontend Client:**
- Connect frontend to cloud backend by setting API URL: `?apiBaseUrl=https://teamx-backend-118279583185.us-central1.run.app`
- Or use the API Configuration panel in the client UI

**Testing:**
- All API endpoints are accessible without authentication (for demo purposes)
- Use Postman collection with `baseUrl` set to cloud URL
- Test multiple client instances by using different `X-Client-ID` headers

**Demo Checklist:**
1. Verify health endpoint responds
2. Register a mobile user via `POST /api/persons`
3. Access profile via `GET /api/persons/me` with returned client ID
4. Test BMI and calorie calculations
5. Register a research client and access research endpoints
6. Verify client isolation (different client IDs see different data)

---

## Final Entry Point Documentation

### Full API Documentation

Complete API documentation is available in **`docs/API_REFERENCE.md`**. Below is a summary of all endpoints:

### API Base URL

- **Local**: `http://localhost:8080`
- **Production**: `https://teamx-backend-118279583185.us-central1.run.app`

### Personal Endpoints (`/api/persons`)

**1. POST /api/persons** - Register New Profile
- **Method**: `POST`
- **Path**: `/api/persons`
- **Headers**: `Content-Type: application/json` (no `X-Client-ID` required for registration)
- **Request Body**:
  ```json
  {
    "name": "string",
    "weight": number,
    "height": number,
    "birthDate": "YYYY-MM-DD",
    "gender": "MALE" | "FEMALE",
    "goal": "CUT" | "BULK" | "RECOVER"
  }
  ```
- **Response**: `201 Created`
  ```json
  {
    "id": number,
    "clientId": "mobile-<identifier>",
    "name": "string",
    "weight": number,
    "height": number,
    "birthDate": "YYYY-MM-DD",
    "gender": "MALE" | "FEMALE",
    "goal": "CUT" | "BULK" | "RECOVER"
  }
  ```
- **Status Codes**: 201 (Created), 400 (Bad Request)

**2. GET /api/persons/me** - Get Current Profile
- **Method**: `GET`
- **Path**: `/api/persons/me`
- **Headers**: `X-Client-ID: mobile-<id>` (required)
- **Query Parameters**: None
- **Response**: `200 OK` with profile data, `404 Not Found` if no profile exists
- **Status Codes**: 200 (OK), 400 (Bad Request - missing/invalid header), 404 (Not Found)

**3. PUT /api/persons/me** - Update Profile
- **Method**: `PUT`
- **Path**: `/api/persons/me`
- **Headers**: `X-Client-ID: mobile-<id>` (required), `Content-Type: application/json`
- **Request Body**: Same as POST
- **Response**: `200 OK` with updated profile, `404 Not Found` if no profile exists
- **Status Codes**: 200 (OK), 400 (Bad Request), 404 (Not Found)

**4. DELETE /api/persons/me** - Delete Profile
- **Method**: `DELETE`
- **Path**: `/api/persons/me`
- **Headers**: `X-Client-ID: mobile-<id>` (required)
- **Response**: `204 No Content`, `404 Not Found` if no profile exists
- **Status Codes**: 204 (No Content), 400 (Bad Request), 404 (Not Found)

**5. GET /api/persons/bmi** - Calculate BMI
- **Method**: `GET`
- **Path**: `/api/persons/bmi`
- **Headers**: `X-Client-ID: mobile-<id>` (required)
- **Query Parameters**:
  - `weight` (required): Weight in kg (number)
  - `height` (required): Height in cm (number)
- **Response**: `200 OK`
  ```json
  {
    "weight": number,
    "height": number,
    "bmi": number,
    "category": "string"
  }
  ```
- **Status Codes**: 200 (OK), 400 (Bad Request - missing/invalid parameters)

**6. GET /api/persons/calories** - Calculate Daily Calories
- **Method**: `GET`
- **Path**: `/api/persons/calories`
- **Headers**: `X-Client-ID: mobile-<id>` (required)
- **Query Parameters**:
  - `weight` (required): Weight in kg (number)
  - `height` (required): Height in cm (number)
  - `age` (required): Age in years (number)
  - `gender` (required): "MALE" | "FEMALE"
  - `weeklyTrainingFreq` (required): Training frequency per week, 1-14 (number)
- **Response**: `200 OK`
  ```json
  {
    "bmr": number,
    "dailyCalories": number,
    "weeklyTrainingFreq": number
  }
  ```
- **Status Codes**: 200 (OK), 400 (Bad Request - missing/invalid parameters)

**7. GET /api/persons/recommendation** - Get Recommendations
- **Method**: `GET`
- **Path**: `/api/persons/recommendation`
- **Headers**: `X-Client-ID: mobile-<id>` (required)
- **Response**: `200 OK` with personalized recommendations
- **Status Codes**: 200 (OK), 400 (Bad Request), 404 (Not Found - no profile/plan)

### Research Endpoints (`/api/research`)

All research endpoints require `X-Client-ID: research-<id>` header. Mobile clients receive `403 Forbidden`.

**8. GET /api/research/persons** - Aggregated Persons
- **Method**: `GET`
- **Path**: `/api/research/persons`
- **Headers**: `X-Client-ID: research-<id>` (required)
- **Response**: `200 OK` with anonymized counts
- **Status Codes**: 200 (OK), 400 (Bad Request), 403 (Forbidden - mobile client)

**9. GET /api/research/demographics** - Demographic Statistics
- **Method**: `GET`
- **Path**: `/api/research/demographics`
- **Headers**: `X-Client-ID: research-<id>` (required)
- **Query Parameters** (all optional):
  - `ageRange`: Age range filter (string)
  - `gender`: Gender filter ("MALE" | "FEMALE")
  - `objective`: Goal filter ("CUT" | "BULK" | "RECOVER")
- **Response**: `200 OK` with demographic breakdowns
- **Status Codes**: 200 (OK), 400 (Bad Request), 403 (Forbidden)

**10. GET /api/research/workout-patterns** - Workout Patterns
- **Method**: `GET`
- **Path**: `/api/research/workout-patterns`
- **Headers**: `X-Client-ID: research-<id>` (required)
- **Query Parameters**: `ageRange` (optional)
- **Response**: `200 OK` with workout distribution data
- **Status Codes**: 200 (OK), 400 (Bad Request), 403 (Forbidden)

**11. GET /api/research/nutrition-trends** - Nutrition Trends
- **Method**: `GET`
- **Path**: `/api/research/nutrition-trends`
- **Headers**: `X-Client-ID: research-<id>` (required)
- **Query Parameters**: `objective` (optional: "CUT" | "BULK" | "RECOVER")
- **Response**: `200 OK` with nutrition data
- **Status Codes**: 200 (OK), 400 (Bad Request), 403 (Forbidden)

**12. GET /api/research/population-health** - Population Health
- **Method**: `GET`
- **Path**: `/api/research/population-health`
- **Headers**: `X-Client-ID: research-<id>` (required)
- **Response**: `200 OK` with population health metrics
- **Status Codes**: 200 (OK), 400 (Bad Request), 403 (Forbidden)

### Health and Documentation Endpoints

**GET /health** - Health Check
- **Method**: `GET`
- **Path**: `/health`
- **Headers**: None required
- **Response**: `200 OK` with service status

**GET /swagger-ui/index.html** - Swagger UI
- Interactive API documentation and testing interface

**GET /api-docs** - OpenAPI Specification
- Machine-readable API specification in OpenAPI 3.0 format

### Ordering Constraints Between API Calls

**Mobile Client Onboarding Sequence:**
1. `GET /health` - Verify service availability (optional)
2. `POST /api/persons` - Register profile and receive client ID (required first)
3. `GET /api/persons/me` - Verify profile was created (optional)
4. `PUT /api/persons/me` - Update profile if needed (optional)
5. `GET /api/persons/bmi` - Calculate BMI (requires profile or query parameters)
6. `GET /api/persons/calories` - Calculate calories (requires query parameters)
7. `GET /api/persons/recommendation` - Get recommendations (requires profile and goal plan)
8. `DELETE /api/persons/me` - Delete profile (cleanup, optional)

**Research Client Sequence:**
1. `GET /health` - Verify service availability (optional)
2. Register research client (via separate registration endpoint or manual client ID assignment)
3. `GET /api/research/persons` - Verify access and get totals
4. `GET /api/research/demographics` - Get demographic breakdowns
5. `GET /api/research/workout-patterns` - Get workout patterns
6. `GET /api/research/nutrition-trends` - Get nutrition trends
7. `GET /api/research/population-health` - Get overall health metrics

**Endpoints That Should NOT Be Called in Certain Orders:**
- **`GET /api/persons/me`** should NOT be called before `POST /api/persons` (will return 404)
- **`PUT /api/persons/me`** should NOT be called before `POST /api/persons` (will return 404)
- **`DELETE /api/persons/me`** should NOT be called before `POST /api/persons` (will return 404)
- **`GET /api/persons/recommendation`** should NOT be called without a profile and goal plan (will return 404)
- **Research endpoints** should NOT be called with mobile client IDs (will return 403)
- **Personal endpoints** should NOT be called without `X-Client-ID` header (will return 400)

### Configuration Files Included in Repo

**Application Configuration:**
- `src/main/resources/application.yml` - Spring Boot application configuration
  - Database connection settings
  - Server port (8080)
  - Logging configuration
  - OpenAPI/Swagger configuration
  - External API settings (USDA, Nutritionix)

**Build Configuration:**
- `pom.xml` - Maven project configuration
  - Dependencies (Spring Boot, PostgreSQL, Lombok, etc.)
  - Plugins (JaCoCo, Checkstyle, PMD, etc.)
  - Build settings

**Static Analysis Configuration:**
- `checkstyle.xml` - Checkstyle rules configuration
- `pmd-ruleset.xml` - PMD rules configuration

**Docker Configuration:**
- `Dockerfile` - Application container image definition
- `docker-compose.yml` - Application and database services
- `docker-compose.tests.yml` - Test services (Newman, unit tests)
- `database/docker-compose.yml` - Standalone database setup

**CI/CD Configuration:**
- `.github/workflows/ci.yml` - GitHub Actions CI workflow
- `cloudbuild.yaml` - Google Cloud Build configuration (if present)

**API Testing Configuration:**
- `postman/fitness-api-tests.postman_collection.json` - Postman API test collection
- `postman/fitness-api-tests.postman_environment.json` - Postman environment variables

**Database Migration:**
- `database/init/002_add_gender_column.sql` - Database migration script
- `database/init/003_add_goal_plan_columns.sql` - Database migration script

---

## Project Management

### Project Management Tool

**JIRA Board**: [COMS4156 Scrum Board](https://columbia-teamx-coms4156.atlassian.net/jira/software/projects/SCRUM/boards/1)

The team uses JIRA for task tracking, sprint planning, and progress monitoring.

### Team Task Distribution

**Iteration 1:**
- **Backend Development**: Core REST API endpoints, service layer, repository layer
- **Security Implementation**: Client ID interceptor, context management, access control
- **Database Design**: Schema design, migrations, persistence layer
- **Testing**: Unit tests, integration tests, API tests
- **Documentation**: API reference, architecture documentation

**Iteration 2:**
- **Frontend Development**: Web client implementation, mobile and research interfaces
- **Advanced Features**: Goal planning, recommendations, research analytics
- **Testing Enhancement**: E2E tests, improved coverage, bug fixes
- **Static Analysis**: Checkstyle and PMD integration, code quality improvements
- **CI/CD**: GitHub Actions workflow, automated testing
- **Cloud Deployment**: GCP Cloud Run deployment, environment configuration
- **Documentation**: Comprehensive README, testing documentation, deployment guides

### Work Tracking

**Iteration 1:**
- Tasks tracked in JIRA with user stories, tasks, and bugs
- Sprint planning and daily standups
- Completed: Core API, security, database, initial testing

**Iteration 2:**
- Tasks tracked in JIRA with focus on testing, quality, and deployment
- Sprint planning with emphasis on coverage and bug fixing
- Completed: Frontend client, E2E testing, static analysis, CI/CD, cloud deployment

**Current Status:**
- All Iteration 2 tasks completed
- Ready for Iteration 2 demo and evaluation

---

## Third-Party Code Disclosure

### External Libraries

The project uses the following external libraries and frameworks (managed via Maven in `pom.xml`):

**Spring Boot Ecosystem:**
- `spring-boot-starter-web` (3.2.0) - Web framework and REST API support
- `spring-boot-starter-data-jpa` (3.2.0) - JPA and database integration
- `spring-boot-starter-validation` (3.2.0) - Bean validation
- `spring-boot-starter-actuator` (3.2.0) - Health checks and metrics
- `spring-boot-starter-webflux` (3.2.0) - Reactive web client for external APIs
- `spring-boot-starter-test` (3.2.0) - Testing support

**Database:**
- `postgresql` (runtime) - PostgreSQL JDBC driver

**Code Quality:**
- `lombok` (1.18.30) - Reduces boilerplate code (getters, setters, constructors)

**API Documentation:**
- `springdoc-openapi-starter-webmvc-ui` (2.3.0) - OpenAPI/Swagger documentation

**Testing:**
- `junit-jupiter` - JUnit 5 test framework
- `mockito-core` - Mocking framework
- `rest-assured` (5.4.0) - API testing library

**JSON Processing:**
- `jackson-datatype-jsr310` - Java 8 time API support for JSON serialization

**Build Tools:**
- `maven-checkstyle-plugin` (3.3.1) with `checkstyle` (10.12.5)
- `maven-pmd-plugin` (3.21.2) with PMD (6.55.0)
- `jacoco-maven-plugin` (0.8.11) - Code coverage

**All dependencies are publicly available from Maven Central Repository and are used in accordance with their respective licenses (Apache 2.0, MIT, etc.).**

### Third-Party Code Copied into Project

**No third-party code has been copied directly into the project repository.**

All external code is managed through Maven dependencies. The project follows standard Java/Spring Boot practices and does not include any copied third-party source code.

**Configuration Files:**
- `checkstyle.xml` - Based on Google Java Style Guide (standard configuration, not copied code)
- `pmd-ruleset.xml` - Standard PMD ruleset configuration (not copied code)

**If any third-party code is added in the future:**
- Location will be documented in this section
- Source URL and license information will be provided
- Purpose and usage will be explained

---

## Build, Test, and Run

### Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Docker Desktop** (for containerized testing and database)
- **PostgreSQL** (optional, for local database)

### Build and Run

**Local Development:**
```bash
# Build
mvn clean compile

# Run
mvn spring-boot:run
```

**With PostgreSQL:**
```bash
# Windows PowerShell
$env:DB_URL="jdbc:postgresql://localhost:5432/fitnessdb"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
mvn spring-boot:run -Dspring-boot.run.profiles=postgres

# macOS/Linux
export DB_URL="jdbc:postgresql://localhost:5432/fitnessdb"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

**Using Docker:**
```bash
# Start services (app + PostgreSQL)
docker compose up -d --build

# Health check
curl http://localhost:8080/health
```

### Unit Tests and Coverage

```bash
# Run tests
mvn clean test

# Generate coverage report
mvn jacoco:report
# View: target/site/jacoco/index.html
```

### API Tests

**Using Postman:**
- Import collection: `postman/fitness-api-tests.postman_collection.json`
- Import environment: `postman/fitness-api-tests.postman_environment.json`
- Set `baseUrl` to `http://localhost:8080`
- Run collection

**Using Newman:**
```bash
newman run postman/fitness-api-tests.postman_collection.json \
  -e postman/fitness-api-tests.postman_environment.json \
  --env-var baseUrl=http://localhost:8080
```

**Using Docker:**
```bash
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm newman
```

### Additional Documentation

- **API Reference**: `docs/API_REFERENCE.md`
- **Architecture**: `docs/ARCHITECTURE.md`
- **Testing Results**: `docs/TESTING_RESULTS.md`
- **E2E Testing**: `docs/E2E_TESTING.md`
- **Style Check Summary**: `docs/STYLE_CHECK_SUMMARY.md`

---

## License

See `LICENSE` file for license information.
