# Contributing to Payment Gateway Service

Thank you for your interest in contributing to the Payment Gateway Service! This document provides guidelines and instructions for contributing.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Spec-Driven Workflow](#spec-driven-workflow-ai-assisted)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Testing](#testing)
- [Documentation](#documentation)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inspiring community for all.

### Our Standards

- **Be respectful** of differing viewpoints and experiences
- **Be collaborative** and help others when they have questions
- **Be patient** with newcomers and provide constructive feedback
- **Be professional** in all interactions

### Unacceptable Behavior

- Harassment or discriminatory language
- Trolling, insulting, or derogatory comments
- Publishing others' private information without permission
- Any other unprofessional conduct

---

## Getting Started

### Prerequisites

- Java 26+
- Maven 3.8+
- Docker & Docker Compose
- Git
- IDE (IntelliJ IDEA recommended)

For the AI-assisted [spec-driven workflow](#spec-driven-workflow-ai-assisted) you also
need (not required for plain manual contribution):

- **GitHub CLI (`gh`)**, authenticated with the `project` scope (`gh auth refresh -s project`)
- **`jq`** (`brew install jq`)

See [`GUIDE_WORKFLOW.md`](GUIDE_WORKFLOW.md) for the full workflow prerequisites.

### Fork and Clone

```bash
# Fork the repository on GitHub
# Then clone your fork
git clone https://github.com/YOUR_USERNAME/payment-gateway-service.git
cd payment-gateway-service

# Add upstream remote
git remote add upstream https://github.com/lucasbemo/payment-gateway-service.git
```

---

## Development Setup

### 1. Start Infrastructure

```bash
docker-compose up -d
```

Wait for all services to be ready (~30 seconds).

### 2. Build the Project

```bash
./mvnw clean install
```

### 3. Run Tests

```bash
# Unit tests only
./mvnw test -Dtest='!com.payment.gateway.e2e.**'

# All tests (requires Docker)
./mvnw test
```

### 4. Run Application

```bash
./mvnw spring-boot:run
```

### 5. Access Services

| Service | URL |
|---------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| pgAdmin | http://localhost:8083 |
| Kafka UI | http://localhost:8082 |
| Grafana | http://localhost:3000 |

---

## How to Contribute

### Reporting Bugs

1. Check existing issues first
2. Create a new issue with:
   - Clear description
   - Steps to reproduce
   - Expected vs actual behavior
   - Environment details

### Suggesting Features

1. Open a discussion or issue
2. Describe the feature and use case
3. Explain why it would benefit the project

### Submitting Code

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Submit a pull request

---

## Spec-Driven Workflow (AI-Assisted)

For non-trivial feature work, this project offers a **spec-driven, human-in-the-loop
workflow** powered by Claude Code skills. The idea is simple: a backlog item becomes an
approved spec, the spec drives the implementation, and you stay in control at every gate.

> 💡 **Optional.** The standard manual flow in the rest of this guide works exactly the
> same. Full reference: **[`GUIDE_WORKFLOW.md`](GUIDE_WORKFLOW.md)**.

### How it flows

Each step is a Claude Code skill you invoke by name:

| # | Step | Command | What it does |
|:-:|------|---------|--------------|
| 1 | Browse the backlog | `/list-github-project-items` | Lists open GitHub Project items |
| 2 | Import an item | `/import-github-project-item <n>` | Scaffolds a spec at `specs/active/<n>-<slug>/` and asks blocking questions |
| 3 | 🧑‍💻 **Review & approve** | _(you)_ | Answer the questions and edit the spec — it becomes the contract |
| 4 | Implement | `/implement-feature specs/active/<n>-<slug>` | Plans, waits for your `APPROVE`, then builds + tests in an isolated worktree |
| 5 | Review | `/spring-boot-review` | Staff-level adversarial review of the diff |
| 6 | Open the PR | `/create-pr` | Opens a draft PR with risk + rollback notes |
| 7 | After merge, clean up | `/finish-feature <n>-<slug>` | Archives the spec and removes the worktree |

Two human gates keep you in control: approving the **spec** (step 3) and approving the
**plan** before any code is written (step 4).

### Good to know

- **GitHub Project is the backlog's source of truth**; the approved spec is the
  engineering contract; **current code and tests always override older specs**.
- **One worktree per feature** — step 4 auto-creates an isolated git worktree on a
  `feature/<n>-<slug>` branch with its own app port (sharing one local infra stack), so
  you can switch between features without stashing.
- **Same conventions as everywhere else** — these skills follow this guide's branch
  naming (`feature/`, `fix/`, …), [Conventional Commits](#commit-guidelines), the
  [Spotless gate](#code-formatting-spotless), and draft-PRs-never-auto-merged rules
  (see also the *Git workflow* section in [`CLAUDE.md`](CLAUDE.md)).

---

## Pull Request Process

### 1. Create a Branch

```bash
# From main
git checkout main
git pull upstream main
git checkout -b feature/your-feature-name
```

Branch naming conventions:
- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation changes
- `refactor/` - Code refactoring
- `test/` - Test additions/changes

### 2. Make Changes

- Follow coding standards
- Write tests for new code
- Update documentation if needed

### 3. Commit Changes

```bash
git add .
git commit -m "feat: add new payment validation"
```

### 4. Push and Create PR

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub.

### 5. PR Requirements

- [ ] All tests pass
- [ ] Code is formatted (`./mvnw spotless:check` passes — see [Code Formatting](#code-formatting-spotless))
- [ ] Documentation updated (if needed)
- [ ] PR description is clear
- [ ] Linked to relevant issues

### 6. Review Process

- AI Code Review will automatically analyze your PR
- Maintainers will review your changes
- Address any feedback
- Once approved, your PR will be merged

---

## Coding Standards

### Java Code Style

- **Indentation:** 4 spaces (no tabs)
- **Line Length:** 120 characters max
- **Braces:** K&R style
- **Naming:**
  - Classes: `PascalCase`
  - Methods/Variables: `camelCase`
  - Constants: `SCREAMING_SNAKE_CASE`
  - Packages: `lowercase`

### Code Formatting (Spotless)

Formatting is **automated and enforced in CI** — the `Code Quality` job runs
`spotless:check` and **fails the build** if any file is not formatted. Format your code
before committing:

```bash
# Auto-format all Java sources (src/main + src/test)
./mvnw spotless:apply        # or: make format

# Verify formatting without modifying files (this is what CI runs)
./mvnw spotless:check        # or: make validate
```

The formatter is **[palantir-java-format](https://github.com/palantir/palantir-java-format)**,
configured via the `spotless-maven-plugin` in `pom.xml`. It also removes unused imports and
normalises trailing whitespace and final newlines. Its defaults (4-space indentation,
120-column lines, organised imports without wildcards) match the conventions above, so
**`spotless:apply` is the source of truth — don't hand-format**.

**Tip:** add a Git pre-commit hook so you never push unformatted code:

```bash
echo './mvnw -q spotless:apply' > .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

**Editor setup (format on save):**

- **IntelliJ IDEA** — install the **palantir-java-format** plugin (Settings → Plugins), then
  enable *Settings → Tools → Actions on Save → Reformat code*.
- **VS Code** — VS Code's built-in Java formatter is Eclipse-based and does **not** match
  palantir-java-format, so don't rely on it. Run `make format` before committing, or install
  the **Run on Save** (`emeraldwalk.runonsave`) extension and add to `.vscode/settings.json`:

  ```json
  {
    "[java]": { "editor.formatOnSave": false },
    "emeraldwalk.runonsave": {
      "commands": [
        { "match": "\\.java$", "cmd": "./mvnw -q spotless:apply -DspotlessFiles=${file}" }
      ]
    }
  }
  ```

  `-DspotlessFiles=${file}` formats only the saved file to keep it fast, and disabling the
  built-in Java formatter stops it from fighting Spotless.

### Architecture

This project follows **Hexagonal Architecture (Ports and Adapters)**:

```
├── domain/           # Core business logic (no dependencies)
├── application/      # Use cases, DTOs, ports
├── infrastructure/   # Adapters (REST, persistence, external)
└── commons/          # Shared utilities
```

**Key Principles:**

1. **Domain layer** must have no dependencies on infrastructure
2. **Application layer** defines ports (interfaces)
3. **Infrastructure layer** implements adapters
4. **Dependency rule:** domain ← application ← infrastructure

### Code Comments

- Use Javadoc for public APIs
- Explain **why**, not **what**
- Keep comments up-to-date

```java
/**
 * Processes a payment authorization request.
 *
 * @param command the payment command containing amount, currency, etc.
 * @return PaymentResponse with authorization details
 * @throws PaymentProcessingException if authorization fails
 */
public PaymentResponse processPayment(ProcessPaymentCommand command) {
    // Implementation
}
```

---

## Commit Guidelines

### Commit Message Format

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Types

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Code style (formatting, etc.) |
| `refactor` | Code refactoring |
| `test` | Adding/updating tests |
| `chore` | Build, CI, dependencies |
| `perf` | Performance improvement |

### Examples

```bash
feat(payment): add partial refund support
fix(customer): handle null email in registration
docs(api): update swagger documentation
test(refund): add edge case tests for partial refunds
```

---

## Testing

### Unit Tests

- Test each unit in isolation
- Use mocks for dependencies
- Follow Given-When-Then pattern

```java
@Test
void shouldProcessPayment_whenValidRequest() {
    // Given
    var command = ProcessPaymentCommand.builder()
        .amount(10000L)
        .currency("USD")
        .build();
    
    // When
    var response = paymentService.processPayment(command);
    
    // Then
    assertThat(response.getStatus()).isEqualTo("AUTHORIZED");
}
```

### Integration Tests

- Test component interactions
- Use `@SpringBootTest` for Spring context
- Use Testcontainers for Docker dependencies

### Test Naming

```java
// Pattern: methodName_scenario_expectedResult
@Test
void processPayment_withValidRequest_returnsAuthorizedPayment() { }

@Test
void processPayment_withInvalidAmount_throwsException() { }
```

### Running Tests

```bash
# All unit tests
./mvnw test -Dtest='!com.payment.gateway.e2e.**'

# Specific test class
./mvnw test -Dtest=PaymentServiceTest

# E2E tests (requires Docker)
./mvnw test -Dtest='com.payment.gateway.e2e.**'
```

---

## Documentation

### When to Update

- New features → Update API docs
- New endpoints → Update Swagger annotations
- Architecture changes → Update ADRs
- Configuration changes → Update README

### Documentation Files

| File | Purpose |
|------|---------|
| `README.md` | Project overview |
| `GUIDE_WORKFLOW.md` | Spec-driven (AI-assisted) development workflow |
| `CHANGELOG.md` | Version history |
| `docs/API_DOCUMENTATION.md` | API reference |
| `docs/GETTING_STARTED.md` | Quick start |
| `docs/DEVELOPMENT_GUIDE.md` | Development details |
| `docs/DEPLOYMENT_GUIDE.md` | Deployment instructions |

---

## Questions?

- Open a [GitHub Issue](https://github.com/lucasbemo/payment-gateway-service/issues)
- Check existing documentation in `docs/`

---

Thank you for contributing! 🎉