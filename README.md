# Task Manager API

A RESTful API for managing tasks built with Spring Boot, Kotlin, MongoDB, and secured with OAuth2/Keycloak.

## Overview

This application is a task management system that follows Clean Architecture principles (Hexagonal Architecture) with clear separation between domain, use cases, and adapters layers. It provides a secure API to create, read, and update tasks with JWT authentication.

## Features

- **Task Management**: Create, retrieve, and update task status
- **Bulk Operations**: Create multiple tasks at once
- **Pagination**: Retrieve tasks with pagination support
- **Security**: OAuth2 Resource Server with JWT tokens via Keycloak
- **API Documentation**: Interactive Swagger UI for API exploration
- **Persistence**: MongoDB for data storage
- **Clean Architecture**: Domain-driven design with ports and adapters pattern

## Tech Stack

- **Language**: Kotlin 2.2.20
- **Framework**: Spring Boot 3.5.8
- **Java Version**: 21
- **Database**: MongoDB 8.0.13
- **Authentication**: Keycloak 26.2.4 (OAuth2/JWT)
- **API Documentation**: SpringDoc OpenAPI 2.8.8
- **Build Tool**: Maven

## Architecture

The project follows Hexagonal Architecture:
- **Domain Layer**: Core business logic and models (`domain/`)
- **Use Cases**: Application business rules (`usecase/`)
- **Adapters**: 
  - Inbound: REST controllers (`adapters/inbound/web/`)
  - Outbound: Database repositories (`adapters/outbound/`)

## Prerequisites

- Java 21 or higher
- Docker and Docker Compose
- Maven 3.6+ (or use the included `./mvnw` wrapper)

## Getting Started

### 1. Start Infrastructure Services

Start MongoDB and Keycloak using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- **MongoDB** on port `27017`
- **Keycloak** on port `9999` with admin credentials:
  - Username: `admin`
  - Password: `admin`
  - Pre-configured realm: `task`

### 2. Build the Application

Using Maven wrapper (recommended):

```bash
./mvnw clean install
```

Or using system Maven:

```bash
mvn clean install
```

### 3. Run the Application

Using system Maven:

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/task-manager-0.0.1-SNAPSHOT.jar
```

The application will start on **http://localhost:8080**

## API Endpoints

All endpoints are prefixed with `/api` and require authentication.

### Task Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tasks` | Create a new task |
| POST | `/api/tasks/bulk` | Create multiple tasks |
| GET | `/api/tasks` | Get all tasks (paginated) |
| GET | `/api/tasks/{id}` | Get a task by ID |
| PATCH | `/api/tasks/{id}/status` | Update task status |

### Example Requests

**Create a Task:**
```bash
POST http://localhost:8080/api/tasks
Content-Type: application/json
Authorization: Bearer {your-jwt-token}

{
  "label": "My Task",
  "description": "Task description",
  "completed": false
}
```

**Get All Tasks (with pagination):**
```bash
GET http://localhost:8080/api/tasks?page=0&size=10
Authorization: Bearer {your-jwt-token}
```

**Update Task Status:**
```bash
PATCH http://localhost:8080/api/tasks/{taskId}/status
Content-Type: application/json
Authorization: Bearer {your-jwt-token}

{
  "completed": true
}
```

## API Documentation

Once the application is running, access the interactive Swagger UI documentation:

**Swagger UI**: http://localhost:8080/api/swagger-ui/index.html

**OpenAPI JSON**: http://localhost:8080/api/v3/api-docs

## Authentication

The API uses OAuth2 Resource Server with JWT tokens from Keycloak.

### Getting an Access Token

1. Access Keycloak Admin Console: http://localhost:9999
2. Login with admin/admin
3. Navigate to the `task` realm
4. Create a client or user for authentication
5. Obtain a JWT token using OAuth2 flows (Authorization Code, Client Credentials, etc.)

### Using the Token

Include the JWT token in the Authorization header:

```
Authorization: Bearer {your-jwt-token}
```

## Configuration

Main configuration file: `src/main/resources/application.yaml`

Key configurations:
- **Server Port**: 8080
- **Context Path**: /api
- **MongoDB URI**: mongodb://localhost:27017/appdb
- **Keycloak URL**: http://localhost:9999
- **Keycloak Realm**: task

## Testing

Run unit tests:

```bash
./mvnw test
```

Run integration tests:

```bash
./mvnw verify
```

Run all tests (unit and integration):

```bash
./mvnw verify
```

### Test Structure

The project includes comprehensive testing at multiple levels:

#### Unit Tests
Located in `src/test/kotlin/com/personal/`, these tests cover:
- **Domain Models**: `TaskIdTest`, `TaskTest` - Validation of business rules and domain logic
- **Use Cases/Application Services**: 
  - `CreateTaskApplicationServiceTest`
  - `CreateMultipleTasksApplicationServiceTest`
  - `GetTaskByIdApplicationServiceTest`
  - `GetTasksApplicationServiceTest`
  - `UpdateTaskStatusApplicationServiceTest`

Unit tests run during the Maven `test` phase and execute quickly without external dependencies.

#### Integration Tests
Located in `src/test/kotlin/it/`, these tests verify end-to-end functionality:

- **Full API Testing**: Tests all REST endpoints with real HTTP requests
- **Testcontainers**: Automatically spins up real MongoDB and Keycloak instances in Docker containers
- **Authentication Flow**: Tests OAuth2/JWT authentication with actual Keycloak tokens
- **Database Operations**: Validates persistence layer with real MongoDB operations

**Integration Test Coverage:**
- `TaskIT` - Comprehensive API tests including:
  - Creating tasks (single and bulk)
  - Retrieving tasks (by ID and paginated lists)
  - Updating task status
  - Error handling and validation
  - Authentication and authorization

**Test Infrastructure:**
- `IntegrationTestBase` - Base class providing:
  - Shared Testcontainers setup (MongoDB 8.0.13 and Keycloak 26.2.4)
  - OAuth2 token provisioning
  - MockMvc configuration
  - Database cleanup between tests
- `KeycloakTokenProvider` - Utility for obtaining JWT tokens
- `IntegrationTestConfig` - Test-specific Spring configuration

## Postman Collection

A Postman collection is included in the project root: `Task Manager.postman_collection.json`

Import this collection into Postman to quickly test all API endpoints.

## Development

### Project Structure

```
src/main/kotlin/com/personal/
├── Application.kt              # Main application class
├── adapters/
│   ├── inbound/web/           # REST controllers and DTOs
│   └── outbound/              # Database repositories
├── config/                     # Spring configuration
├── domain/                     # Domain models and errors
└── usecase/                    # Use cases, ports, and services
```

### Running in Development Mode

The application supports hot reload when using Spring Boot DevTools. Simply run:

```bash
./mvnw spring-boot:run
```