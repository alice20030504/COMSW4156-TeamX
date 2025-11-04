PostgreSQL (Persistent Data) - Quick Start

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

2) Run the Spring Boot app
- Open a new terminal at the repo root.
- Set environment variables (optional - defaults match Docker config) and start the app.

Windows PowerShell
```
cd <repo-root>\COMSW4156-TeamX
$env:DB_URL = "jdbc:postgresql://localhost:5432/fitnessdb"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
mvn spring-boot:run
```

macOS/Linux
```
cd <repo-root>/COMSW4156-TeamX
export DB_URL="jdbc:postgresql://localhost:5432/fitnessdb"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"
mvn spring-boot:run
```

Notes
- Database connection settings now live in `src/main/resources/application.yml` and default to PostgreSQL.
- A helper script (`database/init/002_add_gender_column.sql`) adjusts existing volumes for the new `gender` column. If you already have data:
  1. Run the script inside the container:
     ```
     docker compose exec postgres psql -U postgres -d fitnessdb -f /docker-entrypoint-initdb.d/002_add_gender_column.sql
     ```
  2. Update any rows that should be `FEMALE`:
     ```
     docker compose exec postgres psql -U postgres -d fitnessdb -c "UPDATE persons_simple SET gender = 'FEMALE' WHERE <condition>;"
     ```
- Override `DB_URL`, `DB_USERNAME`, or `DB_PASSWORD` if you need to target a different Postgres instance.

3) Verify persistence (simple manual check)
1. Start the app (step 2).
2. Create a record via API (use Postman/Newman or curl). Example curl (works in PowerShell, Bash, or CMD):
   ```
   curl -X POST http://localhost:8080/api/persons \
     -H "Content-Type: application/json" \
     -d '{"name":"Alice","weight":65,"height":170,"birthDate":"1992-02-01","goal":"CUT","gender":"FEMALE"}'
   ```
   Save the `clientId` returned in the response (e.g., `mobile-id3`).
3. Stop the app (Ctrl+C in the Maven run terminal) and start it again (step 2).
4. Fetch the record:
   ```
   curl http://localhost:8080/api/persons/me \
     -H "X-Client-ID: mobile-id3"
   ```
   You should still see the same record—confirming persistent storage in PostgreSQL.

4) Optional - Run API tests (Newman)
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
- Port already in use: change "5432:5432" to "5433:5432" in `docker-compose.yml` and update `DB_URL` to match.
- Connection refused: ensure Docker is running and container `fitnessdb-postgres` is healthy (`docker ps`).
- Authentication failed: confirm `DB_USERNAME`/`DB_PASSWORD` match compose env values.

