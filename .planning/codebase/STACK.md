# Technology Stack

**Analysis Date:** 2026-03-04

## Languages

**Primary:**
- Java 21 - All backend application code under `src/main/java/org/athlium/`

**Secondary:**
- SQL (PostgreSQL dialect) - Flyway migrations under `src/main/resources/db/migration/`

## Runtime

**Environment:**
- JDK 21 (Eclipse Temurin, via `registry.access.redhat.com/ubi9/openjdk-21:1.23` in production)
- Quarkus 3.29.0 (supersonic, subatomic Java framework)

**Package Manager:**
- Apache Maven (wrapper included: `./mvnw`)
- No lockfile; dependency versions are governed by `pom.xml` and the Quarkus BOM

## Frameworks

**Core:**
- Quarkus 3.29.0 - Main application framework (CDI, REST, lifecycle)
- Quarkus REST (formerly RESTEasy Reactive) - JAX-RS REST endpoints via `quarkus-rest` + `quarkus-rest-jackson`
- Hibernate ORM with Panache - PostgreSQL JPA persistence via `quarkus-hibernate-orm-panache`
- MongoDB with Panache - Document persistence via `quarkus-mongodb-panache`
- SmallRye JWT - JWT generation/validation via `quarkus-smallrye-jwt`
- SmallRye Health - Health endpoints via `quarkus-smallrye-health`
- SmallRye Metrics - Metrics endpoints via `quarkus-smallrye-metrics`
- SmallRye OpenAPI - Swagger/OpenAPI generation via `quarkus-smallrye-openapi`
- Quarkus Scheduler - Cron-based scheduled tasks via `quarkus-scheduler`
- Quarkus Flyway - Database migration management via `quarkus-flyway`
- Hibernate Validator - Bean validation via `quarkus-hibernate-validator`
- Quarkus ARC - CDI dependency injection container via `quarkus-arc`

**Testing:**
- JUnit 5 - Test runner via `quarkus-junit5` (scope: test)
- REST Assured - HTTP integration testing via `io.rest-assured:rest-assured` (scope: test)

**Build/Dev:**
- Maven Compiler Plugin 3.14.1 - Java compilation with annotation processing
- Maven Surefire Plugin 3.5.4 - Unit test execution
- Maven Failsafe Plugin 3.5.4 - Integration test execution
- Quarkus Maven Plugin 3.29.0 - Build, dev mode, native image support

## Key Dependencies

**Critical:**
- `com.google.firebase:firebase-admin:9.2.0` - Firebase Admin SDK for authentication token verification
- `com.google.cloud:google-cloud-firestore:3.21.0` - Firestore client (present but optional; used for potential document storage)
- `io.jsonwebtoken:jjwt-api:0.11.5` + `jjwt-impl` + `jjwt-jackson` - JJWT library for custom JWT validation (used alongside SmallRye JWT)
- `org.projectlombok:lombok:1.18.38` - Boilerplate reduction (@Builder, @Getter, etc.)
- `org.mapstruct:mapstruct:1.5.5.Final` - Compile-time DTO-to-entity mapping (processor version 1.6.0 in compiler plugin)
- `com.google.code.gson:gson:2.10.1` - JSON serialization (used alongside Jackson)

**Infrastructure:**
- `io.quarkus:quarkus-jdbc-postgresql` - PostgreSQL JDBC driver
- `io.quarkus:quarkus-hibernate-orm` - Core ORM engine
- `io.quarkus:quarkus-mongodb-panache` - MongoDB driver + Panache active record

## Configuration

**Application Properties:**
- Main config: `src/main/resources/application.properties`
- Test config: `src/test/resources/application.properties`
- Profile-based configuration using Quarkus `%prod.` prefix

**Key Config Sections:**
- PostgreSQL datasource: `quarkus.datasource.*`
- MongoDB connection: `quarkus.mongodb.*`
- Hibernate ORM: `quarkus.hibernate-orm.*` (dev: `drop-and-create`, prod: `none`)
- Flyway: `quarkus.flyway.*` (dev: disabled, prod: `migrate-at-start=true`)
- Firebase: `firebase.credentials.path`, `firebase.project-id`, `firebase.mock.enabled`
- JWT: `mp.jwt.verify.issuer`, `mp.jwt.verify.publickey.location`, `smallrye.jwt.sign.key.location`
- CORS: `quarkus.http.cors.*` (wide open regex `/.*/` for MVP)
- Auth tokens: `auth.access-token.expiration-minutes=15`, `auth.refresh-token.expiration-days=30`

**Environment Variables (Production):**
- `DB_URL` - PostgreSQL JDBC URL
- `DB_USERNAME` / `DB_PASSWORD` - PostgreSQL credentials
- `MONGO_URL` / `MONGO_DB` - MongoDB connection string and database name
- `FIREBASE_CREDENTIALS_PATH` - Path to Firebase service account JSON
- `FIREBASE_PROJECT_ID` - Firebase project ID (optional, auto-detected)
- `FIREBASE_MOCK_ENABLED` - Toggle mock auth mode (dev only, default: `true`)
- `JWT_PRIVATE_KEY_LOCATION` / `JWT_PUBLIC_KEY_LOCATION` - RSA key paths for JWT signing

**Secrets Management:**
- Secrets mounted as files via Docker volumes from `./secrets/` directory
- Firebase credentials: `firebase-adminsdk.json`
- JWT keys: `rsaPrivateKey.pem`, `rsaPublicKey.pem`
- Dev keys bundled in classpath: `src/main/resources/privateKey.pem`, `src/main/resources/publicKey.pem`

## Build Configuration

**Maven Build:**
- `pom.xml` - Single-module project, group: `org.athlium`, artifact: `backend`, version: `1.0.0-SNAPSHOT`
- Quarkus BOM managed via `dependencyManagement`
- Google Maven repository added for Firebase SDK
- Annotation processors: Lombok + MapStruct configured in `maven-compiler-plugin`
- Native image profile available: `-Pnative`

**Annotation Processing Order:**
- Lombok 1.18.38 runs first (generates getters/setters/builders)
- MapStruct 1.6.0 runs second (generates mapper implementations using Lombok output)
- Configured via `annotationProcessorPaths` in `maven-compiler-plugin`

**Docker Build:**
- Multi-stage Dockerfile: `Dockerfile`
  - Stage 1: `maven:3.9.6-eclipse-temurin-21` builds the app
  - Stage 2: `registry.access.redhat.com/ubi9/openjdk-21:1.23` runs Quarkus fast-jar
- Additional Dockerfiles in `src/main/docker/`:
  - `Dockerfile.jvm` - Standard JVM mode
  - `Dockerfile.legacy-jar` - Legacy JAR packaging
  - `Dockerfile.native` - GraalVM native image
  - `Dockerfile.native-micro` - Minimal native image

## Dev Commands

```bash
./mvnw quarkus:dev                    # Dev mode with live reload (port 8080)
./mvnw test                           # Run unit tests
./mvnw package -DskipTests            # Build fast-jar (no tests)
./mvnw package -Dnative               # Build native image
./mvnw package -Dquarkus.package.jar.type=uber-jar  # Build uber-jar
```

**Dev UI:** `http://localhost:8080/q/dev`
**Health:** `http://localhost:8080/q/health`
**OpenAPI:** `http://localhost:8080/q/openapi`

## Platform Requirements

**Development:**
- JDK 21+
- Docker or Podman (for PostgreSQL and MongoDB containers)
- Local compose files: `docker-compose.yml` (full stack) or `podman-compose.yaml` (databases only)

**Production:**
- Docker/container runtime
- PostgreSQL 15
- MongoDB 6+
- Firebase project with service account credentials
- RSA key pair for JWT signing

---

*Stack analysis: 2026-03-04*
