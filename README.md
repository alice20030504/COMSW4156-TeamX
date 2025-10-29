# Personal Fitness Management Service

## Overview
Spring Boot REST API serving mobile and research clients.

- Mobile clients manage personal fitness data, including BMI/BMR/calorie metrics.
- Research clients access anonymized population-level analytics (demographics, workout, nutrition).
- Client isolation is enforced by validating user ID and birth date.
- Iteration 1 Focus: secure CRUD operations, fitness calculators, analytics endpoints, and OpenAPI documentation.

See docs in `docs/` for details, including `docs/ARCHITECTURE.md` and `docs/API_REFERENCE.md`.

---

## API Documentation

- Reference: `docs/API_REFERENCE.md`
- Architecture: `docs/ARCHITECTURE.md`
- Testing summary: `docs/TESTING_RESULTS.md`
- Style check: `docs/STYLE_CHECK_SUMMARY.md`

Swagger UI:
http://localhost:8080/swagger-ui/index.html

---

## Build, Test, and Run

Prerequisites:
- Java 17+
- Maven 3.8+

Build and run:
```
mvn clean compile
mvn spring-boot:run
```

Unit tests and coverage:
```
mvn clean test
mvn jacoco:report
```

---

## API Test

Load Postman files from `postman/`:
- `fitness-api-tests.postman_collection.json`
- `fitness-api-tests.postman_environment.json`

---

## Testing

- Unit and integration: JUnit 5, Mockito, Spring Test (via `mvn test`).
- System/API: Postman collection executed with Newman.
- Coverage: JaCoCo reports written to `target/site/jacoco/index.html`.

---

## Run with PostgreSQL (persistent)

Minimal PostgreSQL configuration is provided via the `postgres` Spring profile.

Windows PowerShell:
```
$env:DB_URL="jdbc:postgresql://localhost:5432/fitnessdb"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

macOS/Linux:
```
export DB_URL="jdbc:postgresql://localhost:5432/fitnessdb"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

---

## Run with Docker (App + Postgres)

From the app root:
```
docker compose up -d --build
```
App URL: `http://localhost:8080`
DB URL: `postgres://postgres:postgres@localhost:5432/fitnessdb`

Run Postman tests via Docker once the app is up:
```
docker compose -f docker-compose.yml -f docker-compose.tests.yml up --abort-on-container-exit newman
```
HTML report: `COMSW4156-TeamX/postman/postman-report.html`

Stop services:
```
docker compose down
```

---

## All tests + combined coverage (unit + API)

Windows:
```
pwsh -File scripts/run-all-tests.ps1
```

macOS/Linux:
```
bash scripts/run-all-tests.sh
```

This runs unit tests (JaCoCo), starts the app with a JaCoCo runtime agent, executes the Postman collection via Docker, merges unit and API coverage, and writes a single report to `target/site/jacoco/index.html`.

