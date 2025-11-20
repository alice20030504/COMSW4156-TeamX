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

## Frontend Web Client

A modern web-based client is available in the `frontend/` directory. This provides a user-friendly browser interface to interact with the fitness service API.

### Run with Docker (backend + frontend)

In the project root:

```bash
docker-compose up --build
# Backend: http://localhost:8080
# Frontend: http://localhost:3000
```

### Location

The frontend client code is located in: **`frontend/`**

See `frontend/README.md` for detailed documentation.

### What It Does

The frontend web client provides:

- **User Registration**: Register new fitness profiles through a web form
- **Profile Management**: View and manage your fitness profile
- **Goal Plan Configuration**: Set up personalized fitness plans with target changes, duration, and training frequency
- **Fitness Metrics**:
  - Calculate and view BMI (Body Mass Index)
  - Get daily calorie recommendations
  - Receive personalized fitness recommendations
- **Multiple Client Support**: Each browser tab/window can maintain its own client session independently

### How to Build and Run

**Prerequisites:**

- A modern web browser (Chrome, Firefox, Safari, Edge)
- The backend service running (see "Build, Test, and Run" section below)

**Quick Start:**

1. Ensure the backend service is running on `http://localhost:8080`
2. Open `frontend/index.html` in your web browser
   - **Windows**: Double-click `frontend/index.html` or right-click → "Open with" → your browser
   - **Mac/Linux**: Open from file manager or use `open frontend/index.html` (Mac) / `xdg-open frontend/index.html` (Linux)

**Using a Local Web Server (Recommended):**

```bash
# Python 3
cd frontend
python -m http.server 3000
# Then open: http://localhost:3000

# Or using Node.js http-server
cd frontend
npx http-server -p 3000
# Then open: http://localhost:3000
```

### Connecting to the Service

1. The frontend connects to the backend API using the `X-Client-ID` header for authentication
2. Default API URL is `http://localhost:8080` (configurable in the web UI)
3. After registration, your client ID is stored in browser localStorage
4. All authenticated requests automatically include your client ID

### Multiple Client Instances

The service supports multiple simultaneous client instances through the `X-Client-ID` header mechanism:

**How It Works:**

1. **Client Identification**: Each client instance is identified by a unique `X-Client-ID` header (e.g., `mobile-id1`, `mobile-id2`)
2. **Request Interception**: The backend's `ClientIdInterceptor` intercepts every API request (except open endpoints like registration)
3. **Context Storage**: The client ID is stored in `ClientContext` (thread-local) for the request lifecycle
4. **Data Isolation**: Database queries are filtered by client ID, ensuring each client only sees/modifies their own data
5. **Concurrent Support**: Multiple clients can run simultaneously without interference

**Testing Multiple Clients:**

- **Different Browser Tabs**: Each tab can have a different client ID in localStorage
- **Different Browsers**: Each browser maintains separate localStorage
- **Different Machines**: Multiple machines can connect to the same backend simultaneously
- **Different Client IDs**: Each instance uses its own `X-Client-ID` header, and the backend routes requests to the correct data based on this header

**Example:**

- Tab 1: Client ID `mobile-id1` → sees only profiles created with `mobile-id1`
- Tab 2: Client ID `mobile-id2` → sees only profiles created with `mobile-id2`
- Both tabs can run simultaneously, making requests to the same backend, and data remains isolated

For more details, see `frontend/README.md`.

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
