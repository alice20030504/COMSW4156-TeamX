# Personal Fitness Management Service

A comprehensive Spring Boot-based fitness tracking and management service that provides personalised health calculations, goal planning, and population analytics for fitness researchers.

---

## Quick Start

This project runs as a fully Dockerized stack—comprising a Spring Boot backend, React frontend, and PostgreSQL database—so you can bring everything up with a single command.

1. **Prerequisites** – Install Docker Desktop (or Docker Engine + Docker Compose) and ensure it is running.
2. **Build & Launch** – From the repo root, run:
   ```bash
   docker compose up -d --build
   ```
   This builds the backend image, seeds the PostgreSQL container, and serves the frontend from the `frontend/` bundle.
3. **Verify** – After the containers report healthy:
   - Backend API: `http://localhost:8080`
   - Frontend UI: `http://localhost:3000`
   - PostgreSQL data volume: `database/data` (persisted between runs)
4. **Shutdown / Reset** – Use `docker compose down` to stop services. For a clean slate, including DB data, run `docker compose down -v`.

See [`DockerCommandInstruction.md`](DockerCommandInstruction.md) for the full matrix of clean/build/test commands (unit tests, Checkstyle, PMD, Newman, DB resets) executed via Docker.

---

## 1. Service Overview

The Personal Fitness Management Service is a production-grade **Spring Boot backend** paired with a modern **React frontend**.  It powers personalised health planning for individual users while exposing advanced **cohort analytics** for researchers—all backed by a **PostgreSQL datastore**.  
The API goes well beyond CRUD: it delivers validated **biometric calculations**, **prescriptive plan insights**, and a **research-mode percentile engine**.  Every request is authenticated via **per-client headers**, logged centrally, and persisted, making it suitable for both **multi-tenant deployments** and **academic studies**.


### 1.1 What This Service Does

The Personal Fitness Management Service is a RESTful API that performs sophisticated fitness-related computations beyond simple CRUD operations:


### 1.2 Iteration 2 Tagged Version

The tagged Iteration 2 version is located at: **`Iteration_2`**

---

## 2. Client Program

A modern web-based client is available in the `frontend/` directory, providing user-friendly browser interfaces for both mobile users to manage fitness profiles and research analysts to access population health analytics. It supports simultaneous multi-client sessions with complete data isolation via `X-Client-ID` header authentication.

**For complete documentation on building, running, testing locally, and connecting to the GCP-deployed backend server, see [`frontend/README.md`](frontend/README.md).**

---

## 3. Static Analysis & Style Checker

Detailed configuration, tooling, and findings are documented in [`docs/STYLE_CHECK_SUMMARY.md`](docs/STYLE_CHECK_SUMMARY.md).

---

## 4. Unit Testing, API Testing, Integration Testing

### 4.1 Architecture Overview
The system’s high-level design is documented in [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).  
It provides a detailed breakdown of the **three-tier architecture** (**React client**, **Spring Boot backend**, **PostgreSQL data layer**), along with the **request flow**, **`X-Client-ID` authentication model**, and the internal boundaries between the **Person**, **Research**, and **Insights** components.  
The architecture guide also includes **sequence diagrams**, **deployment topology**, and **data-ownership rules**, helping mentors and reviewers understand how the modules interact in a production environment.

### 4.2 API Reference Documentation
A complete reference for all REST endpoints is provided in  [`docs/API_REFERENCE.md`](docs/API_REFERENCE.md).  
The guide specifies **request/response schemas**, **validation rules**, **sample payloads**, and behavioural notes for each route—covering the **Person profile APIs**, **health insight endpoints**, **research-mode percentile and cohort analytics**, and all **client-header authentication** requirements.  
This document is the **authoritative source** for external client integration and for validating backend behaviour via **automated or manual API testing**.

### 4.3 End-to-End Testing
End-to-end (E2E) workflows are documented in  [`docs/E2E_TESTING.md`](docs/E2E_TESTING.md).  
These procedures outline the **manual and semi-automated verification steps** used during Iteration 2 to validate **full-stack behaviour**—from **frontend interactions**, through **REST API calls**, to **PostgreSQL persistence checks**.  
The document also details the **testing environments**, **input/output expectations**, **edge-case scenarios**, and the steps mentors should follow when **reproducing the demo during grading**.

---

Execution summaries for JUnit suites, Newman API runs, and integration tests live in [`docs/TESTING_RESULTS.md`](docs/TESTING_RESULTS.md).

---

## 5. CI Execution Overview
Our GitHub Actions workflow ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)) runs on every pull request targeting `main` and on all pushes. The job executes on `ubuntu-latest` and uses JDK 17 (Temurin distribution) via `actions/setup-java@v4`.

### Stages & Commands
1. **Checkout** – `actions/checkout@v4` pulls the repository contents.
2. **JDK Setup** – `actions/setup-java@v4` installs Temurin 17 and enables Maven caching.
3. **Unit Tests & Build** – `mvn -B clean test` compiles the project and runs the test suite (Surefire reports under `target/surefire-reports`).
4. **Checkstyle** – `mvn -B checkstyle:check` enforces Google-style formatting; failures surface directly in the Actions log and can be inspected via `target/site/checkstyle.html` if downloaded.
5. **PMD** – `mvn -B pmd:check` executes the PMD ruleset. Results are written to `target/site/pmd.html` and `target/pmd.xml` when you collect artifacts locally.

### Notes
- No Docker services run inside CI; the workflow relies on Maven alone.
- API/Newman regression tests remain outside the workflow because spinning up the dockerized Newman runner causes excessive wait times on GitHub-hosted runners. Those tests run on-demand using `docker compose ... run --rm newman` in local/QA environments.
- CI runs in parallel for multiple pushes but fails fast if any Maven goal returns non-zero.
- Artifacts (Surefire XML, Checkstyle/PMD reports) are available locally by rerunning the same Maven commands; we currently don’t upload them as CI artifacts.

---

## 6. Branch Coverage & Bug Fixing

### 6.1 Coverage Report Location

Coverage reports are stored at: **[`testresult/unit-coverage/jacoco/index.html`](testresult/unit-coverage/jacoco/index.html)**

Open the HTML file in a web browser to view detailed coverage metrics by package, class, and method.

### 6.2 API Regression Tests

Postman/Newman runs write their HTML and JSON summaries to **[`testresult/api/postman-report.html`](testresult/api/postman-report.html)** (plus raw logs under **[`testresult/api/postman-summary.json`](testresult/api/postman-summary.json)**). Open the HTML report to inspect pass/fail status, response assertions, and run metadata for each endpoint.

### 6.3 Bugs Found and Fixed

See [`docs/STYLE_CHECK_SUMMARY.md`](docs/STYLE_CHECK_SUMMARY.md#functional-bugs-found-and-fixed) for the curated list of static-analysis and functional bugs that were identified and resolved (client ID handling, BMI edge cases, data isolation, and logging hygiene). The summary includes the evidence paths for each fix.


## 7. Cloud Deployment

### 7.1 Deployed URLs

**GCP Deployment:**
- **Backend:** `http://34.30.81.33:8080`
- **Frontend:** `http://34.30.81.33:3000`

**Service Endpoints:**
- **Health Check:** `http://34.30.81.33:8080/health`
- **Swagger UI:** `http://34.30.81.33:8080/swagger-ui.html`

### 7.2 Access for Mentors/Testers During Iteration 2 Demo

**Backend API:**
- Base URL: `http://34.30.81.33:8080`
- Health Check: `http://34.30.81.33:8080/health`
- Swagger UI: `http://34.30.81.33:8080/swagger-ui.html`

**Frontend:**
- URL: `http://34.30.81.33:3000`
---

## 8. Project Management

### 8.1 Task Tracking

**Jira Board:** https://columbia-teamx-coms4156.atlassian.net/jira/software/projects/SCRUM/boards/1

### 8.2 Team Task Distribution

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

### 8.3 Work Tracking

Work is tracked via:
- GitHub Issues for bug reports and feature requests
- Pull requests for code reviews
- Commit messages following conventional commit format
- CI/CD pipeline for automated testing and validation

---

## 9. Third-Party Code Disclosure

### 9.1 External Libraries

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

### 9.2 Third-Party Code Copied into Project

**None.** All third-party code is managed via Maven dependencies. No third-party source code has been copied directly into the project repository.

---

## 10. Project Proposal Implementation Status

Mid-semester our team was reduced to three members, so we made a deliberate and strategic scope decision for Iteration 2:  
we focused on **core, foundational features**—health insights, goal-planning APIs, client isolation, and the multi-client experience.  
Several originally proposed features required **substantial additional data modeling**, **external API dependencies**, or **duplicate technical stacks** (e.g., dual-backend Python/Java architecture).  
These non-essential or high-complexity features were **intentionally cut from scope** to ensure the stability, completeness, and quality of the core system delivered in this iteration.

---

### Table 1 — Core Features vs. Implementation Status

| Feature Category | Proposed Feature | Status | Notes |
|------------------|------------------|--------|-------|
| **User Data** | Structured user profiles | ✅ Implemented | Full profile model + validation |
| | Goal & progress model | ⚠️ Partially Implemented | CUT/BULK done; RECOVER & history not included in scope |
| **Health Computation** | BMI, age, BMR, calorie needs | ✅ Implemented | Fully implemented in HealthInsights engine |
| | Macronutrient computation | 🔄 Pruned| Requires nutrition database + food logs |
| **Workout Features** | Exercise logs + MET-based calories | 🔄 Pruned | Only weekly training frequency implemented |
| **Nutrition Features** | Food logs + nutrient breakdown | 🔄 Pruned | External API infra ready (USDA/Nutritionix) |
| | Recipe generation (AI/DB) | 🔄 Not in Scope | Explored; requires dual-backend architecture |
| **API Layer** | RESTful API | ✅ Implemented | Personal + research APIs complete |
| | Full request logging | ✅ Implemented | Method, status, duration, clientId, UA, IP logged |
| **Persistence** | PostgreSQL persistence | ✅ Implemented | JPA/Hibernate entities for all core models |
| **Clients** | Mobile client | ✅ Implemented | `frontend/mobile.html` |
| | Research analytics client | ✅ Implemented | `frontend/research.html` |

---

### Table 2 — Development Tools & Infrastructure Status

| Tool / Component | Proposed | Status | Notes |
|------------------|----------|--------|-------|
| **Backend Framework** | Spring Boot | ✅ Implemented | Core engine + controllers |
| **Database** | PostgreSQL | ✅ Implemented | Full persistence with JPA |
| **Testing** | JUnit + Mockito | ✅ Implemented | Unit & service-layer tests |
| | API testing (Postman/Newman) | ✅ Implemented | Regression suite under `postman/` |
| **Coverage** | JaCoCo (≥80%) | ✅ Implemented | Coverage target met |
| **Static Analysis** | Checkstyle + PMD | ✅ Implemented | Enforced locally and in CI |
| **CI/CD** | GitHub Actions | ✅ Implemented | Build, test, quality checks |
| **Task Tracking** | GitHub Projects / Jira | ✅ Implemented | All sprint work tracked |
| **Frontend** | Web-based mobile + research clients | ✅ Implemented | Deployed + local support |


---

## Additional Documentation

- **[Architecture Overview](docs/ARCHITECTURE.md)**: System design and component interactions
- **[API Reference](docs/API_REFERENCE.md)**: Complete API endpoint documentation
- **[End-to-End Testing](docs/E2E_TESTING.md)**: Manual testing procedures and checklists
- **[Testing Results](docs/TESTING_RESULTS.md)**: Test execution summaries
- **[Style Check Summary](docs/STYLE_CHECK_SUMMARY.md)**: Static analysis configuration
- **[Frontend README](frontend/README.md)**: Client-specific documentation
- 
---
## License

See [`LICENSE`](LICENSE) file for details.
