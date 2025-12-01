# Continuous Integration (CI) Setup

This document describes the Continuous Integration (CI) pipeline configuration for the Personal Fitness Management Service.

## Overview

The CI pipeline automates code quality checks, static analysis, and testing on every push and pull request. It ensures code quality standards are maintained and all tests pass before code is merged.

## CI Pipeline Components

### 1. Code Quality Checks

#### Checkstyle
- **Purpose**: Enforces Java code style conventions
- **Configuration**: `checkstyle.xml`
- **Command**: `mvn checkstyle:check`
- **Report**: `target/checkstyle-result.xml`
- **Threshold**: 0 violations (warnings allowed but reported)

#### PMD
- **Purpose**: Static code analysis to detect potential bugs, code smells, and design issues
- **Configuration**: Default PMD rulesets
- **Command**: `mvn pmd:check`
- **Report**: `target/pmd.xml`, `target/site/pmd.html`
- **Threshold**: Violations are reported but do not fail the build

### 2. Testing

#### Unit and Integration Tests
- **Framework**: JUnit 5, Mockito, Spring Test
- **Command**: `mvn clean test`
- **Report**: `target/surefire-reports/`
- **Coverage Tool**: JaCoCo
- **Coverage Report**: `target/site/jacoco/index.html`
- **Coverage Threshold**: Minimum 80% line coverage

#### Test Execution
- All tests must pass for CI to succeed
- Test results are archived as artifacts
- Coverage reports are generated and archived

### 3. Build

- **Command**: `mvn clean package -DskipTests`
- **Output**: JAR file in `target/`
- **Artifact**: Application JAR is archived for deployment

## GitHub Actions Workflow

### Workflow File
- **Location**: `.github/workflows/ci.yml`
- **Triggers**:
  - Push to `main` or `develop` branches
  - Pull requests to `main` or `develop` branches
  - Manual workflow dispatch (via GitHub UI)

### Jobs

1. **code-quality**: Runs Checkstyle and PMD analysis
2. **test**: Runs unit/integration tests and generates coverage reports
3. **build**: Builds the application (depends on code-quality and test)
4. **generate-reports**: Aggregates all reports and commits them to the repository

### Artifacts

All reports are archived as GitHub Actions artifacts:
- Checkstyle results (30 days retention)
- PMD results (30 days retention)
- Test results (30 days retention)
- Coverage reports (30 days retention)
- Application JAR (7 days retention)

## Local CI Execution

### Using Scripts

**Linux/Mac:**
```bash
bash scripts/ci-local.sh
```

**Windows PowerShell:**
```powershell
.\scripts\ci-local.ps1
```

### Manual Execution

Run each step individually:

```bash
# 1. Code Quality
mvn checkstyle:checkstyle checkstyle:check
mvn pmd:check

# 2. Tests
mvn clean test jacoco:report
mvn jacoco:check

# 3. Build
mvn clean package -DskipTests
```

## CI Reports

### Location
All CI reports are stored in the `ci-reports/` directory:
- `checkstyle/` - Checkstyle analysis results
- `pmd/` - PMD static analysis results
- `test/` - Test execution results
- `coverage/` - Code coverage HTML reports
- `ci-summary.md` - Pipeline execution summary

### Viewing Reports

- **Checkstyle**: Open `ci-reports/checkstyle/checkstyle-result.xml`
- **PMD**: Open `ci-reports/pmd/pmd.html` in a web browser
- **Coverage**: Open `ci-reports/coverage/index.html` in a web browser
- **Test Results**: See `ci-reports/test/surefire-reports/`

## Automated vs Manual Testing

### Automated in CI

✅ **Code Style Checks** (Checkstyle)
- Fully automated
- Runs on every commit

✅ **Static Analysis** (PMD)
- Fully automated
- Runs on every commit

✅ **Unit Tests**
- Fully automated
- Runs on every commit
- 106 tests currently

✅ **Integration Tests**
- Fully automated
- Runs on every commit

✅ **Code Coverage**
- Fully automated
- Validates minimum 80% line coverage
- Generates HTML reports

### Manual (Not Automated)

⚠️ **End-to-End (E2E) Testing**
- **Reason**: Requires manual interaction with web browsers and client applications
- **Location**: See `docs/E2E_TESTING.md`
- **Procedure**: Manual step-by-step testing with verification checklists

⚠️ **Postman/Newman API Tests**
- **Reason**: Requires running backend service and database infrastructure
- **Manual Command**: 
  ```bash
  docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm newman
  ```
- **Location**: `postman/fitness-api-tests.postman_collection.json`

## CI Status Badge

To add a CI status badge to your README, use:

```markdown
![CI](https://github.com/YOUR_USERNAME/YOUR_REPO/workflows/CI%20-%20Code%20Quality%20and%20Testing/badge.svg)
```

## Troubleshooting

### CI Fails on Checkstyle
- Check `ci-reports/checkstyle/checkstyle-result.xml` for violations
- Fix code style issues
- Run `mvn checkstyle:check` locally before pushing

### CI Fails on PMD
- Check `ci-reports/pmd/pmd.xml` for violations
- Review PMD suggestions and fix issues
- Run `mvn pmd:check` locally before pushing

### CI Fails on Tests
- Check `ci-reports/test/surefire-reports/` for test failures
- Run `mvn test` locally to reproduce
- Fix failing tests before pushing

### CI Fails on Coverage
- Check `ci-reports/coverage/index.html` for coverage details
- Ensure minimum 80% line coverage
- Add tests for uncovered code
- Run `mvn jacoco:check` locally

## Best Practices

1. **Run CI Locally First**: Always run `scripts/ci-local.sh` (or `.ps1`) before pushing
2. **Fix Issues Early**: Address code quality issues as they arise
3. **Maintain Coverage**: Keep test coverage above 80%
4. **Review Reports**: Regularly review CI reports to identify code quality trends
5. **Update Thresholds**: Adjust coverage thresholds in `pom.xml` if needed

## Configuration Files

- **CI Workflow**: `.github/workflows/ci.yml`
- **Checkstyle Config**: `checkstyle.xml`
- **PMD Config**: Default rulesets (configurable in `pom.xml`)
- **JaCoCo Config**: `pom.xml` (coverage threshold: 80%)
- **Maven Config**: `pom.xml`

## Future Enhancements

Potential improvements to the CI pipeline:
- [ ] Add SonarQube integration for advanced code quality metrics
- [ ] Add dependency vulnerability scanning (OWASP Dependency-Check)
- [ ] Add automated security scanning
- [ ] Add performance testing in CI
- [ ] Add automated API documentation generation
- [ ] Add automated deployment to staging environment

