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

### Docker-based API tests and coverage

- Unit tests (local):
  - `mvn clean test`
- Start app with coverage + DB (Docker):
  - `docker compose -f docker-compose.yml -f docker-compose.coverage.yml up -d --build`
- Run Postman tests (Docker; writes HTML report):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml up --abort-on-container-exit --build newman`
  - Report: `postman/postman-report.html`
- Stop containers (keep coverage file):
  - `docker compose down`
- Merge unit + API coverage and generate a single report:
  - `mvn org.jacoco:jacoco-maven-plugin:0.8.11:merge -Djacoco.destFile=target/jacoco-merged.exec -Djacoco.dataFileList="target/jacoco.exec,coverage/jacoco-it.exec"`
  - `mvn org.jacoco:jacoco-maven-plugin:0.8.11:report -Djacoco.dataFile=target/jacoco-merged.exec`
  - Open: `target/site/jacoco/index.html`

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



### Run with PostgreSQL (persistent)

Minimal PostgreSQL configuration is provided via the `postgres` Spring profile.

1) Ensure PostgreSQL is running and accessible.
   - Defaults used if env vars unset: `DB_URL=jdbc:postgresql://localhost:5432/fitnessdb`, `DB_USERNAME=postgres`, `DB_PASSWORD=postgres`.

2) Run the app with the profile and optional env vars:

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

The `postgres` profile uses `ddl-auto=update` to create/update tables automatically, and persists data between restarts.

### Run with Docker (App + Postgres)

This spins up both PostgreSQL and the app in containers.

1) Install and start Docker Desktop, then verify:
   - `docker --version`
   - `docker compose version` (or use `docker-compose` if older)

2) From the app root, build and start services:
```
cd COMSW4156-TeamX
# Build the app image and start Postgres + app
# Newer Docker
docker compose up -d --build
# Older Docker
# docker-compose up -d --build
```
   - App URL: `http://localhost:8080`
   - DB: `postgres://postgres:postgres@localhost:5432/fitnessdb` (data persists in `COMSW4156-TeamX/database/data`)

3) Optional: run Postman tests via Docker (after app is up)
```
# Combine base compose with test compose and run the newman service
docker compose -f docker-compose.yml -f docker-compose.tests.yml up --abort-on-container-exit newman
# Older Docker
# docker-compose -f docker-compose.yml -f docker-compose.tests.yml up --abort-on-container-exit newman
```
   - HTML report: `COMSW4156-TeamX/postman/postman-report.html`

4) Stop services
```
docker compose down
# docker-compose down
```

Troubleshooting
- If `docker` is not recognized, install Docker Desktop and restart your terminal.
- If port 5432 is busy, edit `docker-compose.yml` to map e.g. `5433:5432` and update `DB_URL` if you access from outside Docker.
- First build takes a while (Maven downloads). Subsequent builds are faster.
### All tests + combined coverage (unit + API)

- PowerShell (Windows):
  - `pwsh -File scripts/run-all-tests.ps1`
- macOS/Linux:
  - `bash scripts/run-all-tests.sh`

This runs unit tests (JaCoCo), starts the app with a JaCoCo runtime agent, executes the Postman collection via Docker, merges unit and API coverage, and writes a single report to `target/site/jacoco/index.html`.
