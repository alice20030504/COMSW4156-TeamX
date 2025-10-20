# Personal Fitness Management Service

**Course**: COMSW4156 Advanced Software Engineering
**Team**: TeamX
**Semester**: Fall 2025
**Iteration**: 1 of 2

---

## Table of Contents
1. [Overview](#overview)
2. [Quick Start](#quick-start)
3. [API Documentation](#api-documentation)
4. [Building, Running, and Testing](#building-running-and-testing)
5. [Tools and Configuration](#tools-and-configuration)
6. [Project Structure](#project-structure)
7. [Team Members](#team-members)
8. [Project Management](#project-management)
9. [Third-Party Code](#third-party-code)
10. [AI Tool Usage](#ai-tool-usage)
11. [Documentation](#documentation)
12. [License](#license)

---

## Overview

The Personal Fitness Management Service is a RESTful API built with Spring Boot that provides fitness calculations, health metrics, and aggregated research data for fitness and nutrition tracking.

### Key Features (Iteration 1)

- ✅ **Fitness Calculations**: BMI, BMR, TDEE, age calculations
- ✅ **Research Analytics**: Aggregated workout patterns, population health metrics
- ✅ **Client Isolation**: Multi-client data separation with per-client authentication
- ✅ **Access Control**: Client-type based authorization (mobile vs research)
- ✅ **Interactive API Documentation**: Swagger UI for testing and exploration
- ✅ **Database Integration**: H2 (development) and PostgreSQL (production) support
- ✅ **API Logging**: Comprehensive request/response logging
- ✅ **Code Quality**: Checkstyle integration, Javadoc documentation

### Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Maven 3.9+
- **Database**: H2 (in-memory) / PostgreSQL (production)
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger UI)
- **Testing**: JUnit 5, Mockito, REST Assured (configured for iteration 2)
- **Code Quality**: Checkstyle 10.12.5, PMD 6.55.0, JaCoCo 0.8.11

---

## Quick Start

### Prerequisites

- **Java 17 or higher** - [Download JDK](https://adoptium.net/)
- **Maven 3.6+** - [Download Maven](https://maven.apache.org/download.cgi)

### Installation and Running

```bash
# Clone the repository
git clone https://github.com/[your-org]/COMSW4156-TeamX.git
cd COMSW4156-TeamX

# Build and run
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

### First-Time Verification

1. **Check Application Health**:
   ```bash
   curl http://localhost:8080/api/persons/health
   ```

2. **Open Swagger UI**:
   - Navigate to http://localhost:8080/swagger-ui.html
   - Or visit http://localhost:8080/ (auto-redirects to Swagger)

3. **Test an Endpoint**:
   ```bash
   curl "http://localhost:8080/api/persons/bmi?weight=70&height=175"
   ```

---

## Client Authentication and Data Isolation

### Overview

The service implements a header-based client authentication system that ensures:
1. **Data Isolation**: Each client can only access their own data
2. **Client Type Differentiation**: Mobile apps and research tools have different access levels
3. **Access Control**: Research endpoints are restricted to research-tool clients only

### Authentication Method

All API requests (except `/`, `/swagger-ui`, and `/actuator`) require the `X-Client-ID` header.

**Header Format**:
```
X-Client-ID: <client-type>-<identifier>
```

**Valid Client ID Patterns**:
- **Mobile Clients**: `mobile-*` (e.g., `mobile-app1`, `mobile-app2`, `mobile-beta`)
- **Research Clients**: `research-*` (e.g., `research-tool1`, `research-analytics`)

### Example Requests

**Mobile Client Request**:
```bash
curl -H "X-Client-ID: mobile-app1" \
  http://localhost:8080/api/persons
```

**Research Client Request**:
```bash
curl -H "X-Client-ID: research-tool1" \
  http://localhost:8080/api/research/demographics
```

**Invalid Request (missing header)**:
```bash
curl http://localhost:8080/api/persons
# Response: 400 Bad Request
# {"error":"Bad Request","message":"X-Client-ID header is required","status":400}
```

### Data Isolation

Each client's data is completely isolated:

1. **Separate Data Spaces**:
   - `mobile-app1` can only see data created by `mobile-app1`
   - `mobile-app2` can only see data created by `mobile-app2`
   - Data posted by one client is invisible to all other clients

2. **Protected Operations**:
   - GET: Returns only the client's own data
   - POST: Automatically associates new data with the requesting client
   - PUT: Only allows updating the client's own data (404 for others)
   - DELETE: Only allows deleting the client's own data (404 for others)

3. **Example Scenario**:
   ```bash
   # Client 1 creates a person
   curl -X POST -H "X-Client-ID: mobile-app1" \
     -H "Content-Type: application/json" \
     -d '{"name":"Alice","weight":65,"height":170,"birthDate":"1990-05-15"}' \
     http://localhost:8080/api/persons

   # Client 1 can see Alice
   curl -H "X-Client-ID: mobile-app1" http://localhost:8080/api/persons
   # Returns: [{"id":1,"name":"Alice",...,"clientId":"mobile-app1"}]

   # Client 2 cannot see Alice
   curl -H "X-Client-ID: mobile-app2" http://localhost:8080/api/persons
   # Returns: []
   ```

### Access Control by Client Type

| Endpoint | Mobile Clients | Research Clients |
|----------|----------------|------------------|
| `/api/persons` | ✅ Full Access | ✅ Full Access |
| `/api/persons/bmi` | ✅ Allowed | ✅ Allowed |
| `/api/persons/calories` | ✅ Allowed | ✅ Allowed |
| `/api/research/*` | ❌ 403 Forbidden | ✅ Allowed |

**Mobile Client Accessing Research Endpoint**:
```bash
curl -H "X-Client-ID: mobile-app1" \
  http://localhost:8080/api/research/demographics
# Response: 403 Forbidden
# {"error":"Forbidden","message":"Mobile clients are not authorized to access research endpoints..."}
```

### Testing Client Isolation

The project includes comprehensive integration tests demonstrating:
- Data isolation between multiple mobile clients
- Cross-client access prevention
- Research endpoint access control
- Invalid client ID rejection

**Run Tests**:
```bash
# All client isolation tests
mvn test -Dtest=ClientIsolationIntegrationTest

# Research access control tests
mvn test -Dtest=ResearchEndpointAccessControlTest
```

**Test Coverage**: 21 integration tests covering all client isolation scenarios

---

## API Documentation

**Complete API Documentation**: http://localhost:8080/swagger-ui.html (when running)

The Swagger UI provides:
- Complete, always up-to-date API reference
- Interactive "Try it out" functionality for all endpoints
- Request/response schemas with examples
- Authentication requirements (X-Client-ID header)
- Error code documentation

### Quick API Overview

**Person Endpoints** (`/api/persons`)
- CRUD operations for persons (client-isolated)
- Fitness calculations: BMI, age, daily calorie needs
- All require `X-Client-ID` header

**Research Endpoints** (`/api/research`)
- Aggregated analytics: demographics, workout patterns, nutrition trends
- Research-tool clients only (mobile clients get 403)

**Home** (`/`)
- Redirects to Swagger UI

For detailed endpoint specifications, parameters, and examples, use Swagger UI when the application is running.

---

## Building, Running, and Testing

### Build Commands

```bash
# Clean build
mvn clean install

# Compile only
mvn compile

# Package as JAR
mvn package

# Skip tests during build
mvn install -DskipTests
```

### Running the Application

**Method 1: Maven** (Development)
```bash
mvn spring-boot:run
```

**Method 2: JAR File** (Production)
```bash
# Build JAR first
mvn clean package

# Run the JAR
java -jar target/fitness-management-service-1.0.0-SNAPSHOT.jar
```

**Method 3: Different Port**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Testing

**Note**: Comprehensive test suite will be implemented in Iteration 2.

```bash
# Run all tests (when implemented)
mvn test

# Run with coverage report
mvn clean test

# Run specific test class
mvn test -Dtest=PersonServiceTest

# View coverage report (after running tests)
open target/site/jacoco/index.html  # macOS
```

**Test Configuration Files**:
- Testing framework: JUnit 5 (configured in `pom.xml`)
- Mocking framework: Mockito (configured in `pom.xml`)
- API testing tool: REST Assured (configured in `pom.xml`)
- Coverage tool: JaCoCo (configured in `pom.xml`)

**Coverage Goals**:
- Iteration 1: Framework configured (tests TBD)
- Iteration 2: 80% branch coverage required

### Configuration Files

| File | Purpose | Location |
|------|---------|----------|
| `pom.xml` | Maven build configuration, dependencies | Root directory |
| `application.yml` | Spring Boot application settings | `src/main/resources/` |
| `application-prod.yml` | Production-specific settings | `src/main/resources/` |
| `checkstyle.xml` | Code style rules | Root directory |
| `data.sql` | Sample database initialization | `src/main/resources/` |

---

## Tools and Configuration

### Style Checker

**Tool**: Checkstyle 10.12.5
**Ruleset**: Based on Google Java Style Guide
**Configuration**: `checkstyle.xml` in repository root

**Running Style Check**:
```bash
# Check for violations
mvn checkstyle:check

# Generate HTML report
mvn checkstyle:checkstyle

# View report
open target/site/checkstyle.html  # macOS
xdg-open target/site/checkstyle.html  # Linux
```

**Current Status**:
- ✅ 0 Errors (build succeeds)
- ⚠️ 113 Warnings (non-critical, to be addressed in iteration 2)
- Report location: `target/site/checkstyle.html`

**See**: [docs/STYLE_CHECK_SUMMARY.md](docs/STYLE_CHECK_SUMMARY.md) for detailed analysis

### Testing Frameworks

| Framework | Version | Purpose | Configuration |
|-----------|---------|---------|---------------|
| JUnit 5 | 5.9.3 | Unit testing | `pom.xml` |
| Mockito | 5.3.1 | Mocking | `pom.xml` |
| REST Assured | 5.4.0 | API testing | `pom.xml` |
| Spring Boot Test | 3.2.0 | Integration testing | `pom.xml` |

**Test Runner**: Maven Surefire Plugin (push-button via `mvn test`)

### Code Coverage

**Tool**: JaCoCo 0.8.11
**Configuration**: `pom.xml` (jacoco-maven-plugin)
**Report Location**: `target/site/jacoco/index.html` (after running `mvn test`)
**Minimum Coverage**: 80% (enforced in iteration 2)

```bash
# Generate coverage report
mvn clean test

# View report
open target/site/jacoco/index.html
```

### Static Analysis

**PMD**: Configured in `pom.xml` for additional static analysis
```bash
mvn pmd:check
mvn pmd:pmd  # Generate report
```

---

## Project Structure and Architecture

**Complete documentation**: See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

### Quick Overview

The project follows a layered architecture pattern:

**Layers:**
- **Controllers** (`controller/`) - REST API endpoints with client isolation
- **Services** (`service/`) - Business logic and calculations
- **Repositories** (`repository/`) - Data access with client filtering
- **Models** (`model/`) - JPA entities (PersonSimple, ApiLog)
- **Cross-Cutting** - Interceptors, context, exception handling, utilities

**Key Directories:**
- `src/main/java/` - Application source code (layered architecture)
- `src/test/java/` - Unit and integration tests
- `src/main/resources/` - Configuration files (application.yml, data.sql)
- `docs/` - Architecture, reports, and iteration documentation
- `target/` - Build output and generated reports

For detailed code hierarchy, component responsibilities, and design patterns, refer to [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## Team Members

| Name | Role | GitHub | Email | Responsibilities |
|------|------|--------|-------|------------------|
| Shurong Zhang (Alice) | Developer | [@alice20030504](https://github.com/alice20030504) | - | Backend development, API design |
| Luojie Wang (Roger) | Developer | [@2025FALLCOMSW4156](https://github.com/2025FALLCOMSW4156) | - | Service layer, fitness calculations |
| Shenghang Luo (Peter) | Developer | [@shenghangluo](https://github.com/shenghangluo) | - | Data models, database setup |
| Chang Ma (Mercury) | Developer | [@MercuryCod](https://github.com/MercuryCod) | - | Documentation, testing infrastructure |

### Contribution Guidelines

- All pull requests must be reviewed by at least one other team member
- Meaningful commit messages required (no "update", "fix", etc.)
- Branch protection rules enforced on `main` branch
- Feature branches merged to `dev`, then to `main`

---

## Project Management

### Tool

**GitHub Projects** (built-in project management)

**Access**: [Repository Projects Tab](https://github.com/[your-org]/COMSW4156-TeamX/projects)

### Current Sprint (Iteration 1)

**Status Board**:
- ✅ Repository setup and configuration
- ✅ Maven build system setup
- ✅ Implement Person Controller endpoints
- ✅ Implement Research Controller endpoints
- ✅ Create Home Controller
- ✅ Add Swagger/OpenAPI documentation
- ✅ Fix database initialization errors
- ✅ Create comprehensive documentation
- ✅ Style checking integration
- 🔄 Implement unit tests (Iteration 2)
- 🔄 Implement API tests (Iteration 2)
- 🔄 Achieve 80% code coverage (Iteration 2)

### Task Assignment

Tasks are tracked via GitHub Issues and Projects, with assignments visible in the project board.

**Example Completed Tasks**:
- Fix data.sql initialization error (Mercury)
- Implement BMI calculation endpoint (Roger)
- Create API documentation (Mercury)
- Setup database entities (Peter)
- Implement research endpoints (Alice)

---

## Third-Party Code

### Dependencies Management

All third-party code is managed via **Maven** and fetched automatically from Maven Central Repository. No third-party code is directly included in the repository codebase.

### Key Dependencies

| Dependency | Version | Purpose | License | Source |
|------------|---------|---------|---------|--------|
| Spring Boot | 3.2.0 | Web framework, dependency injection | Apache 2.0 | https://spring.io/projects/spring-boot |
| Spring Data JPA | 3.2.0 | Database access and ORM | Apache 2.0 | https://spring.io/projects/spring-data-jpa |
| H2 Database | 2.2.224 | In-memory database for development | MPL 2.0 / EPL 1.0 | https://h2database.com |
| PostgreSQL Driver | 42.6.0 | Production database driver | BSD 2-Clause | https://jdbc.postgresql.org |
| SpringDoc OpenAPI | 2.3.0 | API documentation (Swagger UI) | Apache 2.0 | https://springdoc.org |
| Lombok | 1.18.30 | Reduce boilerplate code | MIT | https://projectlombok.org |
| JUnit 5 | 5.9.3 | Unit testing framework | EPL 2.0 | https://junit.org/junit5/ |
| Mockito | 5.3.1 | Mocking framework for tests | MIT | https://site.mockito.org |
| REST Assured | 5.4.0 | REST API testing | Apache 2.0 | https://rest-assured.io |
| JaCoCo | 0.8.11 | Code coverage analysis | EPL 2.0 | https://www.jacoco.org |
| Checkstyle | 10.12.5 | Code style checking | LGPL 2.1 | https://checkstyle.org |
| PMD | 6.55.0 | Static code analysis | Apache 2.0 | https://pmd.github.io |

### Complete Dependency List

See `pom.xml` for the complete list of dependencies with exact versions.

### Dependency Installation

```bash
# Install all dependencies
mvn clean install

# Update dependencies
mvn dependency:resolve

# View dependency tree
mvn dependency:tree
```

---

## Development Process

### Tools and Assistance Used

Development involved both human team effort and AI assistance:

**AI Tools**: Claude Code (free tier) was used for:
- Documentation generation and formatting
- Bug diagnosis assistance
- Technical explanation of frameworks

**Human Development**: All core code written by team members:
- Controllers, services, repositories, models
- Business logic and algorithms
- Architecture and design decisions
- Testing strategy and implementation

---

## Documentation

### Documentation Structure (Single Source of Truth)

| Document | Purpose | Source of Truth |
|----------|---------|----------------|
| **README.md** | Project overview and quick start | This file |
| **Swagger UI** | Complete API documentation | http://localhost:8080/swagger-ui.html (auto-generated) |
| **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** | Code hierarchy and design patterns | Architecture doc |
| **[docs/FIRST_ITERATION_REPORT.md](docs/FIRST_ITERATION_REPORT.md)** | Iteration status report | Report doc |
| **[docs/STYLE_CHECK_SUMMARY.md](docs/STYLE_CHECK_SUMMARY.md)** | Checkstyle results | Report doc |
| **[docs/TESTING_RESULTS.md](docs/TESTING_RESULTS.md)** | Test execution results | Report doc |
| **Code Comments** | Implementation details | Inline Javadoc |

### Where to Find Information

- **API Endpoints**: Swagger UI (runtime only)
- **Architecture**: `docs/ARCHITECTURE.md`
- **Setup & Running**: README sections above
- **Testing**: `mvn test` commands in README
- **Reports**: `docs/*.md` files
- **Implementation Details**: Source code comments

---

## Version Control

### Repository

**Platform**: GitHub
**URL**: https://github.com/[your-org]/COMSW4156-TeamX

### Branching Strategy

- `main`: Production-ready code (protected branch)
- `dev`: Integration branch for features
- `feature/*`: Feature development branches

### Commit Guidelines

✅ **Good commit messages**:
```
Add BMI calculation endpoint with category classification
Fix data.sql to only reference existing entities
Implement research controller with mock data
Create comprehensive API documentation
```

❌ **Bad commit messages** (avoid):
```
update
fixed stuff
changes
wip
```

### Tagging

**First Iteration Release**: Tagged as `v1.0.0-iteration1`

```bash
# View tags
git tag

# Checkout iteration 1 code
git checkout v1.0.0-iteration1
```

---

## Database

### Development Database

**Type**: H2 (in-memory)
**Configuration**: `application.yml`
**Access**: http://localhost:8080/h2-console (when running)

**Connection Details**:
- URL: `jdbc:h2:mem:fitnessdb`
- Username: `sa`
- Password: (leave empty)

### Production Database

**Type**: PostgreSQL
**Configuration**: `application-prod.yml`

To use PostgreSQL:
1. Update `application-prod.yml` with your database credentials
2. Run with production profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

### Database Entities

| Entity | Table Name | Purpose |
|--------|-----------|---------|
| PersonSimple | persons_simple | Store person information |
| ApiLog | api_logs | Log API requests/responses |

---

## Troubleshooting

### Common Issues

**Issue**: Port 8080 already in use
```bash
# Solution: Find and kill the process
lsof -i :8080
kill -9 <PID>

# Or run on different port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

**Issue**: Java version mismatch
```bash
# Check version
java -version  # Must be 17 or higher

# Set JAVA_HOME (if needed)
export JAVA_HOME=/path/to/java17
```

**Issue**: Build fails
```bash
# Clean and rebuild
mvn clean install

# Skip tests if needed
mvn clean install -DskipTests
```

For more troubleshooting, see: [docs/SETUP_AND_TESTING.md](docs/SETUP_AND_TESTING.md)

---

## Monitoring and Logging

### Application Logs

**Location**: `logs/fitness-app.log`
**Console**: Standard output

### Log Levels

Configured in `application.yml`:
- Root: INFO
- Application (com.teamx.fitness): DEBUG
- Spring Web: DEBUG
- Hibernate SQL: DEBUG

### Change Log Level at Runtime

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--logging.level.com.teamx.fitness=TRACE
```

---

## Future Work (Iteration 2)

### Planned Features

1. **Testing** (Highest Priority)
   - Comprehensive unit tests (3+ per unit)
   - API/system tests (3+ per endpoint)
   - 80% branch coverage minimum
   - Test logging and multi-client scenarios

2. **Code Quality**
   - Fix all Checkstyle warnings
   - Implement CI/CD (GitHub Actions)
   - Enhanced static analysis

3. **New Features**
   - User authentication (Spring Security)
   - CRUD operations for Person entity
   - Exercise and meal tracking
   - Goal setting and tracking

4. **Sample Client**
   - Web frontend or CLI demonstrating API usage

---

## License

This project is licensed under the **MIT License**.

See the [LICENSE](LICENSE) file for full details.

---

## Contact and Support

### For Issues

- **GitHub Issues**: [Repository Issues Page](https://github.com/[your-org]/COMSW4156-TeamX/issues)
- **Pull Requests**: [Repository PRs](https://github.com/[your-org]/COMSW4156-TeamX/pulls)

### For Questions

Contact any team member listed in the [Team Members](#team-members) section.

### Course Mentor

- Access provided to course mentor for code review and grading

---

## Acknowledgments

- **Spring Boot**: Simplified application development
- **SpringDoc**: Automatic API documentation
- **Google**: Java Style Guide (Checkstyle basis)
- **Columbia University**: COMSW4156 course structure and guidance

---

**Document Version**: 1.0
**Last Updated**: 2025-10-16
**Iteration**: 1 of 2
**Status**: Ready for Submission ✅
