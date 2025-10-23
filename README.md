# Personal Fitness Management Service

## Overview
- Spring Boot REST API serving mobile and research clients.
- Mobile clients manage person records and derive BMI/BMR/calorie metrics.
- Research clients access anonymized population analytics dashboards.
- Client isolation enforced via the `X-Client-ID` header. Architecture details: `docs/ARCHITECTURE.md`.
- Iteration 1 focus: secure CRUD operations, fitness calculators, research dashboards, and SpringDoc-powered API documentation.

## API Reference
Full endpoint definitions, inputs/outputs, status codes, and recommended call sequences (including suggested ordering for mobile and research onboarding flows) are documented in `docs/API_REFERENCE.md`.

## Build, Test and Run
Prerequisites: Java 17+, Maven 3.8+, optional Node.js 18+ (for Newman-based API tests).
```bash
mvn clean compile # Build
mvn spring-boot:run # Run
```
## Unit Test
```bash
mvn clean test # Unit Test
mvn jacoco:report # Test Coverage Report
```
## API Test

Load fitness-api-tests.postman_collection.json and fitness-api-tests.postman_environment.json files from postman/ to Postman for API testing

Configuration files: `pom.xml`, `src/main/resources/application.yml`, `application-prod.yml`, `data.sql`.

## Testing
Frameworks and configuration locations:
- Unit and integration: JUnit 5, Mockito, Spring Test (configured via `pom.xml`, executed with Maven Surefire `mvn test`).
- System/API: Postman collection executed with Newman (`docs/API_REFERENCE.md`, assets in `postman/`).
- Coverage: JaCoCo reports 69% branch and 68% line coverage (snapshot `reports/test-coverage-10202025.png`; narrative in `docs/TESTING_RESULTS.md`).
- Parameterized and mocked tests cover typical, boundary, and invalid scenarios for each major unit.

## Style and Quality
- Style checker: Checkstyle 10.12.5 using `checkstyle.xml` (Google Java Style base). (summary in `docs/STYLE_CHECK_SUMMARY.md`). Latest report (`reports/checkstyle-result.xml`) shows zero violations.
- Static analysis: PMD configured in `pom.xml` 
- Logging: SLF4J + Spring Boot configuration in `application.yml`, writing to console and `logs/fitness-app.log`.

## Tooling Summary
- Runtime stack: Spring Boot, Spring Data JPA, SpringDoc OpenAPI.
- Persistence: H2 (development), Hibernate; PostgreSQL driver.
- Build/test: Maven, JUnit 5, Mockito, Newman, JaCoCo.
- Custom OpenAPI configuration: `src/main/java/com/teamx/fitness/config/OpenApiConfig.java` configures the third-party SpringDoc OpenAPI library so the generated Swagger UI enforces the `X-Client-ID` header while leveraging SpringDoc APIs only.
- Dependencies declared in `pom.xml` and fetched from Maven Central; no vendored third-party source code.

## Bug Fixes and TODOs
- Fixed: Guarded BMI calculation against zero height to prevent division errors.
- Fixed: Cleared `ClientContext` after interceptor execution to avoid cross-request leakage.
- TODO: Harden research analytics queries once PostgreSQL integration is in place.
- TODO: Add error-handling coverage for future Spring Security integration.

## Project Management
- Tool: Jira ([JIRA](https://columbia-teamx-coms4156.atlassian.net/jira/software/projects/SCRUM/boards/1)).

## AI Usage
Documented in `docs/AI_USAGE.md` (ChatGPT used for documentation formatting and test verification).

## Dependencies
Managed through Maven (`pom.xml`) with resolved artifacts from Maven Central. Dependency tables and licenses are summarized in the README.

## Tags
`iteration-1` marks the Iteration 1 submission snapshot.

