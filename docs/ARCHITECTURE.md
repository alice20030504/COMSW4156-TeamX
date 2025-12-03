# Architecture Overview

## System Context

- **Personal Fitness Management Service** provides REST endpoints for fitness calculations and research analytics.
- Clients interact over HTTP using JSON payloads and must supply `X-Client-ID` headers for isolation.
- The application is deployed as a **Spring Boot 3.2.0** service running on **Java 17**.
- Frontend web interface provides interactive UI for mobile and research clients.
- Database persistence via **PostgreSQL** (local via Docker Compose or external instance).

## Core Modules

### 1. Application Entry Point
- **`FitnessManagementApplication.java`** - Spring Boot main class that bootstraps the application

### 2. Configuration (`config/`)
- **`GlobalExceptionHandler.java`** - Centralized exception handling for REST API errors
- **`OpenApiConfig.java`** - OpenAPI/Swagger configuration for API documentation
- **`WebMvcConfig.java`** - Web MVC configuration including interceptor registration

### 3. Controllers (`controller/`)
- **`PersonController.java`** - Personal client endpoints:
  - Profile CRUD operations (`POST /api/persons`, `GET /api/persons/me`, `PUT /api/persons/me`, `DELETE /api/persons/me`)
  - Goal plan configuration (`POST /api/persons/plan`)
  - BMI calculation (`GET /api/persons/bmi`)
  - Calorie calculation (`GET /api/persons/calories`)
  - Fitness recommendations (`GET /api/persons/recommendation`)
- **`ResearchController.java`** - Research client endpoints:
  - Researcher registration (`POST /api/research`)
  - Demographics analytics (`GET /api/research/demographics`)
  - Population health metrics (`GET /api/research/population-health`)
- **`HealthController.java`** - System health check endpoint (`GET /health`)
- **`HomeController.java`** - Root URL redirect to Swagger UI

#### DTOs (`controller/dto/`)
- **`PersonCreateRequest.java`** - Request DTO for creating a person profile
- **`PersonCreatedResponse.java`** - Response DTO for person creation
- **`PersonProfileResponse.java`** - Response DTO for person profile retrieval
- **`GoalPlanRequest.java`** - Request DTO for configuring goal plan
- **`ResearcherCreateRequest.java`** - Request DTO for researcher registration
- **`ResearcherCreatedResponse.java`** - Response DTO for researcher creation

### 4. Service Layer (`service/`)
- **`PersonService.java`** - Business logic for:
  - BMI calculations
  - BMR (Basal Metabolic Rate) calculations using Harris-Benedict equation
  - Calorie requirement calculations with activity multipliers
  - Goal-based calorie adjustments (CUT/BULK)
- **`HealthInsightService.java`** - Advanced health analytics:
  - Health index calculations
  - Plan alignment scoring
  - Cohort-based percentile calculations
  - Personalized fitness recommendations
- **`HealthInsightResult.java`** - Result DTO for health insights

### 5. Model (`model/`)
- **`PersonSimple.java`** - Entity representing a person profile with:
  - Basic info (name, weight, height, birthDate)
  - Gender and fitness goal (CUT/BULK)
  - Goal plan details (target change, duration, training frequency, strategy)
- **`Researcher.java`** - Entity representing a researcher profile
- **`Gender.java`** - Enum: `MALE`, `FEMALE`
- **`FitnessGoal.java`** - Enum: `CUT`, `BULK`
- **`PlanStrategy.java`** - Enum: `DIET_ONLY`, `WORKOUT_ONLY`, `BOTH`

### 6. Repository (`repository/`)
- **`PersonRepository.java`** - JPA repository for `PersonSimple` entity
- **`ResearcherRepository.java`** - JPA repository for `Researcher` entity

### 7. Security (`security/`)
- **`ClientIdInterceptor.java`** - Interceptor that:
  - Validates `X-Client-ID` header presence and format
  - Enforces client type restrictions (mobile vs research)
  - Stores client context in thread-local storage
- **`ClientContext.java`** - Thread-local context holder for current client ID

### 8. Logging (`logging/`)
- **`ApiLoggingInterceptor.java`** - Interceptor that logs all API requests:
  - Client ID, HTTP method, path
  - Response status and duration
  - Outputs to `logs/fitness-app.log`

## Data Flow

1. **Request Reception**: Client sends HTTP request with `X-Client-ID` header
2. **Interception**: `ClientIdInterceptor` validates header and stores in `ClientContext`
3. **Logging**: `ApiLoggingInterceptor` logs request details
4. **Routing**: Spring MVC routes to appropriate controller
5. **Business Logic**: Controller delegates to service layer for calculations
6. **Persistence**: Service uses repository to access/modify database
7. **Response**: JSON response serialized and returned
8. **Cleanup**: Interceptors clear thread-local context after completion

## External Integrations

- **SpringDoc OpenAPI** - Powers `/swagger-ui.html` and `/api-docs` for interactive API documentation
- **PostgreSQL** - Database for persistent storage (configured via `application.yml`)
- **Logback** - Logging framework configured via `application.yml` to capture structured output in `logs/fitness-app.log`
- **Docker Compose** - Local database setup via `database/docker-compose.yml`

## Frontend Architecture

The frontend is a static web application located in the `frontend/` directory:

- **`index.html`** - Landing page
- **`mobile.html`** - Mobile client interface
- **`mobile.js`** - Mobile client JavaScript logic
- **`research.html`** - Research client interface
- **`research.js`** - Research client JavaScript logic
- **`styles.css`** - Shared styling
- **`app.js`** - Shared utility functions

## Testing Architecture

### Unit Tests (`src/test/java/com/teamx/fitness/`)
- **`controller/`**:
  - `PersonControllerTest.java` - Controller unit tests with mocked services
  - `HealthControllerTest.java` - Health endpoint tests
  - `HomeControllerTest.java` - Home redirect tests
- **`service/`**:
  - `PersonServiceTest.java` - Business logic tests for BMI, BMR, calorie calculations
  - `HealthInsightServiceTest.java` - Health insight calculation tests
- **`model/`**:
  - `FitnessGoalTest.java`, `GenderTest.java`, `PlanStrategyTest.java` - Enum validation tests
- **`security/`**:
  - `ClientIdInterceptorTest.java` - Interceptor validation tests
  - `ClientContextTest.java` - Context management tests
- **`config/`**:
  - `GlobalExceptionHandlerTest.java` - Exception handling tests
  - `OpenApiConfigTest.java` - OpenAPI configuration tests
- **`logging/`**:
  - `ApiLoggingInterceptorTest.java` - Logging interceptor tests

### Integration Tests (`src/test/java/com/teamx/fitness/integration/`)
- **`ClientIsolationIntegrationTest.java`** - Multi-client isolation verification
- **`ResearchControllerTest.java`** - Research endpoint integration tests

### API Tests (`postman/`)
- **`fitness-api-tests.postman_collection.json`** - Postman collection with 36 requests
- **`fitness-api-tests.postman_environment.json`** - Environment variables for testing
- Tests verify:
  - All endpoints (normal, boundary, invalid cases)
  - Data persistence and isolation
  - Multi-client scenarios
  - Error handling

## Build and Deployment

### Build Tools
- **Maven** (`pom.xml`) - Dependency management and build configuration
- **Java 17** - Runtime and compilation target
- **Spring Boot 3.2.0** - Framework version

### Static Analysis
- **Checkstyle 10.12.5** (`checkstyle.xml`) - Code style enforcement (Google Java Style)
- **PMD 6.55.0** (`pmd-ruleset.xml`) - Code quality analysis
- **JaCoCo** - Code coverage reporting

### Docker
- **`Dockerfile`** - Application containerization
- **`docker-compose.yml`** - Main service orchestration
- **`docker-compose.tests.yml`** - Test environment configuration
- **`database/docker-compose.yml`** - PostgreSQL database setup
- **`database/init/`** - Database initialization scripts:
  - `002_add_gender_column.sql`
  - `003_add_goal_plan_columns.sql`

### CI/CD
- Test results stored in `testresult/`:
  - `api/` - Postman test reports
  - `checkstyle/` - Style check results
  - `pmd/` - PMD analysis reports
  - `unit-coverage/` - JaCoCo coverage reports

## Repository Layout

```
COMSW4156-TeamX/
├── README.md                          # Main project documentation
├── LICENSE                            # Project license
├── pom.xml                            # Maven build configuration
├── Dockerfile                         # Application Docker image
├── docker-compose.yml                 # Main Docker Compose configuration
├── docker-compose.tests.yml           # Test environment Docker Compose
├── checkstyle.xml                     # Checkstyle configuration
├── pmd-ruleset.xml                   # PMD rules configuration
├── DockerCommandInstruction.md       # Docker usage instructions
│
├── docs/                              # Documentation
│   ├── AI_USAGE.md                   # AI tool usage log
│   ├── ARCHITECTURE.md               # This file
│   ├── API_REFERENCE.md              # Complete API documentation
│   ├── E2E_TESTING.md                # End-to-end testing guide
│   ├── STYLE_CHECK_SUMMARY.md        # Static analysis summary
│   └── TESTING_RESULTS.md            # Test coverage and results
│
├── frontend/                          # Frontend web application
│   ├── index.html                    # Landing page
│   ├── landing.html                  # Landing page variant
│   ├── mobile.html                   # Mobile client UI
│   ├── mobile.js                     # Mobile client logic
│   ├── research.html                 # Research client UI
│   ├── research.js                   # Research client logic
│   ├── app.js                        # Shared utilities
│   ├── styles.css                    # Shared styles
│   └── README.md                     # Frontend documentation
│
├── postman/                           # API testing
│   ├── fitness-api-tests.postman_collection.json
│   └── fitness-api-tests.postman_environment.json
│
├── database/                          # Database configuration
│   ├── docker-compose.yml            # PostgreSQL setup
│   ├── init/                         # Database initialization scripts
│   │   ├── 002_add_gender_column.sql
│   │   └── 003_add_goal_plan_columns.sql
│   ├── data/                         # Database data directory (gitignored)
│   └── README.md                     # Database documentation
│
├── src/
│   ├── main/
│   │   ├── java/com/teamx/fitness/
│   │   │   ├── FitnessManagementApplication.java
│   │   │   │
│   │   │   ├── config/               # Configuration classes
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── WebMvcConfig.java
│   │   │   │
│   │   │   ├── controller/          # REST controllers
│   │   │   │   ├── PersonController.java
│   │   │   │   ├── ResearchController.java
│   │   │   │   ├── HealthController.java
│   │   │   │   ├── HomeController.java
│   │   │   │   └── dto/             # Data Transfer Objects
│   │   │   │       ├── PersonCreateRequest.java
│   │   │   │       ├── PersonCreatedResponse.java
│   │   │   │       ├── PersonProfileResponse.java
│   │   │   │       ├── GoalPlanRequest.java
│   │   │   │       ├── ResearcherCreateRequest.java
│   │   │   │       └── ResearcherCreatedResponse.java
│   │   │   │
│   │   │   ├── service/              # Business logic
│   │   │   │   ├── PersonService.java
│   │   │   │   ├── HealthInsightService.java
│   │   │   │   └── HealthInsightResult.java
│   │   │   │
│   │   │   ├── model/               # Domain models
│   │   │   │   ├── PersonSimple.java
│   │   │   │   ├── Researcher.java
│   │   │   │   ├── Gender.java
│   │   │   │   ├── FitnessGoal.java
│   │   │   │   └── PlanStrategy.java
│   │   │   │
│   │   │   ├── repository/          # Data access
│   │   │   │   ├── PersonRepository.java
│   │   │   │   └── ResearcherRepository.java
│   │   │   │
│   │   │   ├── security/           # Security and interceptors
│   │   │   │   ├── ClientIdInterceptor.java
│   │   │   │   └── ClientContext.java
│   │   │   │
│   │   │   └── logging/             # Logging interceptors
│   │   │       └── ApiLoggingInterceptor.java
│   │   │
│   │   └── resources/
│   │       └── application.yml     # Application configuration
│   │
│   └── test/
│       └── java/com/teamx/fitness/
│           ├── config/              # Configuration tests
│           │   ├── GlobalExceptionHandlerTest.java
│           │   └── OpenApiConfigTest.java
│           │
│           ├── controller/          # Controller tests
│           │   ├── PersonControllerTest.java
│           │   ├── HealthControllerTest.java
│           │   └── HomeControllerTest.java
│           │
│           ├── service/            # Service tests
│           │   ├── PersonServiceTest.java
│           │   └── HealthInsightServiceTest.java
│           │
│           ├── model/              # Model tests
│           │   ├── FitnessGoalTest.java
│           │   ├── GenderTest.java
│           │   └── PlanStrategyTest.java
│           │
│           ├── security/           # Security tests
│           │   ├── ClientIdInterceptorTest.java
│           │   └── ClientContextTest.java
│           │
│           ├── logging/            # Logging tests
│           │   └── ApiLoggingInterceptorTest.java
│           │
│           └── integration/       # Integration tests
│               ├── ClientIsolationIntegrationTest.java
│               └── ResearchControllerTest.java
│
├── target/                          # Build output (gitignored)
│   ├── classes/                     # Compiled classes
│   ├── test-classes/                # Compiled test classes
│   ├── fitness-management-service-1.0.0-SNAPSHOT.jar
│   └── checkstyle-result.xml
│
├── testresult/                      # Test results and reports
│   ├── api/                         # API test results
│   │   ├── postman-report.html
│   │   └── postman-summary.json
│   ├── checkstyle/                  # Style check results
│   │   └── checkstyle-result.xml
│   ├── pmd/                         # PMD analysis results
│   │   └── pmd.html
│   └── unit-coverage/               # Code coverage reports
│       └── jacoco/
│           └── index.html
│
└── logs/                            # Application logs (gitignored)
    └── fitness-app.log
```

## Key Design Patterns

1. **Layered Architecture**: Clear separation between controllers, services, and repositories
2. **DTO Pattern**: Request/Response DTOs for API boundaries
3. **Interceptor Pattern**: Cross-cutting concerns (authentication, logging) via interceptors
4. **Repository Pattern**: Data access abstraction via Spring Data JPA
5. **Service Layer Pattern**: Business logic encapsulation in service classes
6. **Thread-Local Context**: Client context stored in thread-local for request-scoped data

## Technology Stack

- **Backend**: Spring Boot 3.2.0, Java 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, Postman/Newman
- **Static Analysis**: Checkstyle, PMD, JaCoCo
- **Containerization**: Docker, Docker Compose
- **Frontend**: Vanilla JavaScript, HTML5, CSS3
