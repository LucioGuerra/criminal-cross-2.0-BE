---
status: complete
phase: 01-user-query-endpoints
source: 01-01-SUMMARY.md, 01-02-SUMMARY.md, 01-03-SUMMARY.md
started: 2026-03-04T19:30:00Z
updated: 2026-03-04T19:50:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Query Users by HQ
expected: GET /api/users?headquartersId={id}&page=1&size=10 returns a paginated list of users belonging to that HQ, each with a packageStatus field (ACTIVE, EXPIRING, INACTIVE, or NO_PACKAGE) and role information.
result: issue
reported: "SQL sort clause uses table alias prefix 'u.' (e.g. u.name, u.last_name) but ORDER BY is appended outside CTE where alias 'u' does not exist. Will cause runtime SQL error on any request with sort parameter."
severity: blocker

### 2. Query Users by Organization
expected: GET /api/users?organizationId={id}&page=1&size=10 returns users across all HQs in that org, each with packageStatus and an hqMemberships array showing per-HQ membership details (HQ name, package status, period end, days remaining).
result: issue
reported: "Same sort alias bug as Test 1. Sort clause 'u.name ASC' used outside CTE context at line 266. Will fail at runtime."
severity: blocker

### 3. Get All Users Paginated
expected: GET /api/users?page=1&size=10 returns all users paginated with packageStatus. Supports optional status filter (e.g. &status=ACTIVE) and search parameter. Sort defaults to name ascending.
result: issue
reported: "Same sort alias bug as Test 1. Sort clause 'u.name ASC' used outside CTE context at line 454. Will fail at runtime."
severity: blocker

### 4. Get User by ID
expected: GET /api/users/{id} returns a single user with packageStatus. If the user has no packages, returns NO_PACKAGE status instead of an error. Returns 404 if user does not exist.
result: pass

### 5. Pagination and Sorting
expected: Changing page/size params returns different slices. Sort accepts name, lastName, email fields. Invalid sort fields are rejected or ignored. Page 0 or negative values return 400.
result: issue
reported: "Sort field mapping uses 'u.name', 'u.last_name', 'u.email' with table alias prefix, but ORDER BY clause is outside CTE where no alias exists. Any sorted query will fail with SQL error. Also: API returns 0-based page in response even though client sends 1-based page (minor inconsistency)."
severity: blocker

### 6. Package Status Computation
expected: Users with active, non-expired packages show ACTIVE. Users within 3 days of package expiration show EXPIRING. Users with expired/inactive packages show INACTIVE. Users with no packages show NO_PACKAGE. When a user has multiple packages, the best status wins.
result: pass

### 7. Unit Tests Pass
expected: Running the project's test command executes all 40 new unit tests with 0 failures. Pre-existing E2E test (GymBookingsE2ETest) may fail due to PostgreSQL dependency — that's expected and unrelated.
result: issue
reported: "36/40 new tests pass. 4 tests in GetUserByIdUseCaseTest fail because User.java uses Lombok (@Getter, @Builder) but Lombok annotation processing is broken — compiled User.class has no generated methods. The test uses User.builder() and getId()/getName() etc. which don't exist at runtime. Pre-existing Lombok issue, but the test should have used plain POJO construction to match the project's newer convention."
severity: major

## Summary

total: 7
passed: 2
issues: 5
pending: 0
skipped: 0

## Gaps

- truth: "GET /api/users?headquartersId={id} with sort returns ordered results"
  status: failed
  reason: "SORT_FIELD_MAP maps to 'u.name', 'u.last_name', 'u.email' with table alias prefix, but ORDER BY is appended outside CTE at lines 136, 266, 454 where alias 'u' does not exist. SQL error at runtime."
  severity: blocker
  test: 1
  root_cause: "UserQueryRepositoryImpl.java lines 28-32: SORT_FIELD_MAP values use 'u.' prefix but buildSortClause() output is appended outside CTE WHERE rn=1 context"
  artifacts:
    - path: "src/main/java/org/athlium/users/infrastructure/repository/UserQueryRepositoryImpl.java"
      issue: "SORT_FIELD_MAP values 'u.name', 'u.last_name', 'u.email' should be 'name', 'last_name', 'email'"
  missing:
    - "Remove 'u.' prefix from SORT_FIELD_MAP values"
    - "Default sort 'name ASC' in buildSortClause() is correct (no prefix) — only the map values need fixing"
  debug_session: ""

- truth: "All 40 Phase 1 unit tests pass"
  status: failed
  reason: "4 tests in GetUserByIdUseCaseTest fail: User.builder() and Lombok getters (getId, getName, etc.) don't exist at runtime because Lombok annotation processing is broken for User.java"
  severity: major
  test: 7
  root_cause: "GetUserByIdUseCaseTest uses User.builder() and Lombok-generated getters on User class, but Lombok annotation processing doesn't generate methods (compiled User.class has no getters/builder). Pre-existing Lombok issue in project build."
  artifacts:
    - path: "src/test/java/org/athlium/users/application/usecase/GetUserByIdUseCaseTest.java"
      issue: "Uses User.builder() at line 58 and Lombok getters at lines 44-49, which don't exist at runtime"
    - path: "src/main/java/org/athlium/users/domain/model/User.java"
      issue: "Lombok @Getter/@Builder not processed — compiled class has no generated methods"
  missing:
    - "Replace User.builder() with manual User construction in GetUserByIdUseCaseTest"
    - "Or fix Lombok annotation processing in pom.xml (broader project issue)"
  debug_session: ""

- truth: "Duplicate @Path('/api/users') may cause routing conflicts"
  status: failed
  reason: "UserQueryResource.java and UserResource.java both use @Path('/api/users'). JAX-RS may handle it but it's a potential deployment issue with ambiguous class-level path resolution."
  severity: minor
  test: 1
  root_cause: "Two resource classes share the same base path. UserResource at infrastructure/controller/, UserQueryResource at presentation/controller/."
  artifacts:
    - path: "src/main/java/org/athlium/users/presentation/controller/UserQueryResource.java"
      issue: "@Path('/api/users') duplicates existing UserResource"
    - path: "src/main/java/org/athlium/users/infrastructure/controller/UserResource.java"
      issue: "@Path('/api/users') — original resource"
  missing:
    - "Verify at runtime whether Quarkus/RESTEasy handles dual @Path correctly"
    - "Consider consolidating or using distinct paths"
  debug_session: ""
