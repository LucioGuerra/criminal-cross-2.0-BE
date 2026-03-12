# Athlium Backend

Quarkus-based monolith backend for Athlium fitness platform with modular architecture.

## 🚀 Quick Start

```bash
# Development mode (with live reload)
./mvnw quarkus:dev

# Dev UI available at http://localhost:8080/q/dev
```

## 📋 Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- MongoDB 5.0+
- Firebase project (for authentication)

## 🏗️ Architecture

Modular monolith split into feature modules under `src/main/java/org/athlium/`:

```
org/athlium/
├── auth/          # Authentication & Authorization (Firebase + JWT)
├── users/         # User management & profiles
├── clients/       # Client-specific features
├── bookings/      # Booking & scheduling
├── exercises/     # Exercise catalog
├── routines/      # Workout routines
├── payments/      # Payment processing
├── gym/           # Gym management
└── shared/        # Shared utilities, DTOs, exceptions
```

Each module follows clean architecture principles:
- `application/` - Use cases and orchestration
- `domain/` - Business entities and logic
- `infrastructure/` - External concerns (DB, APIs, controllers)
- `presentation/` - REST resources (when applicable)

## 🔐 Authentication System

The Auth module uses a **Firebase BFF flow**:

- **Backend intermediary** for `register` and `login` credential payloads (`email`, `password`)
- **Firebase-issued tokens** returned by backend (`accessToken`/ID token + `refreshToken`)
- **Authorization with Bearer ID token** (`Authorization: Bearer <idToken>`)
- **Local user enrichment** via `GET /api/auth/me` (includes `userId`, `roles`, `registered`, `active`)

See [Auth Module Documentation](src/main/java/org/athlium/auth/README.md) for details.

## 🗄️ Database Configuration

### PostgreSQL
```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/athlium-pg
quarkus.datasource.username=root
quarkus.datasource.password=root
```

### MongoDB
```properties
quarkus.mongodb.connection-string=mongodb://root:root@localhost:27017/athlium-mongo?authSource=admin
```

### Schema Management
- **Development:** `drop-and-create` mode (auto-recreates on restart)
- **Production:** Flyway migrations in `src/main/resources/db/migration/`

```properties
# Development
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.flyway.migrate-at-start=false

# Production
quarkus.hibernate-orm.database.generation=none
quarkus.flyway.migrate-at-start=true
```

## 🛠️ Development

### Running the application in dev mode

Live coding enabled with automatic reload:

```bash
./mvnw quarkus:dev
```

**Dev UI:** http://localhost:8080/q/dev  
**Health Check:** http://localhost:8080/q/health  
**OpenAPI/Swagger:** http://localhost:8080/q/swagger-ui

### Environment Variables

```bash
# Firebase
export FIREBASE_CREDENTIALS_PATH=src/main/resources/athlium-credentials.json
export FIREBASE_PROJECT_ID=athlium-11937

# Optional: Enable mock mode (development only)
export FIREBASE_MOCK_ENABLED=false
```

## 📦 Building & Packaging

### Standard JAR
```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

Produces `quarkus-run.jar` with dependencies in `target/quarkus-app/lib/`.

### Uber JAR
```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

Single JAR with all dependencies embedded.

### Native Executable
```bash
# With GraalVM installed
./mvnw package -Dnative

# Using container build
./mvnw package -Dnative -Dquarkus.native.container-build=true

# Run
./target/backend-1.0.0-SNAPSHOT-runner
```

For more: [Quarkus Native Guide](https://quarkus.io/guides/maven-tooling)

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw clean verify

# Skip tests during build
./mvnw package -DskipTests
```

## 📡 API Endpoints

### Authentication
- `POST /api/auth/register` - Register with credentials, returns Firebase token pair + local user data
- `POST /api/auth/login` - Login with credentials, returns Firebase token pair + local user data
- `POST /api/auth/refresh` - Refresh Firebase token pair
- `POST /api/auth/logout` - Logout (revoke tokens)
- `GET /api/auth/me` - Get current user (Firebase identity + local roles)
- `POST /api/auth/verify-token` - Deprecated (`410 Gone`)

### Users & Gym (Current Contract)
- `GET /api/users` and `GET /api/users/{id}` are available for `SUPERADMIN`, `ORG_ADMIN`, and `ORG_OWNER`.
- Preferred full user update route is `PUT /api/users/{id}`; `PUT /api/users/firebase/{uid}` remains for backward compatibility.
- Role assignment matrix: `ORG_OWNER` can assign `ORG_ADMIN`/`PROFESSOR`/`CLIENT`; `ORG_ADMIN` can assign `PROFESSOR`/`CLIENT`; `SUPERADMIN` keeps full control.
- Headquarters membership updates allow self assign/remove; managing other users requires org-scoped `ORG_ADMIN`/`ORG_OWNER` or `SUPERADMIN`.
- `GET /api/organizations` / `GET /api/organizations/{id}` and `GET /api/headquarters` / `GET /api/headquarters/{id}` are open to any authenticated user.
- Session responses include `participants` with participant identity fields: `id`, `name`, `lastName`, `email`.

### Health & Monitoring
- `GET /q/health` - Health check
- `GET /q/health/ready` - Readiness check
- `GET /q/health/live` - Liveness check
- `GET /q/metrics` - Prometheus metrics

### Documentation
- `GET /q/swagger-ui` - Interactive API docs
- `GET /q/openapi` - OpenAPI spec (JSON/YAML)
- `GET /q/dev` - Dev UI (dev mode only)

## 🔧 Tech Stack

### Framework & Core
- **Quarkus 3.29.0** - Supersonic Subatomic Java Framework
- **Java 17+** - LTS version
- **Jakarta EE** - REST, CDI, Bean Validation

### Persistence
- **Hibernate ORM with Panache** - PostgreSQL persistence
- **MongoDB with Panache** - Document storage
- **Flyway** - Database migrations
- **PostgreSQL 14+** - Relational database
- **MongoDB 5.0+** - Document database

### Security & Auth
- **Firebase Admin SDK 9.2.0** - Firebase identity/token validation
- **Firebase Identity Toolkit integration** - Register/login/refresh mediation
- **Bearer Firebase ID tokens** - Protected API authorization

### Mapping & Serialization
- **MapStruct 1.5.5** - DTO ↔ Entity mapping
- **Lombok 1.18.38** - Boilerplate reduction
- **Jackson** - JSON serialization

### Monitoring & Docs
- **SmallRye Health** - Health checks
- **SmallRye Metrics** - Prometheus metrics
- **SmallRye OpenAPI** - API documentation
- **SmallRye GraphQL** - GraphQL API (optional)

## 📁 Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/org/athlium/
│   │   │   ├── auth/              # Authentication module
│   │   │   │   ├── application/   # Use cases
│   │   │   │   ├── domain/        # Entities, value objects
│   │   │   │   └── infrastructure/# Controllers, repos, adapters
│   │   │   ├── users/             # User management
│   │   │   ├── clients/           # Client features
│   │   │   ├── bookings/          # Booking system
│   │   │   ├── exercises/         # Exercise catalog
│   │   │   ├── routines/          # Workout routines
│   │   │   ├── payments/          # Payment processing
│   │   │   ├── gym/               # Gym management
│   │   │   └── shared/            # Shared code
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── privateKey.pem     # JWT signing key
│   │       ├── publicKey.pem      # JWT validation key
│   │       ├── athlium-credentials.json  # Firebase credentials
│   │       └── db/migration/      # Flyway migrations
│   └── test/                       # Unit & integration tests
├── pom.xml                         # Maven dependencies
└── README.md                       # This file
```

## 🚀 Deployment

### Development
```bash
./mvnw quarkus:dev
```

### Production

1. **Build:**
   ```bash
   ./mvnw package -Dquarkus.package.jar.type=uber-jar
   ```

2. **Environment Variables:**
   ```bash
   export FIREBASE_CREDENTIALS_PATH=/path/to/credentials.json
   export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://prod-db:5432/athlium
   export QUARKUS_DATASOURCE_USERNAME=prod_user
   export QUARKUS_DATASOURCE_PASSWORD=secure_password
   ```

3. **Run:**
   ```bash
   java -jar target/*-runner.jar
   ```

### Docker

```dockerfile
FROM registry.access.redhat.com/ubi8/openjdk-17:1.18

COPY target/quarkus-app/lib/ /deployments/lib/
COPY target/quarkus-app/*.jar /deployments/
COPY target/quarkus-app/app/ /deployments/app/
COPY target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185

ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]
```

## 🔒 Security Considerations

### Production Checklist
- [ ] Rotate RSA keypair (don't use dev keys)
- [ ] Store private keys in secrets manager
- [ ] Enable HTTPS only
- [ ] Configure CORS properly
- [ ] Set up rate limiting
- [ ] Enable database connection pooling
- [ ] Configure log levels appropriately
- [ ] Set up monitoring and alerting
- [ ] Use environment-specific Firebase projects
- [ ] Enable database encryption at rest

## 📚 Module Documentation

- [Auth Module](src/main/java/org/athlium/auth/README.md) - Complete authentication guide
- [Users Module](src/main/java/org/athlium/users/README.md) - User management
- More modules coming soon...

## 🤝 Contributing

1. Follow the module structure convention
2. Write tests for new features
3. Update documentation
4. Follow Java code style guidelines
5. Keep modules independent
