# Local CI script for Windows PowerShell
# Run all code quality checks and tests locally
# This mimics the GitHub Actions CI pipeline

$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Running Local CI Pipeline" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# Create reports directory
New-Item -ItemType Directory -Force -Path "ci-reports\checkstyle" | Out-Null
New-Item -ItemType Directory -Force -Path "ci-reports\pmd" | Out-Null
New-Item -ItemType Directory -Force -Path "ci-reports\test" | Out-Null
New-Item -ItemType Directory -Force -Path "ci-reports\coverage" | Out-Null

# Step 1: Code Quality Checks
Write-Host "`nStep 1: Running Code Quality Checks" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

Write-Host "Running Checkstyle..."
try {
    mvn checkstyle:checkstyle checkstyle:check
    Write-Host "[OK] Checkstyle passed" -ForegroundColor Green
    Copy-Item -Path "target\checkstyle-result.xml" -Destination "ci-reports\checkstyle\" -ErrorAction SilentlyContinue
} catch {
    Write-Host "[FAIL] Checkstyle found violations" -ForegroundColor Red
    Copy-Item -Path "target\checkstyle-result.xml" -Destination "ci-reports\checkstyle\" -ErrorAction SilentlyContinue
}

Write-Host "Running PMD..."
try {
    mvn pmd:check
    Write-Host "[OK] PMD passed" -ForegroundColor Green
    Copy-Item -Path "target\pmd.xml" -Destination "ci-reports\pmd\" -ErrorAction SilentlyContinue
    Copy-Item -Path "target\site\pmd.html" -Destination "ci-reports\pmd\" -ErrorAction SilentlyContinue
} catch {
    Write-Host "[FAIL] PMD found violations" -ForegroundColor Red
    Copy-Item -Path "target\pmd.xml" -Destination "ci-reports\pmd\" -ErrorAction SilentlyContinue
    Copy-Item -Path "target\site\pmd.html" -Destination "ci-reports\pmd\" -ErrorAction SilentlyContinue
}

# Step 2: Tests
Write-Host "`nStep 2: Running Tests" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

Write-Host "Running tests with coverage..."
try {
    mvn clean test jacoco:report
    Write-Host "[OK] All tests passed" -ForegroundColor Green
    Copy-Item -Path "target\surefire-reports\*" -Destination "ci-reports\test\" -Recurse -ErrorAction SilentlyContinue
    Copy-Item -Path "target\site\jacoco\*" -Destination "ci-reports\coverage\" -Recurse -ErrorAction SilentlyContinue
} catch {
    Write-Host "[FAIL] Some tests failed" -ForegroundColor Red
    Copy-Item -Path "target\surefire-reports\*" -Destination "ci-reports\test\" -Recurse -ErrorAction SilentlyContinue
    exit 1
}

# Step 3: Coverage Check
Write-Host "`nStep 3: Checking Test Coverage" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

try {
    mvn jacoco:check
    Write-Host "[OK] Coverage requirements met" -ForegroundColor Green
} catch {
    Write-Host "[WARN] Coverage below threshold (80%)" -ForegroundColor Yellow
}

# Step 4: Build
Write-Host "`nStep 4: Building Application" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

try {
    mvn clean package -DskipTests
    Write-Host "[OK] Build successful" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Build failed" -ForegroundColor Red
    exit 1
}

# Generate summary
$branch = git branch --show-current 2>$null
if (-not $branch) { $branch = "unknown" }
$commit = git rev-parse HEAD 2>$null
if (-not $commit) { $commit = "unknown" }
$date = Get-Date -Format "yyyy-MM-dd HH:mm:ss UTC"

$summary = @"
# CI Pipeline Summary

Generated: $date
Branch: $branch
Commit: $commit

## Code Quality
- Checkstyle: See checkstyle/checkstyle-result.xml
- PMD: See pmd/pmd.xml

## Testing
- Test Results: See test/surefire-reports/
- Coverage Report: See coverage/index.html

## Status
- Code Quality: [OK] Complete
- Tests: [OK] Complete
- Build: [OK] Complete
"@

$summary | Out-File -FilePath "ci-reports\ci-summary.md" -Encoding UTF8

Write-Host "`n==========================================" -ForegroundColor Green
Write-Host "CI Pipeline Complete!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host "`nReports generated in: ci-reports/"
Write-Host "- Checkstyle: ci-reports/checkstyle/"
Write-Host "- PMD: ci-reports/pmd/"
Write-Host "- Tests: ci-reports/test/"
Write-Host "- Coverage: ci-reports/coverage/"
Write-Host "`nSummary: ci-reports/ci-summary.md"

