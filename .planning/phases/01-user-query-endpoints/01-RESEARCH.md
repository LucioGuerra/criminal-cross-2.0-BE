# Phase 1: User Query Endpoints - Research

**Researched:** 2026-03-04
**Domain:** Quarkus REST endpoints with cross-module SQL queries (hexagonal architecture)
**Confidence:** HIGH

## Summary

This phase adds four user query endpoints to an existing Quarkus 3.29 / Java 21 backend that already follows hexagonal architecture with vertical slicing. The core technical challenge is a cross-module SQL JOIN across 4 tables spanning 3 bounded contexts (users → clients → gym) to resolve user-HQ membership through the `users → client_packages → client_package_credits → activity → headquarters` chain, with package status computation (ACTIVE/EXPIRING/INACTIVE/NO_PACKAGE) done in SQL.

The codebase has two established patterns: the `users` module uses `infrastructure/controller` and `infrastructure/dto`, while newer modules (gym, bookings) use `presentation/controller` and `presentation/dto`+`presentation/mapper`. The project already has `PageResponse<T>`, `ApiResponse<T>`, `@Authenticated(roles={...})`, MapStruct mappers, Panache repositories, and in-memory test doubles with reflection-based field access. No new libraries are needed — everything required exists in the current stack.

**Primary recommendation:** Create new use cases and a new port (repository interface) in the `users` module for the enriched user queries, with the repository implementation in `users.infrastructure` executing native/JPQL cross-module JOINs against entity tables from `clients` and `gym`. Follow the newer `presentation/` layer convention (gym/bookings pattern). Compute package status entirely in SQL using CASE WHEN.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- Core fields per user: id, name, lastName, email, roles, active, packageStatus
- Package status represented as: status enum (ACTIVE/EXPIRING/INACTIVE/NO_PACKAGE) + periodEnd date + daysRemaining
- GetUsersByOrg includes nested HQ object per user: `{ id, name }`
- A user with packages in multiple HQs within the same org returns an array of HQ objects, each with its own package status
- GetUsersByHq returns users with a single package status (best status across packages at that HQ)
- **ACTIVE**: package has `active=true` AND `periodEnd > now`
- **EXPIRING**: package has `active=true` AND `periodEnd` is within 3 days from now (fixed threshold, not configurable)
- **INACTIVE**: package has `periodEnd < now` (natural expiry) OR `active=false` (revoked/cancelled — even if periodEnd is in the future)
- **NO_PACKAGE**: user has no packages with credits for activities at the given HQ
- `active=false` means revoked/cancelled (separate from natural expiry via periodEnd)
- Multiple packages at same HQ: best status wins (ACTIVE > EXPIRING > INACTIVE > NO_PACKAGE)
- Credits used up (tokens=0) does NOT affect HQ membership — user still belongs if package exists
- When best status is EXPIRING or ACTIVE, use the periodEnd and daysRemaining from that winning package
- All three list endpoints (byHq, byOrg, allUsers) support pagination with PageResponse
- Filter by package status: optional `status` query param (ACTIVE, EXPIRING, INACTIVE, NO_PACKAGE)
- Search by name/email: optional `search` query param (partial match on name, lastName, or email)
- Configurable sort param: `sort` query param, default by name ascending
- GetAllUsers has the same filter set as GetUsersByHq/GetUsersByOrg (status, search, sort, pagination)
- Data resolution: Single SQL query with JOINs across the 4-table chain
- Package status computation: In SQL using CASE WHEN for efficiency
- URL structure: `GET /api/users?headquartersId=X`, `GET /api/users?organizationId=X`
- GetUserById: `GET /api/users/{id}` for internal ID
- Existing Firebase UID endpoint at `/api/users/firebase/{uid}` stays as-is

### Claude's Discretion
- Which module owns the new use cases and where cross-module ports go
- DTO/mapper structure for the enriched user response
- Exact SQL query optimization approach
- Error handling for invalid HQ/Org IDs (404 vs empty list)

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| USER-01 | Admin can get all users belonging to a specific HQ (via client packages → credits → activities → HQ) with package status | Cross-module SQL JOIN pattern, package status CASE WHEN logic, `@Authenticated(roles={"SUPERADMIN","ORG_ADMIN"})` annotation |
| USER-02 | Admin can get all users belonging to a specific org with package status + HQ name | Same JOIN extended to headquarters table for HQ name, array grouping of HQ memberships per user |
| USER-03 | Admin can get all users with pagination (page/size) returning PageResponse | Existing `PageResponse<T>` + Panache `PanacheQuery.page()` pattern from `SessionInstanceRepositoryImpl` |
| USER-04 | Admin can get a user by internal database ID | Simple `findById` on `UserRepository`, enriched with package data via separate query or single query |
| USER-05 | Unit tests for GetUsersByHq use case and controller | In-memory test double pattern from `CreateUserUseCaseUnitTest`, stub pattern from `SessionResourceUnitTest` |
| USER-06 | Unit tests for GetUsersByOrg use case and controller | Same test patterns |
| USER-07 | Unit tests for GetAllUsers use case and controller | Same test patterns |
| USER-08 | Unit tests for GetUserById use case and controller | Same test patterns |
| AUTH-01 | All new user endpoints require SUPERADMIN or ORG_ADMIN role | `@Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN"})` annotation — already implemented in `FirebaseAuthFilter` |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Quarkus | 3.29.0 | Application framework | Already in use, BOM-managed |
| quarkus-hibernate-orm-panache | 3.29.0 (BOM) | ORM + repository pattern | Already in use for all entity access |
| quarkus-rest (RESTEasy Reactive) | 3.29.0 (BOM) | JAX-RS endpoints | Already in use for all controllers |
| quarkus-rest-jackson | 3.29.0 (BOM) | JSON serialization | Already in use |
| MapStruct | 1.5.5 (dep) / 1.6.0 (processor) | DTO mapping | Already in use for all mappers |
| Lombok | 1.18.38 | Boilerplate reduction | Already in use across domain models and DTOs |
| PostgreSQL | via quarkus-jdbc-postgresql | Database | Already in use |
| Flyway | via quarkus-flyway | Schema migrations | Already in use |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| quarkus-junit5 | 3.29.0 (BOM) | Test framework | All unit tests |
| JUnit 5 Assertions | (transitive) | Assertions | All tests — no Mockito, use in-memory doubles |

### Alternatives Considered
None — the stack is fully established. No new libraries needed for this phase.

**Installation:**
No new dependencies required. Everything is already in `pom.xml`.

## Architecture Patterns

### Recommended Project Structure

The `users` module currently uses `infrastructure/` for controller+dto. Newer modules (gym, bookings) separate into `presentation/` layer. The new endpoints should follow the newer convention for consistency with the majority of the codebase:

```
src/main/java/org/athlium/users/
├── domain/
│   ├── model/
│   │   ├── User.java                          # existing
│   │   ├── Role.java                          # existing
│   │   ├── PackageStatus.java                 # NEW: enum ACTIVE/EXPIRING/INACTIVE/NO_PACKAGE
│   │   └── UserWithPackageStatus.java         # NEW: enriched domain model
│   └── repository/
│       ├── UserRepository.java                # existing
│       └── UserQueryRepository.java           # NEW: port for cross-module queries
├── application/
│   └── usecase/
│       ├── GetUsersByHqUseCase.java           # NEW
│       ├── GetUsersByOrgUseCase.java          # NEW
│       ├── GetAllUsersUseCase.java            # NEW
│       ├── GetUserByIdUseCase.java            # NEW
│       └── ... (existing use cases)
├── infrastructure/
│   ├── controller/UserResource.java           # existing (keep as-is)
│   ├── repository/
│   │   ├── UserRepositoryImpl.java            # existing
│   │   ├── UserPanacheRepository.java         # existing
│   │   └── UserQueryRepositoryImpl.java       # NEW: cross-module SQL queries
│   └── ... (existing mappers, entities, dtos)
└── presentation/
    ├── controller/
    │   └── UserQueryResource.java             # NEW: /api/users query endpoints
    ├── dto/
    │   ├── UserWithStatusResponse.java        # NEW
    │   ├── UserHqMembership.java              # NEW: { id, name, packageStatus }
    │   └── UserPageResponse.java              # NEW
    └── mapper/
        └── UserQueryDtoMapper.java            # NEW
```

### Pattern 1: Cross-Module SQL Query via Native/JPQL Query
**What:** The repository implementation executes a single SQL query that JOINs across entity tables from multiple modules (users, client_packages, client_package_credits, activity, headquarters). This avoids N+1 queries and keeps the application layer clean.
**When to use:** When the domain port needs data that spans multiple bounded contexts but the query is read-only.
**Confidence:** HIGH — verified from codebase analysis

```java
// In UserQueryRepositoryImpl — uses EntityManager for native SQL
@ApplicationScoped
public class UserQueryRepositoryImpl implements UserQueryRepository {

    @Inject
    EntityManager em;

    @Override
    public PageResponse<UserWithPackageStatus> findUsersByHeadquarters(
            Long headquartersId, String status, String search, int page, int size, String sort) {
        
        // Native SQL with CASE WHEN for package status computation
        String sql = """
            SELECT u.id, u.name, u.last_name, u.email, u.active,
                   CASE
                       WHEN cp.active = true AND cp.period_end > CURRENT_DATE + INTERVAL '3 days' THEN 'ACTIVE'
                       WHEN cp.active = true AND cp.period_end > CURRENT_DATE THEN 'EXPIRING'
                       WHEN cp.active = false OR cp.period_end <= CURRENT_DATE THEN 'INACTIVE'
                   END AS package_status,
                   cp.period_end,
                   (cp.period_end - CURRENT_DATE) AS days_remaining
            FROM users u
            JOIN client_packages cp ON cp.user_id = u.id
            JOIN client_package_credits cpc ON cpc.package_id = cp.id
            JOIN activity a ON a.id = cpc.activity_id
            WHERE a.hq_id = :hqId
            ...
            """;
        // Use best-status-wins aggregation with window functions or GROUP BY
    }
}
```

### Pattern 2: Existing Use Case Pattern
**What:** Use cases are `@ApplicationScoped` CDI beans with `@Inject` field injection for repository ports. They validate input and delegate to repositories.
**When to use:** All new use cases.
**Confidence:** HIGH — verified from `GetSessionsUseCase`, `GetUserByUidUseCase`

```java
@ApplicationScoped
public class GetUsersByHqUseCase {

    @Inject
    UserQueryRepository userQueryRepository;

    public PageResponse<UserWithPackageStatus> execute(
            Long headquartersId, String status, String search,
            int page, int size, String sort) {
        // Validate input
        if (page < 1) throw new BadRequestException("Page must be >= 1");
        if (size < 1 || size > 100) throw new BadRequestException("Size must be between 1 and 100");
        // Delegate to repository
        return userQueryRepository.findUsersByHeadquarters(headquartersId, status, search, page - 1, size, sort);
    }
}
```

### Pattern 3: Controller Pattern with Auth
**What:** Controllers use `@Authenticated(roles={"SUPERADMIN","ORG_ADMIN"})` for authorization, return `Response` wrapping `ApiResponse<T>`, catch specific exceptions.
**When to use:** All new endpoints.
**Confidence:** HIGH — verified from `UserResource`, `SessionResource`, `BookingResource`

```java
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN"})
public class UserQueryResource {

    @GET
    public Response getUsers(
            @QueryParam("headquartersId") Long headquartersId,
            @QueryParam("organizationId") Long organizationId,
            @QueryParam("status") String status,
            @QueryParam("search") String search,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("20") @QueryParam("size") int size,
            @DefaultValue("name:asc") @QueryParam("sort") String sort) {
        // Route to appropriate use case based on which param is present
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id) {
        // ...
    }
}
```

### Pattern 4: Test Doubles (No Mockito)
**What:** Tests use static inner classes that extend use cases or implement repository interfaces, overriding methods to return configured responses. Field injection is done via direct field assignment.
**When to use:** All unit tests.
**Confidence:** HIGH — verified from `SessionResourceUnitTest`, `CreateUserUseCaseUnitTest`

```java
// Controller test pattern (from SessionResourceUnitTest)
class UserQueryResourceUnitTest {
    private UserQueryResource resource;
    private StubGetUsersByHqUseCase getUsersByHqUseCase;

    @BeforeEach
    void setUp() {
        resource = new UserQueryResource();
        getUsersByHqUseCase = new StubGetUsersByHqUseCase();
        resource.getUsersByHqUseCase = getUsersByHqUseCase;
        resource.userQueryDtoMapper = new StubUserQueryDtoMapper();
    }

    private static class StubGetUsersByHqUseCase extends GetUsersByHqUseCase {
        PageResponse<UserWithPackageStatus> response;
        @Override
        public PageResponse<UserWithPackageStatus> execute(...) {
            return response;
        }
    }
}

// Use case test pattern (from CreateUserUseCaseUnitTest)
class GetUsersByHqUseCaseTest {
    private GetUsersByHqUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUsersByHqUseCase();
        useCase.userQueryRepository = new InMemoryUserQueryRepository();
    }

    private static class InMemoryUserQueryRepository implements UserQueryRepository {
        // In-memory implementation with configured data
    }
}
```

### Anti-Patterns to Avoid
- **N+1 Queries:** Do NOT load all users then fetch packages for each user individually. The SQL JOIN must resolve everything in one query.
- **Application-level status computation:** Do NOT load raw package data and compute ACTIVE/EXPIRING/INACTIVE in Java. The user decision mandates SQL CASE WHEN.
- **Cross-module domain model dependencies:** The `UserQueryRepository` port should return `UserWithPackageStatus` (a users module domain model), NOT depend on `ClientPackage` domain model from the clients module. The SQL query in infrastructure maps directly to the users domain.
- **Modifying existing `UserResource`:** Keep the existing controller untouched. Create a separate `UserQueryResource` in the `presentation/` layer. Both will be under `/api/users` but with different method signatures. JAX-RS handles this via a single `@Path` annotation with different sub-paths.
- **Using Mockito:** Project uses in-memory test doubles exclusively. Never add Mockito dependency.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Pagination | Custom offset/limit logic | `PanacheQuery.page(Page.of(page, size))` + `query.count()` + `PageResponse<T>` | Already established pattern in `SessionInstanceRepositoryImpl` |
| DTO mapping | Manual mapping methods | MapStruct `@Mapper(componentModel = "jakarta-cdi")` | Already established across all modules |
| Auth/Role checking | Custom filter or interceptor | `@Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN"})` annotation | Already implemented in `FirebaseAuthFilter` |
| Response wrapping | Custom response structure | `ApiResponse.success(message, data)` / `ApiResponse.error(message)` | Already established across all controllers |
| Exception handling | Custom try-catch per exception | Catch `BadRequestException`/`EntityNotFoundException` in controller, global handler for rest | Already established pattern |

**Key insight:** This phase requires zero new infrastructure. Every cross-cutting concern (auth, pagination, response format, exception handling, DTO mapping) is already solved in the codebase. The only new work is the domain models, use cases, SQL queries, and tests.

## Common Pitfalls

### Pitfall 1: JAX-RS Path Conflict Between UserResource and UserQueryResource
**What goes wrong:** Two `@Path("/api/users")` resources cause deployment failure or ambiguous routing.
**Why it happens:** Both the existing `UserResource` and new `UserQueryResource` need to serve `/api/users`.
**How to avoid:** JAX-RS supports multiple resource classes on the same base path as long as the full paths are unambiguous. The existing `UserResource` has `POST /api/users`, `GET /api/users/firebase/{uid}`, `PUT /api/users/firebase/{uid}/roles`, `POST /api/users/sync`. The new resource has `GET /api/users` (with query params) and `GET /api/users/{id}`. These are distinct. Verify Quarkus handles this by checking deployment. Alternatively, use a single class but that violates the "don't modify existing" principle.
**Warning signs:** Deployment error about ambiguous resource methods; 404 on new endpoints.

### Pitfall 2: "Best Status Wins" Aggregation in SQL
**What goes wrong:** SQL returns multiple rows per user (one per package), and the aggregation logic to pick the "best" status is incorrect or returns wrong periodEnd/daysRemaining.
**Why it happens:** Package status has a priority order (ACTIVE > EXPIRING > INACTIVE > NO_PACKAGE) and the winning row's periodEnd must be used, not an arbitrary one.
**How to avoid:** Use a CTE or subquery that ranks packages per user using a numeric priority mapping, then select the top-ranked package per user. Example:
```sql
WITH ranked_packages AS (
    SELECT u.id, u.name, ...,
           CASE WHEN cp.active = true AND cp.period_end > CURRENT_DATE + 3 THEN 1    -- ACTIVE
                WHEN cp.active = true AND cp.period_end > CURRENT_DATE THEN 2          -- EXPIRING
                ELSE 3                                                                   -- INACTIVE
           END AS status_rank,
           cp.period_end,
           ROW_NUMBER() OVER (PARTITION BY u.id ORDER BY <status_rank> ASC, cp.period_end DESC) AS rn
    FROM users u JOIN ... WHERE ...
)
SELECT * FROM ranked_packages WHERE rn = 1
```
**Warning signs:** Users appearing multiple times in results; wrong periodEnd shown.

### Pitfall 3: EXPIRING Boundary Condition with LocalDate
**What goes wrong:** Package status computation uses wrong date arithmetic. `periodEnd` is a `LocalDate` (no timezone), but "within 3 days" comparison must use the database's `CURRENT_DATE`.
**Why it happens:** `client_packages.period_end` is `DATE` type, `CURRENT_DATE` in PostgreSQL also returns `DATE`. Simple: `period_end > CURRENT_DATE` and `period_end <= CURRENT_DATE + 3`. No timezone issues with DATE-to-DATE comparison.
**How to avoid:** Use `CURRENT_DATE + INTERVAL '3 days'` or `CURRENT_DATE + 3` in PostgreSQL (both work for DATE arithmetic). The 3-day threshold is a business constant.
**Warning signs:** Off-by-one errors on boundary dates; packages showing as ACTIVE when they should be EXPIRING.

### Pitfall 4: GetUsersByOrg Response Shape — Array of HQ Memberships
**What goes wrong:** A user with packages at 2 HQs in the same org appears as 2 separate user entries instead of 1 user with 2 HQ memberships.
**Why it happens:** The SQL query naturally returns one row per user-HQ-package combination. Application-level grouping is needed.
**How to avoid:** Either: (a) Use SQL `json_agg`/`array_agg` to aggregate HQ memberships per user in the query, or (b) return flat rows from SQL and group in Java (Map<userId, List<HqMembership>>). Option (b) is simpler and more testable. The SQL handles pagination by distinct user count, then a second query (or same query without pagination) gets the HQ details.
**Warning signs:** Pagination counts being wrong (counting rows instead of distinct users); missing HQ memberships.

### Pitfall 5: Pagination with Distinct Users
**What goes wrong:** When a user has packages at multiple HQs (for byOrg) or multiple packages at same HQ, the SQL JOIN produces multiple rows per user. `COUNT(*)` returns inflated totals.
**Why it happens:** JOINs multiply rows.
**How to avoid:** Use `COUNT(DISTINCT u.id)` for total element count. For the actual page, use `DISTINCT u.id` in the subquery or use `ROW_NUMBER()` to deduplicate before pagination.
**Warning signs:** Total count doesn't match actual user count; wrong number of items per page.

### Pitfall 6: NO_PACKAGE Users in GetAllUsers
**What goes wrong:** GetAllUsers with no status filter should return ALL users, including those with NO packages at all. A LEFT JOIN is needed, not INNER JOIN.
**Why it happens:** INNER JOIN on client_packages excludes users who never bought a package.
**How to avoid:** Use `LEFT JOIN` from users to the package chain. Users with no matching packages get `NULL` package status → mapped to `NO_PACKAGE`.
**Warning signs:** Users with no packages missing from results entirely.

## Code Examples

### Package Status SQL Logic (PostgreSQL)
```sql
-- Source: Derived from codebase analysis of ClientPackageEntity fields + CONTEXT.md decisions
-- package_status computation for a single package row
CASE
    WHEN cp.active = false THEN 'INACTIVE'                                    -- revoked/cancelled
    WHEN cp.period_end < CURRENT_DATE THEN 'INACTIVE'                         -- naturally expired
    WHEN cp.period_end <= CURRENT_DATE + 3 THEN 'EXPIRING'                    -- within 3 days
    ELSE 'ACTIVE'                                                              -- active and not expiring
END AS package_status
```

### Best-Status-Wins Aggregation (GetUsersByHq)
```sql
-- Source: Derived from CONTEXT.md decision on "best status wins" + entity analysis
WITH user_packages AS (
    SELECT DISTINCT u.id AS user_id, u.name, u.last_name, u.email, u.active,
           cp.period_end,
           CASE
               WHEN cp.active = false THEN 3                                   -- INACTIVE (revoked)
               WHEN cp.period_end < CURRENT_DATE THEN 3                        -- INACTIVE (expired)
               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 2                   -- EXPIRING
               ELSE 1                                                           -- ACTIVE
           END AS status_rank,
           CASE
               WHEN cp.active = false THEN 'INACTIVE'
               WHEN cp.period_end < CURRENT_DATE THEN 'INACTIVE'
               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 'EXPIRING'
               ELSE 'ACTIVE'
           END AS package_status
    FROM users u
    JOIN client_packages cp ON cp.user_id = u.id
    JOIN client_package_credits cpc ON cpc.package_id = cp.id
    JOIN activity a ON a.id = cpc.activity_id
    WHERE a.hq_id = :hqId
),
best_status AS (
    SELECT user_id, name, last_name, email, active,
           package_status, period_end,
           (period_end - CURRENT_DATE) AS days_remaining,
           ROW_NUMBER() OVER (
               PARTITION BY user_id
               ORDER BY status_rank ASC, period_end DESC
           ) AS rn
    FROM user_packages
)
SELECT user_id, name, last_name, email, active,
       package_status, period_end, days_remaining
FROM best_status
WHERE rn = 1
```

### Native Query with EntityManager in Panache Repository
```java
// Source: Quarkus Panache documentation — using EntityManager for native SQL
@ApplicationScoped
public class UserQueryRepositoryImpl implements UserQueryRepository {

    @Inject
    EntityManager em;

    @Override
    public PageResponse<UserWithPackageStatus> findUsersByHeadquarters(
            Long headquartersId, String status, String search,
            int page, int size, String sort) {

        // Count query (for pagination total)
        String countSql = "SELECT COUNT(DISTINCT u.id) FROM users u JOIN ... WHERE a.hq_id = :hqId";
        // Add status/search filters...

        Query countQuery = em.createNativeQuery(countSql);
        countQuery.setParameter("hqId", headquartersId);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        // Data query with pagination
        String dataSql = "WITH ... SELECT ... LIMIT :size OFFSET :offset";
        Query dataQuery = em.createNativeQuery(dataSql);
        dataQuery.setParameter("hqId", headquartersId);
        dataQuery.setParameter("size", size);
        dataQuery.setParameter("offset", page * size);

        List<Object[]> rows = dataQuery.getResultList();
        List<UserWithPackageStatus> users = rows.stream()
                .map(this::mapRow)
                .toList();

        return new PageResponse<>(users, page, size, total);
    }
}
```

### PageResponse Pattern (existing)
```java
// Source: src/main/java/org/athlium/shared/domain/PageResponse.java
// Already exists — reuse directly for all paginated responses
new PageResponse<>(content, page, size, totalElements);
// page is 0-indexed internally, controller converts from 1-indexed
```

### Controller Page Response DTO Pattern
```java
// Source: SessionPageResponse.java, BookingPageResponse.java — existing pattern
public class UserPageResponse {
    private List<UserWithStatusResponse> items;
    private int page;        // 1-indexed (controller adds +1)
    private int size;
    private long total;
}
```

### @Authenticated Usage
```java
// Source: src/main/java/org/athlium/users/infrastructure/controller/UserResource.java:44
// Class-level annotation secures all methods
@Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN"})
public class UserQueryResource { ... }

// FirebaseAuthFilter checks roles automatically, returns 401 (no auth) or 403 (no role)
```

### User Roles Collection Query (for enriched response)
```sql
-- user_roles is a separate table with (user_id, role) rows
-- Must be fetched separately or via a second query after main pagination
-- UserEntity uses @ElementCollection(fetch = EAGER) but native SQL won't trigger this
-- Options: (a) separate query for roles per user on the page, or (b) join user_roles with STRING_AGG
SELECT ur.user_id, STRING_AGG(ur.role, ',') AS roles
FROM user_roles ur
WHERE ur.user_id IN (:userIds)
GROUP BY ur.user_id
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `infrastructure/controller` + `infrastructure/dto` (users module) | `presentation/controller` + `presentation/dto` + `presentation/mapper` (gym, bookings) | Newer modules in this project | New code should follow `presentation/` convention |
| Single active package per user (V3 migration unique index) | Multiple active packages per user (V4 dropped unique index) | V4 migration | Must handle multiple packages per user in all queries |

**Deprecated/outdated:**
- The unique index `uq_client_packages_user_active_true` was dropped in V4. A user CAN have multiple active packages simultaneously. All aggregation logic must account for this.

## Open Questions

1. **Separate controller class vs extending existing UserResource?**
   - What we know: `UserResource` already has `@Path("/api/users")`. New endpoints also need `/api/users`. JAX-RS supports multiple classes on same path if methods are unambiguous.
   - What's unclear: Whether Quarkus/RESTEasy Reactive handles two classes with same `@Path` without issues.
   - Recommendation: Create a separate `UserQueryResource` class. If deployment fails due to path conflict, merge into existing `UserResource`. This is LOW risk — JAX-RS spec supports this.

2. **user_roles in native SQL results**
   - What we know: `UserEntity` uses `@ElementCollection(fetch=EAGER)` for roles, but native SQL bypasses JPA relationship loading.
   - What's unclear: Best approach — join with STRING_AGG, separate query, or use JPQL instead of native SQL.
   - Recommendation: Use a second batch query `SELECT user_id, role FROM user_roles WHERE user_id IN (...)` for the paginated user IDs. This is clean, avoids complicating the main query, and the overhead is minimal (one extra query per page, not per user).

3. **GetAllUsers without HQ/Org filter — what package status to show?**
   - What we know: CONTEXT.md says GetAllUsers has same filter set. But without an HQ context, package status is ambiguous (user might be ACTIVE at one HQ and INACTIVE at another).
   - What's unclear: Whether to show "best overall status" across all HQs, or no package status for the all-users endpoint.
   - Recommendation: Show best overall package status across all HQs/packages. This is consistent with the "best status wins" rule and provides the most useful information for admins browsing all users. If `status` filter is applied, it filters by this best-overall-status.

## Sources

### Primary (HIGH confidence)
- Codebase analysis: `UserEntity.java`, `ClientPackageEntity.java`, `ClientPackageCreditEntity.java`, `ActivityEntity.java`, `HeadquartersEntity.java`, `OrganizationEntity.java` — entity field names and relationships
- Codebase analysis: `SessionInstanceRepositoryImpl.java` — pagination with Panache pattern
- Codebase analysis: `SessionResourceUnitTest.java`, `CreateUserUseCaseUnitTest.java` — test double patterns
- Codebase analysis: `FirebaseAuthFilter.java`, `Authenticated.java` — auth/role enforcement
- Codebase analysis: Flyway migrations V1-V4 — database schema and constraints
- Codebase analysis: `PageResponse.java`, `ApiResponse.java` — shared patterns
- CONTEXT.md — locked user decisions on response shape, status logic, URL structure

### Secondary (MEDIUM confidence)
- PostgreSQL date arithmetic (`CURRENT_DATE + 3`, `period_end - CURRENT_DATE`) — standard PostgreSQL behavior for DATE types
- JAX-RS multiple classes on same `@Path` — spec allows this; Quarkus support needs verification at deployment time

### Tertiary (LOW confidence)
- None — all findings verified from codebase sources

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — fully established, no new dependencies needed
- Architecture: HIGH — patterns directly observed in existing codebase (gym, bookings, users modules)
- Pitfalls: HIGH — derived from concrete analysis of the data model (multiple packages, JOINs, pagination)
- SQL queries: MEDIUM — logic is sound but exact PostgreSQL syntax for window functions with native queries needs validation during implementation

**Research date:** 2026-03-04
**Valid until:** 2026-04-04 (stable — no external dependencies changing)
