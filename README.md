# Personal Fitness Management Service

A comprehensive Spring Boot-based fitness tracking and management service that provides personalized health calculations, goal planning, and population analytics for fitness researchers.

---

## Quick Start

This project runs as a full Dockerized stack—Spring Boot backend, React frontend, and PostgreSQL database—so you can bring everything up with a single command.

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
4. **Shutdown / Reset** – Use `docker compose down` to stop services. For a clean slate including DB data, run `docker compose down -v`.

See `DockerCommandInstruction.md` for additional commands (clean builds, unit tests, Checkstyle/PMD, Newman API tests) executed via Docker.

---

## 1. Service Overview

The Personal Fitness Management Service is a production-grade Spring Boot backend paired with a modern React frontend. It powers personalized health planning for individual users while exposing advanced cohort analytics for researchers—all backed by a PostgreSQL datastore. The API goes well beyond CRUD: it delivers validated biometric calculations, prescriptive plan insights, and a research-mode percentile engine. Every request is authenticated via per-client headers, logged centrally, and persisted, making it suitable for multi-tenant deployments and academic studies alike.

### What This Service Does

The Personal Fitness Management Service is a RESTful API that performs sophisticated fitness-related computations beyond simple CRUD operations:


### Iteration 2 Tagged Version

The tagged Iteration 2 version is located at: **`Iteration_2`** (to be updated with actual git tag)

---

## 2. Client Program

A modern web-based client is available in the `frontend/` directory, providing user-friendly browser interfaces for both mobile users to manage fitness profiles and research analysts to access population health analytics. It supports simultaneous multi-client sessions with complete data isolation via `X-Client-ID` header authentication.

**For complete documentation on building, running, testing locally, and connecting to the GCP-deployed backend server, see [`frontend/README.md`](frontend/README.md).**

---

## 3. Static Analysis & Style Checker

**Please refer to [`docs/STYLE_CHECK_SUMMARY`](docs/STYLE_CHECK_SUMMARY.md)**
---

## 4. Unit Testing, API Testing, Integration Testing

**Please refer to [`docs/TESTING_RESULTS`](docs/TESTING_RESULTS.md)**
---

## 5. CI Execution Overview
Our GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every pull request targeting `main` and on all pushes. The job executes on `ubuntu-latest` and uses JDK 17 (Temurin distribution) via `actions/setup-java@v4`.

## Stages & Commands
1. **Checkout** – `actions/checkout@v4` pulls the repository contents.
2. **JDK Setup** – `actions/setup-java@v4` installs Temurin 17 and enables Maven caching.
3. **Unit Tests & Build** – `mvn -B clean test` compiles the project and runs the test suite (Surefire reports under `target/surefire-reports`).
4. **Checkstyle** – `mvn -B checkstyle:check` enforces Google-style formatting; failures surface directly in the Actions log and can be inspected via `target/site/checkstyle.html` if downloaded.
5. **PMD** – `mvn -B pmd:check` executes the PMD ruleset. Results are written to `target/site/pmd.html` and `target/pmd.xml` when you collect artifacts locally.

## Notes
- No Docker services run inside CI; the workflow relies on Maven alone.
- API/Newman regression tests remain outside the workflow because spinning up the dockerized Newman runner causes excessive wait times on GitHub-hosted runners. Those tests run on-demand using `docker compose ... run --rm newman` in local/QA environments.
- CI runs in parallel for multiple pushes but fails fast if any Maven goal returns non-zero.
- Artifacts (Surefire XML, Checkstyle/PMD reports) are available locally by rerunning the same Maven commands; we currently don’t upload them as CI artifacts.

---

## 6. Branch Coverage & Bug Fixing

### Coverage Report Location

Coverage reports are stored at: **[`testresult/unit-coverage/jacoco/index.html`](testresult/unit-coverage/jacoco/index.html)**

Open the HTML file in a web browser to view detailed coverage metrics by package, class, and method.

### API Regression Tests

Postman/Newman runs write their HTML and JSON summaries to **[`testresult/api/postman-report.html`](testresult/api/postman-report.html)** (plus raw logs under `testresult/api/postman-summary.json`). Open the HTML report to inspect pass/fail status, response assertions, and run metadata for each endpoint.

### Bugs Found and Fixed

See [`docs/STYLE_CHECK_SUMMARY.md`](docs/STYLE_CHECK_SUMMARY.md#functional-bugs-found-and-fixed) for the curated list of static-analysis and functional bugs that were identified and resolved (client ID handling, BMI edge cases, data isolation, and logging hygiene). The summary includes the evidence paths for each fix.


## 7. Cloud Deployment

### Deployed URLs

**GCP Deployment:**
- **Backend:** `http://34.30.81.33:8080`
- **Frontend:** `http://34.30.81.33:3000`

**Service Endpoints:**
- **Health Check:** `http://34.30.81.33:8080/health`
- **Swagger UI:** `http://34.30.81.33:8080/swagger-ui.html`

### Access for Mentors/Testers During Iteration 2 Demo

**Backend API:**
- Base URL: `http://34.30.81.33:8080`
- Health Check: `http://34.30.81.33:8080/health`
- Swagger UI: `http://34.30.81.33:8080/swagger-ui.html`

**Frontend:**
- URL: `http://34.30.81.33:3000`
---


## 8. Project Management

### Task Tracking

**GitHub Projects Board:** [Link to be added]

**Jira Board:** https://columbia-teamx-coms4156.atlassian.net/jira/software/projects/SCRUM/boards/1

### Team Task Distribution

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

### Work Tracking

Work is tracked via:
- GitHub Issues for bug reports and feature requests
- Pull requests for code reviews
- Commit messages following conventional commit format
- CI/CD pipeline for automated testing and validation

---

## 9. Third-Party Code Disclosure

### External Libraries

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

### Third-Party Code Copied into Project

**None.** All third-party code is managed via Maven dependencies. No third-party source code has been copied directly into the project repository.


## Additional Documentation

- **[Architecture Overview](docs/ARCHITECTURE.md)**: System design and component interactions
- **[API Reference](docs/API_REFERENCE.md)**: Complete API endpoint documentation
- **[End-to-End Testing](docs/E2E_TESTING.md)**: Manual testing procedures and checklists
- **[Testing Results](docs/TESTING_RESULTS.md)**: Test execution summaries
- **[Style Check Summary](docs/STYLE_CHECK_SUMMARY.md)**: Static analysis configuration
- **[Frontend README](frontend/README.md)**: Client-specific documentation

---

## 10. Project Proposal Implementation Status

Mid-semester we lost a teammate, so we focused on the health insights engine, goal planning APIs, and the multi-client experience. All deep diet/workout automation (nutrition logs, recipe generation, MET-based workout calculators, RECOVER objective, historical progress dashboards) from the original proposal were deferred because reduced capacity made those features impractical this term.

---

## License

See [`LICENSE`](LICENSE) file for details.
