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

**For complete documentation on building, running, testing locally, and connecting to the GCP-deployed backend server, see [`frontend/README.md`](../frontend/README.md).**

---

## 3. Static Analysis & Style Checker

**Please refer to [`docs/STYLE_CHECK_SUMMARY`](../docs/STYCLE_CHECK_SUMMARY.md)
---

## 4. Unit Testing, API Testing, Integration Testing

**Please refer to [`docs/STYLE_CHECK_SUMMARY`](../docs/TESTING_RESULTS.md)
---

## 5. CI Execution Overview
Our GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every pull request targeting `main` and on all pushes. The job executes on `ubuntu-latest` and uses JDK 17 (Temurin distribution) via `actions/setup-java@v4`.

## Stages & Commands
1. **Checkout** – `actions/checkout@v4` pulls the repository contents.
2. **JDK Setup** – `actions/setup-java@v4` installs Temurin 17 and enables Maven caching.
3. **Unit Tests & Build** – `mvn -B clean test` compiles the project and runs the test suite (Surefire reports under `target/surefire-reports`).
4. **Checkstyle** – `mvn -B checkstyle:check` enforces Google-style formatting; failures surface directly in the Actions log and can be inspected via `target/site/checkstyle.html` if downloaded.
5. **PMD** – `mvn -B pmd:check` executes the PMD ruleset. Results are written to `target/site/pmd.html` and `target/pmd.xml` when you collect artifacts locally.

## Notes
- No Docker services run inside CI; the workflow relies on Maven alone.
- API/Newman regression tests remain outside the workflow because spinning up the dockerized Newman runner causes excessive wait times on GitHub-hosted runners. Those tests run on-demand using `docker compose ... run --rm newman` in local/QA environments.
- CI runs in parallel for multiple pushes but fails fast if any Maven goal returns non-zero.
- Artifacts (Surefire XML, Checkstyle/PMD reports) are available locally by rerunning the same Maven commands; we currently don’t upload them as CI artifacts.

---

## 6. Branch Coverage & Bug Fixing

### Coverage Report Location

Coverage reports are stored at: **[`testresult/unit-coverage/jacoco/index.html`](testresult/unit-coverage/jacoco/index.html)**

Open the HTML file in a web browser to view detailed coverage metrics by package, class, and method.

### API Regression Tests

Postman/Newman runs write their HTML and JSON summaries to **[`testresult/api/postman-report.html`](testresult/api/postman-report.html)** (plus raw logs under `testresult/api/postman-summary.json`). Open the HTML report to inspect pass/fail status, response assertions, and run metadata for each endpoint.

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


## 7. Cloud Deployment

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

## 8. Final Entry Point Documentation

For complete API documentation including all endpoints, request/response formats, parameters, and detailed specifications, see:

- **Detailed API Reference**: [`docs/API_REFERENCE.md`](docs/API_REFERENCE.md)
- **Swagger UI**: `http://34.30.81.33:8080/swagger-ui.html` (when service is running)
- **Frontend Client Examples**: [`frontend/README.md`](frontend/README.md) - Includes curl examples for all endpoints

### Calling Sequence Requirements

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

### Endpoints That Should NOT Be Called in Certain Orders

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

## 9. Project Management

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

## 10. Third-Party Code Disclosure

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

## 11. Project Proposal Implementation Status

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

