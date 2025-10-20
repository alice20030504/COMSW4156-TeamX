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

## API Documentation

### API Entry Points

The service provides 9 RESTful API endpoints organized into two controllers:

#### Person Controller (`/api/persons`)

Fitness calculation endpoints:

| Endpoint | Method | Description | Parameters | Response |
|----------|--------|-------------|------------|----------|
| `/api/persons/bmi` | GET | Calculate BMI | weight (Double), height (Double) | JSON with BMI and category |
| `/api/persons/age` | GET | Calculate age | birthDate (String, YYYY-MM-DD) | JSON with age |
| `/api/persons/calories` | GET | Calculate daily calorie needs | weight, height, age, gender, weeklyTrainingFreq | JSON with BMR and TDEE |
| `/api/persons/health` | GET | Service health check | None | JSON with service status |

#### Research Controller (`/api/research`)

Research and analytics endpoints (mock data for iteration 1):

| Endpoint | Method | Description | Response |
|----------|--------|-------------|----------|
| `/api/research/workout-patterns` | GET | Aggregated workout data | JSON with patterns and statistics |
| `/api/research/population-health` | GET | Population health metrics | JSON with demographics and health data |
| `/api/research/nutrition-trends` | GET | Nutrition trends | JSON with macro and calorie distribution |
| `/api/research/demographics` | GET | Demographic information | JSON with demographic data |

#### Home Controller

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Redirects to Swagger UI |

### Detailed API Documentation

For complete API documentation including:
- Input/output specifications
- Example requests and responses
- Error codes and handling
- Usage examples (cURL, JavaScript, Python)

**See**: [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md)

### Interactive Testing

**Swagger UI**: http://localhost:8080/swagger-ui.html (when application is running)

The Swagger UI provides:
- Complete API reference
- Interactive "Try it out" functionality
- Request/response schemas
- Example values

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

## Project Structure

```
COMSW4156-TeamX/
├── .git/                           # Git version control
├── .github/                        # GitHub configuration (CI/CD planned for iteration 2)
├── docs/                           # Documentation
│   ├── API_DOCUMENTATION.md        # Complete API reference
│   ├── SETUP_AND_TESTING.md        # Setup and testing guide
│   ├── FIRST_ITERATION_REPORT.md   # Iteration 1 status report
│   ├── STYLE_CHECK_SUMMARY.md      # Style checking results
│   └── AI_USAGE.md                 # AI tool usage documentation
├── logs/                           # Application logs
│   └── fitness-app.log             # Runtime logs
├── src/
│   ├── main/
│   │   ├── java/com/teamx/fitness/
│   │   │   ├── FitnessManagementApplication.java  # Main entry point
│   │   │   ├── controller/         # REST API controllers
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── PersonController.java
│   │   │   │   └── ResearchController.java
│   │   │   ├── service/            # Business logic layer
│   │   │   │   └── PersonService.java
│   │   │   └── model/              # JPA entities
│   │   │       ├── PersonSimple.java
│   │   │       └── ApiLog.java
│   │   └── resources/
│   │       ├── application.yml     # Main configuration
│   │       ├── application-prod.yml # Production config
│   │       └── data.sql            # Sample data initialization
│   └── test/                       # Test files (to be implemented in iteration 2)
│       └── java/com/teamx/fitness/
├── target/                         # Build output (generated)
│   ├── classes/                    # Compiled classes
│   ├── site/                       # Generated reports
│   │   ├── checkstyle.html         # Style check report
│   │   └── jacoco/                 # Coverage reports
│   └── *.jar                       # Packaged application
├── checkstyle.xml                  # Checkstyle configuration
├── pom.xml                         # Maven build configuration
├── LICENSE                         # MIT License
└── README.md                       # This file
```

### Architecture

The application follows a standard 3-tier architecture:

```
┌─────────────────────┐
│   Controllers       │  <- REST API endpoints, request/response handling
│  (Presentation)     │
└─────────┬───────────┘
          │
┌─────────▼───────────┐
│    Services         │  <- Business logic, calculations, orchestration
│  (Business Logic)   │
└─────────┬───────────┘
          │
┌─────────▼───────────┐
│     Models          │  <- Data entities, JPA mappings
│   (Data Layer)      │
└─────────────────────┘
```

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

## AI Tool Usage

### Tools Used

**Claude Code** (Anthropic Claude 3.7 Sonnet via CLI)
- **Cost**: Free tier (no charges)
- **Access**: https://claude.com/claude-code
- **Usage**: Documentation generation, bug diagnosis, technical explanations

### AI-Generated Code

The following code was generated or significantly assisted by AI:

1. **Documentation** (85% AI-generated, 15% human-edited):
   - `docs/API_DOCUMENTATION.md`
   - `docs/SETUP_AND_TESTING.md`
   - `docs/FIRST_ITERATION_REPORT.md`
   - `docs/AI_USAGE.md`
   - `docs/STYLE_CHECK_SUMMARY.md`
   - `README.md` (this file)

2. **Bug Fixes** (80% AI-suggested, 20% human-reviewed):
   - Fix for `data.sql` initialization error
   - `HomeController.java` (404 error resolution)

### Human-Written Code

All core application code was written by human team members:
- All controllers (PersonController, ResearchController)
- All services (PersonService)
- All models (PersonSimple, ApiLog)
- All configuration files (pom.xml, application.yml)
- All business logic and algorithms

### Complete AI Usage Documentation

**See**: [docs/AI_USAGE.md](docs/AI_USAGE.md) for detailed documentation including:
- Specific prompts used
- Code samples generated
- Human review process
- Learning outcomes
- Ethical considerations

---

## Documentation

### External Documentation

| Document | Purpose | Location |
|----------|---------|----------|
| README.md | Main project documentation | This file |
| API_DOCUMENTATION.md | Complete API reference | [docs/](docs/API_DOCUMENTATION.md) |
| SETUP_AND_TESTING.md | Setup, build, run, test instructions | [docs/](docs/SETUP_AND_TESTING.md) |
| FIRST_ITERATION_REPORT.md | Iteration 1 status and assessment | [docs/](docs/FIRST_ITERATION_REPORT.md) |
| STYLE_CHECK_SUMMARY.md | Code style analysis and results | [docs/](docs/STYLE_CHECK_SUMMARY.md) |
| AI_USAGE.md | AI tool usage documentation | [docs/](docs/AI_USAGE.md) |

### Internal Documentation

All code includes comprehensive Javadoc:
- Class-level documentation
- Method-level documentation with parameters and return values
- Inline comments where needed
- Examples and usage notes

### API Documentation

**Swagger/OpenAPI**: Automatically generated from code annotations

**Access**: http://localhost:8080/swagger-ui.html (when running)

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
