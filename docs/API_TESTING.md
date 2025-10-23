# API System Testing (Postman + Newman)

This project now includes an automated API test suite that exercises the
Personal Fitness Management Service through Postman collections driven by the
Newman CLI.

## What's Included
- **Collection:** `postman/fitness-api-tests.postman_collection.json`
- **Environment:** `postman/fitness-api-tests.postman_environment.json`
- **Run artifact:** `reports/postman-newman-results.json`

Each API entry point is covered by three scenarios (typical valid, atypical
valid, and invalid) in order to hit distinct equivalence classes and error
paths. The suite spans:

- Personal client flows (create, read, update, delete, BMI, calories, health)
- Research endpoints (persons, demographics, workout patterns, nutrition
  trends, population health)
- Multi-client isolation (mobile vs. research IDs)
- Persistence by creating records, fetching them, mutating them, and verifying
  cross-client isolation through the live H2 data store
- Logging/exception behavior (403/401 cases captured in `logs/fitness-app.log`)

## How to Run Locally
1. Start the Spring Boot service:
   - `mvn spring-boot:run`
2. Install [Node.js](https://nodejs.org/) and Newman if you do not already have
   them (`npm install -g newman`), then execute:
   ```powershell
   newman run postman/fitness-api-tests.postman_collection.json `
     -e postman/fitness-api-tests.postman_environment.json `
     --reporters cli,json `
     --reporter-json-export reports/postman-newman-results.json
   ```
3. Review the CLI summary and the JSON report for evidence of the 36 requests
   and 79 assertions that now pass.

## Current Results (2025-10-23)
- ✅ 36 requests, 79 assertions, 0 failures
- ✅ Coverage of all personal and research controller endpoints
- ✅ Authentication/authorization regressions caught (401 re-delete, 403 mobile
  research access)
- ✅ Logging verified in `logs/fitness-app.log` during the run

These assets satisfy the rubric requirement for three API tests per endpoint
while leveraging an industry-standard API testing toolchain.
