# AI Usage Log

## Tools Used
- **Cursor AI** (Auto agent) - Primary AI assistant for Iteration 2 documentation and code analysis
- **ChatGPT/Codex CLI** (free tier) - Used during Iteration 1 for initial documentation formatting and test review

## How AI Assisted in This Project

### Iteration 1
- Reformatted project documentation to maintain consistent headings, font styles, and alignment
- Reviewed and double-checked unit test logic and accompanying Javadoc for clarity
- Suggested improvements to API testing strategy to ensure coverage of controller and system scenarios

### Iteration 2

#### 1. Documentation formatting and Updates
- **README.md**: Formatted comprehensive README covering 12 required sections:
  - Service overview with computation details
  - Complete client documentation
  - Static analysis, unit testing, API testing, integration testing sections
  - Branch coverage and bug fixing documentation
  - CI/CD pipeline description
  - Cloud deployment information
  - Full API documentation
  - Project proposal implementation status comparison
- **API_REFERENCE.md**: Updated with:
  - GCP deployment URLs (replaced localhost references)
  - All implemented endpoints with accurate descriptions
  - Dependency-based API calling sequences
  - Removed non-existent endpoints, added missing ones (e.g., `/api/persons/plan`, `/api/persons/recommendation`)
- **STYLE_CHECK_SUMMARY.md**: Updated from Iteration 1 to Iteration 2 status
- **TESTING_RESULTS.md**: Updated coverage metrics and Iteration 2 achievements

#### 2. Code Analysis and Understanding
- Used semantic search to understand:
  - Project structure and architecture
  - Controller implementations and endpoints
  - Service layer business logic
  - Model definitions and relationships
  - Testing strategies and coverage
- Analyzed dependency relationships between API endpoints
- Identified implemented vs. planned features

#### 3. Documentation Enhancement
- Added hyperlinks to all file references throughout README
- Improved organization of documentation
- Created dependency graphs for API calling sequences
- Added comprehensive project proposal comparison section

#### 4. Information Integration
- Integrated project proposal requirements with actual implementation
- Created detailed comparison tables showing implemented vs. planned features
- Documented rationale for scope decisions and deferred features
- Organized API endpoints by dependency relationships

## Specific AI-Generated Content

### Documentation Sections Created by AI
- Complete README.md structure
- API dependency graphs and calling sequences
- Project proposal implementation status tables
- Updated API endpoint descriptions with accurate parameters and responses
- Dependency relationship documentation

### Code Understanding Tasks
- Semantic codebase searches to locate and understand:
  - All controller endpoints
  - Service implementations
  - Model structures
  - Test coverage
  - Configuration files

## Human Oversight and Validation

### Review Process
- All AI-generated documentation was reviewed by team members
- Technical accuracy verified against actual codebase
- Port numbers and URLs validated against deployment configuration
- Endpoint descriptions cross-checked with Swagger UI and actual implementations

### Validation Steps
- Generated text validated through:
  - Code review against actual source files
  - Build verification (Maven builds)
  - Automated test execution
  - Manual testing of documented workflows
  - Peer review before commit

### Corrections Made
- Fixed incorrect endpoint paths (e.g., `/api/research/register` → `/api/research`)
- Removed non-existent endpoints from documentation
- Added missing endpoints discovered during code analysis
- Updated port numbers from localhost to GCP deployment URLs
- Corrected dependency relationships based on actual code behavior

## AI Limitations and Human Intervention

### What AI Did NOT Do
- **No code generation without human oversight**: All application code (Java, JavaScript, HTML, CSS) was written by team members
- **No test writing without human oversight**: All unit tests, integration tests, and API tests were created by the team
- **No architecture decisions**: System design and architectural choices were made by the team

### What Required Human Input
- Verification of technical accuracy
- Decision-making on scope and features
- Code implementation and debugging
- Test case design and execution
- Deployment configuration
- Project proposal comparison and rationale

## Prompt Examples Used

### Human Oversight
- All AI suggestions were reviewed and edited by the team before inclusion.
- Generated text and code was validated through builds, automated tests, and peer 
review prior to commit.
### Iteration 1
- "Reformat the README testing section to match Google style and clarify the Newman command."
- "Review PersonServiceTest for missing invalid input scenarios."
- "Check Javadoc on ClientIdInterceptor for accuracy and completeness."

### Iteration 2
- "Create a comprehensive README covering all 12 sections from the rubric requirements"
- "Update API_REFERENCE.md with GCP ports and current endpoint implementations"
- "Add dependency-based API calling sequences to the documentation"
- "Create a section comparing project proposal with actual implementation"
- "Update all documentation files from Iteration 1 to Iteration 2 status"
- "Add hyperlinks to all file references in README"

## Transparency Statement

This document accurately reflects the use of AI tools in this project. AI was primarily used for:
1. **Documentation generation and organization** - Creating structured, comprehensive documentation
2. **Code analysis** - Understanding existing codebase structure and relationships
3. **Information synthesis** - Integrating requirements, implementation status, and documentation

All code, business logic, tests, and architectural decisions were made by the development team. AI served as a documentation and analysis assistant, not as a code generator or decision-maker.
