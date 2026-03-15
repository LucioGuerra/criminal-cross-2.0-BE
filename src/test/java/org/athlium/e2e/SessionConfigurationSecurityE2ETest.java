package org.athlium.e2e;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.athlium.bookings.infrastructure.repository.BookingPanacheRepository;
import org.athlium.gym.infrastructure.repository.ActivityConfigPanacheRepository;
import org.athlium.gym.infrastructure.repository.ActivityPanacheRepository;
import org.athlium.gym.infrastructure.repository.HeadquartersConfigPanacheRepository;
import org.athlium.gym.infrastructure.repository.HeadquartersPanacheRepository;
import org.athlium.gym.infrastructure.repository.OrganizationConfigPanacheRepository;
import org.athlium.gym.infrastructure.repository.OrganizationPanacheRepository;
import org.athlium.gym.infrastructure.repository.SessionConfigPanacheRepository;
import org.athlium.gym.infrastructure.repository.SessionInstancePanacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestProfile(SessionConfigurationSecurityE2ETest.NoBypassAuthProfile.class)
class SessionConfigurationSecurityE2ETest {

    private static final String ADMIN_TOKEN = "cfg-admin-e2e";
    private static final String PROFESSOR_TOKEN = "cfg-professor-e2e";
    private static final String CLIENT_TOKEN = "cfg-client-e2e";

    @Inject
    BookingPanacheRepository bookingRepository;

    @Inject
    SessionInstancePanacheRepository sessionInstanceRepository;

    @Inject
    ActivityPanacheRepository activityRepository;

    @Inject
    HeadquartersPanacheRepository headquartersRepository;

    @Inject
    OrganizationPanacheRepository organizationRepository;

    @Inject
    OrganizationConfigPanacheRepository organizationConfigRepository;

    @Inject
    HeadquartersConfigPanacheRepository headquartersConfigRepository;

    @Inject
    ActivityConfigPanacheRepository activityConfigRepository;

    @Inject
    SessionConfigPanacheRepository sessionConfigRepository;

    @Inject
    EntityManager entityManager;

    @BeforeEach
    void setUp() {
        cleanMongo();
        cleanRelational();
        ensureUserWithRoles(1L, ADMIN_TOKEN, "ORG_ADMIN");
        ensureUserWithRoles(2L, PROFESSOR_TOKEN, "PROFESSOR");
        ensureUserWithRoles(3L, CLIENT_TOKEN, "CLIENT");
    }

    @Test
    void shouldAllowProfessorToUpsertActivityConfigAndKeepOtherRestrictions() {
        Long organizationId = createOrganization("Config Security Org");
        Long headquartersId = createHeadquarters(organizationId, "Config Security HQ");
        Long activityId = createActivity(headquartersId, "Config Security Activity", "Config security test");

        given()
                .header("Authorization", bearer(PROFESSOR_TOKEN))
                .contentType("application/json")
                .body(Map.of(
                        "maxParticipants", 15,
                        "waitlistEnabled", true,
                        "waitlistMaxSize", 6,
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
                .header("Authorization", bearer(PROFESSOR_TOKEN))
                .contentType("application/json")
                .body(Map.of("maxParticipants", 20))
                .when()
                .put("/api/gym/config/organizations/{organizationId}", organizationId)
                .then()
                .statusCode(403)
                .body("success", equalTo(false));

        given()
                .header("Authorization", bearer(CLIENT_TOKEN))
                .contentType("application/json")
                .body(Map.of("maxParticipants", 20))
                .when()
                .put("/api/gym/config/activities/{activityId}", activityId)
                .then()
                .statusCode(403)
                .body("success", equalTo(false));
    }

    @Transactional
    void cleanRelational() {
        bookingRepository.deleteAll();
        sessionInstanceRepository.deleteAll();
        entityManager.createNativeQuery("DELETE FROM activity").executeUpdate();
        headquartersRepository.deleteAll();
        organizationRepository.deleteAll();
        entityManager.createNativeQuery("DELETE FROM user_headquarters").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM user_roles").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
    }

    void cleanMongo() {
        sessionConfigRepository.deleteAll();
        activityConfigRepository.deleteAll();
        headquartersConfigRepository.deleteAll();
        organizationConfigRepository.deleteAll();
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

    private Long createActivity(Long headquartersId, String name, String description) {
        JsonPath response = given()
                .header("Authorization", bearer(ADMIN_TOKEN))
                .contentType("application/json")
                .body(Map.of("name", name, "description", description, "hqId", headquartersId))
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

    public static class NoBypassAuthProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Collections.singletonMap("auth.dev-bypass.enabled", "false");
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
