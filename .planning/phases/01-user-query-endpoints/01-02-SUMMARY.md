---
phase: 01-user-query-endpoints
plan: 02
subsystem: api
tags: [quarkus, jax-rs, native-sql, jpa, pagination, dto-mapper]

# Dependency graph
requires:
  - phase: 01-user-query-endpoints
    plan: 01
    provides: "Domain models (UserWithPackageStatus, UserHqMembership, PackageStatus, Role), repository port (UserQueryRepository), and 4 query use cases"
provides:
  - "UserQueryRepositoryImpl with cross-module native SQL queries (users → packages → credits → activities → headquarters)"
  - "REST endpoints GET /api/users and GET /api/users/{id} with query param routing"
  - "Response DTOs (UserWithStatusResponse, UserHqMembershipResponse) and manual DTO mapper"
  - "Sort field allowlist and parameterized queries for SQL injection prevention"
affects: [01-user-query-endpoints]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Native SQL with EntityManager for cross-module JOINs (bypasses JPA entity boundaries)"
    - "CTE + ROW_NUMBER() window function for best-status-wins aggregation"
    - "Presentation layer convention: presentation/controller, presentation/dto, presentation/mapper"
    - "Manual DTO mapper as @ApplicationScoped CDI bean (not MapStruct) for enum→string conversion"
    - "Sort field allowlist Map for SQL injection prevention on ORDER BY"
    - "LEFT JOIN for NO_PACKAGE users (findAllUsers)"

key-files:
  created:
    - src/main/java/org/athlium/users/infrastructure/repository/UserQueryRepositoryImpl.java
    - src/main/java/org/athlium/users/presentation/controller/UserQueryResource.java
    - src/main/java/org/athlium/users/presentation/dto/UserWithStatusResponse.java
    - src/main/java/org/athlium/users/presentation/dto/UserHqMembershipResponse.java
    - src/main/java/org/athlium/users/presentation/mapper/UserQueryDtoMapper.java
  modified: []

key-decisions:
  - "Used presentation/ layer convention (not infrastructure/controller/) for REST controller, DTOs, and mapper"
  - "Manual DTO mapper as CDI bean instead of MapStruct — mapping involves enum→string, Set<Role>→Set<String>, and conditional HQ memberships"
  - "Sort field allowlist via Map.of() rather than regex validation — simpler, whitelists exact column mappings"
  - "Plain POJOs with explicit getters/setters for DTOs (newer convention, not Lombok)"

patterns-established:
  - "Presentation layer: controller/dto/mapper package structure under presentation/"
  - "Native SQL repository: @ApplicationScoped + EntityManager injection for cross-module queries"
  - "CTE best-status pattern: user_packages CTE → best_status CTE with ROW_NUMBER() → final SELECT WHERE rn = 1"
  - "Batch roles fetch: secondary query SELECT user_id, role FROM user_roles WHERE user_id IN (:ids)"
  - "Controller routing: single GET endpoint with query param dispatch (headquartersId → HQ, organizationId → org, neither → all)"

requirements-completed: [USER-01, USER-02, USER-03, USER-04, AUTH-01]

# Metrics
duration: 4min
completed: 2026-03-04
---

# Phase 01 Plan 02: Infrastructure & Presentation Layer Summary

**Cross-module native SQL repository with CTE best-status-wins aggregation, REST controller with query param routing, and manual DTO mapper for user query endpoints**

## Performance

- **Duration:** 4 min
- **Started:** 2026-03-04T18:07:23Z
- **Completed:** 2026-03-04T18:11:57Z
- **Tasks:** 2
- **Files created:** 5

## Accomplishments
- UserQueryRepositoryImpl with 5 repository methods using native SQL across users → client_packages → client_package_credits → activity → headquarters table chain
- Package status computed in SQL via CASE WHEN, best-status-wins via CTE + ROW_NUMBER() window function
- REST controller at /api/users with query param routing (headquartersId, organizationId, or all users) and /api/users/{id}
- Response DTOs and manual mapper converting domain enums to JSON-friendly strings
- SQL injection prevention via parameterized queries and sort field allowlist

## Task Commits

Each task was committed atomically:

1. **Task 1: Create UserQueryRepositoryImpl with cross-module SQL queries** - `84401c0` (feat)
2. **Task 2: Create REST controller, response DTOs, and DTO mapper** - `edd310e` (feat)

**Plan metadata:** `2757b87` (docs: complete plan)

## Files Created/Modified
- `src/main/java/org/athlium/users/infrastructure/repository/UserQueryRepositoryImpl.java` - Cross-module SQL query implementation with CTE best-status pattern, 5 repository methods
- `src/main/java/org/athlium/users/presentation/controller/UserQueryResource.java` - REST controller with GET /api/users (query param routing) and GET /api/users/{id}
- `src/main/java/org/athlium/users/presentation/dto/UserWithStatusResponse.java` - Response DTO with all user fields + packageStatus + hqMemberships
- `src/main/java/org/athlium/users/presentation/dto/UserHqMembershipResponse.java` - HQ membership response DTO
- `src/main/java/org/athlium/users/presentation/mapper/UserQueryDtoMapper.java` - Manual mapper with enum→string and Role→String conversion

## Decisions Made
- Used `presentation/` layer convention (not `infrastructure/controller/`) for REST controller, DTOs, and mapper — cleaner separation of concerns
- Manual DTO mapper as CDI bean instead of MapStruct — mapping involves enum→string conversion and conditional HQ memberships that would require custom MapStruct qualifiers anyway
- Sort field allowlist via `Map.of("name", "name", "lastName", "last_name", "email", "email")` — simpler than regex, exact column mapping
- Plain POJOs with explicit getters/setters for DTOs (newer project convention, not Lombok)
- Used `mvn` instead of `./mvnw` — wrapper config missing from project

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- LSP shows errors in pre-existing files (DecodedToken, ActivityRepositoryImpl, UpdateUserRolesUseCase, HeadquartersMapper) due to Lombok annotation processing. These are cosmetic LSP issues — Maven compilation succeeds fine. Not caused by our changes.
- Plan references `./mvnw` but wrapper properties file doesn't exist; used system `mvn` instead.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- All 5 infrastructure/presentation layer files created and compiling
- Plan 03 (integration testing) can proceed — all endpoints and SQL queries are wired
- Full vertical slice complete: domain → application → infrastructure → presentation

## Self-Check: PASSED

All 5 created files verified on disk. Both task commits (84401c0, edd310e) verified in git log.

---
*Phase: 01-user-query-endpoints*
*Completed: 2026-03-04*
