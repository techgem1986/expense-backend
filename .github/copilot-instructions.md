# Copilot Workspace Instructions for Expense Management Service

## Purpose
This file provides essential instructions and conventions for AI agents (Copilot, etc.) working in this repository. It ensures agents follow project-specific workflows, build/test commands, and architectural guidelines for maximum productivity and minimal friction.

---

## Build & Test Commands
- **Build:** `./mvnw clean package`
- **Run (dev):** `./mvnw spring-boot:run`
- **Test:** `./mvnw test`
- **Test Coverage:** `./mvnw jacoco:report`
- **Docker Compose (local infra):** `docker-compose up -d postgres redis`
- **Full Docker Compose (app+infra):** `docker-compose up --build`

## Key Conventions
- **Language:** Java 17, Spring Boot 3.x
- **Database:** PostgreSQL (dev: localhost, prod: env vars)
- **Cache:** Redis (dev: localhost, prod: env vars)
- **Security:** JWT (RSA keys, see `src/main/resources/keys/`)
- **Profiles:** `dev`, `test`, `prod` (see `application.yaml`)
- **Package by feature:** Each domain (user, transaction, etc.) is a separate package under `com.expenseapp`.
- **Domain-Driven Design:** Follow DDD principles for modules and services.
- **Constructor Injection:** Use constructor injection for all dependencies.
- **Testing:** Write both unit and integration tests. Use Testcontainers for DB/Redis in integration tests.
- **Documentation:** API docs via Swagger/OpenAPI (http://localhost:8080/swagger-ui.html)

## Project Structure
- `src/main/java/com/expenseapp/` — Main source code, organized by feature
- `src/main/resources/application.yaml` — Main configuration (profiles: dev, test, prod)
- `src/main/resources/db/migration/` — Flyway/Liquibase migrations
- `src/main/resources/keys/` — JWT key files
- `docker-compose.yml` — Local infra and app orchestration
- `Dockerfile` — Multi-stage build for containerization
- `pom.xml` — Maven dependencies and plugins

## Common Pitfalls
- **Database/Redis not running:** Always start with `docker-compose up -d postgres redis` before running the app locally.
- **JWT keys missing:** Ensure `src/main/resources/keys/` contains valid RSA key pairs for dev.
- **Profile mismatch:** Use the correct Spring profile (`dev`, `test`, `prod`) for your environment.
- **Test isolation:** Use H2 and embedded Redis for tests (see `application.yaml`).
- **Secrets in code:** Never commit production secrets or keys.

## Example Prompts
- "Add a new recurring transaction endpoint."
- "Refactor the budget alert logic for better testability."
- "Generate a Flyway migration for a new table."
- "Update Swagger docs for the analytics API."

## See Also
- [README.md](../README.md) — Full project overview and API docs
- [architecture.md](../architecture.md) — High-level system design

---

_This file is auto-generated. Update as project conventions evolve._
