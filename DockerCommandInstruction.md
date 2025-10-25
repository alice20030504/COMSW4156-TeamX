Docker Commands — Clean, Build, Test

Assumptions
- Docker Desktop is running.
- Run commands from `COMSW4156-TeamX`.

1) Clean (start fresh)
- Soft clean (keeps DB data):
  - `docker compose -f docker-compose.yml -f docker-compose.tests.yml down --remove-orphans`
- Hard clean (removes DB data — destructive):
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

Manual API Injection — Newman and curl

Newman (local Node)
- Full suite:
  - `npx -y newman run postman/fitness-api-tests.postman_collection.json -e postman/fitness-api-tests.postman_environment.json --reporters cli,html --reporter-html-export testresult/api/postman-report.html`
- Only a folder:
  - `npx -y newman run postman/fitness-api-tests.postman_collection.json -e postman/fitness-api-tests.postman_environment.json --folder "Personal Endpoints" --reporters cli`

Newman (Docker)
- `docker run --rm -v "%cd%":/etc/newman -w /etc/newman postman/newman:alpine run postman/fitness-api-tests.postman_collection.json -e postman/fitness-api-tests.postman_environment.json --env-var baseUrl=http://localhost:8080 --reporters cli,html --reporter-html-export testresult/api/postman-report.html`

curl (Windows PowerShell; use `curl.exe` to bypass alias)
- Create:
  - `curl.exe -s -X POST "http://localhost:8080/api/persons" -H "Content-Type: application/json" -H "X-Client-ID: mobile-app1" -d "{\"name\":\"Alice\",\"weight\":65,\"height\":170,\"birthDate\":\"1992-02-01\"}"`
- Read (replace ID):
  - `curl.exe -s -X GET "http://localhost:8080/api/persons/<ID>?birthDate=1992-02-01" -H "X-Client-ID: mobile-app1"`

Persistent Data — Write, Restart, Read

Option A: via curl
- Write:
  - `curl.exe -s -X POST "http://localhost:8080/api/persons" -H "Content-Type: application/json" -H "X-Client-ID: mobile-app1" -d "{\"name\":\"Bob\",\"weight\":80,\"height\":180,\"birthDate\":\"1990-01-01\"}"`
- Restart app only:
  - `docker compose restart app`
- Read (replace ID):
  - `curl.exe -s -X GET "http://localhost:8080/api/persons/<ID>?birthDate=1990-01-01" -H "X-Client-ID: mobile-app1"`

Option B: via SQL (psql inside Postgres)
- Before/after restart, list recent rows:
  - `docker exec -it fitnessdb-postgres psql -U postgres -d fitnessdb -c "SELECT id,name,birth_date,client_id FROM persons_simple ORDER BY id DESC LIMIT 5;"`

Notes
- App URL: `http://localhost:8080`
- DB: `postgres://postgres:postgres@localhost:5432/fitnessdb` (use psql/GUI, not a browser)

Testing Persistent Data After Restart (PowerShell)
- Open Windows PowerShell in `COMSW4156-TeamX` and run:
$base = "http://localhost:8080"
$headers = @{ 'Content-Type'='application/json'; 'X-Client-ID'='mobile-app1' }
$body = @'
{"name":"Roger","weight":65,"height":170,"birthDate":"1992-02-01"}
'@
$resp = Invoke-RestMethod -Method POST -Uri "$base/api/persons" -Headers $headers -Body $body
$id = $resp.id
"Created ID: $id"
"Reading before restart..."
Invoke-RestMethod -Method GET -Uri ("$base/api/persons/{0}?birthDate=1992-02-01" -f $id) -Headers @{ 'X-Client-ID'='mobile-app1' } | ConvertTo-Json -Depth 5
"Restarting app..."
docker compose restart app | Out-Null
Start-Sleep -Seconds 3
"Reading after restart..."
Invoke-RestMethod -Method GET -Uri ("$base/api/persons/{0}?birthDate=1992-02-01" -f $id) -Headers @{ 'X-Client-ID'='mobile-app1' } | ConvertTo-Json -Depth 5
- Expect the same record before and after the restart, proving persistence.

