# Architecture Documentation

**Version**: 2.1
**Last Updated**: 2025-10-20

> **Version 2.1 Changes**: Simplified architecture by consolidating authentication components into a unified `security/` package. Reduced from 9 packages to 6 packages while maintaining full functionality.

---

## Repository Structure

```
COMSW4156-TeamX/
├── src/
│   ├── main/
│   │   ├── java/com/teamx/fitness/
│   │   │   ├── FitnessManagementApplication.java    # Spring Boot entry point
│   │   │   │
│   │   │   ├── config/                              # Configuration Layer
│   │   │   │   ├── OpenApiConfig.java              # Swagger/API documentation config
│   │   │   │   ├── WebMvcConfig.java               # Web MVC and interceptor config
│   │   │   │   └── GlobalExceptionHandler.java     # Error response formatting
│   │   │   │
│   │   │   ├── controller/                          # Presentation Layer (REST API)
│   │   │   │   ├── HomeController.java             # Root URL handler
│   │   │   │   ├── PersonController.java           # Person/fitness endpoints
│   │   │   │   └── ResearchController.java         # Research/analytics endpoints
│   │   │   │
│   │   │   ├── service/                             # Business Logic Layer
│   │   │   │   └── PersonService.java              # Fitness calculations (BMI, BMR, TDEE)
│   │   │   │
│   │   │   ├── repository/                          # Data Access Layer
│   │   │   │   └── PersonRepository.java           # JPA repository with client filtering
│   │   │   │
│   │   │   ├── model/                               # Domain/Entity Layer
│   │   │   │   ├── PersonSimple.java               # Person entity (JPA)
│   │   │   │   └── ApiLog.java                     # API logging entity (JPA)
│   │   │   │
│   │   │   └── security/                            # Security & Authentication
│   │   │       ├── ClientContext.java              # Client ID validation & thread-local storage
│   │   │       └── ClientIdInterceptor.java        # Request authentication interceptor
│   │   │
│   │   └── resources/
│   │       ├── application.yml                      # Development configuration
│   │       ├── application-prod.yml                 # Production configuration
│   │       └── data.sql                             # Sample data initialization
│   │
│   └── test/
│       └── java/com/teamx/fitness/
│           ├── controller/                          # Controller unit tests
│           ├── service/                             # Service unit tests
│           └── integration/                         # Integration tests
│               ├── ClientIsolationIntegrationTest.java
│               └── ResearchEndpointAccessControlTest.java
│
├── docs/                                            # Documentation
├── logs/                                            # Application logs
├── target/                                          # Build output (gitignored)
├── checkstyle.xml                                   # Code style rules
├── pom.xml                                          # Maven build configuration
└── README.md                                        # Project overview

```

---

## Layered Architecture

The application follows a **3-tier + cross-cutting concerns** architecture pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Requests                          │
│                  (HTTP + X-Client-ID header)                │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│               SECURITY LAYER                                 │
│  ClientIdInterceptor: Extract & validate client ID          │
│  ClientContext: Validate format & store in thread-local     │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│               CONTROLLER LAYER (REST API)                    │
│  - HomeController: Root redirects                            │
│  - PersonController: CRUD + fitness calculations             │
│  - ResearchController: Analytics endpoints                   │
│                                                              │
│  Responsibilities:                                           │
│  • HTTP request/response handling                            │
│  • Input validation                                          │
│  • Client authorization checks                               │
│  • Data filtering by client ID                               │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│               SERVICE LAYER                                  │
│  PersonService: Fitness calculation business logic           │
│                                                              │
│  Responsibilities:                                           │
│  • Business rule enforcement                                 │
│  • Complex calculations (BMI, BMR, TDEE)                     │
│  • Data transformations                                      │
│  • No database access (pure functions)                       │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│               REPOSITORY LAYER                               │
│  PersonRepository: Data access with client isolation         │
│                                                              │
│  Responsibilities:                                           │
│  • Database queries (client-filtered)                        │
│  • CRUD operations                                           │
│  • JPA query methods                                         │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│               MODEL LAYER (Entities)                         │
│  - PersonSimple: Person data with client association         │
│  - ApiLog: API request/response audit logs                   │
│                                                              │
│  Responsibilities:                                           │
│  • Domain model definition                                   │
│  • JPA mapping annotations                                   │
│  • Validation constraints                                    │
└─────────────────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│               DATABASE                                       │
│  H2 (dev) / PostgreSQL (prod)                               │
└─────────────────────────────────────────────────────────────┘
```

---

## Component Responsibilities

### Configuration (`config/`)
- **OpenApiConfig**: Swagger/OpenAPI documentation, security scheme definition
- **WebMvcConfig**: Register interceptors, configure Spring MVC behavior
- **GlobalExceptionHandler**: Formats ResponseStatusException as consistent JSON error responses

### Controllers (`controller/`)
- **HomeController**: Redirect root URL to Swagger UI
- **PersonController**:
  - CRUD operations for persons (client-isolated)
  - Fitness calculation endpoints (BMI, age, calories)
  - Applies client ID filtering to all operations
- **ResearchController**:
  - Aggregated analytics endpoints
  - Enforces research-client-only access control

### Services (`service/`)
- **PersonService**:
  - Pure business logic for fitness calculations
  - No side effects, no database access
  - Implements: BMI, BMR (Mifflin-St Jeor), TDEE, age calculations

### Repositories (`repository/`)
- **PersonRepository**:
  - JPA repository interface
  - Custom queries with client ID filtering
  - Methods: `findByClientId()`, `findByIdAndClientId()`

### Models (`model/`)
- **PersonSimple**: Person entity with client association
- **ApiLog**: Audit log entity for API requests

### Security (`security/`)
- **ClientIdInterceptor**:
  - Extracts `X-Client-ID` header from requests
  - Validates client ID format using ClientContext
  - Stores client ID in thread-local context
  - Rejects invalid/missing client IDs with 400 Bad Request
  - Auto-cleans context after request completion

- **ClientContext**:
  - Consolidated client ID validation and storage
  - Thread-local storage for current request's client ID
  - Validates client ID format (mobile-* or research-*)
  - Determines client type (mobile vs research)
  - Provides static utility methods for client type checks

---

## Client Isolation Architecture

### Authentication Flow

```
1. Client sends request with X-Client-ID header
   ↓
2. ClientIdInterceptor intercepts (/api/** paths only)
   ↓
3. Extract & validate X-Client-ID format
   ↓
4. ClientContext.setClientId(clientId) - thread-local storage
   ↓
5. Request proceeds to controller
   ↓
6. Controller retrieves: ClientContext.getClientId()
   ↓
7. Repository filters queries by client ID
   ↓
8. Response sent (client sees only their data)
   ↓
9. ClientContext.clear() - cleanup after request
```

### Data Isolation Mechanism

**Approach**: Header-based client identification with repository-level filtering

**Client ID Format**: `<type>-<identifier>`
- Mobile clients: `mobile-app1`, `mobile-app2`
- Research clients: `research-tool1`, `research-analytics`

**Isolation Points**:
1. **Repository Layer**: All queries filter by `clientId`
2. **Controller Layer**: Auto-associates new data with requesting client
3. **Access Control**: Research endpoints reject mobile clients (403)

**Data Ownership**:
- Each `PersonSimple` record has a `clientId` field
- Clients can only access records where `clientId` matches their ID
- Cross-client access returns 404 (not found) to prevent information leakage

---

## API Design Patterns

### RESTful Conventions
- Resource-based URLs: `/api/persons`, `/api/research`
- HTTP methods: GET (read), POST (create), PUT (update), DELETE (delete)
- Proper status codes: 200 OK, 201 Created, 400 Bad Request, 403 Forbidden, 404 Not Found

### Client Type Access Matrix

| Endpoint Pattern | Mobile Clients | Research Clients |
|------------------|----------------|------------------|
| `/api/persons/*` | ✅ Full access (own data only) | ✅ Full access (own data only) |
| `/api/research/*` | ❌ 403 Forbidden | ✅ Full access |

### Error Handling Strategy
- Invalid client ID format → 400 Bad Request
- Missing client ID header → 400 Bad Request
- Mobile client accessing research endpoint → 403 Forbidden
- Accessing another client's data → 404 Not Found (security by obscurity)
- Server errors → 500 Internal Server Error

---

## Technology Stack

### Core Framework
- **Spring Boot 3.2.0**: Application framework
- **Spring Web MVC**: REST API implementation
- **Spring Data JPA**: Data persistence abstraction

### Database
- **H2**: In-memory database (development)
- **PostgreSQL**: Relational database (production)
- **Hibernate**: JPA implementation

### API Documentation
- **SpringDoc OpenAPI 3**: Auto-generated API docs
- **Swagger UI**: Interactive API explorer

### Build & Quality
- **Maven**: Dependency management and build
- **Checkstyle**: Code style enforcement
- **JaCoCo**: Code coverage measurement
- **JUnit 5 + Mockito**: Testing framework

---

## Design Principles Applied

1. **Separation of Concerns**: Clear layer boundaries, single responsibility
2. **Dependency Injection**: Spring manages all component dependencies
3. **Open/Closed Principle**: Extensible through interfaces, closed for modification
4. **Data Encapsulation**: JPA entities with proper validation
5. **Security by Design**: Client isolation enforced at repository level
6. **Fail-Fast**: Input validation at controller layer
7. **Thread Safety**: Thread-local context for client ID storage

---

## Extension Points

### Adding New Endpoints
1. Create method in appropriate controller
2. Inject required service/repository
3. Use `ClientContext.getClientId()` for data filtering
4. Add integration tests

### Adding New Entities
1. Create JPA entity class in `model/`
2. Add `clientId` field for isolation
3. Create repository interface in `repository/`
4. Add client-filtered query methods

### Adding New Client Types
1. Update `ClientContext.isValidClientId()` with new prefix pattern
2. Add access control logic in affected controllers
3. Update integration tests

---

## References

- **API Documentation**: http://localhost:8080/swagger-ui.html (when running)
- **Code Comments**: See inline Javadoc in source files for implementation details
- **Testing**: See `src/test/` for comprehensive test examples
- **Configuration**: See `application.yml` for configurable properties
