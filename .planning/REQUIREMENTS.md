# Requirements: Athlium User & Schedule Endpoints

**Defined:** 2026-03-04
**Core Value:** Users can be queried by their gym membership (via packages) with real-time package status, enabling admins to manage members effectively across HQs and organizations.

## v1 Requirements

### User Query Endpoints

- [ ] **USER-01**: Admin can get all users belonging to a specific headquarters (via client packages → credits → activities → HQ) with package status (active/expiring/inactive)
- [ ] **USER-02**: Admin can get all users belonging to a specific organization (via HQ chain) with package status + HQ name
- [ ] **USER-03**: Admin can get all users with pagination (page/size params) returning PageResponse
- [ ] **USER-04**: Admin can get a user by internal database ID
- [ ] **USER-05**: Unit tests for GetUsersByHq use case and controller
- [ ] **USER-06**: Unit tests for GetUsersByOrg use case and controller
- [ ] **USER-07**: Unit tests for GetAllUsers use case and controller
- [ ] **USER-08**: Unit tests for GetUserById use case and controller

### Activity Schedule Management

- [ ] **SCHED-01**: Admin can soft-delete an activity schedule (sets active=false)
- [ ] **SCHED-02**: Admin can update all fields of an activity schedule (PUT/PATCH)
- [ ] **SCHED-03**: Unit tests for DeleteActivitySchedule use case and controller
- [ ] **SCHED-04**: Unit tests for UpdateActivitySchedule use case and controller

### Cross-Cutting

- [ ] **AUTH-01**: All new user endpoints require SUPERADMIN or ORG_ADMIN role
- [ ] **AUTH-02**: All new schedule endpoints require authentication

## v2 Requirements

### Enhanced User Queries

- **USER-V2-01**: Filter users by package status (active/expiring/inactive only)
- **USER-V2-02**: Search users by name/email within HQ/Org
- **USER-V2-03**: Export user list to CSV

### Schedule Enhancements

- **SCHED-V2-01**: Bulk delete/update multiple schedules
- **SCHED-V2-02**: Schedule conflict detection on update

## Out of Scope

| Feature | Reason |
|---------|--------|
| User creation/registration | Handled by auth module |
| Role management | Already exists at PUT /api/users/firebase/{uid}/roles |
| Real-time expiration notifications | Future feature, not requested |
| E2E integration tests | Only unit tests requested |
| Package management endpoints | Already exist at /api/clients/{userId}/packages |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| USER-01 | Phase 1 | Pending |
| USER-02 | Phase 1 | Pending |
| USER-03 | Phase 1 | Pending |
| USER-04 | Phase 1 | Pending |
| USER-05 | Phase 1 | Pending |
| USER-06 | Phase 1 | Pending |
| USER-07 | Phase 1 | Pending |
| USER-08 | Phase 1 | Pending |
| SCHED-01 | Phase 2 | Pending |
| SCHED-02 | Phase 2 | Pending |
| SCHED-03 | Phase 2 | Pending |
| SCHED-04 | Phase 2 | Pending |
| AUTH-01 | Phase 1 | Pending |
| AUTH-02 | Phase 2 | Pending |

**Coverage:**
- v1 requirements: 14 total
- Mapped to phases: 14 ✓
- Unmapped: 0

---
*Requirements defined: 2026-03-04*
*Last updated: 2026-03-04 after roadmap creation*
