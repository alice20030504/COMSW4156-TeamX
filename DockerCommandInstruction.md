Docker Commands — Run, Test, Shutdown, Clean

Assumptions
- Docker Desktop is running.
- You are in `COMSW4156-TeamX`.

0) Clean up leftovers first (recommended)
- Soft clean (keeps DB data):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml down --remove-orphans || true`
- Hard clean (removes DB data too — destructive):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml down -v --remove-orphans || true`
  - Optionally delete bind-mounted data: remove folder `database/data`

1) Run (app + Postgres)
- `docker compose up -d --build`
- Optional health checks (view-only): open `http://localhost:8080/health` or `http://localhost:8080/actuator/health` in a browser.

2) Run unit tests (Dockerized Maven)
- `docker compose -f docker-compose.tests.yml run --rm unit-tests`
- Outputs:
  - `target/surefire-reports` (unit test logs)
  - `target/site/jacoco/index.html` (unit coverage)

3) Test API (Newman in Docker)
- Keep app/DB running and execute Newman as a one‑shot task:
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm newman`
- Report: `postman/postman-report.html`
- Note: Using `up --abort-on-container-exit newman` will stop all services when newman exits; prefer `run --rm newman` during local dev.

4) Verify persistent data (no curl; use SQL via psql)
- After tests run, the collection has created records. Inspect them directly in Postgres:
  - `docker exec -it fitnessdb-postgres psql -U postgres -d fitnessdb -c "SELECT id,name,birth_date,client_id FROM persons_simple ORDER BY id DESC LIMIT 5;"`
- Restart only the app (DB stays up):
  - `docker compose restart app`
- Inspect again (rows should still be present):
  - `docker exec -it fitnessdb-postgres psql -U postgres -d fitnessdb -c "SELECT id,name,birth_date,client_id FROM persons_simple ORDER BY id DESC LIMIT 5;"`

5) Shutdown
- Stop and remove containers (keep data): `docker compose down`

6) Clean
- Remove containers, networks, and volumes (deletes DB data):
  - `docker compose down -v --remove-orphans`
- Optional: delete persisted files (destructive):
  - remove folder `database/data`

Notes
- App URL: `http://localhost:8080`
- DB: `postgres://postgres:postgres@localhost:5432/fitnessdb` (binary protocol; use psql/GUI, not a browser)

Optional: Combined code coverage (unit + API)
- Start app with coverage overlay (writes runtime coverage to `coverage/jacoco-it.exec`):
  - `docker compose -f docker-compose.yml -f docker-compose.coverage.yml up -d --build`
- Run unit tests (Dockerized Maven) and API tests (Newman):
  - `docker compose -f docker-compose.tests.yml run --rm unit-tests`
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm newman`
- Stop containers:
  - `docker compose down`
- Merge and generate combined report with JaCoCo:
  - `mvn org.jacoco:jacoco-maven-plugin:0.8.11:merge -Djacoco.destFile=target/jacoco-merged.exec -Djacoco.dataFileList="target/jacoco.exec,coverage/jacoco-it.exec"`
  - `mvn org.jacoco:jacoco-maven-plugin:0.8.11:report -Djacoco.dataFile=target/jacoco-merged.exec`
- Open: `target/site/jacoco/index.html`
