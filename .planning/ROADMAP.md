# Roadmap: Athlium User & Schedule Endpoints

## Overview

Two-phase delivery: first, the core value — user query endpoints that resolve gym membership through the client_packages chain and expose package status (active/expiring/inactive). Second, complete the schedule CRUD with update and soft-delete operations. Each phase includes its own unit tests and auth constraints, so each delivers a fully working, tested vertical slice.

## Phases

- [x] **Phase 1: User Query Endpoints** - Four user query endpoints (by HQ, by Org, all paginated, by ID) with package status, auth, and unit tests
- [x] **Phase 2: Schedule CRUD Completion** - Update and soft-delete endpoints for activity schedules with auth and unit tests

## Phase Details

### Phase 1: User Query Endpoints
**Goal**: Admins can query users by HQ, organization, or globally — seeing real-time package status (active/expiring/inactive) — enabling effective member management
**Depends on**: Nothing (first phase)
**Requirements**: USER-01, USER-02, USER-03, USER-04, USER-05, USER-06, USER-07, USER-08, AUTH-01
**Success Criteria** (what must be TRUE):
  1. GET request with an HQ ID returns all users who have packages with credits for activities in that HQ, each user showing their package status (active/expiring/inactive)
  2. GET request with an Org ID returns all users across all HQs in that org, each user showing package status and which HQ they belong to
  3. GET request for all users returns a paginated response (page/size params) with correct total counts
  4. GET request with an internal user ID returns that single user's full details
  5. All four endpoints reject requests from users without SUPERADMIN or ORG_ADMIN roles (401/403)
**Plans**: 4 plans

Plans:
- [x] 01-01: Domain + application layer (use cases, ports, package status logic)
- [x] 01-02: Infrastructure + presentation layer (repository impls, REST controllers, auth)
- [x] 01-03: Unit tests for all use cases and controllers
- [x] 01-04: Gap closure — Fix SORT_FIELD_MAP alias prefix causing SQL errors on sorted queries

### Phase 2: Schedule CRUD Completion
**Goal**: Admins can update and soft-delete activity schedules, completing the CRUD operations for the schedule management feature
**Depends on**: Phase 1
**Requirements**: SCHED-01, SCHED-02, SCHED-03, SCHED-04, AUTH-02
**Success Criteria** (what must be TRUE):
  1. DELETE request for a schedule ID sets active=false on the record (soft delete) and returns success — the schedule no longer appears in active listings
  2. PUT/PATCH request for a schedule ID updates all mutable fields and returns the updated schedule
  3. Both endpoints require authentication and reject unauthenticated requests (401)
  4. Unit tests pass for both use cases and both controllers using in-memory test doubles
**Plans**: 2 plans

Plans:
- [x] 02-01: Update and soft-delete use cases, controllers, and auth
- [x] 02-02: Unit tests for update and delete (use cases + controllers)

## Progress

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. User Query Endpoints | 4/4 | Complete    | 2026-03-04 |
| 2. Schedule CRUD Completion | 2/2 | Complete | 2026-03-04 |
