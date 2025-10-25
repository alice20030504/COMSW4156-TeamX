PostgreSQL (Persistent Data) — Quick Start

This project includes a minimal PostgreSQL setup for persistent storage using Docker.

Prerequisites
- Docker Desktop installed and running
- Java 17 and Maven installed

1) Start PostgreSQL (Docker)
- From the repo root, go to the database folder:
  - Windows PowerShell: `cd COMSW4156-TeamX\database`
  - macOS/Linux: `cd COMSW4156-TeamX/database`
- Start the DB:
  - Newer Docker: `docker compose up -d`
  - Older Docker: `docker-compose up -d`

This launches a Postgres 15 instance with:
- Host: `localhost`
- Port: `5432`
- DB: `fitnessdb`
- User: `postgres`
- Password: `postgres`

Data persists under `COMSW4156-TeamX/database/data` between app restarts.

2) Run the Spring Boot app with the postgres profile
- Open a new terminal at the repo root.
- Set environment variables (optional — defaults match Docker config) and start the app.

Windows PowerShell
```
cd <repo-root>\COMSW4156-TeamX
$env:DB_URL = "jdbc:postgresql://localhost:5432/fitnessdb"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

macOS/Linux
```
cd <repo-root>/COMSW4156-TeamX
export DB_URL="jdbc:postgresql://localhost:5432/fitnessdb"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Notes
- The `postgres` profile is defined in `src/main/resources/application-postgres.yml`.
- It uses `ddl-auto=update` so tables are created/updated automatically.
- If `src/main/resources/data.sql` exists, Spring Boot may load it on startup to seed data.

3) Verify persistence (simple manual check)
1. Start the app (step 2).
2. Create a record via API (use Postman/Newman or curl). Example curl:
   - Windows PowerShell:
     ```
     curl -Method POST `
       -Uri "http://localhost:8080/api/persons" `
       -Headers @{ 'Content-Type'='application/json'; 'X-Client-ID'='mobile-app1' } `
       -Body '{"name":"Alice","weight":65,"height":170,"birthDate":"1992-02-01"}'
     ```
   - macOS/Linux:
     ```
     curl -X POST \
       -H 'Content-Type: application/json' \
       -H 'X-Client-ID: mobile-app1' \
       -d '{"name":"Alice","weight":65,"height":170,"birthDate":"1992-02-01"}' \
       http://localhost:8080/api/persons
     ```
3. Stop the app (Ctrl+C in the Maven run terminal) and start it again (step 2).
4. Fetch the record (replace {id} with the value returned in step 2):
   - Windows PowerShell:
     ```
     curl -Uri "http://localhost:8080/api/persons/{id}?birthDate=1992-02-01" -Headers @{ 'X-Client-ID'='mobile-app1' }
     ```
   - macOS/Linux:
     ```
     curl -H 'X-Client-ID: mobile-app1' \
       "http://localhost:8080/api/persons/{id}?birthDate=1992-02-01"
     ```
   You should still see the same record — confirming persistent storage in PostgreSQL.

4) Optional — Run API tests (Newman)
- From repo root (Windows PowerShell):
```
cd <repo-root>
npx -y newman run COMSW4156-TeamX/postman/fitness-api-tests.postman_collection.json \
  -e COMSW4156-TeamX/postman/fitness-api-tests.postman_environment.json \
  --reporters cli,html \
  --reporter-html-export postman-report.html
```
- Ensure the environment is pointed at `http://localhost:8080` or update it accordingly.

5) Stop and clean the database (if needed)
- Stop DB: `docker compose down` (or `docker-compose down`)
- Wipe stored data: delete the `COMSW4156-TeamX/database/data` folder (this is destructive)

Troubleshooting
- Port already in use: change `"5432:5432"` to `"5433:5432"` in `docker-compose.yml` and update `DB_URL` to match.
- Connection refused: ensure Docker is running and container `fitnessdb-postgres` is healthy (`docker ps`).
- Authentication failed: confirm `DB_USERNAME`/`DB_PASSWORD` match compose env values.

