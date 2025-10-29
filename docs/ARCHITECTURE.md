# Architecture Overview

## System Context
- Personal Fitness Management Service provides REST endpoints for fitness calculations and research analytics.
- Clients interact over HTTP using JSON payloads and must supply `X-Client-ID` headers for isolation.
- The application is deployed as a Spring Boot service running on Java 17.

## Core Modules
1. Configuration
   - Bootstrapped by `FitnessManagementApplication`.
   - OpenAPI configuration exposes Swagger UI for interactive exploration.
2. Controllers
   - `PersonController` serves client-facing endpoints for BMI, calorie needs, CRUD actions, and health checks.
   - `ResearchController` delivers anonymized analytics reserved for research clients.
3. Service Layer
   - `PersonService` calculates BMI, BMR, and calorie requirements.
   - `AuthService` validates identity using repository lookups and encapsulates unauthorized responses.
4. Security
   - `ClientIdInterceptor` enforces presence and format of `X-Client-ID` headers and stores context in `ClientContext`.
5. Persistence
   - `PersonRepository` is a Spring Data JPA repository backed by an H2 database (development) or PostgreSQL (production).

## Data Flow
1. Client sends HTTP request with `X-Client-ID`.
2. Interceptor validates the header and assigns it to `ClientContext`.
3. Controller delegates to services and repositories to process the request.
4. Responses are serialized as JSON and returned to the caller; the interceptor clears the context after completion.

## External Integrations
- SpringDoc OpenAPI powers `/swagger-ui.html` and `/api-docs` for documentation.
- Logging is configured via `application.yml` to capture structured output in `logs/fitness-app.log`.

## Testing Hooks
- Unit tests mock services and repositories to validate controller logic.
- Integration and system tests use Postman/Newman to hit HTTP endpoints and verify multi-client isolation, persistence, and error handling.

## Repository Layout (simplified)

```
COMSW4156-TeamX/
  README.md
  pom.xml
  docs/
    AI_USAGE.md
    ARCHITECTURE.md
    STYLE_CHECK_SUMMARY.md
    TESTING_RESULTS.md
    API_REFERENCE.md
  postman/
    fitness-api-tests.postman_collection.json
    fitness-api-tests.postman_environment.json
  reports/
    checkstyle-result.xml
    postman-newman-results.json
    test-coverage
  src/
    main/
      java/com/teamx/fitness/
        FitnessManagementApplication.java
        config/
        controller/
        repository/
        security/
        service/
      resources/
        application.yml
        data.sql
    test/
      java/com/teamx/fitness/
        controller/
        integration/
        security/
        service/
  .gitignore
```

