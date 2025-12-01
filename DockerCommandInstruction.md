Docker Commands - Clean, Build, Test

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
- Checkstyle only (Dockerized Maven; same as `mvn -e -B checkstyle:check`, copies report to `testresult/checkstyle/`):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests /bin/sh -lc "mvn -e -B checkstyle:check && mkdir -p testresult/checkstyle && cp -f target/checkstyle-result.xml testresult/checkstyle/checkstyle-result.xml 2>/dev/null || true"`
- PMD only (Dockerized Maven):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests mvn -e -B pmd:check`
- PMD (Maven plugin, reports copied to `testresult/pmd/` like JaCoCo):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests /bin/sh -lc "mvn -e -B pmd:check && mkdir -p testresult/pmd && { cp target/site/pmd.xml testresult/pmd/ 2>/dev/null || true; } && { cp target/site/pmd.html testresult/pmd/ 2>/dev/null || true; }"`
- API tests (Newman):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm newman`
- Outputs (kept under `testresult/`):
  - `testresult/unit/` (Surefire)
  - `testresult/unit-coverage/jacoco/index.html` (JaCoCo)
  - `testresult/api/postman-report.html` (Newman HTML)

4) Shutdown
- `docker compose down`



