# 🧾 Inventory Management Backend

This is the **backend service** for the Inventory Management System — a secure, production-ready Spring Boot application providing REST APIs for user authentication, inventory tracking, and administrative management.

The backend is designed with **modularity**, **scalability**, and **security** in mind — using **Spring Boot 3**, **PostgreSQL**, **Redis**, and **Docker**.

---

## 📁 Project Structure

src/
├── main/
│ ├── java/dev/roland/inventory_management_backend/
│ │ ├── annotation/ # Custom annotations (e.g. role-based validation, auditing)
│ │ ├── config/ # Spring configurations (security, Redis, etc.)
│ │ ├── controller/ # REST controllers (entry points for API requests)
│ │ ├── dto/ # Data Transfer Objects (API request/response models)
│ │ ├── exception/ # Custom exception handlers and messages
│ │ ├── facade/ # High-level orchestrators combining multiple services
│ │ ├── messageKey/ # Centralized enums for error and success messages
│ │ ├── model/ # JPA entities and enums representing database tables
│ │ ├── repository/ # JPA repositories for data persistence
│ │ ├── security/ # Security, JWT, and 2FA (TOTP) configuration
│ │ ├── service/ # Business logic and interaction with repositories
│ │ └── util/ # Helper utilities (token generators, date utilities, etc.)
│ └── resources/
│ ├── db/migration/ # Flyway migration scripts
│ ├── templates/ # Email templates for mail service
│ ├── application.yml # Application configuration
│ └── logback.xml # Logging configuration
└── test/ # Unit and integration tests

---

## ⚙️ Setup Guide

### 🧩 Prerequisites
Before running the project, make sure you have:
- **Java 25+**
- **Maven 3.9+**
- **Docker & Docker Compose**
- **PostgreSQL** (automatically set up via Docker)
- **Redis** (automatically set up via Docker)
- (Optional) **Mail server credentials** for sending verification emails

---

### 💻 Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/inventory-management-backend.git
   cd inventory-management-backend
2. **Start the database and Redis**
   ```bash
   docker compose up -d
3. **Run the backend**
   ```bash
   mvn spring-boot:run
4. **The app will be available at**
    ```bash
   http://localhost:8080/api/V1

### 💻 VPS / Production Setup (coming soon)
Will include:
- Dockerized **backend & frontend**
- Nginx **reverse proxy** configuration
- **HTTPS** setup with Certbot (Let's encrypt)
- **Environment variable** management
- **Automatic startup** using Docker Compose

---

## 🔐 Authentication Flow

The application uses a multi-step secure authentication process with **JWT, refresh tokens, and TOTP-based 2FA.**

### 🧭 Flow Overview
1. **Admin Registration**
- The first registered user is automatically assigned the **`ADMIN`** role.
- There is **no self-registration** - only admins can register new users.
2. **First Login (No Password Yet)**
- User enters email.
- Receives a **one-time login code** via email.
- Validates code and sets up a new password.
3. **Regular Login**
- User enters email and password.
- If 2FA is enabled backend returns a **short-lived session token.**
4. **2FA Verification (Microsoft/Google Authenticator**
- User submits their **email, 2FA code, short-lived token.**
- On success → receives **access token and refresh token.**
5. **Refresh Token Flow**
- When access token expires, frontend calls **`/auth/refresh`** to obtain a new access token.

---

### 🔒 Technologies
- **JWT (Access + Refresh tokens) for stateless auth**
- **Redis for short-lived login sessions**
- **TOTP (RFC 6238) via Authenticator apps**
- **Flyway for database migrations**
- **Lombok + JPA + Validation for clean model layer**
- **Mail templating system for transactional emails**

---

### 🌟 Features

- **✅ User registration and onboarding**
- **✅ One-time code for first login**
- **✅ Password setup and reset flow**
- **✅ Email-based 2FA setup and verification**
- **✅ JWT access and refresh token system**
- **✅ Redis-based short-lived session storage**
- **✅ Modular architecture with services and facades**
- **✅ Centralized exception handling and message keys**
- **✅ Ready for Docker + Nginx deployment**

### 🧰 Tech Stack

| Layer                | Technology                       |
|----------------------| -------------------------------- |
| **Backend**          | Spring Boot 3 (Java 25)          |
| **Database**         | PostgreSQL 16                    |
| **Cache**            | Redis 7                          |
| **Auth**             | JWT, TOTP (2FA), Spring Security |
| **Migrations**       | Flyway                           |
| **Testing**          | JUnit 5, Mockito                 |
| **Build Tool**       | Maven                            |
| **Containerization** | Docker, Docker Compose           |

### 📄 License

This project is licensed under the MIT License — free to use and modify.