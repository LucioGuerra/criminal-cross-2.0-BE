# Role Matrix

This matrix defines the target authorization policy for endpoints in `gym`, `bookings`, and `clients` modules.

Canonical roles (from `Role` enum): `CLIENT`, `PROFESSOR`, `ORG_ADMIN`, `ORG_OWNER`, `SUPERADMIN`.

Policy conventions:
- Admin mutations: `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`
- Staff reads: `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR`
- Client booking actions: `CLIENT`, `PROFESSOR`, `ORG_ADMIN`, `ORG_OWNER`, `SUPERADMIN`

## Gym

| Resource | Method | Path | Allowed Roles |
|---|---|---|---|
| OrganizationResource | GET | `/api/organizations` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |
| OrganizationResource | GET | `/api/organizations/{id}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |
| OrganizationResource | POST | `/api/organizations` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN` |
| OrganizationResource | PUT | `/api/organizations/{id}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN` |
| OrganizationResource | DELETE | `/api/organizations/{id}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN` |
| HeadquartersResource | GET | `/api/headquarters` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |
| HeadquartersResource | GET | `/api/headquarters/{id}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |
| HeadquartersResource | POST | `/api/headquarters` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN` |
| HeadquartersResource | PUT | `/api/headquarters/{id}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN` |
| HeadquartersResource | DELETE | `/api/headquarters/{id}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN` |
| ActivityResource | GET | `/api/activities` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR`, `CLIENT` |
| ActivityResource | GET | `/api/activities/{id}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR`, `CLIENT` |
| ActivityResource | POST | `/api/activities` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |
| ActivityResource | PUT | `/api/activities/{id}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |
| ActivityResource | DELETE | `/api/activities/{id}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN` |
| SessionConfigurationResource | PUT | `/api/gym/config/organizations/{organizationId}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN` |
| SessionConfigurationResource | PUT | `/api/gym/config/headquarters/{headquartersId}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN` |
| SessionConfigurationResource | PUT | `/api/gym/config/activities/{activityId}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN` |
| SessionConfigurationResource | PUT | `/api/gym/config/sessions/{sessionId}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN` |
| SessionConfigurationResource | GET | `/api/gym/config/effective` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |
| SessionResource | GET | `/api/sessions` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR`, `CLIENT` |
| SessionResource | GET | `/api/sessions/{id}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR`, `CLIENT` |
| ActivityScheduleResource | POST | `/api/activity-schedules` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |
| ActivityScheduleResource | GET | `/api/activity-schedules` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |
| ActivityScheduleResource | PUT | `/api/activity-schedules/{id}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |
| ActivityScheduleResource | DELETE | `/api/activity-schedules/{id}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |
| ActivityScheduleResource | POST | `/api/activity-schedules/generate-next-week` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |

## Bookings

| Resource | Method | Path | Allowed Roles |
|---|---|---|---|
| SessionBookingResource | POST | `/api/sessions/{sessionId}/bookings` | `CLIENT`, `PROFESSOR`, `ORG_ADMIN`, `ORG_OWNER`, `SUPERADMIN` |
| BookingResource | POST | `/api/bookings/{bookingId}/cancel` | `CLIENT`, `PROFESSOR`, `ORG_ADMIN`, `ORG_OWNER`, `SUPERADMIN` |
| BookingResource | GET | `/api/bookings` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |

## Clients

| Resource | Method | Path | Allowed Roles |
|---|---|---|---|
| ClientPackageResource | POST | `/api/clients/{userId}/packages` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |
| ClientPackageResource | PATCH | `/api/clients/{userId}/packages/{packageId}` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR` |
| ClientPackageResource | GET | `/api/clients/{userId}/packages/active` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR`, `CLIENT` |
| ClientPackageResource | GET | `/api/clients/{userId}/packages` | `SUPERADMIN`, `ORG_OWNER`, `ORG_ADMIN`, `PROFESSOR`, `CLIENT` |

## Baseline Verification (already protected resources)

| Resource | Existing Annotation | Notes |
|---|---|---|
| PaymentResource | `@Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN", "PROFESSOR"})` | Naming matches `Role` enum |
| UserQueryResource | `@Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN"})` | Naming matches `Role` enum |
| UserResource#createUser | `@Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN"})` | Naming matches `Role` enum |
| UserResource#getUserByUid | `@Authenticated` | Auth required, no role restriction |
| UserResource#updateUserRoles | `@Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN"})` | Naming matches `Role` enum |
