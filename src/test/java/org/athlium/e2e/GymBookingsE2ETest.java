package org.athlium.e2e;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.athlium.bookings.infrastructure.repository.BookingPanacheRepository;
import org.athlium.gym.infrastructure.repository.ActivityConfigPanacheRepository;
import org.athlium.gym.infrastructure.repository.ActivityPanacheRepository;
import org.athlium.gym.infrastructure.repository.ActivitySchedulePanacheRepository;
import org.athlium.gym.infrastructure.repository.HeadquartersConfigPanacheRepository;
import org.athlium.gym.infrastructure.repository.HeadquartersPanacheRepository;
import org.athlium.gym.infrastructure.repository.OrganizationConfigPanacheRepository;
import org.athlium.gym.infrastructure.repository.OrganizationPanacheRepository;
import org.athlium.gym.infrastructure.repository.SessionConfigPanacheRepository;
import org.athlium.gym.infrastructure.repository.SessionInstancePanacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class GymBookingsE2ETest {

    @Inject
    BookingPanacheRepository bookingRepository;

    @Inject
    SessionInstancePanacheRepository sessionInstanceRepository;

    @Inject
    ActivitySchedulePanacheRepository activityScheduleRepository;

    @Inject
    ActivityPanacheRepository activityRepository;

    @Inject
    HeadquartersPanacheRepository headquartersRepository;

    @Inject
    OrganizationPanacheRepository organizationRepository;

    @Inject
    EntityManager entityManager;

    @Inject
    OrganizationConfigPanacheRepository organizationConfigRepository;

    @Inject
    HeadquartersConfigPanacheRepository headquartersConfigRepository;

    @Inject
    ActivityConfigPanacheRepository activityConfigRepository;

    @Inject
    SessionConfigPanacheRepository sessionConfigRepository;

    @BeforeEach
    void setUp() {
        cleanMongo();
        cleanRelational();
    }

    @Test
    void shouldRunGymToBookingsFlowWithWaitlistPromotionAndIdempotency() {
        Long organizationId = createOrganization("Gym E2E");
        Long headquartersId = createHeadquarters(organizationId, "HQ E2E");
        Long activityId = createActivity(headquartersId, "Cross E2E", "Clase e2e");

        GymIds gymIds = new GymIds(organizationId, headquartersId, activityId);
        organizationId = gymIds.organizationId();
        headquartersId = gymIds.headquartersId();
        activityId = gymIds.activityId();

        given()
                .contentType("application/json")
                .body(Map.of(
                        "maxParticipants", 2,
                        "waitlistEnabled", true,
                        "waitlistMaxSize", 3,
                        "waitlistStrategy", "FIFO",
                        "cancellationMinHoursBeforeStart", 2,
                        "cancellationAllowLateCancel", true
                ))
                .when()
                .put("/api/gym/config/activities/{activityId}", activityId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true));

        given()
                .contentType("application/json")
                .body(Map.of(
                        "organizationId", organizationId,
                        "headquartersId", headquartersId,
                        "activityId", activityId,
                        "dayOfWeek", 1,
                        "startTime", "18:00",
                        "durationMinutes", 60,
                        "active", true
                ))
                .when()
                .post("/api/activity-schedules")
                .then()
                .statusCode(201)
                .body("success", equalTo(true));

        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/api/activity-schedules/generate-next-week")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .log().all();

        JsonPath sessionList = given()
                .queryParam("organizationId", organizationId)
                .queryParam("headquartersId", headquartersId)
                .queryParam("activityId", activityId)
                .queryParam("page", 1)
                .queryParam("limit", 20)
                .when()
                .get("/api/sessions")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.items[0].id", notNullValue())
                .extract()
                .jsonPath();

        Long sessionId = sessionList.getLong("data.items[0].id");
        assertNotNull(sessionId);

        grantCredits(101L, activityId, 10);
        grantCredits(102L, activityId, 10);
        grantCredits(103L, activityId, 10);

        Long booking1 = createBooking(sessionId, 101L, "e2e-bk-1");
        Long booking2 = createBooking(sessionId, 102L, "e2e-bk-2");
        Long booking3 = createBooking(sessionId, 103L, "e2e-bk-3");

        JsonPath listBeforeCancel = given()
                .queryParam("sessionId", sessionId)
                .queryParam("sort", "createdAt:asc")
                .when()
                .get("/api/bookings")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract()
                .jsonPath();

        assertEquals("CONFIRMED", listBeforeCancel.getString("data.items[0].status"));
        assertEquals("CONFIRMED", listBeforeCancel.getString("data.items[1].status"));
        assertEquals("WAITLISTED", listBeforeCancel.getString("data.items[2].status"));

        JsonPath cancelResult = given()
                .contentType("application/json")
                .header("Idempotency-Key", "e2e-cancel-1")
                .body("{}")
                .when()
                .post("/api/bookings/{bookingId}/cancel", booking1)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.cancelledBooking.status", equalTo("CANCELLED"))
                .body("data.promotedBooking.status", equalTo("CONFIRMED"))
                .extract()
                .jsonPath();

        Long promotedId = cancelResult.getLong("data.promotedBooking.id");
        assertEquals(booking3, promotedId);

        given()
                .contentType("application/json")
                .header("Idempotency-Key", "e2e-cancel-1")
                .body("{}")
                .when()
                .post("/api/bookings/{bookingId}/cancel", booking1)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.cancelledBooking.id", equalTo(booking1.intValue()))
                .body("data.promotedBooking.id", equalTo(promotedId.intValue()));

        JsonPath listAfterCancel = given()
                .queryParam("sessionId", sessionId)
                .queryParam("sort", "createdAt:asc")
                .when()
                .get("/api/bookings")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract()
                .jsonPath();

        assertEquals("CANCELLED", listAfterCancel.getString("data.items[0].status"));
        assertEquals("CONFIRMED", listAfterCancel.getString("data.items[1].status"));
        assertEquals("CONFIRMED", listAfterCancel.getString("data.items[2].status"));

        assertEquals(booking2.intValue(), listAfterCancel.getInt("data.items[1].id"));
        assertEquals(booking3.intValue(), listAfterCancel.getInt("data.items[2].id"));
    }

    @Test
    void shouldAllowOnlyOneActiveBookingPerUserUnderConcurrentRequests() throws Exception {
        Long organizationId = createOrganization("Gym Concurrency");
        Long headquartersId = createHeadquarters(organizationId, "HQ Concurrency");
        Long activityId = createActivity(headquartersId, "Cross Concurrent", "Clase concurrency");

        given()
                .contentType("application/json")
                .body(Map.of(
                        "maxParticipants", 2,
                        "waitlistEnabled", true,
                        "waitlistMaxSize", 2,
                        "waitlistStrategy", "FIFO",
                        "cancellationMinHoursBeforeStart", 2,
                        "cancellationAllowLateCancel", true
                ))
                .when()
                .put("/api/gym/config/activities/{activityId}", activityId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true));

        given()
                .contentType("application/json")
                .body(Map.of(
                        "organizationId", organizationId,
                        "headquartersId", headquartersId,
                        "activityId", activityId,
                        "dayOfWeek", 1,
                        "startTime", "20:00",
                        "durationMinutes", 60,
                        "active", true,
                        "activeFrom", "2024-01-01",
                        "activeUntil", "2030-01-01"
                ))
                .when()
                .post("/api/activity-schedules")
                .then()
                .statusCode(201)
                .body("success", equalTo(true));

        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/api/activity-schedules/generate-next-week")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .log().all();

        Long sessionId = given()
                .queryParam("organizationId", organizationId)
                .queryParam("headquartersId", headquartersId)
                .queryParam("activityId", activityId)
                .queryParam("page", 1)
                .queryParam("limit", 20)
                .when()
                .get("/api/sessions")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract()
                .jsonPath()
                .getLong("data.items[0].id");

        grantCredits(999L, activityId, 10);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> firstCreate = () -> given()
                    .contentType("application/json")
                    .header("Idempotency-Key", "concurrent-a")
                    .body(Map.of("userId", 999L))
                    .when()
                    .post("/api/sessions/{sessionId}/bookings", sessionId)
                    .then()
                    .extract()
                    .statusCode();

            Callable<Integer> secondCreate = () -> given()
                    .contentType("application/json")
                    .header("Idempotency-Key", "concurrent-b")
                    .body(Map.of("userId", 999L))
                    .when()
                    .post("/api/sessions/{sessionId}/bookings", sessionId)
                    .then()
                    .extract()
                    .statusCode();

            List<Future<Integer>> futures = executor.invokeAll(List.of(firstCreate, secondCreate));
            int statusA = futures.get(0).get(10, TimeUnit.SECONDS);
            int statusB = futures.get(1).get(10, TimeUnit.SECONDS);

            List<Integer> statuses = List.of(statusA, statusB);
            long createdCount = statuses.stream().filter(code -> code == 201).count();
            long rejectedCount = statuses.stream().filter(code -> code == 400).count();

            assertEquals(1L, createdCount);
            assertEquals(1L, rejectedCount);
        } finally {
            executor.shutdownNow();
        }
    }

    private Long createBooking(Long sessionId, Long userId, String idempotencyKey) {
        JsonPath response = given()
                .contentType("application/json")
                .header("Idempotency-Key", idempotencyKey)
                .body(Map.of("userId", userId))
                .when()
                .post("/api/sessions/{sessionId}/bookings", sessionId)
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("data.id", notNullValue())
                .extract()
                .jsonPath();

        return response.getLong("data.id");
    }

    @Transactional
    void insertUserIfAbsent(Long userId) {
        try {
            entityManager.createNativeQuery("INSERT INTO payments (id, amount, method, paid_at) VALUES (1, 100, 'CASH', CURRENT_DATE) ON CONFLICT DO NOTHING").executeUpdate();
            entityManager.createNativeQuery("INSERT INTO users (id, active, email, firebase_uid, last_name, name) VALUES (?, true, ?, ?, 'Last', 'Name') ON CONFLICT DO NOTHING")
                    .setParameter(1, userId)
                    .setParameter(2, "user" + userId + "@test.com")
                    .setParameter(3, "mock-user-" + userId)
                    .executeUpdate();
        } catch (Exception e) {}
    }

    private void grantCredits(Long userId, Long activityId, int tokens) {
        insertUserIfAbsent(userId);

        given()
                .contentType("application/json")
                .body(Map.of(
                        "paymentId", 1,
                        "activityTokens", Map.of(activityId.toString(), tokens)
                ))
                .when()
                .post("/api/clients/{userId}/packages", userId)
                .then()
                .statusCode(201)
                .body("success", equalTo(true));
    }

    @Transactional
    void cleanRelational() {
        bookingRepository.deleteAll();
        sessionInstanceRepository.deleteAll();
        entityManager.createNativeQuery("DELETE FROM activity").executeUpdate();
        headquartersRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    private Long createOrganization(String name) {
        JsonPath response = given()
                .contentType("application/json")
                .body(Map.of("name", name))
                .when()
                .post("/api/organizations")
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("data.id", notNullValue())
                .extract()
                .jsonPath();

        return response.getLong("data.id");
    }

    private Long createHeadquarters(Long organizationId, String name) {
        JsonPath response = given()
                .contentType("application/json")
                .body(Map.of(
                        "organizationId", organizationId,
                        "name", name
                ))
                .when()
                .post("/api/headquarters")
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("data.id", notNullValue())
                .extract()
                .jsonPath();

        return response.getLong("data.id");
    }

    private Long createActivity(Long headquartersId, String name, String description) {
        JsonPath response = given()
                .contentType("application/json")
                .body(Map.of(
                        "name", name,
                        "description", description,
                        "hqId", headquartersId
                ))
                .when()
                .post("/api/activities")
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("data.id", notNullValue())
                .extract()
                .jsonPath();

        return response.getLong("data.id");
    }

    void cleanMongo() {
        activityScheduleRepository.deleteAll();
        sessionConfigRepository.deleteAll();
        activityConfigRepository.deleteAll();
        headquartersConfigRepository.deleteAll();
        organizationConfigRepository.deleteAll();
    }

    private record GymIds(Long organizationId, Long headquartersId, Long activityId) {
    }
}
