# Architecture

**Analysis Date:** 2026-03-04

## Pattern Overview

**Overall:** Hexagonal Architecture (Ports & Adapters) with modular monolith structure

**Key Characteristics:**
- Vertical slicing by business domain (auth, gym, bookings, clients, users, payments, exercises, routines)
- Each module follows a three-layer pattern: `domain` → `application` → `infrastructure` (+ optional `presentation`)
- Domain layer has zero framework dependencies (pure Java POJOs)
- Inter-module communication through port interfaces and adapter implementations
- Dual-database design: PostgreSQL (relational data via Hibernate/Panache) + MongoDB (configuration documents via MongoDB Panache)
- Quarkus CDI (`@ApplicationScoped`, `@Inject`) for dependency injection throughout
- Use case pattern: each application-layer operation is a separate `@ApplicationScoped` class with an `execute()` method

## Layers

**Domain Layer:**
- Purpose: Core business logic and entities, free from framework dependencies
- Location: `src/main/java/org/athlium/{module}/domain/`
- Contains: Domain models (`domain/model/`), repository interfaces (`domain/repository/`), domain exceptions (`domain/exception/`)
- Depends on: Nothing (pure Java)
- Used by: Application layer, Infrastructure layer (for mapping)

**Application Layer:**
- Purpose: Orchestrate use cases, enforce business rules, coordinate domain objects
- Location: `src/main/java/org/athlium/{module}/application/`
- Contains: Use case classes (`application/usecase/`), service facades (`application/service/`), port interfaces (`application/ports/`)
- Depends on: Domain layer, port interfaces
- Used by: Presentation layer (controllers), Infrastructure layer (schedulers)

**Infrastructure Layer:**
- Purpose: Framework integration, persistence, external service adapters
- Location: `src/main/java/org/athlium/{module}/infrastructure/`
- Contains: JPA entities (`infrastructure/entity/`), MongoDB documents (`infrastructure/document/`), Panache repositories (`infrastructure/repository/`), MapStruct mappers (`infrastructure/mapper/`), adapters (`infrastructure/adapter/`), security (`infrastructure/security/`), schedulers (`infrastructure/scheduler/`), config (`infrastructure/config/`)
- Depends on: Domain layer, Application layer (implements ports), Quarkus/Hibernate/MongoDB frameworks
- Used by: Quarkus CDI container (auto-wired)

**Presentation Layer:**
- Purpose: HTTP REST endpoints, request/response DTOs, DTO mapping
- Location: `src/main/java/org/athlium/{module}/presentation/` (newer modules) or `src/main/java/org/athlium/{module}/infrastructure/controller/` (auth, users modules)
- Contains: JAX-RS resource classes (`presentation/controller/`), request/response DTOs (`presentation/dto/`), DTO mappers (`presentation/mapper/`)
- Depends on: Application layer (use cases), Shared module (ApiResponse, exceptions)
- Used by: External HTTP clients

**Shared Module:**
- Purpose: Cross-cutting concerns used by all modules
- Location: `src/main/java/org/athlium/shared/`
- Contains: `ApiResponse<T>` wrapper (`shared/dto/ApiResponse.java`), domain exceptions (`shared/exception/`), `PageResponse<T>` pagination (`shared/domain/PageResponse.java`), utilities (`shared/util/DateUtils.java`)
- Depends on: Nothing
- Used by: All modules

## Data Flow

**Standard REST Request (e.g., Create Organization):**

1. HTTP request hits `OrganizationResource` (`gym/presentation/controller/OrganizationResource.java`)
2. Controller uses `OrganizationDtoMapper` (MapStruct) to convert `OrganizationInput` DTO → `Organization` domain model
3. Controller calls `CreateOrganizationUseCase.execute(organization)` 
4. Use case calls `OrganizationRepository.save(organization)` (domain interface)
5. CDI resolves to `OrganizationRepositoryImpl` which uses `OrganizationMapper` to convert domain → `OrganizationEntity`
6. `OrganizationPanacheRepository.persist(entity)` saves to PostgreSQL via Hibernate
7. `OrganizationMapper.toDomain(entity)` converts back to domain model
8. Controller maps domain → `OrganizationResponse` DTO, wraps in `ApiResponse.success()`
9. Returns `Response.status(CREATED).entity(ApiResponse.success(...))` 

**Authenticated Request Flow:**

1. Every request passes through `FirebaseAuthFilter` (`auth/infrastructure/security/FirebaseAuthFilter.java`)
2. Filter checks for `@PublicEndpoint` annotation → skips auth if present
3. Filter checks for `@Authenticated` annotation → requires auth if present
4. Token validation: tries custom JWT first (`CustomJwtValidator`), falls back to Firebase token (`TokenValidator` port → `FirebaseTokenValidator`)
5. Enriches user with database data via `UserProvider` port → `UserModuleAdapter` → `UserRepository`
6. Populates request-scoped `SecurityContext` with `AuthenticatedUser`
7. Checks role requirements from `@Authenticated(roles = {...})` annotation
8. Downstream code accesses `SecurityContext.getCurrentUser()` for user info

**Session Configuration Resolution (Hierarchical Merge):**

1. `ResolveSessionConfigurationUseCase` fetches config from MongoDB at four levels
2. Starts with `SessionConfiguration.defaults()`
3. Merges organization-level config (from `organization_config` MongoDB collection)
4. Merges headquarters-level config (from `headquarters_config` collection)
5. Merges activity-level config (from `activity_config` collection)
6. Optionally merges session-level overrides (from `session_config` collection)
7. Each level only overrides non-null fields from the more specific level

**Booking Creation with Credit Consumption:**

1. `CreateBookingUseCase` validates session exists and is OPEN
2. Checks idempotency via `createRequestId` field
3. Resolves booking status (CONFIRMED vs WAITLISTED) based on capacity
4. If CONFIRMED: `ClientPackageCreditService.consumeCredit()` deducts a token from the user's active package
5. If WAITLISTED: only checks `hasAvailableCredit()` without consuming
6. Persists booking via `BookingRepository`

**State Management:**
- Request-scoped via CDI `@RequestScoped` (`SecurityContext`)
- Application-scoped singletons via `@ApplicationScoped` (use cases, repositories, services)
- No in-memory caching layer; all state goes to PostgreSQL or MongoDB
- `@Transactional` on use case methods or repository implementations for data integrity

## Key Abstractions

**Use Case Pattern:**
- Purpose: Single-responsibility operation handlers
- Examples: `src/main/java/org/athlium/gym/application/usecase/CreateOrganizationUseCase.java`, `src/main/java/org/athlium/bookings/application/usecase/CreateBookingUseCase.java`, `src/main/java/org/athlium/auth/application/usecase/LoginUseCase.java`
- Pattern: `@ApplicationScoped` class with `public T execute(...)` method. Each use case does one thing. Injected directly by controllers.

**Domain Repository Interface:**
- Purpose: Abstract persistence from domain logic
- Examples: `src/main/java/org/athlium/gym/domain/repository/OrganizationRepository.java`, `src/main/java/org/athlium/bookings/domain/repository/BookingRepository.java`
- Pattern: Plain Java interface in `domain/repository/`. Implemented by `*RepositoryImpl` in `infrastructure/repository/`. The impl delegates to a `*PanacheRepository` which extends Quarkus's `PanacheRepository<Entity>`.

**Repository Implementation Chain (3-class pattern):**
- `{Entity}PanacheRepository` - Extends `PanacheRepository<{Entity}Entity>`, thin Quarkus Panache wrapper
- `{Entity}RepositoryImpl` - Implements domain `{Entity}Repository`, orchestrates Panache + MapStruct mapping
- `{Entity}Mapper` - MapStruct interface for entity↔domain conversion
- Examples: `OrganizationPanacheRepository` → `OrganizationRepositoryImpl` → `OrganizationMapper`

**Port / Adapter (Auth Module):**
- Purpose: Anti-corruption layer between auth and users modules
- Examples: `src/main/java/org/athlium/auth/application/ports/UserProvider.java` (port), `src/main/java/org/athlium/auth/infrastructure/adapter/UserModuleAdapter.java` (adapter), `src/main/java/org/athlium/auth/application/ports/TokenValidator.java` (port), `src/main/java/org/athlium/auth/infrastructure/security/FirebaseTokenValidator.java` (adapter)
- Pattern: Interface in `application/ports/`, implementation in `infrastructure/adapter/`

**Service Facade (Auth Module):**
- Purpose: Orchestrate multiple use cases behind a single interface
- Example: `src/main/java/org/athlium/auth/application/service/AuthService.java`
- Pattern: `@ApplicationScoped` class that delegates to individual use cases. Used when a controller needs to call multiple related operations.

**Session Template Builder (Builder Pattern):**
- Purpose: Generate session slots from activity schedules using different strategies
- Examples: `src/main/java/org/athlium/gym/application/usecase/template/SessionTemplateDirector.java`, `src/main/java/org/athlium/gym/application/usecase/template/WeeklyRangeSessionTemplateBuilder.java`, `src/main/java/org/athlium/gym/application/usecase/template/OneTimeSessionTemplateBuilder.java`
- Pattern: Strategy + Builder. `SessionTemplateDirector` iterates CDI-discovered `SessionTemplateBuilder` implementations. Each builder `supports(schedule)` and produces `List<SessionSlot>`.

**Two-Mapper Pattern:**
- Purpose: Separate infrastructure mapping (entity↔domain) from presentation mapping (domain↔DTO)
- Infrastructure mapper: `src/main/java/org/athlium/gym/infrastructure/mapper/OrganizationMapper.java` (entity ↔ domain)
- Presentation mapper: `src/main/java/org/athlium/gym/presentation/mapper/OrganizationDtoMapper.java` (domain ↔ DTO)
- Pattern: MapStruct `@Mapper(componentModel = "jakarta-cdi")` interfaces. Two per module in newer modules.

## Entry Points

**HTTP REST API:**
- Location: All `*Resource.java` files in `presentation/controller/` or `infrastructure/controller/`
- Triggers: External HTTP requests
- Base path: `/api/` (all endpoints)
- Key resources:
  - `src/main/java/org/athlium/auth/infrastructure/controller/AuthResource.java` → `/api/auth/**`
  - `src/main/java/org/athlium/users/infrastructure/controller/UserResource.java` → `/api/users/**`
  - `src/main/java/org/athlium/gym/presentation/controller/OrganizationResource.java` → `/api/organizations/**`
  - `src/main/java/org/athlium/gym/presentation/controller/HeadquartersResource.java` → `/api/organizations/{orgId}/headquarters/**`
  - `src/main/java/org/athlium/gym/presentation/controller/ActivityResource.java` → `/api/headquarters/{hqId}/activities/**`
  - `src/main/java/org/athlium/gym/presentation/controller/ActivityScheduleResource.java` → `/api/activities/{activityId}/schedules/**`
  - `src/main/java/org/athlium/gym/presentation/controller/SessionResource.java` → `/api/sessions/**`
  - `src/main/java/org/athlium/gym/presentation/controller/SessionConfigurationResource.java` → `/api/**/session-configuration`
  - `src/main/java/org/athlium/bookings/presentation/controller/SessionBookingResource.java` → `/api/sessions/{sessionId}/bookings`
  - `src/main/java/org/athlium/bookings/presentation/controller/BookingResource.java` → `/api/bookings/**`
  - `src/main/java/org/athlium/clients/presentation/controller/ClientPackageResource.java` → `/api/clients/{userId}/packages/**`
  - `src/main/java/org/athlium/payments/presentation/controller/PaymentResource.java` → `/api/payments`

**Scheduled Tasks:**
- `src/main/java/org/athlium/gym/infrastructure/scheduler/WeeklySessionScheduler.java` → Cron: `0 0 3 ? * MON` (Monday 3AM UTC). Generates next week's session instances from active schedules.
- `src/main/java/org/athlium/clients/infrastructure/scheduler/ClientPackageExpirationScheduler.java` → Cron: `0 15 3 * * ?` (Daily 3:15AM UTC). Expires client packages past their period end date.

**Application Startup:**
- `@PostConstruct` in `SessionConfigurationRepositoryImpl` creates MongoDB unique indexes on startup
- Quarkus auto-discovers all `@Provider`, `@ApplicationScoped`, `@Path` classes via ARC (CDI)

## Error Handling

**Strategy:** Exception-based with centralized catch + per-controller manual handling

**Patterns:**
- Domain exceptions extend `RuntimeException`: `DomainException`, `BadRequestException`, `EntityNotFoundException` (all in `src/main/java/org/athlium/shared/exception/`)
- Auth-specific exceptions: `AuthenticationException`, `InvalidRefreshTokenException`, `InvalidTokenException`, `TokenExpiredException`, `UnauthorizedException`, `UserAlreadyExistsException` (in `src/main/java/org/athlium/auth/domain/exception/`)
- Controllers catch specific exceptions and map to HTTP status codes manually (try/catch pattern in every endpoint method)
- `GlobalExceptionMapper` (`src/main/java/org/athlium/shared/exception/GlobalExceptionMapper.java`) catches unhandled `Exception` and returns 500 with `ApiResponse.error()`
- All error responses use `ApiResponse.error(message)` wrapper: `{"success": false, "message": "...", "data": null}`
- All success responses use `ApiResponse.success(message, data)` wrapper: `{"success": true, "message": "...", "data": {...}}`

## Cross-Cutting Concerns

**Logging:**
- JBoss Logger (`org.jboss.logging.Logger`) used throughout
- Pattern: `private static final Logger LOG = Logger.getLogger(ClassName.class);`
- Log levels: `debugf` for tracing, `infof` for business events, `warn` for auth failures, `errorf` for exceptions

**Validation:**
- `quarkus-hibernate-validator` dependency is present for Bean Validation (`@Valid`)
- Most validation is done manually in use case `execute()` methods (checking nulls, ranges, business rules)
- Controllers use `@Valid` on some request DTOs (e.g., `UserResource`)
- Business rule validation throws `BadRequestException` or `DomainException`

**Authentication:**
- JAX-RS `ContainerRequestFilter` (`FirebaseAuthFilter`) intercepts all requests
- Annotation-based: `@Authenticated` (requires auth), `@Authenticated(roles = {"SUPERADMIN"})` (requires specific roles), `@PublicEndpoint` (no auth)
- Dual token support: Firebase ID tokens + custom JWT (SmallRye JWT + JJWT)
- Request-scoped `SecurityContext` holds current user throughout request lifecycle

**Serialization:**
- Jackson via `quarkus-rest-jackson` for JSON serialization/deserialization
- All endpoints produce/consume `MediaType.APPLICATION_JSON`
- Domain models use Lombok annotations for getters/builders
- Presentation DTOs use Lombok `@Data` for request/response objects

**Database Transactions:**
- `@Transactional` (Jakarta) on use case methods or repository implementation methods
- No distributed transactions between PostgreSQL and MongoDB
- Pessimistic locking via `findByIdForUpdate()` methods for concurrent booking/credit operations

---

*Architecture analysis: 2026-03-04*
