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

Assumptions
- Docker Desktop is running.
- Run commands from `COMSW4156-TeamX`.

1) Clean (start fresh)
- Soft clean (keeps DB data):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml down --remove-orphans`
- Hard clean (removes DB data - destructive):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml down -v --remove-orphans`
  - Optionally delete `database/data`

2) Build + run services (app + Postgres)
- `docker compose up -d --build`
- Health checks: `http://localhost:8080/health` or `http://localhost:8080/actuator/health`

3) Test
- Unit + Checkstyle (Dockerized Maven):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests`
- API tests (Newman):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm newman`
- Outputs (kept under `testresult/`):
  - `testresult/unit/` (Surefire)
  - `testresult/unit-coverage/jacoco/index.html` (JaCoCo)
  - `testresult/api/postman-report.html` (Newman HTML)

4) Shutdown
- `docker compose down`

---

## Deploy to Google Cloud Run (CI / Cloud Build)

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

```

