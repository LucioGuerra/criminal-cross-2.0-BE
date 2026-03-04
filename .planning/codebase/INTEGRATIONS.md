# External Integrations

**Analysis Date:** 2026-03-04

## APIs & External Services

**Firebase Authentication:**
- Purpose: Primary identity provider for user authentication
- SDK: `com.google.firebase:firebase-admin:9.2.0`
- Configuration: `src/main/java/org/athlium/auth/infrastructure/config/FirebaseConfig.java`
- Token Validator: `src/main/java/org/athlium/auth/infrastructure/security/FirebaseTokenValidator.java`
- Auth env var: `FIREBASE_CREDENTIALS_PATH` (path to service account JSON)
- Optional env var: `FIREBASE_PROJECT_ID`
- Mock mode: `firebase.mock.enabled=true` (default in dev) - skips real Firebase calls, returns mock user data
- Flow: Frontend authenticates with Firebase -> sends ID token to backend -> `FirebaseTokenValidator` verifies via `FirebaseAuth.verifyIdToken()` -> creates `DecodedToken` domain model

**Google Cloud Firestore:**
- SDK: `com.google.cloud:google-cloud-firestore:3.21.0`
- Status: Dependency present in `pom.xml` but marked as optional ("Firestore (opcional)"). No active usage detected in application code. The MongoDB Panache is used instead for document storage.
- Risk: Unused dependency, adds build weight

## Data Storage

**PostgreSQL (Primary Relational Database):**
- Version: 15 (Alpine image in Docker)
- Connection config: `quarkus.datasource.jdbc.url`
- Production env: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- ORM: Hibernate ORM with Panache (`quarkus-hibernate-orm-panache`)
- Driver: `quarkus-jdbc-postgresql`
- Dev schema strategy: `drop-and-create` (Hibernate auto-generates schema)
- Prod schema strategy: `none` (Flyway manages migrations)
- Tables (from `src/main/resources/db/migration/V1.0.0__init.sql`):
  - `users` - User accounts linked to Firebase UIDs
  - `user_roles` - Role assignments (CLIENT, PROFESSOR, ORG_ADMIN, ORG_OWNER, SUPERADMIN)
  - `organizations` - Gym/fitness organizations
  - `headquarters` - Physical locations within organizations
  - `activity` - Activities offered at headquarters
  - `activity_schedules` - Recurring/one-time activity schedules
  - `session_instances` - Generated session instances from schedules
  - `bookings` - User bookings for sessions (with waitlist support)
  - `client_packages` - Client subscription packages with credits
  - `client_package_credits` - Per-activity token credits within packages
  - `payments` - Payment records linked to packages
  - `refresh_tokens` - JWT refresh tokens for session management
- Migrations: `src/main/resources/db/migration/V1.0.0__init.sql` through `V6__Create_payments_and_link_to_packages.sql`
- Seed data: `src/main/resources/import.sql` (used in dev with `drop-and-create`)

**MongoDB (Document Database):**
- Version: 6 (Docker Compose) / 7 (Podman Compose)
- Connection config: `quarkus.mongodb.connection-string`
- Production env: `MONGO_URL`, `MONGO_DB`
- Client: Quarkus MongoDB with Panache (`quarkus-mongodb-panache`)
- Database name: `athlium-mongo`
- Purpose: Stores hierarchical session configuration documents (flexible schema for config inheritance)
- Collections (inferred from `@MongoEntity` annotations):
  - `gym_config` - Organization-level session configuration (`src/main/java/org/athlium/gym/infrastructure/document/OrganizationConfigDocument.java`)
  - `branch_config` - Headquarters-level configuration overrides (`src/main/java/org/athlium/gym/infrastructure/document/HeadquartersConfigDocument.java`)
  - `activity_config` - Activity-level configuration overrides (`src/main/java/org/athlium/gym/infrastructure/document/ActivityConfigDocument.java`)
  - `session_overrides` - Per-session configuration overrides (`src/main/java/org/athlium/gym/infrastructure/document/SessionConfigDocument.java`)
- Repository: `src/main/java/org/athlium/gym/infrastructure/repository/SessionConfigurationRepositoryImpl.java`
- Index management: Unique indexes created programmatically via `@PostConstruct` in `SessionConfigurationRepositoryImpl`

**File Storage:**
- Local filesystem only (secrets mounted as Docker volumes from `./secrets/`)
- No cloud file storage (S3, GCS, etc.)

**Caching:**
- None detected. No Redis, Caffeine, or Quarkus cache extensions present.

## Authentication & Identity

**Auth Architecture:**
- Two-phase authentication flow implemented in `src/main/java/org/athlium/auth/`
- Phase 1 (Firebase): Client authenticates with Firebase, backend verifies Firebase ID tokens
- Phase 2 (Custom JWT): Backend issues its own short-lived JWTs (15 min) + refresh tokens (30 days)

**Firebase Token Validation:**
- Implementation: `src/main/java/org/athlium/auth/infrastructure/security/FirebaseTokenValidator.java`
- Validates Firebase ID tokens via `FirebaseAuth.verifyIdToken()`
- Supports mock mode for local development
- Extracts `uid`, `email`, `name`, `sign_in_provider` from Firebase token claims

**Custom JWT Generation:**
- Implementation: `src/main/java/org/athlium/auth/infrastructure/security/JwtTokenGenerator.java`
- Uses SmallRye JWT (`io.smallrye.jwt.build.Jwt`) for token generation
- Signs with RSA private key (`smallrye.jwt.sign.key.location`)
- Claims: `sub` (Firebase UID), `upn` (email), `userId`, `groups` (roles), `firebaseUid`
- Issuer: `https://athlium.com`
- Expiry: 15 minutes (configurable via `auth.access-token.expiration-minutes`)

**Custom JWT Validation:**
- Implementation: `src/main/java/org/athlium/auth/infrastructure/security/CustomJwtValidator.java`
- Uses JJWT library (`io.jsonwebtoken.Jwts`) for signature verification with RSA public key
- Detects token type (custom vs Firebase) by decoding JWT payload and checking issuer

**Refresh Tokens:**
- Stored in PostgreSQL `refresh_tokens` table
- Entity: `src/main/java/org/athlium/auth/infrastructure/entity/RefreshTokenEntity.java`
- Repository: `src/main/java/org/athlium/auth/infrastructure/repository/RefreshTokenRepository.java`
- Tracks device info, IP address, revocation status

**Auth Filter (Request Interceptor):**
- Implementation: `src/main/java/org/athlium/auth/infrastructure/security/FirebaseAuthFilter.java`
- JAX-RS `ContainerRequestFilter` at `@Priority(Priorities.AUTHENTICATION)`
- Annotations: `@Authenticated` (requires auth + optional role check), `@PublicEndpoint` (no auth required)
- Sets `SecurityContext` with `AuthenticatedUser` for downstream use
- Supports optional authentication on public endpoints (enriches context if token present)

**Auth Endpoints (`src/main/java/org/athlium/auth/infrastructure/controller/AuthResource.java`):**
- `POST /api/auth/verify-token` - Verify Firebase ID token (public)
- `POST /api/auth/register` - Register new user from Firebase token (public)
- `POST /api/auth/login` - Login with Firebase token, returns JWT + refresh token (public)
- `POST /api/auth/refresh` - Exchange refresh token for new JWT pair (public)
- `POST /api/auth/logout` - Revoke refresh tokens (public)
- `GET /api/auth/me` - Get current authenticated user (requires auth)
- `GET /api/auth/health` - Auth module health check (public)

**Role-Based Access Control:**
- Roles defined in `src/main/java/org/athlium/users/domain/model/Role.java`: CLIENT, PROFESSOR, ORG_ADMIN, ORG_OWNER, SUPERADMIN
- Enforced via `@Authenticated(roles = {...}, requireAll = true/false)` annotation
- Checked in `FirebaseAuthFilter.checkRoles()`

## Monitoring & Observability

**Health Checks:**
- SmallRye Health enabled via `quarkus-smallrye-health`
- Endpoint: `GET /q/health`
- Custom auth health: `GET /api/auth/health` (reports Firebase initialization status)

**Metrics:**
- SmallRye Metrics enabled via `quarkus-smallrye-metrics`
- Standard MicroProfile Metrics endpoint

**Error Tracking:**
- No external error tracking service (Sentry, Datadog, etc.)
- Global exception handler: `src/main/java/org/athlium/shared/exception/GlobalExceptionMapper.java`
- Custom domain exceptions: `EntityNotFoundException`, `DomainException`, `BadRequestException` in `src/main/java/org/athlium/shared/exception/`
- Auth-specific exceptions in `src/main/java/org/athlium/auth/domain/exception/`

**Logging:**
- JBoss LogManager (`org.jboss.logging.Logger`)
- SQL logging enabled in dev: `quarkus.hibernate-orm.log.sql=true`

## API Documentation

**OpenAPI/Swagger:**
- SmallRye OpenAPI enabled via `quarkus-smallrye-openapi`
- Spec endpoint: `GET /q/openapi`
- Swagger UI available in dev mode via Dev UI

**Postman Collections:**
- `postman/Athlium-Backend-Manual-E2E.postman_collection.json`
- `postman/Criminal-Cross-2.0-Full.postman_collection.json`
- `postman/Criminal-Cross-2.0-Local.postman_environment.json`

## CI/CD & Deployment

**Hosting:**
- Docker-based deployment via `docker-compose.yml`
- No cloud-specific deployment configs detected (no Kubernetes manifests, Terraform, etc.)

**CI Pipeline:**
- `.github/` directory exists but only contains `copilot-instructions.md`
- No GitHub Actions workflows detected

**Container Orchestration:**
- `docker-compose.yml` - Production-ready: backend + PostgreSQL 15 + MongoDB 6 on `cc_network` bridge
- `podman-compose.yaml` - Dev-only: PostgreSQL 15 + MongoDB 7 (databases only, no backend)
- Backend container: port 8080, depends on healthy Postgres + started Mongo

## Scheduled Jobs

**Weekly Session Generator:**
- Implementation: `src/main/java/org/athlium/gym/infrastructure/scheduler/WeeklySessionScheduler.java`
- Schedule: `0 0 3 ? * MON` (Every Monday at 3:00 AM UTC)
- Purpose: Auto-generates session instances for the upcoming week from activity schedules

**Client Package Expiration:**
- Implementation: `src/main/java/org/athlium/clients/infrastructure/scheduler/ClientPackageExpirationScheduler.java`
- Schedule: `0 15 3 * * ?` (Daily at 3:15 AM UTC)
- Purpose: Marks expired client packages as inactive

## Environment Configuration

**Required env vars for production:**
- `DB_URL` - PostgreSQL JDBC URL (e.g., `jdbc:postgresql://postgres:5432/criminal_cross`)
- `DB_USERNAME` - PostgreSQL username
- `DB_PASSWORD` - PostgreSQL password
- `MONGO_URL` - MongoDB connection string
- `MONGO_DB` - MongoDB database name
- `FIREBASE_CREDENTIALS_PATH` - Path to Firebase service account JSON file
- `JWT_PRIVATE_KEY_LOCATION` - Path to RSA private key PEM for JWT signing
- `JWT_PUBLIC_KEY_LOCATION` - Path to RSA public key PEM for JWT verification

**Optional env vars:**
- `FIREBASE_PROJECT_ID` - Firebase project ID (auto-detected from credentials)
- `FIREBASE_MOCK_ENABLED` - Enable mock auth (default: `true` in dev, `false` in prod)

**Secrets location:**
- Mounted as Docker volume: `./secrets:/app/secrets:ro`
- Expected files: `firebase-adminsdk.json`, `rsaPrivateKey.pem`, `rsaPublicKey.pem`

## Webhooks & Callbacks

**Incoming:**
- None detected

**Outgoing:**
- None detected

## Module Domains

The application is organized into 9 feature modules under `src/main/java/org/athlium/`:

| Module | Storage | Key Resources |
|--------|---------|--------------|
| `auth` | PostgreSQL (refresh_tokens) | `/api/auth/*` |
| `users` | PostgreSQL (users, user_roles) | `/api/users/*` |
| `gym` | PostgreSQL + MongoDB | `/api/organizations/*`, `/api/headquarters/*`, `/api/activities/*`, `/api/sessions/*` |
| `bookings` | PostgreSQL | `/api/bookings/*`, `/api/sessions/*/bookings/*` |
| `clients` | PostgreSQL | `/api/client-packages/*` |
| `payments` | PostgreSQL | `/api/payments/*` |
| `exercises` | TBD (module scaffolded, empty) | TBD |
| `routines` | TBD (module scaffolded, empty) | TBD |
| `shared` | N/A | DTOs, exceptions, utilities |

---

*Integration audit: 2026-03-04*
