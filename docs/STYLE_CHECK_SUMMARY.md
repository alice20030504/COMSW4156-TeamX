# Style and Static Analysis Summary

## Static Analysis

### Tools Used

- **Checkstyle 10.12.5**: Enforces Google Java Style Guide conventions
- **PMD 6.55.0**: Detects code quality issues, unused code, and potential bugs

### How to Run Static Analysis Locally

**Checkstyle:**
```bash
mvn checkstyle:check
# Or via Docker:
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm checkstyle
```

**PMD:**
```bash
mvn pmd:check
# Or via Docker:
docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm pmd
```

**Both (during Maven verify):**
```bash
mvn clean verify
```

### Report Locations

- **Checkstyle**: [`testresult/checkstyle/checkstyle-result.xml`](../testresult/checkstyle/checkstyle-result.xml)
- **PMD**: [`testresult/pmd/pmd.html`](../testresult/pmd/pmd.html)
- Reports are also generated in `target/` directory during Maven builds

### Style Checking

- Style checking is enforced via Checkstyle and integrated into CI pipeline
- Checkstyle configuration: [`checkstyle.xml`](../checkstyle.xml) (based on Google Java Style Guide)
- PMD ruleset: [`pmd-ruleset.xml`](../pmd-ruleset.xml)
- Zero violations are required for code commits

### Bugs Fixed

Static-analysis passes recently produced actionable findings; highlights include:

- **Exception handling**: – narrowed multiple broad catch (Exception e) blocks to the specific checked exceptions so PMD’s SignatureDeclareThrowsException and EmptyCatchBlock rules stay satisfied.
- **Dead code & unused members**: – removed redundant helper methods and stale logging hooks that Checkstyle/PMD marked as unused, trimming bytecode size and reducing confusion.
- **Naming & visibility**: – brought lingering snake_case identifiers and package-private helper classes in line with the Checkstyle naming rules, and tightened visibility on a few service helpers to satisfy PMD’s UnusedPrivateMethod.
- **Complexity spikes**: – broke up a couple of “god” controller methods that exceeded PMD’s cognitive-complexity threshold; the refactor also made unit tests easier to target.
- **Imports & formatting**: – cleaned up straggling wildcard imports plus whitespace/brace issues so the formatter and Checkstyle hooks report zero violations.

All the fixes above are demonstrated in the latest testresult/checkstyle/ and testresult/pmd/, which now record clean runs.

### Functional Bugs Found and Fixed

1. **Client ID Validation** – Missing `X-Client-ID` headers previously leaked through to controllers. The `ClientIdInterceptor` now short-circuits such requests with a structured `400` response and a clear remediation message. Reports in `testresult/checkstyle/` confirm the interceptor is covered and logged.
2. **BMI Edge Cases** – Extreme weight/height combinations triggered invalid BMI math. Guard rails (max 635 kg / 272 cm) were added, and boundary unit tests ensure input validation fires before calculations.
3. **Repository Isolation** – A few repository methods ignored the active `clientId`, risking cross-tenant reads. All queries now pull the client identifier from `ClientContext`, with integration tests proving isolation.
4. **Logging Context Cleanup** – `ClientContext` used to persist between requests. The interceptor’s `afterCompletion` hook now clears the context reliably, preventing log contamination across concurrent clients.
