# Codebase Structure

**Analysis Date:** 2026-03-04

## Directory Layout

```
backend/
├── src/
│   ├── main/
│   │   ├── java/org/athlium/
│   │   │   ├── auth/                    # Authentication & authorization module
│   │   │   │   ├── application/
│   │   │   │   │   ├── ports/           # Port interfaces (UserProvider, TokenValidator, RefreshTokenStore)
│   │   │   │   │   ├── service/         # AuthService facade
│   │   │   │   │   └── usecase/         # Login, Register, Verify, Refresh, Logout use cases
│   │   │   │   ├── domain/
│   │   │   │   │   ├── exception/       # Auth-specific exceptions
│   │   │   │   │   └── model/           # AuthenticatedUser, DecodedToken, RefreshToken, AuthProvider
│   │   │   │   └── infrastructure/
│   │   │   │       ├── adapter/         # UserModuleAdapter, RefreshTokenStoreAdapter
│   │   │   │       ├── config/          # FirebaseConfig
│   │   │   │       ├── controller/      # AuthResource (REST endpoints)
│   │   │   │       ├── dto/             # Auth request/response DTOs
│   │   │   │       ├── entity/          # RefreshTokenEntity (JPA)
│   │   │   │       ├── mapper/          # AuthMapper, RefreshTokenMapper (MapStruct)
│   │   │   │       ├── repository/      # RefreshTokenRepository (Panache)
│   │   │   │       └── security/        # FirebaseAuthFilter, JwtTokenGenerator, SecurityContext, annotations
│   │   │   ├── bookings/               # Booking management module
│   │   │   │   ├── application/
│   │   │   │   │   └── usecase/         # Create, Cancel, Get bookings
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/           # Booking, BookingStatus
│   │   │   │   │   └── repository/      # BookingRepository interface
│   │   │   │   ├── infrastructure/
│   │   │   │   │   ├── entity/          # BookingEntity (JPA)
│   │   │   │   │   ├── mapper/          # BookingMapper (MapStruct, entity↔domain)
│   │   │   │   │   └── repository/      # BookingPanacheRepository, BookingRepositoryImpl
│   │   │   │   └── presentation/
│   │   │   │       ├── controller/      # BookingResource, SessionBookingResource
│   │   │   │       ├── dto/             # BookingResponse, CreateBookingRequest, etc.
│   │   │   │       └── mapper/          # BookingDtoMapper (domain↔DTO)
│   │   │   ├── clients/                # Client package & credit management
│   │   │   │   ├── application/
│   │   │   │   │   ├── service/         # ClientPackageCreditService
│   │   │   │   │   └── usecase/         # Create, Get, Update, Expire packages
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/           # ClientPackage, ClientPackageCredit
│   │   │   │   │   └── repository/      # ClientPackageRepository interface
│   │   │   │   ├── infrastructure/
│   │   │   │   │   ├── entity/          # ClientPackageEntity, ClientPackageCreditEntity
│   │   │   │   │   ├── repository/      # ClientPackagePanacheRepository, ClientPackageRepositoryImpl
│   │   │   │   │   └── scheduler/       # ClientPackageExpirationScheduler (daily cron)
│   │   │   │   └── presentation/
│   │   │   │       ├── controller/      # ClientPackageResource
│   │   │   │       ├── dto/             # ClientPackageResponse, ClientPackageUpsertRequest
│   │   │   │       └── mapper/          # ClientPackageDtoMapper
│   │   │   ├── exercises/              # Exercises module (scaffold only, not yet implemented)
│   │   │   ├── gym/                    # Core gym management module (organizations, headquarters, activities, sessions)
│   │   │   │   ├── application/
│   │   │   │   │   ├── usecase/         # CRUD for org/hq/activity + session generation, config resolution
│   │   │   │   │   └── usecase/template/ # Session template builders (Builder pattern)
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/           # Organization, Headquarters, Activity, ActivitySchedule, SessionInstance, SessionConfiguration, enums
│   │   │   │   │   └── repository/      # Repository interfaces for all gym entities
│   │   │   │   ├── infrastructure/
│   │   │   │   │   ├── document/        # MongoDB documents for hierarchical config
│   │   │   │   │   ├── entity/          # JPA entities for PostgreSQL
│   │   │   │   │   ├── mapper/          # MapStruct entity↔domain mappers
│   │   │   │   │   ├── repository/      # Panache repos (PG + Mongo) + RepositoryImpl classes
│   │   │   │   │   └── scheduler/       # WeeklySessionScheduler (weekly cron)
│   │   │   │   └── presentation/
│   │   │   │       ├── controller/      # OrganizationResource, HeadquartersResource, ActivityResource, SessionResource, etc.
│   │   │   │       ├── dto/             # Input/Response DTOs for all gym entities
│   │   │   │       └── mapper/          # MapStruct domain↔DTO mappers
│   │   │   ├── payments/               # Payment recording module
│   │   │   │   ├── application/usecase/ # CreatePaymentUseCase
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/           # Payment, PaymentMethod
│   │   │   │   │   └── repository/      # PaymentRepository interface
│   │   │   │   ├── infrastructure/
│   │   │   │   │   ├── entity/          # PaymentEntity (JPA)
│   │   │   │   │   └── repository/      # PaymentPanacheRepository, PaymentRepositoryImpl
│   │   │   │   └── presentation/
│   │   │   │       ├── controller/      # PaymentResource
│   │   │   │       ├── dto/             # CreatePaymentRequest, PaymentResponse
│   │   │   │       └── mapper/          # PaymentDtoMapper
│   │   │   ├── routines/               # Routines module (scaffold only, not yet implemented)
│   │   │   ├── shared/                 # Cross-cutting shared utilities
│   │   │   │   ├── domain/             # PageResponse<T>
│   │   │   │   ├── dto/                # ApiResponse<T>
│   │   │   │   ├── exception/          # GlobalExceptionMapper, DomainException, BadRequestException, EntityNotFoundException
│   │   │   │   └── util/               # DateUtils
│   │   │   └── users/                  # User management module
│   │   │       ├── application/
│   │   │       │   └── usecase/         # CreateUser, GetUserByUid, SyncUserWithFirebase, UpdateUserRoles
│   │   │       ├── domain/
│   │   │       │   ├── model/           # User, Role (enum)
│   │   │       │   └── repository/      # UserRepository interface
│   │   │       └── infrastructure/
│   │   │           ├── controller/      # UserResource
│   │   │           ├── dto/             # CreateUserRequestDto, UpdateRolesRequestDto, UserResponseDto
│   │   │           ├── entity/          # UserEntity (JPA)
│   │   │           ├── mapper/          # UserMapper (entity↔domain), UserDtoMapper (domain↔DTO)
│   │   │           └── repository/      # UserPanacheRepository, UserRepositoryImpl
│   │   ├── docker/                      # Quarkus-generated Dockerfiles (JVM, legacy-jar, native, native-micro)
│   │   └── resources/
│   │       ├── application.properties   # Main Quarkus config (datasources, Firebase, JWT, CORS, profiles)
│   │       ├── db/migration/            # Flyway SQL migrations (V1 through V6)
│   │       ├── import.sql               # Dev/test seed data (currently empty)
│   │       ├── privateKey.pem           # JWT signing key (dev only)
│   │       └── publicKey.pem            # JWT verification key (dev only)
│   └── test/
│       ├── java/org/athlium/
│       │   ├── auth/                    # Auth test stubs (mostly .gitkeep)
│       │   ├── bookings/               # Booking use case + controller unit tests
│       │   ├── clients/                # Client package use case + repository tests
│       │   ├── e2e/                    # End-to-end integration tests
│       │   ├── exercises/              # Exercise test stubs (mostly .gitkeep)
│       │   ├── gym/                    # Gym use case + controller + mapper tests
│       │   ├── payments/               # Payment use case tests
│       │   ├── routines/               # Routine test stubs (mostly .gitkeep)
│       │   ├── shared/                 # Shared test stubs (mostly .gitkeep)
│       │   └── users/                  # User use case + DTO validation + repository tests
│       └── resources/
│           └── application.properties   # Test-specific Quarkus config
├── postman/                             # Postman collection files for API testing
├── docker-compose.yml                   # Production Docker Compose (backend + PostgreSQL + MongoDB)
├── Dockerfile                           # Multi-stage build (Maven → Quarkus fast-jar)
├── podman-compose.yaml                  # Alternative container orchestration
├── pom.xml                              # Maven project config (Quarkus 3.29.0, Java 21)
├── mvnw / mvnw.cmd                      # Maven wrapper scripts
├── .env.example                         # Environment variable template
└── .github/
    └── copilot-instructions.md          # GitHub Copilot context instructions
```

## Directory Purposes

**`src/main/java/org/athlium/auth/`:**
- Purpose: Authentication and authorization. Firebase token validation, custom JWT issuance, refresh tokens, user registration/login flows.
- Contains: Auth filter, security annotations, token generators/validators, auth service facade, auth-specific domain exceptions.
- Key files: `infrastructure/security/FirebaseAuthFilter.java` (request interceptor), `application/service/AuthService.java` (facade), `infrastructure/security/SecurityContext.java` (request-scoped user holder), `infrastructure/adapter/UserModuleAdapter.java` (auth→users bridge).
- **Note:** Uses `infrastructure/controller/` and `infrastructure/dto/` (older convention) instead of `presentation/`.

**`src/main/java/org/athlium/gym/`:**
- Purpose: Core gym domain. Organization → Headquarters → Activity → ActivitySchedule → SessionInstance hierarchy. Session configuration (hierarchical merge from MongoDB).
- Contains: Largest module. CRUD use cases for all gym entities, session auto-generation, template builders, hierarchical config resolution.
- Key files: `application/usecase/GenerateNextWeekSessionsUseCase.java`, `application/usecase/ResolveSessionConfigurationUseCase.java`, `infrastructure/repository/SessionConfigurationRepositoryImpl.java` (MongoDB), `infrastructure/scheduler/WeeklySessionScheduler.java`.

**`src/main/java/org/athlium/bookings/`:**
- Purpose: Session booking management. Create, cancel, list bookings with waitlist support and idempotency.
- Contains: Booking use cases, booking entity/repository, split controllers (BookingResource for queries/cancel, SessionBookingResource for create).
- Key files: `application/usecase/CreateBookingUseCase.java` (complex: capacity check, waitlist, credit consumption, idempotency).

**`src/main/java/org/athlium/clients/`:**
- Purpose: Client package (subscription/credit) management. Activity-specific token credits within time-limited packages.
- Contains: CRUD for client packages, credit service for consuming/refunding tokens, expiration scheduler.
- Key files: `application/service/ClientPackageCreditService.java` (cross-module service used by bookings).

**`src/main/java/org/athlium/users/`:**
- Purpose: User account management. CRUD, role management, Firebase UID sync.
- Contains: User domain model with roles (CLIENT, PROFESSOR, ORG_ADMIN, ORG_OWNER, SUPERADMIN), RBAC support.
- Key files: `domain/model/User.java`, `domain/model/Role.java`.
- **Note:** Uses `infrastructure/controller/` and `infrastructure/dto/` (older convention) instead of `presentation/`.

**`src/main/java/org/athlium/payments/`:**
- Purpose: Payment recording. Currently supports creating payments (CASH, CARD, TRANSFER, OTHER).
- Contains: Minimal CRUD for payment records. Linked to client packages via `paymentId`.

**`src/main/java/org/athlium/shared/`:**
- Purpose: Cross-cutting utilities shared by all modules.
- Contains: `ApiResponse<T>` (universal JSON response wrapper), `PageResponse<T>` (pagination), exception classes, `DateUtils`.
- Key files: `dto/ApiResponse.java`, `exception/GlobalExceptionMapper.java`, `domain/PageResponse.java`.

**`src/main/java/org/athlium/exercises/` and `src/main/java/org/athlium/routines/`:**
- Purpose: Placeholder modules for future development. Only contain `.gitkeep` files and README.md.
- Contains: Empty directory scaffolds following the standard module structure.

**`src/main/resources/db/migration/`:**
- Purpose: Flyway database migrations for PostgreSQL schema.
- Contains: `V1.0.0__init.sql` (full schema), `V2__Create_users_table.sql`, `V3__Create_client_packages_tables.sql`, `V4__Allow_multiple_active_client_packages.sql`, `V5__Add_consumed_package_id_to_bookings.sql`, `V6__Create_payments_and_link_to_packages.sql`.
- Note: Flyway is disabled by default in dev (`migrate-at-start=false`), enabled in prod.

**`postman/`:**
- Purpose: API testing collections.
- Contains: Full API collection (`Criminal-Cross-2.0-Full.postman_collection.json`), E2E collection (`Athlium-Backend-Manual-E2E.postman_collection.json`), local environment config.

## Key File Locations

**Entry Points:**
- `pom.xml`: Maven project configuration, all dependency declarations
- `src/main/resources/application.properties`: Quarkus configuration (datasources, Firebase, JWT, CORS, profiles)

**Configuration:**
- `pom.xml`: Dependencies and build config
- `src/main/resources/application.properties`: Runtime config (DB URLs, Firebase, JWT keys, CORS)
- `docker-compose.yml`: Production deployment (backend + Postgres 15 + Mongo 6)
- `Dockerfile`: Multi-stage build (Maven 3.9.6 + JDK 21 → Quarkus fast-jar on UBI9)
- `.env.example`: Environment variable template (existence noted only)

**Core Logic:**
- `src/main/java/org/athlium/gym/application/usecase/`: Gym business logic (25+ use cases)
- `src/main/java/org/athlium/bookings/application/usecase/CreateBookingUseCase.java`: Booking with waitlist + credits
- `src/main/java/org/athlium/clients/application/service/ClientPackageCreditService.java`: Credit consumption/refund logic
- `src/main/java/org/athlium/auth/application/service/AuthService.java`: Auth orchestration facade
- `src/main/java/org/athlium/auth/infrastructure/security/FirebaseAuthFilter.java`: Request authentication

**Testing:**
- `src/test/java/org/athlium/`: Mirror structure of main source
- `src/test/java/org/athlium/e2e/`: End-to-end tests (Quarkus @QuarkusTest)
- `src/test/resources/application.properties`: Test-specific config

**Database Migrations:**
- `src/main/resources/db/migration/V*.sql`: Flyway migrations (V1-V6)

## Naming Conventions

**Files:**
- Domain models: `PascalCase.java` (e.g., `Organization.java`, `SessionInstance.java`)
- Use cases: `VerbNounUseCase.java` (e.g., `CreateOrganizationUseCase.java`, `GetSessionByIdUseCase.java`)
- Services: `NounService.java` (e.g., `AuthService.java`, `ClientPackageCreditService.java`)
- JPA entities: `NounEntity.java` (e.g., `OrganizationEntity.java`, `BookingEntity.java`)
- MongoDB documents: `NounDocument.java` or `NounConfigDocument.java` (e.g., `OrganizationConfigDocument.java`)
- MapStruct mappers (entity): `NounMapper.java` (e.g., `OrganizationMapper.java`)
- MapStruct mappers (DTO): `NounDtoMapper.java` (e.g., `OrganizationDtoMapper.java`)
- Panache repositories: `NounPanacheRepository.java` (e.g., `OrganizationPanacheRepository.java`)
- Repository implementations: `NounRepositoryImpl.java` (e.g., `OrganizationRepositoryImpl.java`)
- REST controllers: `NounResource.java` (e.g., `OrganizationResource.java`, `AuthResource.java`)
- Request DTOs: `NounInput.java`, `NounRequest.java`, or `NounRequestDto.java`
- Response DTOs: `NounResponse.java` or `NounResponseDto.java`
- Schedulers: `NounScheduler.java` (e.g., `WeeklySessionScheduler.java`)

**Directories:**
- Module root: lowercase plural or noun (`gym/`, `bookings/`, `auth/`, `shared/`)
- Layer dirs: lowercase (`application/`, `domain/`, `infrastructure/`, `presentation/`)
- Sub-layer dirs: lowercase (`usecase/`, `model/`, `repository/`, `controller/`, `dto/`, `mapper/`, `entity/`, `document/`, `scheduler/`, `adapter/`, `config/`, `security/`, `ports/`, `service/`, `exception/`)

**Packages:**
- Base: `org.athlium.{module}.{layer}.{sublayer}`
- Example: `org.athlium.gym.application.usecase`, `org.athlium.bookings.infrastructure.repository`

## Where to Add New Code

**New Business Module (e.g., `notifications`):**
- Create scaffold: `src/main/java/org/athlium/notifications/`
  - `application/usecase/` - Use case classes
  - `application/ports/` - Port interfaces (if needed)
  - `application/service/` - Service facades (if needed)
  - `domain/model/` - Domain models
  - `domain/repository/` - Repository interfaces
  - `infrastructure/entity/` - JPA entities
  - `infrastructure/mapper/` - MapStruct entity↔domain mappers
  - `infrastructure/repository/` - PanacheRepository + RepositoryImpl
  - `presentation/controller/` - REST resource class
  - `presentation/dto/` - Request/response DTOs
  - `presentation/mapper/` - MapStruct domain↔DTO mappers
- Tests: `src/test/java/org/athlium/notifications/` (mirror structure)
- Migration: `src/main/resources/db/migration/V{N}__description.sql`

**New Use Case in Existing Module:**
- Use case: `src/main/java/org/athlium/{module}/application/usecase/{VerbNoun}UseCase.java`
- Annotate with `@ApplicationScoped`, add `public T execute(...)` method
- Inject domain repository interfaces, not infrastructure classes
- Test: `src/test/java/org/athlium/{module}/application/usecase/{VerbNoun}UseCaseTest.java`

**New REST Endpoint in Existing Module:**
- Add method to existing `*Resource.java` in `presentation/controller/` or `infrastructure/controller/`
- Follow pattern: inject use case → call execute → map response → wrap in `ApiResponse.success()`
- For new resource class: add `@Path("/api/...")`, `@Produces(APPLICATION_JSON)`, `@Consumes(APPLICATION_JSON)`

**New JPA Entity:**
- Entity: `src/main/java/org/athlium/{module}/infrastructure/entity/{Name}Entity.java` (extend `PanacheEntity`)
- Domain model: `src/main/java/org/athlium/{module}/domain/model/{Name}.java`
- Domain repo interface: `src/main/java/org/athlium/{module}/domain/repository/{Name}Repository.java`
- Panache repo: `src/main/java/org/athlium/{module}/infrastructure/repository/{Name}PanacheRepository.java`
- Repo impl: `src/main/java/org/athlium/{module}/infrastructure/repository/{Name}RepositoryImpl.java`
- Mapper: `src/main/java/org/athlium/{module}/infrastructure/mapper/{Name}Mapper.java`
- Migration: `src/main/resources/db/migration/V{N}__description.sql`

**New MongoDB Document (config-style):**
- Document: `src/main/java/org/athlium/{module}/infrastructure/document/{Name}Document.java`
- Panache repo: `src/main/java/org/athlium/{module}/infrastructure/repository/{Name}PanacheRepository.java` (implements `PanacheMongoRepositoryBase<Doc, ObjectId>`)
- Add logic to existing `*RepositoryImpl` or create new one

**New Shared Exception:**
- Exception class: `src/main/java/org/athlium/shared/exception/{Name}Exception.java`
- Extend `RuntimeException`
- If needs specific HTTP mapping, add handling in controllers' catch blocks

**New Scheduled Task:**
- Scheduler: `src/main/java/org/athlium/{module}/infrastructure/scheduler/{Name}Scheduler.java`
- Annotate method with `@Scheduled(cron = "...")` + `timeZone = "UTC"`
- Delegate to a use case

## Special Directories

**`target/`:**
- Purpose: Maven build output (compiled classes, JARs, generated sources)
- Generated: Yes (by `mvn compile` / `mvn package`)
- Committed: No (in `.gitignore`)

**`target/generated-sources/annotations/`:**
- Purpose: MapStruct-generated mapper implementations (e.g., `OrganizationMapperImpl.java`)
- Generated: Yes (by MapStruct annotation processor during compilation)
- Committed: No

**`src/main/docker/`:**
- Purpose: Quarkus-generated Dockerfiles for different build modes (JVM, legacy-jar, native, native-micro)
- Generated: Yes (by Quarkus Maven plugin)
- Committed: Yes (checked into git)

**`.idea/`:**
- Purpose: IntelliJ IDEA project configuration
- Generated: Yes (by IDE)
- Committed: Partially (some files in `.gitignore`)

**`.amazonq/rules/`:**
- Purpose: Amazon Q Developer custom rules and memory bank
- Generated: No (manually maintained)
- Committed: Yes

**`postman/`:**
- Purpose: Postman API collections and environments for manual/E2E testing
- Generated: No (manually maintained)
- Committed: Yes

---

*Structure analysis: 2026-03-04*
