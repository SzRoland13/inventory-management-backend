# Inventory Management Backend

This is the **backend service** for the Inventory Management System: a Spring Boot application providing REST APIs and persistence for user authentication, product catalog data, warehouse stock tracking, document workflows, media storage, and administrative management.

The backend is designed with **modularity**, **scalability**, and **security** in mind, using **Spring Boot 3**, **PostgreSQL**, **Redis**, **Liquibase**, **MinIO/S3-compatible storage**, and **Docker**.

---

## Project Structure

```text
src/
├── main/
│ ├── java/dev/roland/inventory_management_backend/
│ │ ├── annotation/ # Custom validation annotations and validators
│ │ ├── configuration/ # Spring configuration classes
│ │ ├── controller/ # REST controllers (entry points for API requests)
│ │ ├── dto/ # Data Transfer Objects (API request/response models)
│ │ ├── exception/ # Custom exception handlers and messages
│ │ ├── facade/ # High-level orchestrators combining multiple services
│ │ ├── message_key/ # Centralized message key enums for API responses
│ │ ├── model/ # JPA entities and enums representing database tables
│ │ ├── repository/ # JPA repositories for data persistence
│ │ ├── security/ # Security, JWT, and 2FA (TOTP) configuration
│ │ └── service/ # Business logic and common services
│ └── resources/
│ ├── db/changelog/ # Liquibase changelogs
│ ├── templates/ # Email templates for mail service
│ └── application.yml # Application configuration
└── test/ # Unit and integration tests
```

---

## Setup Guide

### Prerequisites
Before running the project, make sure you have:
- **Java 21+**
- **Maven 3.9+**
- **Docker & Docker Compose**
- **PostgreSQL** (automatically set up via Docker)
- **Redis** (automatically set up via Docker)
- **MinIO** (automatically set up via Docker for S3-compatible object storage)
- (Optional) **Mail server credentials** for sending verification emails

---

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/inventory-management-backend.git
   cd inventory-management-backend
   ```
2. **Create local environment configuration**
   ```bash
   cp .env-example .env
   ```
   Fill in JWT, mail, Redis, and storage values in `.env`.
3. **Start PostgreSQL, Redis, and MinIO**
   ```bash
   docker compose up -d
   ```
4. **Run the backend**
   ```bash
   ./mvnw spring-boot:run
   ```
5. **The API will be available under**
   ```text
   http://localhost:8080/api/v1
   ```

### VPS / Production Setup (coming soon)
Will include:
- Dockerized **backend & frontend**
- Nginx **reverse proxy** configuration
- **HTTPS** setup with Certbot (Let's encrypt)
- **Environment variable** management
- **Automatic startup** using Docker Compose

---

## Authentication Flow

The application uses a multi-step secure authentication process with **JWT, refresh tokens, and TOTP-based 2FA.**

### Flow Overview
1. **Admin Registration**
- The first registered user is automatically assigned the **`ADMIN`** role.
- There is **no self-registration** - only admins can register new users.
2. **First Login (No Password Yet)**
- User enters email.
- Receives a **one-time login code** via email.
- Validates code and sets up a new password.
3. **Regular Login**
- User enters email and password.
- If 2FA is enabled backend returns a **short-life session token.**
4. **2FA Verification (Microsoft/Google Authenticator)**
- User submits their **email, 2FA code, short-life token.**
- On success, receives **access token and refresh token** as HTTP-only cookies.
5. **Refresh Token Flow**
- When access token expires, frontend calls **`/auth/refresh`** to obtain a new access token.

---

### Technologies
- **JWT (Access + Refresh tokens) for stateless auth**
- **Redis for short-life login sessions**
- **TOTP (RFC 6238) via Authenticator apps**
- **Liquibase for database migrations**
- **Lombok + JPA + Validation for clean model layer**
- **Mail templating system for transactional emails**
- **S3-compatible object storage for media assets**

---

### Features

- **User registration and onboarding**
- **One-time code for first login**
- **Password setup and reset flow**
- **TOTP-based 2FA setup and verification**
- **JWT access and refresh token system**
- **Redis-based short-life session storage**
- **Media upload and preview through S3-compatible storage**
- **Product catalog persistence with units, product attributes, attribute options, typed attribute values, and categories**
- **Document persistence with prefixes, sequences, document lines, document relations, source and target warehouses, and currency snapshots**
- **Stock balance and stock movement persistence for warehouse/product inventory tracking**
- **Modular architecture with services and facades**
- **Centralized exception handling and message keys**

### Domain Model

The database model currently covers:

- **Products** with SKU, EAN, brand, pricing, dimensions, status, company, currency, and unit references.
- **Units** with system defaults and company-specific unit codes.
- **Product attributes** through definitions, fixed options, and typed values: fixed option, text, number, date, and boolean.
- **Product categories** with parent/child hierarchy and many-to-many product assignments.
- **Warehouses and stock** through warehouse locations, stock balances, and stock movements linked to products, warehouses, documents, document lines, and users.
- **Documents** with prefixes, yearly sequences, categories, statuses, source/target warehouses, partner/currency references, document lines, and document relations.

Public REST controllers currently exist for auth, company, currency, document prefixes, media, and users; product/document/stock persistence exists below the API layer and can be exposed by future controllers.

### Tech Stack

| Layer                | Technology                       |
|----------------------| -------------------------------- |
| **Backend**          | Spring Boot 3.5.5 (Java 21)      |
| **Database**         | PostgreSQL 16                    |
| **Cache**            | Redis 7                          |
| **Auth**             | JWT, TOTP (2FA), Spring Security |
| **Migrations**       | Liquibase                        |
| **Object Storage**   | MinIO / S3-compatible storage    |
| **Testing**          | JUnit 5, Mockito                 |
| **Build Tool**       | Maven                            |
| **Containerization** | Docker, Docker Compose           |

### License

This project is licensed under the MIT License — free to use and modify.
