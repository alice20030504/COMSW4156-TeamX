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

## Demo Client

`DemoClient.java` is a standalone Java CLI that drives the Personal Fitness Management Service API. It issues HTTP requests with the required `X-Client-ID` header so you can register users, configure plans, and retrieve BMI/calorie guidance.

### Prerequisites

- Java 17+

### Build and Run

```
javac DemoClient.java
java DemoClient demo
```

The `demo` command registers a sample profile (when no client ID is supplied), stores a goal plan, and fetches profile/BMI/calorie summaries.

### Common Commands

- Register (open endpoint, returns a generated client ID):

  ```
  java DemoClient register --name "Ava Stone" --weight 68.5 --height 172 \
      --birth-date 1995-03-18 --goal CUT --gender FEMALE
  ```

- Use the returned ID for authenticated commands (either pass `--client-id` or set `FITNESS_CLIENT_ID`):

  ```
  java DemoClient plan --client-id mobile-id1 \
      --target-change 3.5 --duration-weeks 6 --training-frequency 4 --plan-strategy BOTH
  java DemoClient bmi --client-id mobile-id1
  java DemoClient calories --client-id mobile-id1
  java DemoClient recommendation --client-id mobile-id1
  ```

### Multiple Concurrent Clients

Each instance isolates requests using the supplied `X-Client-ID`, which the service stores in `ClientContext`. Run parallel terminals with different IDs to simulate multiple clients:

```
export FITNESS_CLIENT_ID=mobile-id1   # Windows PowerShell: $env:FITNESS_CLIENT_ID="mobile-id1"
java DemoClient profile
```

```
export FITNESS_CLIENT_ID=mobile-id2
java DemoClient profile
```

Because the backend intercepts every request via `ClientIdInterceptor`, state remains separated per ID even when instances run simultaneously.

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

Assumptions

- Docker Desktop is running.
- Run commands from `COMSW4156-TeamX`.

1. Clean (start fresh)

- Soft clean (keeps DB data):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml down --remove-orphans`
- Hard clean (removes DB data - destructive):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml down -v --remove-orphans`
  - Optionally delete `database/data`

2. Build + run services (app + Postgres)

- `docker compose up -d --build`
- Health checks: `http://localhost:8080/health` or `http://localhost:8080/actuator/health`

3. Test

- Unit + Checkstyle (Dockerized Maven):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests`
- API tests (Newman):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm newman`
- Outputs (kept under `testresult/`):
  - `testresult/unit/` (Surefire)
  - `testresult/unit-coverage/jacoco/index.html` (JaCoCo)
  - `testresult/api/postman-report.html` (Newman HTML)

4. Shutdown

- `docker compose down`

---

## Deploy to Google Cloud Run (CI / Cloud Build)

GCP: https://teamx-backend-118279583185.us-central1.run.app

This repository includes a `cloudbuild.yaml` that builds the Docker image, deploys the service to Cloud Run and runs the Postman/Newman collection against the deployed URL.

Important notes before running Cloud Build:

- Update the Cloud Build substitutions (or pass them on the `gcloud builds submit` command):
  - `_REGION` (default: `us-central1`)
  - `_REPO` (Artifact Registry repo; default in the file: `fitness-repo`)
  - `_SERVICE_NAME` (Cloud Run service name; default: `fitness-service`)
  - `_DB_URL`, `_DB_USERNAME`, `_DB_PASSWORD` (temporary DB credentials used during deployment; for production store these in Secret Manager and reference them instead)

Quick command (use Cloud Shell or local gcloud after login):

```powershell
# from repo root
#gcloud builds submit --config cloudbuild.yaml --substitutions=_REGION="us-central1",_REPO="fitness-repo",_SERVICE_NAME="fitness-service",_DB_URL="jdbc:postgresql://<DB_IP>:5432/fitnessdb",_DB_USERNAME="fitnessuser",_DB_PASSWORD="your-pass"
```

After the build completes the pipeline will deploy to Cloud Run and automatically run the Newman collection found at `postman/fitness-api-tests.postman_collection.json` against the deployed service URL.

Security note: The `cloudbuild.yaml` included is intended for demos and CI during development. For production you should:

- Use Secret Manager for DB credentials and bind them to Cloud Build or Cloud Run.
- Use Cloud SQL Private IP or the Cloud SQL Auth proxy with a VPC connector to avoid public DB ips.
- Restrict Cloud Run access (remove `--allow-unauthenticated`) and front with an API gateway or IAM.

## Project Management

- Tool: JIRA([COMS4156 Scrum Board](https://columbia-teamx-coms4156.atlassian.net/jira/software/projects/SCRUM/boards/1)).

## Tags

- **Iteration 1 focus:** Designing and integrating **secure and meaningful data operations** (beyond basic CRUD),
  implementing **fitness calculators**, **research dashboards**, and **SpringDoc-powered API documentation**.

- **Iteration 1 Demo focus:** Ready for iteration 1 demo \*\*.
