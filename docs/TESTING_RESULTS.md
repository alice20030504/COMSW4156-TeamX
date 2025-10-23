# Testing Results

## Summary
- Test run date: 2025-10-23
- Frameworks: JUnit 5, Mockito, Spring Test, Postman/Newman
- Coverage: 69% branches, 68% lines (JaCoCo)

## Automated Suites
1. **Unit Tests**
   - `PersonServiceTest` verifies BMI, BMR, age, and calorie calculations including boundary and null cases.
   - `ClientIdInterceptorTest` covers header validation, error payloads, and context cleanup.
2. **Controller and Integration Tests**
   - `PersonControllerTest` and `ClientIsolationIntegrationTest` assert client-scoped CRUD operations with mocked repositories.
   - `ResearchEndpointAccessControlTest` ensures mobile clients receive 403 responses and research clients obtain expected payloads.
3. **System/API Tests**
   - Postman collection `postman/fitness-api-tests.postman_collection.json` executed via Newman.
   - 36 requests, 79 assertions; all passed.
   - Scenarios include typical, atypical, and invalid inputs for every `/api/persons` and `/api/research` endpoint, ensuring logging and persistence behaviors are exercised.

## Notable Findings
- Unauthorized access attempts now return structured JSON with status codes validated in both unit and system suites.
- Repository mocks confirmed that client IDs are written on create/update and enforced on retrieval.
- Logging configuration captured request traces during Newman runs; spot checks in `logs/fitness-app.log` verified context clearing.

## Manual Verification
- Swagger UI (`/swagger-ui.html`) spot-tested for BMI and health-check endpoints.
- H2 console inspected to confirm test-created records and automatic cleanup after deletions.

## Reports
- JaCoCo coverage report: `reports/test-coverage-10232025.png`
- Postman/Newman execution log: `reports/postman-newman-results.json`

## Next Steps
- Extend coverage to include error-handling paths in `ResearchController` once new analytics logic lands.
- Add database integration tests with Testcontainers to mirror PostgreSQL behavior.
- Automate Newman execution in CI pipelines for every pull request.
