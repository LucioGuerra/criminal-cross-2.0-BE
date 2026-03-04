# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-03-04)

**Core value:** Users can be queried by their gym membership (via packages) with real-time package status, enabling admins to manage members across HQs and organizations.
**Current focus:** Phase 1 — User Query Endpoints

## Current Position

Phase: 1 of 2 (User Query Endpoints)
Plan: 2 of 3 in current phase
Status: Executing
Last activity: 2026-03-04 — Completed 01-02-PLAN.md

Progress: [████░░░░░░] 40%

## Performance Metrics

**Velocity:**
- Total plans completed: 2
- Average duration: 3.5 min
- Total execution time: 0.12 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01 | 2/3 | 7 min | 3.5 min |

**Recent Trend:**
- Last 5 plans: 3 min, 4 min
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

### Pending Todos

None yet.

### Blockers/Concerns

None yet.

## Session Continuity

Last session: 2026-03-04
Stopped at: Completed 01-02-PLAN.md
Resume file: None
