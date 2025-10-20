# Setup and Testing Guide

## Prerequisites

Before you can run this project, ensure you have the following installed:

- **Java 17** or higher
  - Check version: `java -version`
  - Download from: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)

- **Maven 3.6+**
  - Check version: `mvn -version`
  - Download from: [Apache Maven](https://maven.apache.org/download.cgi)

## Project Overview

This is a Spring Boot fitness management service built with:
- **Framework**: Spring Boot 3.2.0
- **Java Version**: 17
- **Build Tool**: Maven
- **Database**: H2 (in-memory, for development) / PostgreSQL (for production)
- **API Documentation**: Swagger/OpenAPI

## Initial Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd COMSW4156-TeamX
```

### 2. Verify Java and Maven Installation

```bash
java -version   # Should show Java 17 or higher
mvn -version    # Should show Maven 3.6+
```

### 3. Install Dependencies

```bash
mvn clean install
```

This will:
- Download all project dependencies
- Compile the source code
- Run all tests
- Package the application

## Running the Application

### Method 1: Using Maven (Recommended for Development)

```bash
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

### Method 2: Using the JAR File

```bash
# First, build the JAR
mvn clean package

# Then run it
java -jar target/fitness-management-service-1.0.0-SNAPSHOT.jar
```

### Verify the Application is Running

Once started, you should see output similar to:
```
Started FitnessManagementApplication in X.XXX seconds
Tomcat started on port 8080 (http) with context path ''
```

You can verify by visiting:
- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console** (development only): http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:fitnessdb`
  - Username: `sa`
  - Password: (leave empty)

### Stopping the Application

- If running via `mvn spring-boot:run`: Press `Ctrl+C`
- If running via JAR: Press `Ctrl+C` or `kill <process-id>`

## Testing

### Run All Tests

```bash
mvn test
```

### Run Tests with Code Coverage

```bash
mvn clean test
```

The JaCoCo code coverage report will be generated at:
```
target/site/jacoco/index.html
```

Open this file in a browser to view detailed coverage metrics.

### Run Tests and Verify Coverage Threshold

```bash
mvn verify
```

This will fail if code coverage is below 80% (as configured in `pom.xml`).

### Run Specific Test Class

```bash
mvn test -Dtest=PersonServiceTest
```

### Run Specific Test Method

```bash
mvn test -Dtest=PersonServiceTest#testCalculateBMI
```

## Code Quality Checks

### Checkstyle

Checkstyle ensures code follows consistent style guidelines:

```bash
mvn checkstyle:check
```

Configuration file: `checkstyle.xml`

### PMD

PMD performs static code analysis to find potential bugs:

```bash
mvn pmd:check
```

### Run All Quality Checks

```bash
mvn clean verify
```

This runs:
1. Checkstyle validation
2. Compilation
3. All tests
4. JaCoCo coverage check
5. PMD analysis

## Build Commands

### Clean Build

```bash
mvn clean
```

Removes the `target/` directory.

### Compile Only

```bash
mvn compile
```

### Package (Create JAR)

```bash
mvn package
```

Creates JAR file in `target/` directory.

### Install to Local Maven Repository

```bash
mvn install
```

## API Endpoints

Once the application is running, you can access:

### Swagger UI (Interactive API Documentation)

**URL**: http://localhost:8080/swagger-ui.html

This provides an interactive interface to:
- View all available endpoints
- Test API calls directly from the browser
- See request/response schemas

### Person Management Endpoints

- `GET /api/persons` - Get all persons
- `GET /api/persons/{id}` - Get person by ID
- `POST /api/persons` - Create new person
- `PUT /api/persons/{id}` - Update person
- `DELETE /api/persons/{id}` - Delete person
- `GET /api/persons/{id}/bmi` - Calculate BMI
- `GET /api/persons/{id}/bmr` - Calculate BMR
- `GET /api/persons/{id}/tdee` - Calculate TDEE

### Research Endpoints

- `GET /api/research/health-metrics` - Get anonymized health metrics
- `GET /api/research/nutrition-data` - Get nutrition data
- `GET /api/research/exercise-data` - Get exercise data

## Database

### Development Database (H2)

- **Type**: In-memory database
- **URL**: `jdbc:h2:mem:fitnessdb`
- **Console**: http://localhost:8080/h2-console
- **Note**: Data is lost when application stops

### Production Database (PostgreSQL)

To use PostgreSQL in production:

1. Update `src/main/resources/application-prod.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fitnessdb
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
```

2. Run with production profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Troubleshooting

### Issue: Port 8080 Already in Use

**Solution**: Either stop the process using port 8080 or change the port:

```bash
# Find process using port 8080 (macOS/Linux)
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or run on different port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Issue: Java Version Mismatch

**Error**: `Unsupported class file major version`

**Solution**: Ensure you're using Java 17:
```bash
java -version
```

If using multiple Java versions, set `JAVA_HOME`:
```bash
export JAVA_HOME=/path/to/java17
```

### Issue: Maven Build Fails with "Table Not Found"

**Solution**: This was fixed in the latest version. If you still encounter this, ensure `data.sql` only references existing entity tables.

### Issue: Tests Failing

**Solution**:
```bash
# Clean and rebuild
mvn clean compile
mvn test

# If still failing, check for specific test errors
mvn test -X  # Debug mode
```

## Development Workflow

### Typical Development Cycle

1. **Make code changes**
2. **Run tests**: `mvn test`
3. **Check code style**: `mvn checkstyle:check`
4. **Run application**: `mvn spring-boot:run`
5. **Test via Swagger UI**: http://localhost:8080/swagger-ui.html
6. **Commit changes**

### Hot Reload (Optional)

For automatic application restart on code changes, add to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

## Logging

### Log Files

Application logs are written to:
- **Console**: Standard output
- **File**: `logs/fitness-app.log`

### Change Log Level

Edit `src/main/resources/application.yml`:

```yaml
logging:
  level:
    root: INFO
    com.teamx.fitness: DEBUG
```

Or pass as runtime argument:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--logging.level.com.teamx.fitness=TRACE
```

## Additional Resources

- **Spring Boot Documentation**: https://docs.spring.io/spring-boot/docs/current/reference/html/
- **Maven Documentation**: https://maven.apache.org/guides/
- **JaCoCo Coverage**: https://www.jacoco.org/jacoco/trunk/doc/
- **Checkstyle**: https://checkstyle.sourceforge.io/

## Known Issues

### Current Limitations

1. **Limited Entities**: Currently only `PersonSimple` and `ApiLog` entities are implemented. Future entities (Exercise, Food, Goal) are planned.

2. **Sample Data**: The `data.sql` file contains commented-out sample data for future entities. Uncomment these as corresponding entities are implemented.

3. **Checkstyle Warnings**: Some non-critical Checkstyle warnings exist (magic numbers, missing Javadoc). These are tracked for future cleanup.

## Support

For issues or questions:
- Check existing issues in the GitHub repository
- Create a new issue with detailed description
- Contact team members (see README.md)
