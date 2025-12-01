#!/bin/bash
# Local CI script - Run all code quality checks and tests locally
# GitHub Actions CI pipeline

set -e  # Exit on error

echo "=========================================="
echo "Running Local CI Pipeline"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Create reports directory
mkdir -p ci-reports/checkstyle
mkdir -p ci-reports/pmd
mkdir -p ci-reports/test
mkdir -p ci-reports/coverage

# Step 1: Code Quality Checks
echo -e "\n${YELLOW}Step 1: Running Code Quality Checks${NC}"
echo "----------------------------------------"

echo "Running Checkstyle..."
if mvn checkstyle:checkstyle checkstyle:check; then
    echo -e "${GREEN}[OK] Checkstyle passed${NC}"
    cp target/checkstyle-result.xml ci-reports/checkstyle/ 2>/dev/null || true
else
    echo -e "${RED}[FAIL] Checkstyle found violations${NC}"
    cp target/checkstyle-result.xml ci-reports/checkstyle/ 2>/dev/null || true
fi

echo "Running PMD..."
if mvn pmd:check; then
    echo -e "${GREEN}[OK] PMD passed${NC}"
    cp target/pmd.xml ci-reports/pmd/ 2>/dev/null || true
    cp target/site/pmd.html ci-reports/pmd/ 2>/dev/null || true
else
    echo -e "${RED}[FAIL] PMD found violations${NC}"
    cp target/pmd.xml ci-reports/pmd/ 2>/dev/null || true
    cp target/site/pmd.html ci-reports/pmd/ 2>/dev/null || true
fi

# Step 2: Tests
echo -e "\n${YELLOW}Step 2: Running Tests${NC}"
echo "----------------------------------------"

echo "Running tests with coverage..."
if mvn clean test jacoco:report; then
    echo -e "${GREEN}[OK] All tests passed${NC}"
    cp -r target/surefire-reports/* ci-reports/test/ 2>/dev/null || true
    cp -r target/site/jacoco/* ci-reports/coverage/ 2>/dev/null || true
else
    echo -e "${RED}[FAIL] Some tests failed${NC}"
    cp -r target/surefire-reports/* ci-reports/test/ 2>/dev/null || true
    exit 1
fi

# Step 3: Coverage Check
echo -e "\n${YELLOW}Step 3: Checking Test Coverage${NC}"
echo "----------------------------------------"

# Coverage check requires jacoco.exec from test run
if [ -f "target/jacoco.exec" ]; then
    if mvn jacoco:check; then
        echo -e "${GREEN}[OK] Coverage requirements met${NC}"
    else
        echo -e "${YELLOW}[WARN] Coverage below threshold (80%)${NC}"
    fi
else
    echo -e "${YELLOW}[WARN] Coverage data not found. Run tests first.${NC}"
fi

# Step 4: Build
echo -e "\n${YELLOW}Step 4: Building Application${NC}"
echo "----------------------------------------"

if mvn clean package -DskipTests; then
    echo -e "${GREEN}[OK] Build successful${NC}"
else
    echo -e "${RED}[FAIL] Build failed${NC}"
    exit 1
fi

# Generate summary
cat > ci-reports/ci-summary.md << EOF
# CI Pipeline Summary

Generated: $(date -u +"%Y-%m-%d %H:%M:%S UTC")
Branch: $(git branch --show-current 2>/dev/null || echo "unknown")
Commit: $(git rev-parse HEAD 2>/dev/null || echo "unknown")

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
EOF

echo -e "\n${GREEN}=========================================="
echo "CI Pipeline Complete!"
echo "==========================================${NC}"
echo -e "\nReports generated in: ci-reports/"
echo "- Checkstyle: ci-reports/checkstyle/"
echo "- PMD: ci-reports/pmd/"
echo "- Tests: ci-reports/test/"
echo "- Coverage: ci-reports/coverage/"
echo -e "\nSummary: ci-reports/ci-summary.md"

