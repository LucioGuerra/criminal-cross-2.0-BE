# Codebase Concerns

**Analysis Date:** 2026-03-04

## Tech Debt

**MapStruct Version Mismatch:**
- Issue: Dependency declares `1.5.5.Final` but annotation processor in `maven-compiler-plugin` uses `1.6.0`. This means the runtime API is 1.5.5 but the code generator is 1.6.0 — mismatched versions can cause subtle bugs or compilation issues.
- Files: `pom.xml` (lines 158-161 vs lines 203-207)
- Impact: Potential runtime errors if generated code references 1.6.0-only APIs not present in 1.5.5 runtime classes.
- Fix approach: Align both to `1.6.0` by updating the dependency version at lines 160 and 168 to `1.6.0`.

**Dual Database Without Clear Boundary:**
- Issue: PostgreSQL (Hibernate/Panache) and MongoDB (Panache) coexist. MongoDB is only used for hierarchical session configuration overrides (`SessionConfigurationEntity` in MongoDB). This adds operational complexity (two databases to backup, monitor, scale) for a feature that could be modeled in PostgreSQL with JSONB columns.
- Files: `src/main/java/org/athlium/gym/infrastructure/repository/SessionConfigurationRepositoryImpl.java`, `src/main/java/org/athlium/gym/infrastructure/persistence/mongodb/SessionConfigurationEntity.java`
- Impact: Increased deployment complexity, two connection strings to manage, two backup strategies. MongoDB is used for a single entity.
- Fix approach: Evaluate migrating session configuration to PostgreSQL JSONB. If MongoDB stays, document the clear boundary: "MongoDB is for hierarchical configuration only."

**Flyway Migrations Are Broken (V1 + V2+ Conflict):**
- Issue: `V1.0.0__init.sql` is a Hibernate auto-generated DDL dump that creates ALL tables (including `users`, `user_roles`, `client_packages`, `payments`, `bookings`, etc.). Then `V2__Create_users_table.sql` recreates `users` and `user_roles`, `V3` recreates `client_packages` tables, and `V6` recreates `payments`. In dev mode, `drop-and-create` hides this because Flyway is disabled (`quarkus.flyway.migrate-at-start=false`). In production (`%prod.quarkus.flyway.migrate-at-start=true`), V2 would fail because `users` table already exists from V1.
- Files: `src/main/resources/db/migration/V1.0.0__init.sql`, `src/main/resources/db/migration/V2__Create_users_table.sql`, `src/main/resources/db/migration/V3__Create_client_packages_tables.sql`, `src/main/resources/db/migration/V6__Create_payments_and_link_to_packages.sql`
- Impact: **Production deployment is currently impossible** with Flyway enabled. Migrations will fail on V2.
- Fix approach: Either (a) rewrite V1 to only contain the tables that V2-V6 don't create, or (b) squash all migrations into a single V1 and delete V2-V6.

**Duplicate Column in V1 Migration:**
- Issue: The `activity` table in `V1.0.0__init.sql` has TWO soft-delete columns: `deletedAt timestamp(6)` (line 25) and `deleted_at timestamp(6) with time zone` (line 26). The entity uses Hibernate `@SoftDelete` which maps to `deleted_at`. The `deletedAt` column is a leftover from auto-generation.
- Files: `src/main/resources/db/migration/V1.0.0__init.sql` (lines 25-26)
- Impact: Wasted column, potential confusion. Not a runtime bug because Hibernate uses `deleted_at`.
- Fix approach: Remove `deletedAt timestamp(6)` from V1 migration when rewriting migrations.

**Hardcoded Token Expiration in AuthResource:**
- Issue: `expiresIn(900)` is hardcoded in two places instead of using `JwtTokenGenerator.getAccessTokenExpirationSeconds()`. If `auth.access-token.expiration-minutes` config changes, the response will lie to the client about expiration time.
- Files: `src/main/java/org/athlium/auth/infrastructure/controller/AuthResource.java` (lines 198, 240)
- Impact: Client-side token refresh logic will break if config changes. Silent bug — tokens expire at real time but client thinks they have 900 seconds.
- Fix approach: Inject `JwtTokenGenerator` into `AuthResource` and use `getAccessTokenExpirationSeconds()` for the `expiresIn` value.

**SecurityContext Has Redundant Accessors:**
- Issue: Class has both `@Getter`/`@Setter` Lombok annotations AND manually written getters/setters for all three fields. Lombok generates its own, then manual ones override them. The `isAuthenticated()` override is intentional (adds `currentUser != null` check), but the rest are pure duplication.
- Files: `src/main/java/org/athlium/auth/infrastructure/security/SecurityContext.java` (lines 12-14 vs lines 21-39)
- Impact: Maintenance burden, confusing for developers.
- Fix approach: Remove `@Getter`/`@Setter` annotations since the manual implementations have semantic differences for `isAuthenticated()`. Or remove the redundant manual getters/setters for `currentUser` and `rawToken`, keeping only `isAuthenticated()` manual.

**Cross-Module Coupling (No Ports/Adapters Between Modules):**
- Issue: The `bookings` module directly imports from `clients.application.service.ClientPackageCreditService` and `gym.domain.repository.SessionInstanceRepository`. This creates hard coupling between bounded contexts at the application layer.
- Files: `src/main/java/org/athlium/bookings/application/usecase/CreateBookingUseCase.java` (lines 9-11), `src/main/java/org/athlium/bookings/application/usecase/CancelBookingUseCase.java` (lines 9-10)
- Impact: Cannot change client package credit logic without risk to bookings. Cannot test booking use cases without real implementations of client/gym services.
- Fix approach: Define port interfaces in the `bookings` domain (e.g., `CreditChecker`, `SessionProvider`) and implement adapters that delegate to the actual services.

**Deprecated JJWT API Usage:**
- Issue: `CustomJwtValidator` uses deprecated `Jwts.parser().setSigningKey(publicKey)` API (JJWT 0.11.x deprecated style). The modern API is `Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token)`.
- Files: `src/main/java/org/athlium/auth/infrastructure/security/CustomJwtValidator.java` (lines 68-72)
- Impact: Will break when upgrading JJWT to 0.12.x+ where deprecated methods are removed.
- Fix approach: Update to JJWT 0.12.x and use the new builder-based parser API.

**Undecided Architecture: Tier/Billing Level:**
- Issue: TODOs in domain models indicate an unresolved design decision about whether subscription tier and billing should live at the Organization level or Headquarters level.
- Files: `src/main/java/org/athlium/gym/domain/model/Organization.java` (lines 19, 27), `src/main/java/org/athlium/gym/domain/model/Headquarters.java` (lines 19-20)
- Impact: Blocks billing/subscription feature development. Current domain models have placeholder comments but no fields.
- Fix approach: Make a design decision and document it. The Organization model has `name` and `id` only; Headquarters has `name`, `id`, and `organizationId`. Add tier/billing fields to the chosen level.

**No Domain Events:**
- Issue: Business workflows that require notifications or side effects (e.g., waitlist promotion after booking cancellation) use TODOs instead of domain events.
- Files: `src/main/java/org/athlium/bookings/application/usecase/CancelBookingUseCase.java` (line 86)
- Impact: Cannot notify users when promoted from waitlist. Cannot trigger async side effects without coupling use cases.
- Fix approach: Implement a lightweight domain event system using CDI events (`@Observes`/`Event<T>` in Quarkus).

**Hibernate Auto-Generated Foreign Key Names:**
- Issue: V1 migration uses Hibernate-generated constraint names like `FKh5qajputrmmneuesbit34v88h`. These are non-descriptive and will differ between Hibernate versions.
- Files: `src/main/resources/db/migration/V1.0.0__init.sql` (lines 213-230)
- Impact: Hard to debug constraint violations, hard to write manual migration scripts referencing these names.
- Fix approach: Use explicit constraint names in entity `@JoinColumn` annotations or rewrite migrations with meaningful names like `fk_activity_headquarters`.

## Known Bugs

**Waitlist Promotion Throws Instead of Skipping:**
- Symptoms: When a confirmed booking is cancelled and the first waitlisted user has no credits, the entire cancellation throws `BadRequestException("Waitlisted user has no available credits for this activity")` instead of skipping to the next waitlisted user or simply not promoting anyone.
- Files: `src/main/java/org/athlium/bookings/application/usecase/CancelBookingUseCase.java` (lines 85-88)
- Trigger: Cancel a confirmed booking when the first waitlisted user's package has expired or has no credits for this activity.
- Workaround: None — the cancellation fails entirely, leaving the booking in CONFIRMED state.

**Flyway Migrations Will Fail in Production:**
- Symptoms: Application startup fails with "relation already exists" when `quarkus.flyway.migrate-at-start=true` in production profile.
- Files: `src/main/resources/db/migration/V1.0.0__init.sql`, `src/main/resources/db/migration/V2__Create_users_table.sql`
- Trigger: First production deployment with Flyway enabled. V1 creates `users` table, V2 tries to create it again.
- Workaround: Use `drop-and-create` Hibernate strategy (defeats purpose of Flyway).

## Security Considerations

**Most Endpoints Are Unauthenticated (Critical):**
- Risk: `FirebaseAuthFilter` treats endpoints without `@Authenticated` annotation as public (line 68-71). Only 3 controllers have auth annotations: `UserResource`, `PaymentResource`, `AuthResource`. The remaining 9 controllers (all gym, booking, session, and client package operations) are **completely unauthenticated**. Anyone can create/delete organizations, headquarters, activities, sessions, bookings, and client packages without any token.
- Files:
  - Filter logic: `src/main/java/org/athlium/auth/infrastructure/security/FirebaseAuthFilter.java` (lines 68-71)
  - Unprotected controllers:
    - `src/main/java/org/athlium/gym/presentation/controller/OrganizationResource.java`
    - `src/main/java/org/athlium/gym/presentation/controller/HeadquartersResource.java`
    - `src/main/java/org/athlium/gym/presentation/controller/ActivityResource.java`
    - `src/main/java/org/athlium/gym/presentation/controller/ActivityScheduleResource.java`
    - `src/main/java/org/athlium/gym/presentation/controller/SessionResource.java`
    - `src/main/java/org/athlium/gym/presentation/controller/SessionConfigurationResource.java`
    - `src/main/java/org/athlium/bookings/presentation/controller/BookingResource.java`
    - `src/main/java/org/athlium/bookings/presentation/controller/SessionBookingResource.java`
    - `src/main/java/org/athlium/clients/presentation/controller/ClientPackageResource.java`
- Current mitigation: None. These endpoints are fully open.
- Recommendations: Add `@Authenticated` at the class level to ALL resource controllers. Use `@PublicEndpoint` only for endpoints that must be public. Consider inverting the default: require auth unless explicitly marked public.

**CORS Accepts Any Origin:**
- Risk: `quarkus.http.cors.origins=/.*/` is a regex that matches every origin. Combined with `access-control-allow-credentials=true`, this means any website can make authenticated requests to the API using the user's cookies/tokens. This is the classic CORS misconfiguration for credential theft.
- Files: `src/main/resources/application.properties` (lines 62, 65)
- Current mitigation: None. No production CORS override exists (`%prod.quarkus.http.cors.origins` is not set).
- Recommendations: Set specific allowed origins for production. At minimum: `%prod.quarkus.http.cors.origins=https://yourdomain.com`. Never combine wildcard origins with `access-control-allow-credentials=true`.

**Firebase Mock Mode Enabled by Default:**
- Risk: `firebase.mock.enabled=${FIREBASE_MOCK_ENABLED:true}` means any string passed as a Firebase token will be accepted as valid in development. If this accidentally runs in production (env var not set), all Firebase token validation is bypassed.
- Files: `src/main/resources/application.properties` (line 36)
- Current mitigation: Production profile sets `%prod.firebase.mock.enabled=false` (line 79). However, if the app starts without the `prod` profile active, mock mode is on.
- Recommendations: Default to `false` and only enable in dev/test profiles: `firebase.mock.enabled=${FIREBASE_MOCK_ENABLED:false}`, `%dev.firebase.mock.enabled=true`.

**No Rate Limiting on Auth Endpoints:**
- Risk: Login, register, refresh, and verify-token endpoints have no rate limiting. Attackers can brute-force tokens, enumerate users, or exhaust Firebase API quota.
- Files: `src/main/java/org/athlium/auth/infrastructure/controller/AuthResource.java` (all `@PublicEndpoint` methods)
- Current mitigation: None.
- Recommendations: Add rate limiting via a Quarkus filter or reverse proxy. At minimum, limit `/api/auth/login` and `/api/auth/register` to 10 requests per minute per IP.

**GlobalExceptionMapper Leaks Internal Details:**
- Risk: `GlobalExceptionMapper` catches all exceptions and returns the raw `exception.getMessage()` in the API response. This can expose internal class names, SQL errors, file paths, and stack trace fragments to attackers.
- Files: `src/main/java/org/athlium/shared/exception/GlobalExceptionMapper.java` (line 16)
- Current mitigation: None.
- Recommendations: Log the full exception server-side. Return a generic "Internal server error" message to clients. Only return specific messages for known business exceptions (which should have their own exception mappers with appropriate HTTP status codes).

**Public Key Lazy-Loaded Without Thread Safety:**
- Risk: `CustomJwtValidator.publicKey` is lazily loaded on first use without synchronization. In a multi-threaded environment (Quarkus uses multiple IO threads), two threads could simultaneously see `publicKey == null`, both load the key, and one could use a partially-constructed `PublicKey` object. In practice, the risk is low because `loadPublicKey()` is a pure function, but it violates thread-safety contracts.
- Files: `src/main/java/org/athlium/auth/infrastructure/security/CustomJwtValidator.java` (lines 63-65)
- Current mitigation: None.
- Recommendations: Either use `volatile` keyword on the field with double-checked locking, or use a `@PostConstruct` method to eagerly load the key, or use `AtomicReference<PublicKey>`.

**Token Type Detection via String Matching:**
- Risk: `isCustomJwt()` decodes the JWT payload (without signature verification) and checks if it contains the issuer string via `payload.contains("\"iss\":\"" + issuer + "\"")`. An attacker could craft a Firebase-like token with the backend issuer in a non-`iss` field to trick the system into using the custom JWT validation path.
- Files: `src/main/java/org/athlium/auth/infrastructure/security/CustomJwtValidator.java` (lines 133-154)
- Current mitigation: Custom JWT validation verifies the signature afterward, so a forged token would fail signature validation. The risk is low but the heuristic is fragile.
- Recommendations: Parse the payload as JSON and check the `iss` field properly, or use a dedicated header/claim to distinguish token types.

## Performance Bottlenecks

**No Caching on User Enrichment:**
- Problem: Every authenticated request calls `userProvider.enrichWithUserData()` which queries the database for user data (including roles). This happens on every single API call.
- Files: `src/main/java/org/athlium/auth/infrastructure/security/FirebaseAuthFilter.java` (lines 136-137, 150-151)
- Cause: No caching layer. Custom JWT tokens already contain `userId` and `roles` in claims, but the filter still hits the DB to "enrich" user data.
- Improvement path: For custom JWT tokens, trust the embedded claims (userId, roles, email) and skip the DB call. Alternatively, add a short-lived cache (30s-60s) for user data keyed by firebaseUid.

**Pessimistic Locking Under Load:**
- Problem: `findByIdForUpdate()` (SELECT ... FOR UPDATE) is used in booking creation, booking cancellation, session updates, and client package credit operations. Under concurrent load, these locks serialize all operations on the same row.
- Files: `src/main/java/org/athlium/bookings/infrastructure/repository/BookingRepositoryImpl.java`, `src/main/java/org/athlium/clients/infrastructure/repository/ClientPackageRepositoryImpl.java`, `src/main/java/org/athlium/gym/infrastructure/repository/SessionInstanceRepositoryImpl.java`
- Cause: Correct use of pessimistic locking for consistency, but no consideration of lock contention at scale.
- Improvement path: For read-heavy operations, consider optimistic locking with `@Version`. Reserve pessimistic locks only for the credit-consume-and-book transaction.

**Separate COUNT Query on Every Pagination:**
- Problem: Every paginated endpoint executes a separate `query.count()` SQL call in addition to the data query. For large tables, COUNT(*) can be slow.
- Files: `src/main/java/org/athlium/bookings/infrastructure/repository/BookingRepositoryImpl.java`, `src/main/java/org/athlium/gym/infrastructure/repository/SessionInstanceRepositoryImpl.java`
- Cause: Standard Panache pagination pattern.
- Improvement path: Consider cursor-based pagination for high-volume endpoints. Alternatively, cache counts or use estimated counts for large datasets.

## Fragile Areas

**Booking Cancellation with Waitlist Promotion:**
- Files: `src/main/java/org/athlium/bookings/application/usecase/CancelBookingUseCase.java`
- Why fragile: Single transaction does cancellation + credit refund + waitlist lookup + credit consumption + promotion. If any step fails (e.g., waitlisted user has no credits), the entire transaction rolls back, including the cancellation itself. The user's booking remains confirmed even though they wanted to cancel.
- Safe modification: Split into two transactions: (1) cancel + refund, (2) promote from waitlist. If promotion fails, the cancellation still succeeds. Use domain events to trigger promotion asynchronously.
- Test coverage: No tests exist for `CancelBookingUseCase`.

**Session Generation Scheduler:**
- Files: `src/main/java/org/athlium/gym/infrastructure/scheduler/WeeklySessionScheduler.java`, `src/main/java/org/athlium/gym/application/usecase/GenerateNextWeekSessionsUseCase.java`
- Why fragile: Catches exceptions per-schedule but has no retry mechanism, no dead-letter tracking, and no alerting. If a schedule fails to generate sessions, it silently logs the error and moves on. Missing sessions won't be detected until users complain.
- Safe modification: Add a `session_generation_log` table tracking success/failure per schedule. Add health check endpoint that reports last successful generation run.
- Test coverage: No tests exist for the scheduler or generation use case.

**Client Package Credit Service:**
- Files: `src/main/java/org/athlium/clients/application/service/ClientPackageCreditService.java`
- Why fragile: Manages token/credit accounting across multiple tables (`client_packages`, `client_package_credits`). Uses pessimistic locks and multiple queries in a single transaction. Logic for "find package with available credits for activity" involves joining multiple entities.
- Safe modification: Always wrap credit operations in explicit transactions. Add invariant checks (credits >= 0) as database CHECK constraints.
- Test coverage: No tests exist for `ClientPackageCreditService`.

**GlobalExceptionMapper Swallows Specific Mappers:**
- Files: `src/main/java/org/athlium/shared/exception/GlobalExceptionMapper.java`
- Why fragile: Catches `Exception` (the root class) which may intercept exceptions that should be handled by more specific mappers (e.g., `ConstraintViolationException`, `NotFoundException`). JAX-RS mapper resolution can be unpredictable when both a specific and generic mapper exist.
- Safe modification: Either remove the global mapper and let Quarkus handle unknown exceptions, or change it to catch a custom base exception class. Add specific mappers for `ConstraintViolationException` (400), `NotFoundException` (404), `WebApplicationException` (pass-through).
- Test coverage: No tests.

## Scaling Limits

**Single Scheduler Instance:**
- Current capacity: One Quarkus instance runs the `@Scheduled` weekly session generator and daily package expiration checker.
- Limit: If multiple instances are deployed (horizontal scaling), all instances will run the scheduler simultaneously, causing duplicate sessions or double-processing.
- Scaling path: Use Quarkus distributed scheduling (quartz with a JDBC store) or a distributed lock (e.g., `@Lock` with ShedLock or database advisory locks).

**MongoDB Single Collection:**
- Current capacity: One `session_configurations` collection for all organizations.
- Limit: As organizations grow, queries by `organizationId`/`headquartersId`/`activityId` will slow without proper indexes.
- Scaling path: Ensure compound indexes exist on the hierarchy fields. Consider if MongoDB is even needed (see Tech Debt above).

## Dependencies at Risk

**JJWT 0.11.5 (Deprecated API):**
- Risk: Using deprecated `Jwts.parser()` and `setSigningKey()` methods that will be removed in 0.12.x.
- Impact: Upgrade will require code changes in `CustomJwtValidator`.
- Migration plan: Update to JJWT 0.12.x+, use `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)`.

**Google Cloud Firestore 3.21.0 (Unused):**
- Risk: Firestore dependency is declared in `pom.xml` but not used anywhere in the codebase (only Firebase Auth is used). It adds unnecessary transitive dependencies and increases build/deploy size.
- Impact: Slower builds, larger artifact, potential dependency conflicts.
- Migration plan: Remove `google-cloud-firestore` dependency from `pom.xml` (line 118-120).

## Missing Critical Features

**Multi-Tenancy / Data Isolation:**
- Problem: No data isolation between organizations. All queries operate on global tables without filtering by organization. A user with the right ID could access any organization's data.
- Blocks: Cannot safely serve multiple gyms/organizations from the same deployment.

**Authorization (Beyond Authentication):**
- Problem: Even on the 3 controllers that have `@Authenticated`, there is no check that the authenticated user belongs to the organization whose data they're accessing. A logged-in user of Organization A could manipulate Organization B's data.
- Blocks: Safe multi-tenant operation, role-based data access.

**Audit Logging:**
- Problem: No audit trail for critical operations (booking creation/cancellation, payment recording, user deactivation).
- Blocks: Compliance, dispute resolution, debugging production issues.

## Test Coverage Gaps

**Booking Use Cases (Zero Coverage):**
- What's not tested: `CreateBookingUseCase`, `CancelBookingUseCase` — the most critical business logic including credit consumption, pessimistic locking, idempotency, and waitlist promotion.
- Files: `src/main/java/org/athlium/bookings/application/usecase/CreateBookingUseCase.java`, `src/main/java/org/athlium/bookings/application/usecase/CancelBookingUseCase.java`
- Risk: Booking/cancellation bugs will go undetected. The waitlist promotion bug (throws instead of skipping) exists because there are no tests.
- Priority: **High**

**Client Package Credit Service (Zero Coverage):**
- What's not tested: Credit consumption, refund, availability check, package expiration.
- Files: `src/main/java/org/athlium/clients/application/service/ClientPackageCreditService.java`
- Risk: Financial accounting errors (double-charging, negative credits, refund to wrong package).
- Priority: **High**

**Auth Security Filter (Zero Coverage):**
- What's not tested: The entire authentication flow — token validation dispatch, role checking, public endpoint bypass, optional authentication.
- Files: `src/main/java/org/athlium/auth/infrastructure/security/FirebaseAuthFilter.java`
- Risk: Auth bypass vulnerabilities. The "public by default" behavior (line 68-71) is a security-critical design choice that should be tested.
- Priority: **High**

**Schedulers (Zero Coverage):**
- What's not tested: Weekly session generation, client package expiration.
- Files: `src/main/java/org/athlium/gym/infrastructure/scheduler/WeeklySessionScheduler.java`, `src/main/java/org/athlium/clients/infrastructure/scheduler/ClientPackageExpirationScheduler.java`
- Risk: Silent failures in automated processes. Missing sessions, expired packages not deactivated.
- Priority: **Medium**

**Overall Coverage: ~11% (26 test files for 227 source files)**
- Most tests appear to be auto-generated Quarkus resource tests (smoke tests), not business logic tests.
- No integration tests for the PostgreSQL + MongoDB interaction.
- No tests for any mapper (MapStruct generates code, but mapping correctness is untested).

---

*Concerns audit: 2026-03-04*
