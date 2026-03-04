# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-03-04)

**Core value:** Users can be queried by their gym membership (via packages) with real-time package status, enabling admins to manage members across HQs and organizations.
**Current focus:** Phase 2 — Schedule CRUD Completion

## Current Position

Phase: 2 of 2 (Schedule CRUD Completion)
Plan: 2 of 2 in current phase (COMPLETE)
Status: Phase 2 Complete
Last activity: 2026-03-04 — Completed 02-02-PLAN.md

Progress: [██████████] 100%

## Performance Metrics

**Velocity:**
- Total plans completed: 5
- Average duration: 4 min
- Total execution time: 0.33 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01 | 3/3 | 12 min | 4 min |
| 02 | 2/2 | 8 min | 4 min |

**Recent Trend:**
- Last 5 plans: 3 min, 4 min, 5 min, 5 min, 3 min
- Trend: Stable

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- User-HQ link is INDIRECT: via client_packages → client_package_credits (activityId) → activity → headquarters → organization
- Package status: Active (periodEnd > now && active=true), Expiring (within 3 days of periodEnd), Inactive (periodEnd < now || active=false)
- Soft delete for schedules (active=false), consistent with ActivityEntity @SoftDelete pattern
- Added userId field to UserHqMembership for batch grouping in GetUsersByOrgUseCase
- hqMemberships on UserWithPackageStatus is nullable — only populated for org queries
- GetUserByIdUseCase falls back to existing UserRepository when user has no packages
- Used presentation/ layer convention (not infrastructure/controller/) for REST controller, DTOs, and mapper
- Manual DTO mapper as CDI bean instead of MapStruct — enum→string conversion and conditional HQ memberships
- Sort field allowlist via Map.of() for SQL injection prevention on ORDER BY
- Plain POJOs with explicit getters/setters for DTOs (newer project convention)

- No Mockito — all test doubles are hand-written inner static classes following project convention
- Stub use cases extend concrete classes (not interfaces) since CDI beans have no-arg constructors

- Class-level @Authenticated (no roles) on ActivityScheduleResource — any authenticated user can manage schedules
- Partial update pattern for PUT: only non-null fields overwrite existing values
- Identity fields (id, organizationId, headquartersId, activityId) are immutable on update

- InMemoryActivityScheduleRepository with HashMap storage for ID-based lookup in delete/update tests
- Fixed pre-existing test doubles missing findById after 02-01 interface change

### Pending Todos

None yet.

### Blockers/Concerns

None yet.

## Session Continuity

Last session: 2026-03-04
Stopped at: Completed 02-02-PLAN.md
Resume file: None
