---
phase: 02-schedule-crud-completion
plan: 01
subsystem: api
tags: [quarkus, mongodb, crud, rest, authentication, soft-delete]

# Dependency graph
requires:
  - phase: 01-user-query-endpoints
    provides: presentation layer conventions, CDI use case patterns, exception handling
provides:
  - PUT /api/activity-schedules/{id} endpoint for updating schedules
  - DELETE /api/activity-schedules/{id} endpoint for soft-deleting schedules
  - findById(Long) on ActivityScheduleRepository domain port
  - @Authenticated securing all schedule endpoints
affects: [02-schedule-crud-completion]

# Tech tracking
tech-stack:
  added: []
  patterns: [soft-delete via active=false, partial update with null-check guards, class-level @Authenticated]

key-files:
  created:
    - src/main/java/org/athlium/gym/application/usecase/DeleteActivityScheduleUseCase.java
    - src/main/java/org/athlium/gym/application/usecase/UpdateActivityScheduleUseCase.java
  modified:
    - src/main/java/org/athlium/gym/domain/repository/ActivityScheduleRepository.java
    - src/main/java/org/athlium/gym/infrastructure/repository/ActivityScheduleRepositoryImpl.java
    - src/main/java/org/athlium/gym/presentation/controller/ActivityScheduleResource.java

key-decisions:
  - "Class-level @Authenticated (no roles) — any authenticated user can access schedule CRUD, matching AUTH-02 requirement"
  - "Partial update pattern — only non-null fields from request overwrite existing values, preserving unchanged fields"
  - "Identity fields (id, organizationId, headquartersId, activityId) are immutable on update"

patterns-established:
  - "Soft-delete pattern: load by ID, set active=false, save — reuses existing save() update-or-insert logic"
  - "Partial update pattern: null-check each mutable field before applying to existing entity"

requirements-completed: [SCHED-01, SCHED-02, AUTH-02]

# Metrics
duration: 5min
completed: 2026-03-04
---

# Phase 02 Plan 01: Schedule Update & Delete Endpoints Summary

**PUT and DELETE endpoints for activity schedules with soft-delete, partial updates, and class-level @Authenticated securing all schedule operations**

## Performance

- **Duration:** 5 min
- **Started:** 2026-03-04T18:42:53Z
- **Completed:** 2026-03-04T19:18:50Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments
- Added `findById(Long)` to ActivityScheduleRepository port and MongoDB implementation using `scheduleId` business ID
- Created DeleteActivityScheduleUseCase with soft-delete (sets active=false, saves, returns deactivated schedule)
- Created UpdateActivityScheduleUseCase with partial update of mutable fields only
- Added DELETE and PUT REST endpoints on ActivityScheduleResource
- Secured all schedule endpoints with class-level `@Authenticated` annotation

## Task Commits

Each task was committed atomically:

1. **Task 1: Add findById to repository + create Delete/Update use cases** - `e024170` (feat)
2. **Task 2: Add DELETE and PUT endpoints with @Authenticated** - `9cb5cef` (feat)

## Files Created/Modified
- `src/main/java/org/athlium/gym/domain/repository/ActivityScheduleRepository.java` - Added findById(Long) port method
- `src/main/java/org/athlium/gym/infrastructure/repository/ActivityScheduleRepositoryImpl.java` - Implemented findById using MongoDB scheduleId lookup
- `src/main/java/org/athlium/gym/application/usecase/DeleteActivityScheduleUseCase.java` - Soft-delete use case: loads by ID, sets active=false, saves
- `src/main/java/org/athlium/gym/application/usecase/UpdateActivityScheduleUseCase.java` - Update use case: loads by ID, applies mutable field updates, saves
- `src/main/java/org/athlium/gym/presentation/controller/ActivityScheduleResource.java` - Added @Authenticated, DELETE /{id}, PUT /{id} endpoints

## Decisions Made
- Used class-level `@Authenticated` (no roles) — any authenticated user can manage schedules, consistent with AUTH-02 requirement
- Partial update pattern: only non-null fields from the request overwrite the existing entity, preserving unchanged values
- Identity fields (id, organizationId, headquartersId, activityId) are immutable — not updatable via PUT

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Schedule CRUD endpoints (create, read, update, delete) are complete
- Ready for 02-02-PLAN.md (integration tests for schedule endpoints)

## Self-Check: PASSED

All 5 key files verified on disk. Both task commits (e024170, 9cb5cef) found in git log.

---
*Phase: 02-schedule-crud-completion*
*Completed: 2026-03-04*
