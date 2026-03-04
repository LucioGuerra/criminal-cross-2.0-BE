# Coding Conventions

**Analysis Date:** 2026-03-04

## Naming Patterns

**Files:**
- Domain models: `PascalCase.java` — e.g., `SessionInstance.java`, `BookingStatus.java`
- Use cases: `VerbNounUseCase.java` — e.g., `CreateBookingUseCase.java`, `GetSessionsUseCase.java`
- Entities (JPA): `XxxEntity.java` — e.g., `UserEntity.java`, `BookingEntity.java`
- Panache repositories: `XxxPanacheRepository.java` — e.g., `UserPanacheRepository.java`
- Repository implementations: `XxxRepositoryImpl.java` — e.g., `UserRepositoryImpl.java`
- MapStruct mappers: `XxxMapper.java` for Entity↔Domain, `XxxDtoMapper.java` for Domain↔DTO
- REST controllers: `XxxResource.java` — e.g., `ActivityResource.java`, `BookingResource.java`
- Request DTOs: `XxxInput.java` or `XxxRequest.java` or `CreateXxxRequestDto.java` (varies by module, see DTO section)
- Response DTOs: `XxxResponse.java` or `XxxResponseDto.java` (varies by module)

**Functions/Methods:**
- Use case entry point: always `execute(...)` — single public method per use case
- Repository methods: `findById(Long)`, `findAll()`, `save(Domain)`, `update(Domain)`, `deleteById(Long)`, `findByIdForUpdate(Long)` (pessimistic lock)
- Controller methods: short verb names — `create(...)`, `getAll(...)`, `getById(...)`, `update(...)`, `delete(...)`
- Mapper methods: `toDomain(Entity)`, `toEntity(Domain)`, `toResponse(Domain)`, `toDomain(Input)` — MapStruct convention

**Variables:**
- `camelCase` for all local variables and fields
- Domain repository fields: named after the interface — e.g., `bookingRepository`, `sessionRepository`
- Use case fields: named after the use case — e.g., `createBookingUseCase`, `getSessionsUseCase`

**Types:**
- `PascalCase` for all classes, interfaces, enums, records
- Enums: `PascalCase` with `UPPER_SNAKE_CASE` values — e.g., `BookingStatus.CONFIRMED`, `SessionSource.SCHEDULE`
- Records used for complex return types inside use cases — e.g., `CancelBookingUseCase.CancelBookingResult`

**Packages:**
- Base: `org.athlium.{module}`
- Layers: `.domain.model`, `.domain.repository`, `.application.usecase`, `.infrastructure.entity`, `.infrastructure.repository`, `.infrastructure.mapper`, `.presentation.controller`, `.presentation.dto`, `.presentation.mapper`
- Auth module exception: uses `.infrastructure.controller`, `.infrastructure.dto` instead of `.presentation.*`
- Users module exception: uses `.infrastructure.controller`, `.infrastructure.dto` instead of `.presentation.*`

## DTO Naming (Module Variations)

**Newer modules** (`gym`, `bookings`, `payments`, `clients`) — located in `presentation/dto/`:
- Request: `XxxInput.java` — e.g., `ActivityInput.java`, `CreateBookingInput.java`
- Response: `XxxResponse.java` — e.g., `ActivityResponse.java`, `BookingResponse.java`
- Page response: `XxxPageResponse.java` — e.g., `SessionPageResponse.java`

**Older modules** (`users`, `auth`) — located in `infrastructure/dto/`:
- Request: `CreateXxxRequestDto.java`, `UpdateXxxRequestDto.java` — e.g., `CreateUserRequestDto.java`
- Response: `XxxResponseDto.java` — e.g., `UserResponseDto.java`
- Auth-specific: `XxxDto.java` — e.g., `LoginDto.java`, `RegisterDto.java`, `TokenResponseDto.java`

**When adding new code: use the newer convention** — `XxxInput.java` for requests, `XxxResponse.java` for responses, placed in `presentation/dto/`.

## Code Style

**Formatting:**
- No explicit Prettier/formatter config detected in the project
- Standard Java formatting with 4-space indentation (IDE default)
- Opening braces on same line

**Linting:**
- No Checkstyle, PMD, or SpotBugs configured
- Rely on compiler warnings and IDE inspections

## Import Organization

**Order** (observed pattern):
1. `jakarta.*` — Jakarta EE / CDI / JAX-RS annotations
2. `io.quarkus.*` — Quarkus-specific imports
3. `org.athlium.*` — Project internal imports
4. `java.*` / `java.util.*` — Standard library
5. `lombok.*` — Lombok annotations (when used)

**Path Aliases:**
- None — standard Java package imports

## Architecture Patterns

### Use Case Pattern

Every use case follows this structure:
```java
@ApplicationScoped
public class DoSomethingUseCase {

    @Inject
    SomeRepository someRepository;    // package-private field injection

    @Inject
    AnotherRepository anotherRepository;

    @Transactional
    public ReturnType execute(ParamType param) {
        // 1. Validate / load data
        // 2. Execute domain logic
        // 3. Persist changes
        // 4. Return result
    }
}
```

Key rules:
- **One public method**: `execute(...)` — never add secondary public methods
- **Field injection**: Use `@Inject` on package-private fields (not constructor injection)
- **`@Transactional`**: Applied on `execute()` method when persistence is involved
- **`@ApplicationScoped`**: All use cases are CDI application-scoped beans
- **No interfaces**: Use cases are concrete classes, not interface implementations

### Repository Pattern

**Domain interface** (`domain/repository/`):
```java
public interface BookingRepository {
    Booking findById(Long id);
    List<Booking> findBySessionId(Long sessionId);
    Booking save(Booking booking);
    void deleteById(Long id);
}
```

**Infrastructure implementation** (`infrastructure/repository/`):
```java
@ApplicationScoped
public class BookingRepositoryImpl implements BookingRepository {

    @Inject
    BookingPanacheRepository panacheRepository;

    @Inject
    BookingMapper mapper;

    @Override
    public Booking findById(Long id) {
        BookingEntity entity = panacheRepository.findById(id);
        if (entity == null) return null;
        return mapper.toDomain(entity);
    }

    @Override
    public Booking save(Booking booking) {
        BookingEntity entity = mapper.toEntity(booking);
        panacheRepository.persist(entity);
        return mapper.toDomain(entity);
    }
}
```

**Panache repository** (thin wrapper):
```java
@ApplicationScoped
public class BookingPanacheRepository implements PanacheRepository<BookingEntity> {
    // Custom query methods only — CRUD inherited from PanacheRepository
    public List<BookingEntity> findBySessionId(Long sessionId) {
        return find("sessionInstance.id", sessionId).list();
    }
}
```

**Update pattern** — for existing entities, DO NOT call `persist()`:
```java
@Override
public Booking update(Booking booking) {
    BookingEntity entity = panacheRepository.findById(booking.getId());
    // Manually copy fields to the managed entity
    entity.setStatus(booking.getStatus());
    entity.setCancelledAt(booking.getCancelledAt());
    // Hibernate dirty checking auto-flushes — no persist() call
    return mapper.toDomain(entity);
}
```

**Pessimistic locking** (when needed):
```java
public BookingEntity findByIdForUpdate(Long id) {
    return getEntityManager()
        .find(BookingEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
}
```

### REST Resource Pattern

```java
@Path("/api/v1/resources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class ResourceResource {

    @Inject
    CreateResourceUseCase createResourceUseCase;

    @Inject
    ResourceDtoMapper mapper;

    @POST
    public Response create(@Valid ResourceInput input) {
        try {
            Resource result = createResourceUseCase.execute(mapper.toDomain(input));
            return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("Created", mapper.toResponse(result)))
                .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.error(e.getMessage()))
                .build();
        }
    }
}
```

Key rules:
- **Always wrap responses** in `ApiResponse<T>` — use `ApiResponse.success(message, data)` or `ApiResponse.error(message)`
- **Try-catch per method** — map exceptions to HTTP status codes explicitly
- **`@Valid`** on request body parameters for Jakarta Bean Validation
- **`@Authenticated`** at class level for secured endpoints
- **`@PublicEndpoint`** on individual methods that should bypass auth

### MapStruct Mapper Pattern

**Entity↔Domain mapper** (`infrastructure/mapper/`):
```java
@Mapper(componentModel = "jakarta-cdi")
public interface SessionInstanceMapper {
    SessionInstance toDomain(SessionInstanceEntity entity);
    SessionInstanceEntity toEntity(SessionInstance domain);
}
```

**Domain↔DTO mapper** (`presentation/mapper/`):
```java
@Mapper(componentModel = "jakarta-cdi")
public interface ActivityDtoMapper {
    Activity toDomain(ActivityInput input);
    ActivityResponse toResponse(Activity domain);
    List<ActivityResponse> toResponseList(List<Activity> domains);
}
```

When MapStruct can't auto-map (complex nested types), use a **manual mapper**:
```java
@ApplicationScoped
public class BookingDtoMapper {
    public BookingResponse toResponse(Booking booking) {
        // Manual field-by-field mapping
    }
}
```

## Error Handling

**Exception hierarchy** (in `src/main/java/org/athlium/shared/exception/`):
- `DomainException` — base for business rule violations → typically 403 or 400
- `BadRequestException` — invalid input or precondition failures → 400
- `EntityNotFoundException` — entity not found → 404

**Auth-specific exceptions** (in `src/main/java/org/athlium/auth/`):
- `AuthenticationException` → 401
- `InvalidRefreshTokenException` → 401
- `UserAlreadyExistsException` → 409

**Mapping in controllers:**
```java
catch (EntityNotFoundException e) {
    return Response.status(Response.Status.NOT_FOUND)
        .entity(ApiResponse.error(e.getMessage())).build();
} catch (BadRequestException e) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(ApiResponse.error(e.getMessage())).build();
} catch (DomainException e) {
    return Response.status(Response.Status.FORBIDDEN)
        .entity(ApiResponse.error(e.getMessage())).build();
} catch (Exception e) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(ApiResponse.error("Unexpected error")).build();
}
```

**Global fallback** — `GlobalExceptionMapper` at `src/main/java/org/athlium/shared/exception/GlobalExceptionMapper.java`:
- Catches unhandled exceptions and returns 500 with `ApiResponse.error()`

**Validation** — Jakarta Bean Validation on DTOs:
```java
public class CreateUserRequestDto {
    @NotBlank
    private String name;
    @Email
    private String email;
}
```

## Logging

**Framework:** JBoss Logging (via `org.jboss.logging.Logger`)

**Pattern:**
```java
private static final Logger LOG = Logger.getLogger(ClassName.class);
LOG.info("Descriptive message");
LOG.error("Error message", exception);
```

**Usage:** Logging is sparse — primarily in auth filter, global exception mapper, and some use cases. Not every use case logs.

## Comments

**When to Comment:**
- Comments are minimal — code is expected to be self-documenting
- No JSDoc/JavaDoc convention enforced on public APIs
- Occasional inline comments for complex business logic

## Domain Model Construction

**Two patterns coexist:**

**Lombok pattern** (older — `users` module):
```java
@Builder
@Getter
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;
    private Role role;
}
```

**Plain POJO pattern** (newer — `gym`, `bookings`, `clients`, `payments` modules):
```java
public class SessionInstance {
    private Long id;
    private String name;
    private LocalDate date;
    // Explicit getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
```

**When adding new code: use plain POJO pattern** with explicit getters/setters (consistent with newer modules).

## JPA Entity Pattern

```java
@Entity
@Table(name = "table_name")
public class SomeEntity extends PanacheEntity {
    // id inherited from PanacheEntity (auto-generated Long)

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ParentEntity parent;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Getters and setters
}
```

Key rules:
- **Extend `PanacheEntity`** — provides auto `id` field
- **`@CreationTimestamp` / `@UpdateTimestamp`** for audit fields
- **Fetch strategy**: `FetchType.LAZY` for `@ManyToOne` relationships
- **Column naming**: `snake_case` in DB, `camelCase` in Java (Hibernate default mapping)

## Module Design

**Exports:** Each module exposes functionality through its REST resource layer. No barrel files (Java doesn't use them).

**Cross-module dependencies:**
- Modules can inject other modules' **use cases** or **repositories** directly via CDI
- Example: `CreateBookingUseCase` injects `SessionInstanceRepository` from the `gym` module
- Example: `BookingResource` injects both `bookings` and `gym` use cases
- The `shared` module provides cross-cutting concerns available to all modules

**When adding a new module:**
1. Create package `org.athlium.{module}` with sub-packages: `domain.model`, `domain.repository`, `application.usecase`, `infrastructure.entity`, `infrastructure.repository`, `infrastructure.mapper`, `presentation.controller`, `presentation.dto`, `presentation.mapper`
2. Follow the newer module conventions (`gym`, `bookings`) — use `presentation/` layer, `XxxInput`/`XxxResponse` DTOs, plain POJO domain models

---

*Convention analysis: 2026-03-04*
