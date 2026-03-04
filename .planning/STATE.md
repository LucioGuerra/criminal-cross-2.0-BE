# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-03-04)

**Core value:** Users can be queried by their gym membership (via packages) with real-time package status, enabling admins to manage members across HQs and organizations.
**Current focus:** Phase 1 — User Query Endpoints

## Current Position

Phase: 1 of 2 (User Query Endpoints) — COMPLETE
Plan: 3 of 3 in current phase (all done)
Status: Phase 1 Complete — Ready for Phase 2
Last activity: 2026-03-04 — Completed 01-03-PLAN.md

Progress: [██████░░░░] 60%

## Performance Metrics

**Velocity:**
- Total plans completed: 3
- Average duration: 4 min
- Total execution time: 0.20 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01 | 3/3 | 12 min | 4 min |

**Recent Trend:**
- Last 5 plans: 3 min, 4 min, 5 min
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

### Pending Todos

None yet.

### Blockers/Concerns

None yet.

## Session Continuity

Last session: 2026-03-04
Stopped at: Completed 01-03-PLAN.md — Phase 1 complete
Resume file: None
