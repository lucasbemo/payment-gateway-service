# Contributing to Payment Gateway Service

Thank you for your interest in contributing to the Payment Gateway Service! This document provides guidelines and instructions for contributing.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
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

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- Git
- IDE (IntelliJ IDEA recommended)

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
- [ ] Code follows style guidelines
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