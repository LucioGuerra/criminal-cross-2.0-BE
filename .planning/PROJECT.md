# Athlium Backend - User & Schedule Endpoints

## What This Is

New API endpoints for the Athlium gym management backend (Quarkus 3.29 / Java 21). Adds user query endpoints (by HQ, org, all, by ID) that leverage the client_packages relationship to determine user-HQ membership and package status, plus CRUD completion for activity schedules (update and soft delete). Includes unit tests for all new functionality.

## Core Value

Users can be queried by their gym membership (via packages) with real-time package status (active, expiring, inactive), enabling admins to manage members effectively across headquarters and organizations.

## Requirements

### Validated

- ✓ UserEntity exists with roles, email, firebaseUid, active — existing
- ✓ ClientPackageEntity exists with userId, periodStart, periodEnd, active, credits (activityId) — existing
- ✓ ActivityEntity links to HeadquartersEntity which links to OrganizationEntity — existing
- ✓ ActivityScheduleEntity exists with all schedule fields — existing
- ✓ GET /api/users/firebase/{uid} already exists — existing
- ✓ POST /api/activity-schedules and GET already exist — existing
- ✓ Hexagonal architecture pattern established (domain → application → infrastructure → presentation) — existing
- ✓ Test pattern established: in-memory test doubles, no Mockito — existing

### Active

- [ ] GetUsersByHq endpoint with package status
- [ ] GetUsersByOrg endpoint with package status + HQ name
- [ ] GetAllUsers endpoint with pagination
- [ ] GetUserById endpoint (internal ID)
- [ ] DeleteActivitySchedules endpoint (soft delete)
- [ ] Put/PatchActivitySchedules endpoint (all fields)
- [ ] Unit tests for all new use cases
- [ ] Unit tests for all new controllers

### Out of Scope

- User creation/registration — already handled by auth module
- Role management — already exists at PUT /api/users/firebase/{uid}/roles
- Real-time notifications for package expiration — future feature
- Bulk operations on schedules — not requested
- E2E tests — only unit tests requested

## Context

- **Existing codebase**: Quarkus 3.29.0, Java 21, hexagonal architecture with vertical slicing
- **User-HQ relationship**: Indirect via client_packages → client_package_credits → activityId → activity → headquarters → organization
- **Package status logic**: Active (periodEnd > now && active=true), Expiring (periodEnd within 3 days), Inactive (periodEnd < now || active=false)
- **Test pattern**: In-memory repository doubles (static inner classes), direct use case instantiation, field injection for wiring
- **Existing user module**: lives at `org.athlium.users`, has domain/application/infrastructure layers
- **Existing schedule module**: lives at `org.athlium.gym`, schedules under gym bounded context

## Constraints

- **Architecture**: Must follow hexagonal pattern — domain interface → use case → repository impl → panache repository
- **Testing**: In-memory test doubles, no Mockito — follow existing patterns
- **Auth**: SUPERADMIN + ORG_ADMIN roles for user query endpoints, secured for schedule endpoints
- **Soft delete**: ActivitySchedule delete sets active=false, does not remove record
- **Response format**: All responses wrapped in ApiResponse<T>, pagination uses PageResponse<T>

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| User-HQ link via packages, not join table | Users belong to a HQ because they purchased a package with credits for activities in that HQ | — Pending |
| Package status in user response (active/expiring/inactive) | Admins need to see membership status at a glance; "expiring" = 3 days before periodEnd | — Pending |
| GetUsersByOrg includes HQ name | Org-level view needs to show which HQ each user belongs to | — Pending |
| Soft delete for schedules | Consistent with existing patterns (ActivityEntity uses @SoftDelete) | — Pending |
| Both internal ID and Firebase UID for GetUserById | Firebase UID endpoint already exists; add internal ID for admin use | — Pending |

---
*Last updated: 2026-03-04 after initialization*
