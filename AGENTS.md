# AGENTS.md

This file summarizes the `inventory-management-backend` Spring Boot project for future coding agents.

## Project Overview

`inventory-management-backend` is a Java 21 / Spring Boot 3.5.5 REST API for an inventory management system. It uses Maven, PostgreSQL, Redis, Liquibase, Spring Security, JWT cookies, TOTP-based 2FA, email templates, and S3-compatible object storage. Its persistence model covers authentication, company data, media, product catalog data, documents, warehouses, stock balances, and stock movements.

Main package:

```text
dev.roland.inventory_management_backend
```

The application entry point is:

```text
src/main/java/dev/roland/inventory_management_backend/InventoryManagementApplication.java
```

## Local Commands

Run these from `inventory-management-backend/`.

```bash
./mvnw spring-boot:run
./mvnw test
./mvnw spotless:check
./mvnw spotless:apply
docker compose up -d
```

`docker-compose.yml` starts PostgreSQL 16, Redis 7, MinIO, and a MinIO bucket initialization container.

The app is configured for PostgreSQL at `localhost:5432/inventory_db` with username/password `postgres` / `postgres`.

## Environment

The project uses `spring-dotenv`; copy `.env-example` to `.env` for local configuration.

Expected variables:

```text
JWT_SECRET
EMAIL_HOST
EMAIL_PORT
EMAIL_USERNAME
EMAIL_PASSWORD
REDIS_HOST
REDIS_PORT
REDIS_SESSION_TTL_MINUTES
ACCESS_TOKEN_EXPIRY
REFRESH_TOKEN_EXPIRY
SECURE_HTTP
STORAGE_BUCKET
STORAGE_ENDPOINT
STORAGE_REGION
STORAGE_ACCESS_KEY
STORAGE_SECRET_KEY
```

`application.yml` maps these into mail, Redis, JWT, secure cookie, and S3-compatible storage settings.

## Architecture

The code follows a layered Spring structure:

```text
controller/              REST API endpoints
dto/                     Request/response objects grouped by domain
facade/                  Higher-level orchestration across services
facade/implementation/   Facade implementations
service/                 Service interfaces and shared BaseService
service/implementation/  Domain service implementations
service/common/          Cross-cutting services: JWT, cookies, email, Redis sessions, TOTP, S3
repository/              Spring Data JPA repositories
model/                   JPA entities
enums/                   Domain enums
message_key/             MessageKey enums for API response localization keys
exception/               Domain exceptions
exception/handler/       Global REST exception handler
configuration/           Spring configuration classes
security/                Security filter and security chain configuration
annotation/              Custom validation annotations and validators
```

Controllers generally delegate to services or facades and return `ApiResponse<T>`. `ApiResponse` contains `success`, `messageKey`, `payload`, `params`, and `timestamp`.

`BaseService<T, ID>` provides common CRUD helpers for entities implementing `IdInterface<ID>`.

## API Surface

Endpoint base paths are defined as constants in controllers. Current controllers:

```text
api/v1/auth
api/v1/company
api/v1/currency
api/v1/prefix
api/v1/media
api/v1/user
```

Product, document, category, unit, and stock movement entities/services/repositories exist, but public controllers for those domains are not currently exposed.

Notable auth endpoints:

```text
POST api/v1/auth/check-first-login
POST api/v1/auth/send-one-time-code
POST api/v1/auth/validate-one-time-code
POST api/v1/auth/setup-password
POST api/v1/auth/login
POST api/v1/auth/refresh
POST api/v1/auth/2fa/setup
POST api/v1/auth/2fa/login
GET  api/v1/auth/check-session
POST api/v1/auth/logout
```

Admin-only operations use method security with `@PreAuthorize("hasRole('ADMIN')")`, especially in user management and company update endpoints.

## Security And Auth Notes

Security is configured in `security/SecurityConfiguration.java`.

- CSRF and HTTP basic auth are disabled.
- Sessions are stateless.
- `AuthenticationFilter` is inserted before `UsernamePasswordAuthenticationFilter`.
- Public auth endpoints are explicitly permitted.
- `api/v1/auth/check-session` accepts users with `ACTIVE` or `SETUP_REQUIRED` status authority.
- All other requests require `UserStatus.ACTIVE` as an authority.
- Method security is enabled with `@EnableMethodSecurity`.
- Password hashing uses `BCryptPasswordEncoder`.
- Access and refresh tokens are set as HTTP-only cookies by `HttpOnlyCookieService`.
- Refresh token persistence is handled by the refresh token service/repository.
- Short-lived login sessions use Redis, with TTL configured by `REDIS_SESSION_TTL_MINUTES`.
- TOTP support comes from `dev.samstevens.totp`.

CORS currently allows `http://localhost:3001` with credentials.

## Persistence And Migrations

Persistence uses Spring Data JPA and PostgreSQL. Liquibase is configured at:

```text
src/main/resources/db/changelog/db.changelog-master.xml
```

The master changelog currently includes migrations `001` through `013`, including the product, document, and stock movement refactors.

Important schema areas:

- Core inventory tables include warehouse locations, warehouses, products, stock balances, partners, contacts, documents, and document lines.
- Product catalog additions include units, product attribute definitions, product attribute options, typed product attribute values, product categories, and product category assignments.
- Product attribute values support fixed option, text, number, date, and boolean storage via separate nullable columns.
- Document additions include document prefixes, yearly document sequences, document categories/statuses, source and target warehouses, currency references, enriched document lines, and document relations.
- Stock movement persistence records document/document-line links, warehouse, product, quantity delta, movement type, reason, created user, and timestamp.

Migrations `011`, `012`, and `013` are still recent product/document/stock schema work; if they remain uncommitted in a branch, it is acceptable to adjust them directly before they are finalized.

## Storage And Media

Media upload/preview/delete lives under `api/v1/media`.

`ObjectStorageService` uses the AWS SDK S3 client and presigner, configured for path-style access and an endpoint override. This supports MinIO locally through `docker-compose.yml`.

Media object paths are generated under either:

```text
temp/<uuid>.<ext>
<entity-type>/<entity-id>/<usage-type>/<filename>
```

## Formatting And Style

The project uses Spotless with Google Java Format 1.17.0:

```bash
./mvnw spotless:apply
```

Style conventions observed:

- Lombok is used heavily for boilerplate.
- Java imports are ordered by Spotless.
- Controllers define endpoint path constants.
- Domain responses should use `ApiResponse.success(...)` or `ApiResponse.failure(...)` with `MessageKey` enums.
- Prefer adding message keys in `message_key/` instead of hard-coded response text.
- Prefer adding DTOs under a domain-specific `dto/<domain>/` package.
- Keep business orchestration in facades when a flow spans multiple services.
- Keep reusable single-domain persistence helpers in services.
- For new persisted entities, add a JPA model, repository, service interface/implementation as needed, and a Liquibase migration.

## Tests

The current test footprint is minimal: only a Spring context load test exists in `InventoryManagementApplicationTests`.

When making meaningful changes, add focused tests where practical. Be aware that `@SpringBootTest` may require PostgreSQL, Redis, mail/JWT/storage environment values, and Liquibase to be configured.

## Things To Verify

- Keep README facts aligned with `pom.xml`, `application.yml`, package names, and `docker-compose.yml` when the stack changes.
- `target/` exists locally and should not be edited.
- `.env` exists locally and should not be committed or printed.
