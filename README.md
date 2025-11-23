# Overview
This repository contains a Storage Service responsible for the persistence of data.
It runs on port 8081 by default.

### Tech stack
- Language: Java 25 (Temurin)
- Frameworks/Libraries: Spring Boot 3.5.x, Spring Cloud 2025.0.x, Netflix Eureka Server
- Build tool: Gradle
- Testing: JUnit Platform via spring-boot-starter-test
- Code coverage: JaCoCo
- Containerization: Docker
- CI: GitHub Actions
- DB: H2 (runtime, in‑memory) and Flyway
- Service Discovery: Eureka Client

# Requirements
- [JDK 25](https://www.oracle.com/java/technologies/downloads/#jdk25-linux)
- Docker
- Gradle

## API Overview
- Base path: /movies
- Headers: Optional X-Correlation-ID used for logging correlation.
- Endpoints:
    - GET /movies?page={n}&size={m} - paginated list
    - GET /movies/{id} - fetch by id (404 if not found)
    - POST /movies - create (201 Created)
    - PUT /movies/{id} - update if exists (404 if not found)
    - DELETE /movies/{id} - delete (204 No Content)
    - GET /movies/dev/chaos?delay={ms}&errorRate={0..1} - simulate latency and random errors (dev/testing)

# Getting started

1) Clone the repo
```shell
  git clone https://github.com/Milozap/tec-storage-service
  cd tec-storage-service
```

2) Build the project
```shell
  ./gradlew clean build
```

3) Run the application (local JVM)
```shell
  ./gradlew bootRun
```

4) Check health endpoint
- http://localhost:8081/actuator/health

### Docker

Build image:
```shell
  docker build -t storage-service:local .
```

Run container:
```shell
docker run -p 8081:8081 --name storage-service storage-service:local
```

### Database & Migrations
- Default profile uses in‑memory H2 with Flyway migrations.
- Initial schema migration: src/main/resources/db/migration/V1__create_movies_table.sql
- For persistent/production databases (e.g., PostgreSQL, MySQL), configure spring.datasource.* and Flyway accordingly.

### GitHub Actions (CI)

- The workflow .github/workflows/ci.yml:
    - Builds with Java 25 and Gradle
    - Runs tests and generates JaCoCo coverage
    - Uploads test reports on failure
    - Builds and (on non-PR events) pushes a Docker image to GitHub Container Registry (ghcr.io/milozap/tec-storage-service)
