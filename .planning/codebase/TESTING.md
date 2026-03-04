# Testing Patterns

**Analysis Date:** 2026-03-04

## Test Framework

**Runner:**
- JUnit 5 (via `quarkus-junit5`)
- Config: `pom.xml` — `quarkus-junit5` dependency, `maven-surefire-plugin` for unit tests, `maven-failsafe-plugin` for integration tests

**Assertion Library:**
- JUnit 5 assertions (`assertEquals`, `assertNotNull`, `assertThrows`, `assertTrue`)
- Hamcrest matchers for REST Assured E2E tests (`equalTo`, `hasSize`, `greaterThan`)

**Run Commands:**
```bash
./mvnw test                    # Run unit tests (surefire)
./mvnw verify                  # Run unit + integration tests (failsafe)
./mvnw test -pl backend        # Run tests for backend module only
./mvnw test -Dtest=ClassName   # Run specific test class
```

## Test File Organization

**Location:**
- Mirror source structure: `src/test/java/org/athlium/{module}/{layer}/...`
- E2E tests: `src/test/java/org/athlium/e2e/`

**Naming:**
- Unit tests: `XxxTest.java` or `XxxUnitTest.java` — e.g., `CreateBookingUseCaseTest.java`, `BookingResourceUnitTest.java`
- E2E tests: `XxxE2ETest.java` — e.g., `GymBookingsE2ETest.java`, `OrganizationResourceE2ETest.java`
- Validation tests: `XxxValidationTest.java` — e.g., `UserRequestValidationTest.java`

**Structure:**
```
src/test/java/org/athlium/
├── bookings/
│   ├── application/usecase/
│   │   ├── CreateBookingUseCaseTest.java
│   │   ├── CancelBookingUseCaseTest.java
│   │   └── GetBookingsUseCaseTest.java
│   └── presentation/controller/
│       ├── BookingResourceUnitTest.java
│       └── SessionBookingResourceUnitTest.java
├── gym/
│   ├── application/usecase/
│   │   ├── GetSessionsUseCaseTest.java
│   │   ├── GetSessionByIdUseCaseTest.java
│   │   ├── CreateActivityScheduleUseCaseTest.java
│   │   ├── GenerateNextWeekSessionsUseCaseTest.java
│   │   ├── SessionConfigurationValidatorTest.java
│   │   └── ResolveSessionConfigurationUseCaseTest.java
│   ├── presentation/controller/
│   │   ├── SessionResourceUnitTest.java
│   │   └── SessionConfigurationResourceUnitTest.java
│   ├── presentation/mapper/
│   │   └── ActivityScheduleDtoMapperTest.java
│   └── infrastructure/repository/
│       └── RepositoryUpdatePersistencePatternTest.java
├── users/
│   ├── application/usecase/
│   │   ├── CreateUserUseCaseUnitTest.java
│   │   ├── CreateUserUseCaseTest.java
│   │   └── UpdateUserRolesUseCaseTest.java
│   └── infrastructure/
│       ├── repository/UserRepositoryImplTest.java
│       └── dto/UserRequestValidationTest.java
├── clients/
│   ├── application/usecase/
│   │   ├── CreateClientPackageUseCaseTest.java
│   │   └── UpdateActiveClientPackageUseCaseTest.java
│   └── infrastructure/repository/
│       └── ClientPackageRepositoryImplTest.java
├── payments/
│   └── application/usecase/
│       └── CreatePaymentUseCaseTest.java
└── e2e/
    ├── OrganizationResourceE2ETest.java
    └── GymBookingsE2ETest.java
```

## Test Structure

**Suite Organization — Use Case Unit Tests:**
```java
class CreateBookingUseCaseTest {

    private CreateBookingUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateBookingUseCase();
        useCase.bookingRepository = new InMemoryBookingRepository();
        useCase.sessionRepository = new InMemorySessionRepository();
        // Direct field assignment on package-private @Inject fields
    }

    @Test
    void shouldCreateBookingSuccessfully() {
        // Given — set up test data in in-memory repos
        // When — call useCase.execute(...)
        // Then — assert results with JUnit assertions
    }

    @Test
    void shouldThrowWhenSessionIsFull() {
        // Given — fill session to capacity
        // When/Then — assertThrows(BadRequestException.class, () -> ...)
    }

    // Inner static classes for test doubles
    static class InMemoryBookingRepository implements BookingRepository {
        private final List<Booking> bookings = new ArrayList<>();
        private long nextId = 1;

        @Override
        public Booking save(Booking booking) {
            booking.setId(nextId++);
            bookings.add(booking);
            return booking;
        }

        @Override
        public Booking findById(Long id) {
            return bookings.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst().orElse(null);
        }
        // ... implement all interface methods
    }
}
```

**Suite Organization — Controller/Resource Unit Tests:**
```java
class BookingResourceUnitTest {

    private BookingResource resource;

    @BeforeEach
    void setUp() {
        resource = new BookingResource();
        resource.createBookingUseCase = new StubCreateBookingUseCase();
        resource.bookingDtoMapper = new FakeBookingDtoMapper();
        // Direct field assignment
    }

    @Test
    void shouldReturn201WhenBookingCreated() {
        Response response = resource.create(validInput);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    void shouldReturn400WhenBadRequest() {
        // Use a stub that throws BadRequestException
        resource.createBookingUseCase = new StubCreateBookingUseCase() {
            @Override
            public Booking execute(Long sessionId, Long userId) {
                throw new BadRequestException("Session full");
            }
        };
        Response response = resource.create(input);
        assertEquals(400, response.getStatus());
    }

    // Stubs — extend concrete classes, override execute()
    static class StubCreateBookingUseCase extends CreateBookingUseCase {
        @Override
        public Booking execute(Long sessionId, Long userId) {
            return someDefaultBooking();
        }
    }

    // Fakes — implement mapper interface manually
    static class FakeBookingDtoMapper extends BookingDtoMapper {
        @Override
        public BookingResponse toResponse(Booking booking) {
            // Return simple test DTO
        }
    }
}
```

**Patterns:**
- **Setup**: `@BeforeEach` creates fresh use case / resource + wires test doubles via field assignment
- **Teardown**: Not needed — in-memory state is reset per test via `@BeforeEach`
- **Assertion**: JUnit 5 `assertEquals`, `assertNotNull`, `assertThrows`, `assertTrue`
- **Test method naming**: `should[ExpectedBehavior]` or `should[ExpectedBehavior]When[Condition]`

## Mocking

**Framework:** NONE — No Mockito, no mocking framework

**Pattern — Hand-Written In-Memory Repositories:**
```java
static class InMemorySessionRepository implements SessionInstanceRepository {
    private final List<SessionInstance> sessions = new ArrayList<>();
    private long nextId = 1;

    @Override
    public SessionInstance save(SessionInstance session) {
        session.setId(nextId++);
        sessions.add(session);
        return session;
    }

    @Override
    public SessionInstance findById(Long id) {
        return sessions.stream()
            .filter(s -> s.getId().equals(id))
            .findFirst().orElse(null);
    }

    @Override
    public List<SessionInstance> findAll() {
        return new ArrayList<>(sessions);
    }

    // Helper methods for test setup
    public void addSession(SessionInstance session) {
        if (session.getId() == null) session.setId(nextId++);
        sessions.add(session);
    }
}
```

**Pattern — Hand-Written Stubs for Use Cases:**
```java
static class StubGetSessionsUseCase extends GetSessionsUseCase {
    private List<SessionInstance> result = List.of();

    @Override
    public List<SessionInstance> execute() {
        return result;
    }

    void setResult(List<SessionInstance> result) {
        this.result = result;
    }
}
```

**Pattern — Hand-Written Fake Mappers:**
```java
static class FakeSessionDtoMapper extends SessionDtoMapper {
    @Override
    public SessionResponse toResponse(SessionInstance session) {
        SessionResponse dto = new SessionResponse();
        dto.setId(session.getId());
        dto.setName(session.getName());
        return dto;
    }
}
```

**Pattern — Reflection for Lombok @Getter-only Objects:**
```java
// When domain objects have @Getter but no setters, use reflection:
private User createTestUser(Long id, String name, Role role) {
    User user = User.builder().name(name).role(role).build();
    // Set id via reflection since no setter exists
    Field idField = User.class.getDeclaredField("id");
    idField.setAccessible(true);
    idField.set(user, id);
    return user;
}
```

**What to Mock (as in-memory doubles):**
- Domain repository interfaces — always replace with in-memory implementations
- Use cases — when testing controllers, stub the use case's `execute()` method
- Mappers — when testing controllers, provide fake mappers returning simple DTOs

**What NOT to Mock:**
- Domain models — use real domain objects
- Enums — use real enum values
- Value objects — use real values
- The class under test — never mock/stub it

## Fixtures and Factories

**Test Data — Builder Methods:**
```java
// Typically defined as private helper methods in each test class
private SessionInstance createSession(Long id, int capacity) {
    SessionInstance session = new SessionInstance();
    session.setId(id);
    session.setName("Test Session");
    session.setMaxCapacity(capacity);
    session.setCurrentBookings(0);
    session.setStatus(SessionStatus.SCHEDULED);
    session.setDate(LocalDate.now().plusDays(1));
    return session;
}

private Booking createBooking(Long id, Long sessionId, Long userId) {
    Booking booking = new Booking();
    booking.setId(id);
    booking.setSessionInstanceId(sessionId);
    booking.setUserId(userId);
    booking.setStatus(BookingStatus.CONFIRMED);
    return booking;
}
```

**Location:**
- No shared fixture files or factory classes
- Each test class defines its own helper methods and inner-class test doubles
- Test doubles are **inner static classes** within the test file

**No shared test utilities library** — each test class is self-contained.

## Coverage

**Requirements:** None enforced — no JaCoCo or coverage tool configured

**Coverage status:** No coverage reports generated as part of the build

## Test Types

**Unit Tests (majority — 24 of 26 test files):**
- **Use case tests**: Test business logic in isolation with in-memory repositories
  - Files: `src/test/java/org/athlium/{module}/application/usecase/*Test.java`
  - Approach: Instantiate use case directly, wire in-memory test doubles via field assignment
  - No CDI container, no database, no Quarkus runtime

- **Controller/Resource unit tests**: Test HTTP response mapping (status codes, response wrapping)
  - Files: `src/test/java/org/athlium/{module}/presentation/controller/*UnitTest.java`
  - Approach: Instantiate resource directly, stub use cases, invoke methods, assert `Response` objects

- **Mapper tests**: Verify MapStruct or manual mapper correctness
  - File: `src/test/java/org/athlium/gym/presentation/mapper/ActivityScheduleDtoMapperTest.java`

- **DTO validation tests**: Verify Jakarta Bean Validation annotations work
  - File: `src/test/java/org/athlium/users/infrastructure/dto/UserRequestValidationTest.java`
  - Uses `Validation.buildDefaultValidatorFactory()` to get a `Validator` instance

- **Repository persistence pattern tests**: Verify Hibernate dirty checking behavior
  - File: `src/test/java/org/athlium/gym/infrastructure/repository/RepositoryUpdatePersistencePatternTest.java`
  - Uses `@QuarkusTest` with real database

**E2E/Integration Tests (2 of 26 test files):**
- Files: `src/test/java/org/athlium/e2e/OrganizationResourceE2ETest.java`, `src/test/java/org/athlium/e2e/GymBookingsE2ETest.java`
- Framework: `@QuarkusTest` + REST Assured
- Approach: Full Quarkus container running, real database, HTTP requests via REST Assured
- Auth: Firebase mock mode enabled (`firebase.mock.enabled=true`)
- Data cleanup: `@BeforeEach` with `@Transactional` methods that delete all entities

**E2E Test Pattern:**
```java
@QuarkusTest
class GymBookingsE2ETest {

    @Inject
    BookingPanacheRepository bookingRepository;

    @Inject
    SessionInstancePanacheRepository sessionRepository;

    @BeforeEach
    @Transactional
    void cleanup() {
        bookingRepository.deleteAll();
        sessionRepository.deleteAll();
    }

    @Test
    void shouldCreateBookingViaApi() {
        // Setup — create test data via injected repositories
        // Act — REST Assured HTTP call
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
        .when()
            .post("/api/v1/bookings")
        .then()
            .statusCode(201)
            .body("success", equalTo(true))
            .body("data.status", equalTo("CONFIRMED"));
    }
}
```

**Concurrency Tests (within E2E):**
```java
@Test
void shouldHandleConcurrentBookings() {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<Future<io.restassured.response.Response>> futures = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
        futures.add(executor.submit(() ->
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer test-token-" + userId)
                .body(bookingInput)
            .when()
                .post("/api/v1/sessions/" + sessionId + "/bookings")
        ));
    }
    // Assert correct number of successes vs failures
}
```

## Test Configuration

**Test properties file:** `src/test/resources/application.properties`
- Minimal configuration — MongoDB test database name and Firebase mock mode
- Database: Uses Quarkus Dev Services (auto-provisions test database containers)

## Common Patterns

**Testing Business Rule Violations:**
```java
@Test
void shouldThrowWhenSessionIsFull() {
    // Given — session at max capacity
    SessionInstance session = createSession(1L, 10);
    session.setCurrentBookings(10);
    inMemorySessionRepo.addSession(session);

    // When/Then
    BadRequestException exception = assertThrows(
        BadRequestException.class,
        () -> useCase.execute(1L, 100L)
    );
    assertEquals("Session is full", exception.getMessage());
}
```

**Testing Entity Not Found:**
```java
@Test
void shouldThrowWhenSessionNotFound() {
    assertThrows(
        EntityNotFoundException.class,
        () -> useCase.execute(999L, 1L)
    );
}
```

**Testing Controller Error Mapping:**
```java
@Test
void shouldReturn404WhenEntityNotFound() {
    resource.getSessionUseCase = new StubGetSessionUseCase() {
        @Override
        public SessionInstance execute(Long id) {
            throw new EntityNotFoundException("Session not found");
        }
    };

    Response response = resource.getById(999L);
    assertEquals(404, response.getStatus());
}
```

**Testing Successful Operations:**
```java
@Test
void shouldReturnCreatedSession() {
    // Given
    SessionInstance input = createSession(null, 20);

    // When
    SessionInstance result = useCase.execute(input);

    // Then
    assertNotNull(result.getId());
    assertEquals("Test Session", result.getName());
    assertEquals(20, result.getMaxCapacity());
}
```

## When Writing New Tests

**For a new use case:**
1. Create `{UseCaseName}Test.java` in `src/test/java/org/athlium/{module}/application/usecase/`
2. Define inner static `InMemoryXxxRepository` classes implementing domain repository interfaces
3. Wire dependencies via direct field assignment in `@BeforeEach`
4. Test happy path + all exception paths
5. Use `should...` naming for test methods

**For a new controller/resource:**
1. Create `{ResourceName}UnitTest.java` in `src/test/java/org/athlium/{module}/presentation/controller/`
2. Define inner static `Stub{UseCaseName}` classes extending use case classes
3. Define inner static `Fake{MapperName}` classes
4. Test HTTP status codes, response wrapping, and error mapping
5. Do NOT test business logic — that belongs in use case tests

**For E2E tests:**
1. Create `{Feature}E2ETest.java` in `src/test/java/org/athlium/e2e/`
2. Annotate with `@QuarkusTest`
3. Inject Panache repositories for data setup/cleanup
4. Use `@BeforeEach @Transactional` for cleanup
5. Use REST Assured for HTTP calls
6. Assert response status, body structure, and `ApiResponse` fields

---

*Testing analysis: 2026-03-04*
