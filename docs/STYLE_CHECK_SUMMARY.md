# Style and Static Analysis Summary

## Toolchain
- **Checkstyle 10.12.5** driven by `checkstyle.xml` (Google Java Style base).
  - Dockerized run (per `DockerCommandInstruction.md`):
    - `docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm checkstyle` (copies reports under `testresult/checkstyle/`).
- **PMD 6.55.0** wired into the same Maven build.
  - Dockerized command: `docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm pmd` (reports copied into `testresult/pmd/`).
- **Unit + Checkstyle combined** when needed: `docker compose -f docker-compose.yml -f docker-compose.tests.yml run --rm unit-tests` (Surefire/Jacoco + Checkstyle stored under `testresult/unit/` and `testresult/unit-coverage/`).
- **IDE formatter**: IntelliJ IDEA using the Google Java code-style profile.

## Key Rules
- Enforced naming conventions for classes, methods, and constants.
- Required Javadoc on public types and methods.
- Limited method complexity and nesting depth.
- Verified brace placement and indentation consistency.
- Flagged unused imports, dead code, and overly broad exception handling.

## Current Status
- Latest run (`mvn clean checkstyle:check`) reports zero violations for the `iteration-2` submission.
- Checkstyle artifacts appear under `target/site/checkstyle.html` plus the XML copy in `target/checkstyle-result.xml`; we also archive a CI snapshot at `testresult/checkstyle/checkstyle-result.xml`.
- PMD output is stored in `target/site/pmd.html` with the raw XML at `target/pmd.xml`; iteration snapshots live under `testresult/pmd/pmd.html`.
- PMD and Checkstyle are both integrated into the CI pipeline


