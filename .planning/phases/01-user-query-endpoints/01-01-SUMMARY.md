---
phase: 01-user-query-endpoints
plan: 01
subsystem: api
tags: [java, hexagonal-architecture, domain-models, use-cases, pagination, package-status]

# Dependency graph
requires: []
provides:
  - PackageStatus enum with ACTIVE/EXPIRING/INACTIVE/NO_PACKAGE and priority ordering
  - UserWithPackageStatus enriched domain model with optional HQ memberships
  - UserHqMembership domain model for org-level HQ grouping
  - UserQueryRepository port interface with 5 query methods
  - Four query use cases with input validation (GetUsersByHq, GetUsersByOrg, GetAllUsers, GetUserById)
affects: [01-02, 01-03]

# Tech tracking
tech-stack:
  added: []
  patterns: [plain-pojo-domain-models, port-interface-for-cross-module-queries, batch-enrichment-pattern]

key-files:
  created:
    - src/main/java/org/athlium/users/domain/model/PackageStatus.java
    - src/main/java/org/athlium/users/domain/model/UserWithPackageStatus.java
    - src/main/java/org/athlium/users/domain/model/UserHqMembership.java
    - src/main/java/org/athlium/users/domain/repository/UserQueryRepository.java
    - src/main/java/org/athlium/users/application/usecase/GetUsersByHqUseCase.java
    - src/main/java/org/athlium/users/application/usecase/GetUsersByOrgUseCase.java
    - src/main/java/org/athlium/users/application/usecase/GetAllUsersUseCase.java
    - src/main/java/org/athlium/users/application/usecase/GetUserByIdUseCase.java
  modified: []

key-decisions:
  - "Added userId field to UserHqMembership for batch grouping in GetUsersByOrgUseCase"
  - "hqMemberships field on UserWithPackageStatus is nullable — only populated for org-level queries"
  - "GetUserByIdUseCase falls back to existing UserRepository when user has no packages"

patterns-established:
  - "Plain POJO domain models with explicit getters/setters (no Lombok for new code)"
  - "Separate UserQueryRepository port from existing UserRepository for cross-module read queries"
  - "Page 1-indexed at API boundary, converted to 0-indexed at use case → repository boundary"
  - "Batch enrichment pattern: fetch paginated users, then batch-fetch related data by IDs"

requirements-completed: [USER-01, USER-02, USER-03, USER-04, AUTH-01]

# Metrics
duration: 3min
completed: 2026-03-04
---

# Phase 01 Plan 01: Domain + Application Layer Summary

**PackageStatus enum, enriched user domain models, UserQueryRepository port, and four query use cases with input validation and pagination support**

## Performance

- **Duration:** 3 min
- **Started:** 2026-03-04T18:00:44Z
- **Completed:** 2026-03-04T18:03:49Z
- **Tasks:** 2
- **Files modified:** 8

## Accomplishments
- PackageStatus enum with ACTIVE/EXPIRING/INACTIVE/NO_PACKAGE values and numeric priority for "best status wins" comparisons
- UserWithPackageStatus and UserHqMembership domain models following plain POJO convention (zero framework deps)
- UserQueryRepository port interface with 5 methods covering all four query endpoints
- Four use cases with comprehensive input validation (page bounds, size limits, status enum validation)
- GetUsersByOrgUseCase implements batch enrichment pattern for HQ memberships
- GetUserByIdUseCase gracefully handles users without packages via fallback to existing UserRepository

## Task Commits

Each task was committed atomically:

1. **Task 1: Create domain models (PackageStatus, UserWithPackageStatus, UserHqMembership)** - `cfceb1a` (feat)
2. **Task 2: Create UserQueryRepository port and four use cases** - `61bcc13` (feat)

## Files Created/Modified
- `src/main/java/org/athlium/users/domain/model/PackageStatus.java` - Enum with 4 statuses, priority ordering, and fromString validation
- `src/main/java/org/athlium/users/domain/model/UserWithPackageStatus.java` - Enriched user model with package status + optional HQ memberships
- `src/main/java/org/athlium/users/domain/model/UserHqMembership.java` - HQ membership with userId, package status, periodEnd, daysRemaining
- `src/main/java/org/athlium/users/domain/repository/UserQueryRepository.java` - Port interface with 5 query methods
- `src/main/java/org/athlium/users/application/usecase/GetUsersByHqUseCase.java` - Query users by HQ with validation
- `src/main/java/org/athlium/users/application/usecase/GetUsersByOrgUseCase.java` - Query users by org with HQ membership enrichment
- `src/main/java/org/athlium/users/application/usecase/GetAllUsersUseCase.java` - Paginated all-users query
- `src/main/java/org/athlium/users/application/usecase/GetUserByIdUseCase.java` - Single user by ID with NO_PACKAGE fallback

## Decisions Made
- Added `userId` field to `UserHqMembership` — needed for the batch enrichment pattern in GetUsersByOrgUseCase to group memberships by user ID
- Made `hqMemberships` on `UserWithPackageStatus` a nullable `List<UserHqMembership>` — only populated for org-level queries, avoiding a separate domain model
- GetUserByIdUseCase uses existing `UserRepository.findById()` as fallback to handle users without any packages, returning NO_PACKAGE status

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added userId field to UserHqMembership**
- **Found during:** Task 2 (GetUsersByOrgUseCase implementation)
- **Issue:** GetUsersByOrgUseCase needs to group HQ memberships by user ID, but UserHqMembership had no userId field
- **Fix:** Added `userId` field with getter/setter to UserHqMembership
- **Files modified:** `src/main/java/org/athlium/users/domain/model/UserHqMembership.java`
- **Verification:** Compilation succeeds, use case can group memberships by user
- **Committed in:** `61bcc13` (part of Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Minor addition necessary for correctness. No scope creep.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Domain and application layers complete, ready for Plan 02 (infrastructure + presentation layer)
- UserQueryRepository port ready for implementation with SQL queries
- Use cases ready for controller injection and REST endpoint wiring

## Self-Check: PASSED

All 8 created files verified on disk. Both task commits (cfceb1a, 61bcc13) verified in git log. Compilation succeeds.

---
*Phase: 01-user-query-endpoints*
*Completed: 2026-03-04*
