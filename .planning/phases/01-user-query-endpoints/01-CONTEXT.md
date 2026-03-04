# Phase 1: User Query Endpoints - Context

**Gathered:** 2026-03-04
**Status:** Ready for planning

<domain>
## Phase Boundary

Four user query endpoints (by HQ, by Org, all paginated, by ID) that resolve gym membership through the client_packages chain and expose package status (active/expiring/inactive). All endpoints secured with SUPERADMIN/ORG_ADMIN roles. The existing GET /api/users/firebase/{uid} stays untouched.

</domain>

<decisions>
## Implementation Decisions

### User response shape
- Core fields per user: id, name, lastName, email, roles, active, packageStatus
- Package status represented as: status enum (ACTIVE/EXPIRING/INACTIVE/NO_PACKAGE) + periodEnd date + daysRemaining
- GetUsersByOrg includes nested HQ object per user: `{ id, name }`
- A user with packages in multiple HQs within the same org returns an array of HQ objects, each with its own package status
- GetUsersByHq returns users with a single package status (best status across packages at that HQ)

### Package status logic
- **ACTIVE**: package has `active=true` AND `periodEnd > now`
- **EXPIRING**: package has `active=true` AND `periodEnd` is within 3 days from now (fixed threshold, not configurable)
- **INACTIVE**: package has `periodEnd < now` (natural expiry) OR `active=false` (revoked/cancelled — even if periodEnd is in the future)
- **NO_PACKAGE**: user has no packages with credits for activities at the given HQ
- `active=false` means revoked/cancelled (separate from natural expiry via periodEnd)
- Multiple packages at same HQ: best status wins (ACTIVE > EXPIRING > INACTIVE > NO_PACKAGE)
- Credits used up (tokens=0) does NOT affect HQ membership — user still belongs if package exists
- When best status is EXPIRING or ACTIVE, use the periodEnd and daysRemaining from that winning package

### Query filtering
- All three list endpoints (byHq, byOrg, allUsers) support pagination with PageResponse
- Filter by package status: optional `status` query param (ACTIVE, EXPIRING, INACTIVE, NO_PACKAGE)
- Search by name/email: optional `search` query param (partial match on name, lastName, or email)
- Configurable sort param: `sort` query param, default by name ascending (follow existing endpoint patterns like sessions/bookings)
- GetAllUsers has the same filter set as GetUsersByHq/GetUsersByOrg (status, search, sort, pagination)

### Cross-module data access
- Module placement and port design: Claude's discretion based on architecture analysis
- Data resolution: Single SQL query with JOINs across the 4-table chain (users → client_packages → client_package_credits → activities → headquarters)
- Package status computation: In SQL using CASE WHEN for efficiency (avoid N+1 and application-level computation)
- URL structure: Query params on `/api/users` — `GET /api/users?headquartersId=X`, `GET /api/users?organizationId=X`
- GetUserById: `GET /api/users/{id}` for internal ID (existing Firebase UID endpoint at `/api/users/firebase/{uid}` stays as-is)

### Claude's Discretion
- Which module owns the new use cases and where cross-module ports go
- DTO/mapper structure for the enriched user response
- Exact SQL query optimization approach
- Error handling for invalid HQ/Org IDs (404 vs empty list)

</decisions>

<specifics>
## Specific Ideas

- Package status priority: ACTIVE > EXPIRING > INACTIVE > NO_PACKAGE — "best status wins" when multiple packages exist
- The 3-day expiring threshold is a business constant, not configurable per org
- `active=false` on a package is a manual revocation (admin cancelled it), distinct from natural expiry via periodEnd
- GetUsersByOrg response groups HQ memberships per user as an array — one user can appear with multiple HQs

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 01-user-query-endpoints*
*Context gathered: 2026-03-04*
