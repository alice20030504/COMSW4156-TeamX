# Personal Fitness Management Service

## Overview
Spring Boot REST API serving **mobile** and **research** clients.

- **Mobile clients** manage personal fitness data, including BMI/BMR/calorie metrics.  
- **Research clients** access anonymized population-level analytics (demographics, workout, nutrition).  
- **Client isolation** is enforced by validating user ID and birth date (see `authService`).  
- **Iteration 1 Focus:**  
  Secure CRUD operations, fitness calculators, analytics endpoints, and full OpenAPI-powered documentation.

For detailed architecture, refer to [`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md).

---

##  API Documentation Overview

All API endpoints are fully documented under the [`/docs`](./docs) directory, grouped by purpose:

| Document | Description | Linked Sections |
|-----------|--------------|-----------------|
| [`API_REFERENCE.md`](./docs/API_REFERENCE.md) | Complete REST API reference for all endpoints. Describes **functionality, input/output JSON formats, example requests/responses**, and **status/error codes**. | <ul><li>`/api/person` — create, update, get, delete</li><li>`/api/health` — perform service health check</li><li>`/api/bmi` and `/api/calories` — fitness calculations</li><li>`/api/research/*` — analytics for research clients</li></ul> |
| [`TESTING_RESULTS.md`](./docs/TESTING_RESULTS.md) | Contains **unit, integration, and API test coverage**. Explains test methodology (JUnit, MockMvc, Postman) and links to **Jacoco reports**. | <ul><li>Integration testing</li><li>Boundary condition coverage</li><li>Error handling tests</li></ul> |
| [`STYLE_CHECK_SUMMARY.md`](./docs/STYLE_CHECK_SUMMARY.md) | Summarizes **Checkstyle and coding standards compliance**. Includes rule references (Google Java Style) and **zero-violation verification**. | <ul><li>Line length and Javadoc conventions</li><li>Operator wrapping rules</li></ul> |
| [`ARCHITECTURE.md`](./docs/ARCHITECTURE.md) | High-level project design and UML diagrams. Describes **layered architecture** (`controller`, `service`, `repository`, `model`, `config`) and **data flow** between mobile and research modules. | <ul><li>System context and flow</li><li>Authentication validation</li><li>Controller-service interaction</li></ul> |
| [`AI_USAGE.md`](./docs/AI_USAGE.md) | Documents the usage of **ChatGPT (Cursor)** for documentation formatting, code style checking, and test verification. | <ul><li>AI-assisted documentation</li><li>Auto-generated Swagger UI</li></ul> |

Each document is directly accessible via hyperlink above, ensuring **traceability from every API endpoint to its explanation and behavior**.

---
##  Build, Test, and Run

**Prerequisites:**
- Java 17+
- Maven 3.8+
- Optional Node.js 18+ (for Postman CLI testing via Newman)

```bash
mvn clean compile     # Build
mvn spring-boot:run   # Run
```
Swagger UI will be available at:
👉 http://localhost:8080/swagger-ui/index.html

## Unit Test
```bash
mvn clean test # Unit Test
mvn jacoco:report # Test Coverage Report
```
Unit test and coverage results are summarized in  [`TESTING_RESULTS.md`](./docs/TESTING_RESULTS.md)
which includes explanations of:
- Branch and line coverage
- Mocked and parameterized test scenarios
- Coverage snapshots and interpretation

## API Test

Load fitness-api-tests.postman_collection.json and fitness-api-tests.postman_environment.json files from postman/ to Postman for API testing

Configuration files: 
[`pom.xml`](./pom.xml), 
[`src/main/resources/application.yml`](./src/main/resources/application.yml), 
[`data.sql`](./src/main/resources/data.sql).


## Testing

Frameworks and configuration locations:
- **Unit and integration:** JUnit 5, Mockito, Spring Test (configured via [`pom.xml`](./pom.xml), executed with Maven Surefire `mvn test`).
- **System/API:** Postman collection executed with Newman (see [`docs/API_REFERENCE.md`](./docs/API_REFERENCE.md), assets in [`postman/`](./postman/)).
- **Coverage:** JaCoCo reports 69% branch and 68% line coverage (snapshot [`reports/test-coverage-10202025.png`](./reports/test-coverage-10202025.png); narrative in [`docs/TESTING_RESULTS.md`](./docs/TESTING_RESULTS.md)).
- **Parameterized and mocked tests** cover typical, boundary, and invalid scenarios for each major unit.

## Style and Quality

- **Style checker:** Checkstyle 10.12.5 using [`checkstyle.xml`](./checkstyle.xml) (Google Java Style base).  
  Summary available in [`docs/STYLE_CHECK_SUMMARY.md`](./docs/STYLE_CHECK_SUMMARY.md).  
  Latest report [`reports/checkstyle-result.xml`](./reports/checkstyle-result.xml) shows zero violations.

- **Logging:** SLF4J + Spring Boot configuration in [`application.yml`](./src/main/resources/application.yml),  
  writing to console and [`logs/fitness-app.log`](./logs/fitness-app.log).


## Tooling Summary

- **Runtime stack:** Spring Boot, Spring Data JPA, SpringDoc OpenAPI.  
- **Persistence:** H2 (development), Hibernate; PostgreSQL driver.  
- **Build/test:** Maven, JUnit 5, Mockito, Newman, JaCoCo.  
- **Custom OpenAPI configuration:** [`src/main/java/com/teamx/fitness/config/OpenApiConfig.java`](./src/main/java/com/teamx/fitness/config/OpenApiConfig.java)  
  configures the third-party SpringDoc OpenAPI library so the generated Swagger UI enforces the `X-Client-ID` header while leveraging SpringDoc APIs only.  
- **Dependencies:** Declared in [`pom.xml`](./pom.xml) and fetched from Maven Central; no vendored third-party source code.

---

## Bug Fixes and TODOs

### Fixed
- **Guarded BMI calculation** against zero height to prevent division errors.  
- **Cleared `ClientContext`** after interceptor execution to avoid cross-request leakage.

---

### TODO (Planned Enhancements)
- **Nutrition Module Integration:**  
  Extend the existing personal metrics (BMI, BMR, calorie) with a **Nutrition Recommendation module**,  
  providing adaptive meal plans and macro-nutrient ratios tailored to user objectives (e.g., fat loss, muscle gain, weight maintenance).

- **PostgreSQL Deployment:**  
  Migrate from the embedded **H2 development database** to a **PostgreSQL** backend for persistent and scalable data analytics.

- **Error Handling Coverage:**  
  Add enhanced exception mappings and negative test coverage for future **Spring Security** integration.



## Project Management
- Tool: JIRA([COMS4156 Scrum Board](https://columbia-teamx-coms4156.atlassian.net/jira/software/projects/SCRUM/boards/1)).

## AI Usage
Documented in `docs/AI_USAGE.md` (ChatGPT used for documentation formatting and test verification).

## Dependencies
Managed through Maven (`pom.xml`) with resolved artifacts from Maven Central. Dependency tables and licenses are summarized in the README.

## Tags
- **Iteration 1 focus:** Designing and integrating **secure and meaningful data operations** (beyond basic CRUD),  
  implementing **fitness calculators**, **research dashboards**, and **SpringDoc-powered API documentation**.


