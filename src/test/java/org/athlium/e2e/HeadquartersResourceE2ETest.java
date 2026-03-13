package org.athlium.e2e;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.model.SessionSource;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.infrastructure.entity.ActivityEntity;
import org.athlium.gym.infrastructure.entity.SessionInstanceEntity;
import org.athlium.gym.infrastructure.repository.ActivityPanacheRepository;
import org.athlium.bookings.infrastructure.repository.BookingPanacheRepository;
import org.athlium.gym.infrastructure.repository.HeadquartersPanacheRepository;
import org.athlium.gym.infrastructure.repository.OrganizationPanacheRepository;
import org.athlium.gym.infrastructure.repository.SessionInstancePanacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class HeadquartersResourceE2ETest {

    private static final String ADMIN_TOKEN = "hq-admin-e2e";
    private static final String CLIENT_TOKEN = "hq-client-e2e";

    @Inject
    HeadquartersPanacheRepository headquartersRepository;

    @Inject
    OrganizationPanacheRepository organizationRepository;

    @Inject
    BookingPanacheRepository bookingRepository;

    @Inject
    SessionInstancePanacheRepository sessionInstanceRepository;

    @Inject
    ActivityPanacheRepository activityRepository;

    @Inject
    jakarta.persistence.EntityManager entityManager;

    @BeforeEach
    void setUp() {
        cleanData();
        ensureUserWithRoles(1L, ADMIN_TOKEN, "ORG_ADMIN");
        ensureUserWithRoles(2L, CLIENT_TOKEN, "CLIENT");
    }

    @Test
    void shouldAllowClientRoleToListHeadquarters() {
        Long organizationId = createOrganization("Org For HQ List");
        Long headquartersId = createHeadquarters(organizationId, "HQ Visible To Client");
        Long activityId = createActivity(headquartersId, "Crossfit", "Intense class");
        createSession(organizationId, headquartersId, activityId, Instant.parse("2026-03-20T10:00:00Z"));

        given()
                .header("Authorization", bearer(CLIENT_TOKEN))
                .when()
                .get("/api/headquarters")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.size()", greaterThanOrEqualTo(1))
                .body("data[0].activities.size()", greaterThanOrEqualTo(1))
                .body("data[0].activities[0].sessions.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void shouldAllowClientRoleToGetHeadquarterById() {
        Long organizationId = createOrganization("Org For HQ Detail");
        Long headquartersId = createHeadquarters(organizationId, "HQ Detail For Client");
        Long activityId = createActivity(headquartersId, "Pilates", "Core");
        createSession(organizationId, headquartersId, activityId, Instant.parse("2026-03-20T11:00:00Z"));

        given()
                .header("Authorization", bearer(CLIENT_TOKEN))
                .when()
                .get("/api/headquarters/{id}", headquartersId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", equalTo(headquartersId.intValue()))
                .body("data.name", equalTo("HQ Detail For Client"))
                .body("data.activities.size()", greaterThanOrEqualTo(1))
                .body("data.activities[0].sessions.size()", greaterThanOrEqualTo(1));
    }

    @Transactional
    Long createActivity(Long headquartersId, String name, String description) {
        ActivityEntity entity = new ActivityEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setIsActive(true);
        entity.setHqId(headquartersId);
        activityRepository.persist(entity);
        return entity.id;
    }

    @Transactional
    Long createSession(Long organizationId, Long headquartersId, Long activityId, Instant startsAt) {
        SessionInstanceEntity entity = new SessionInstanceEntity();
        entity.setOrganizationId(organizationId);
        entity.setHeadquartersId(headquartersId);
        entity.setActivityId(activityId);
        entity.setStartsAt(startsAt);
        entity.setEndsAt(startsAt.plusSeconds(3600));
        entity.setStatus(SessionStatus.OPEN);
        entity.setSource(SessionSource.MANUAL);
        sessionInstanceRepository.persist(entity);
        return entity.id;
    }

    private Long createOrganization(String name) {
        JsonPath response = given()
                .header("Authorization", bearer(ADMIN_TOKEN))
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
                .header("Authorization", bearer(ADMIN_TOKEN))
                .contentType("application/json")
                .body(Map.of("organizationId", organizationId, "name", name))
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

    @Transactional
    void cleanData() {
        bookingRepository.deleteAll();
        sessionInstanceRepository.deleteAll();
        entityManager.createNativeQuery("DELETE FROM activity").executeUpdate();
        headquartersRepository.deleteAll();
        organizationRepository.deleteAll();
        entityManager.createNativeQuery("DELETE FROM user_roles").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
    }

    @Transactional
    void ensureUserWithRoles(Long userId, String token, String... roles) {
        entityManager.createNativeQuery("INSERT INTO users (id, active, email, firebase_uid, last_name, name) VALUES (?, true, ?, ?, 'Last', 'Name') ON CONFLICT DO NOTHING")
                .setParameter(1, userId)
                .setParameter(2, "user" + userId + "@test.com")
                .setParameter(3, mockUid(token))
                .executeUpdate();

        for (String role : roles) {
            entityManager.createNativeQuery("INSERT INTO user_roles (user_id, role) VALUES (?, ?) ON CONFLICT DO NOTHING")
                    .setParameter(1, userId)
                    .setParameter(2, role)
                    .executeUpdate();
        }
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String mockUid(String token) {
        String normalizedToken = token.length() > 20 ? token.substring(0, 20) : token;
        return "mock-" + normalizedToken;
    }
}
