# Client Isolation and Authentication Implementation

**Date**: 2025-10-20
**Iteration**: 2 (Multi-Client Support)
**Status**: ✅ Complete and Tested

---

## Overview

This document describes the implementation of client isolation and authentication for the Fitness Management Service, addressing the rubric requirement for multi-client data separation and client-type differentiation.

## Rubric Requirement

**3 Points**: Test that API calls on behalf of two or more (faked) clients work properly, such that the service can tell clients apart without interference.

**Instructor Feedback Addressed**:
1. ❌ PersonController and ResearchController exposed endpoints without client-type checks
2. ❌ PersonSimple had no clientId field - no data isolation between clients
3. ✅ **RESOLVED**: Full client isolation and access control implemented

---

## Implementation Approach

### Authentication Method: Header-Based Client Identification

**Chosen Approach**: Simple `X-Client-ID` header authentication
**Rationale**:
- Instructor clarified: "Auth is not a hard requirement just to be clear"
- Simpler than JWT/OAuth while still meeting requirements
- Easy to test and demonstrate
- Clear separation of concerns

### Client ID Format

**Pattern**: `<type>-<identifier>`

**Valid Types**:
- `mobile-*` - Mobile application clients (e.g., `mobile-app1`, `mobile-app2`)
- `research-*` - Research tool clients (e.g., `research-tool1`, `research-analytics`)

**Validation**: Enforced via `ClientValidator` utility class

---

## Architecture Changes

### 1. Data Model Changes

#### PersonSimple Entity
**Added Fields**:
- `clientId` (String, NOT NULL, indexed)

**Updated Constructor**:
```java
public PersonSimple(String name, Double weight, Double height,
                   LocalDate birthDate, String clientId)
```

**Impact**: All person records are now associated with a specific client

#### data.sql
**Updated**: Sample data now includes client IDs
```sql
INSERT INTO persons_simple (name, birth_date, weight, height, client_id)
VALUES
    ('John Smith', '1990-05-15', 80.5, 180, 'mobile-app1'),
    ('Sarah Johnson', '1995-08-22', 65.0, 165, 'mobile-app2'),
    ('Mike Chen', '1988-03-10', 75.0, 175, 'mobile-app1');
```

### 2. New Infrastructure Components

| Component | Type | Purpose |
|-----------|------|---------|
| `ClientValidator` | Utility | Validates client ID format, determines type (mobile/research) |
| `ClientContext` | Thread-Local Storage | Stores client ID for current request thread |
| `ClientIdInterceptor` | Spring Interceptor | Extracts and validates `X-Client-ID` header |
| `ClientUnauthorizedException` | Exception | Thrown when unauthorized access attempted |
| `GlobalExceptionHandler` | Exception Handler | Maps exceptions to proper HTTP responses (403, 400) |
| `WebMvcConfig` | Configuration | Registers interceptor for `/api/**` paths |
| `PersonRepository` | JPA Repository | Provides client-filtered query methods |
| `OpenApiConfig` | Configuration | Documents `X-Client-ID` requirement in Swagger |

### 3. Controller Updates

#### PersonController
**New CRUD Endpoints** (all with client isolation):
- `GET /api/persons` - Get all persons for authenticated client
- `GET /api/persons/{id}` - Get person by ID (only if belongs to client)
- `POST /api/persons` - Create person (auto-associated with client)
- `PUT /api/persons/{id}` - Update person (only if belongs to client)
- `DELETE /api/persons/{id}` - Delete person (only if belongs to client)

**Isolation Mechanism**:
```java
String clientId = ClientContext.getClientId();
personRepository.findByClientId(clientId);
personRepository.findByIdAndClientId(id, clientId);
```

#### ResearchController
**Access Control Added**:
```java
private void validateResearchAccess() {
    String clientId = ClientContext.getClientId();
    if (ClientValidator.isMobileClient(clientId)) {
        throw new ClientUnauthorizedException(
            "Mobile clients are not authorized to access research endpoints...");
    }
}
```

**Applied to**:
- `/api/research/demographics`
- `/api/research/workout-patterns`
- `/api/research/nutrition-trends`
- `/api/research/population-health`

---

## Request Flow

```
1. Client sends request with X-Client-ID header
   ↓
2. ClientIdInterceptor intercepts request
   ↓
3. Extracts X-Client-ID header
   ↓
4. ClientValidator validates format (mobile-* or research-*)
   ↓
5. ClientContext.setClientId(clientId) - stores in thread-local
   ↓
6. Request proceeds to controller
   ↓
7. Controller uses ClientContext.getClientId() to filter data
   ↓
8. For research endpoints: validates client type
   ↓
9. Response sent
   ↓
10. ClientContext.clear() - cleanup in afterCompletion
```

---

## Access Control Matrix

| Client Type | `/api/persons` CRUD | `/api/persons/bmi,age,calories` | `/api/research/*` |
|-------------|---------------------|----------------------------------|-------------------|
| `mobile-*` | ✅ Own data only | ✅ Allowed | ❌ 403 Forbidden |
| `research-*` | ✅ Own data only | ✅ Allowed | ✅ Allowed |
| No header | ❌ 400 Bad Request | ❌ 400 Bad Request | ❌ 400 Bad Request |
| Invalid format | ❌ 400 Bad Request | ❌ 400 Bad Request | ❌ 400 Bad Request |

---

## Testing

### Integration Test Suites

#### ClientIsolationIntegrationTest (9 tests)
Tests demonstrating data isolation between clients:

1. ✅ Mobile client 1 can only see their own data
2. ✅ Mobile client 2 can only see their own data
3. ✅ Data posted by mobile client 1 doesn't show up for mobile client 2
4. ✅ Mobile client cannot access person from another client by ID
5. ✅ Mobile client cannot update person from another client
6. ✅ Mobile client cannot delete person from another client
7. ✅ Multiple persons can exist with same name across different clients
8. ✅ Request without client ID header is rejected (400)
9. ✅ Request with invalid client ID format is rejected (400)

#### ResearchEndpointAccessControlTest (12 tests)
Tests demonstrating client-type access control:

1. ✅ Mobile client forbidden from `/api/research/demographics` (403)
2. ✅ Mobile client forbidden from `/api/research/workout-patterns` (403)
3. ✅ Mobile client forbidden from `/api/research/nutrition-trends` (403)
4. ✅ Mobile client forbidden from `/api/research/population-health` (403)
5. ✅ Research client can access `/api/research/demographics`
6. ✅ Research client can access `/api/research/workout-patterns`
7. ✅ Research client can access `/api/research/nutrition-trends`
8. ✅ Research client can access `/api/research/population-health`
9. ✅ Multiple different mobile clients all forbidden from research endpoints
10. ✅ Multiple research clients can all access research endpoints
11. ✅ Mobile client can still access `/api/persons` endpoints
12. ✅ Research client can still access `/api/persons` endpoints

### Test Results

**Total Tests**: 21
**Passed**: 21 ✅
**Failed**: 0
**Errors**: 0
**Coverage**: Full coverage of all client isolation scenarios

**Run Command**:
```bash
mvn test -Dtest=ClientIsolationIntegrationTest,ResearchEndpointAccessControlTest
```

---

## Example Usage

### Mobile Client Creating and Accessing Data

```bash
# Create a person as mobile-app1
curl -X POST -H "X-Client-ID: mobile-app1" \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","weight":65,"height":170,"birthDate":"1990-05-15"}' \
  http://localhost:8080/api/persons

# Response: 201 Created
{
  "id": 1,
  "name": "Alice",
  "weight": 65.0,
  "height": 170.0,
  "birthDate": "1990-05-15",
  "clientId": "mobile-app1"
}

# Get all persons for mobile-app1
curl -H "X-Client-ID: mobile-app1" \
  http://localhost:8080/api/persons

# Response: [{"id":1,"name":"Alice",...}]

# Try to access as mobile-app2 (different client)
curl -H "X-Client-ID: mobile-app2" \
  http://localhost:8080/api/persons

# Response: [] (empty - cannot see mobile-app1's data)
```

### Mobile Client Blocked from Research Endpoints

```bash
# Mobile client tries to access research endpoint
curl -H "X-Client-ID: mobile-app1" \
  http://localhost:8080/api/research/demographics

# Response: 403 Forbidden
{
  "timestamp": "2025-10-20T18:52:39.123",
  "status": 403,
  "error": "Forbidden",
  "message": "Mobile clients are not authorized to access research endpoints. Research endpoints are restricted to research-tool clients only."
}
```

### Research Client Accessing Research Endpoints

```bash
# Research client accesses research endpoint
curl -H "X-Client-ID: research-tool1" \
  http://localhost:8080/api/research/demographics

# Response: 200 OK
{
  "dataAnonymized": true,
  "privacyCompliant": true,
  "cohort": {...},
  "physicalMetrics": {...},
  "nutritionalMetrics": {...}
}
```

---

## Files Modified/Created

### New Files
- `src/main/java/com/teamx/fitness/util/ClientValidator.java`
- `src/main/java/com/teamx/fitness/context/ClientContext.java`
- `src/main/java/com/teamx/fitness/interceptor/ClientIdInterceptor.java`
- `src/main/java/com/teamx/fitness/exception/ClientUnauthorizedException.java`
- `src/main/java/com/teamx/fitness/exception/GlobalExceptionHandler.java`
- `src/main/java/com/teamx/fitness/config/WebMvcConfig.java`
- `src/main/java/com/teamx/fitness/config/OpenApiConfig.java`
- `src/main/java/com/teamx/fitness/repository/PersonRepository.java`
- `src/test/java/com/teamx/fitness/integration/ClientIsolationIntegrationTest.java`
- `src/test/java/com/teamx/fitness/integration/ResearchEndpointAccessControlTest.java`
- `docs/CLIENT_ISOLATION_IMPLEMENTATION.md`

### Modified Files
- `src/main/java/com/teamx/fitness/model/PersonSimple.java` - Added `clientId` field
- `src/main/java/com/teamx/fitness/controller/PersonController.java` - Added CRUD endpoints with isolation
- `src/main/java/com/teamx/fitness/controller/ResearchController.java` - Added access control
- `src/main/resources/data.sql` - Added client IDs to sample data
- `README.md` - Added client authentication and isolation documentation

---

## Security Considerations

### Current Implementation
- **Header-based authentication**: Simple but sufficient for demonstration
- **No encryption**: Headers are plain text
- **No rate limiting**: Not implemented in this iteration
- **No session management**: Stateless design

### Production Recommendations
For production deployment, consider:
1. Implement JWT or OAuth2 tokens instead of plain client IDs
2. Use HTTPS to encrypt headers in transit
3. Add rate limiting per client
4. Implement API key management and rotation
5. Add request signing for additional security
6. Consider implementing Spring Security for robust authentication

---

## Conclusion

**Rubric Compliance**: ✅ COMPLETE

The implementation successfully demonstrates:
1. ✅ Multi-client support with proper isolation
2. ✅ Clients can be told apart without interference
3. ✅ Mobile vs Research client differentiation
4. ✅ Access control enforcement (403 for unauthorized access)
5. ✅ Comprehensive testing (21 integration tests)

**Key Achievements**:
- Clean architecture with separation of concerns
- Thread-safe client context management
- Comprehensive test coverage
- Well-documented API behavior
- Easy to extend for additional client types

**Next Steps** (Future Enhancements):
- Upgrade to JWT-based authentication
- Add client registration and management endpoints
- Implement API key rotation
- Add audit logging for security events
- Consider implementing Spring Security
