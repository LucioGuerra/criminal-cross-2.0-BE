package org.athlium.e2e;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.athlium.bookings.infrastructure.repository.BookingPanacheRepository;
import org.athlium.gym.infrastructure.repository.ActivityPanacheRepository;
import org.athlium.gym.infrastructure.repository.HeadquartersPanacheRepository;
import org.athlium.gym.infrastructure.repository.OrganizationPanacheRepository;
import org.athlium.gym.infrastructure.repository.SessionInstancePanacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestProfile(ActivityResourceSecurityE2ETest.NoBypassAuthProfile.class)
class ActivityResourceSecurityE2ETest {

    private static final String ADMIN_TOKEN = "activity-admin-e2e";
    private static final String PROFESSOR_TOKEN = "activity-professor-e2e";
    private static final String CLIENT_TOKEN = "activity-client-e2e";

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
    EntityManager entityManager;

    @BeforeEach
    void setUp() {
        cleanRelational();
        ensureUserWithRoles(1L, ADMIN_TOKEN, "ORG_ADMIN");
        ensureUserWithRoles(2L, PROFESSOR_TOKEN, "PROFESSOR");
        ensureUserWithRoles(3L, CLIENT_TOKEN, "CLIENT");
    }

    @Test
    void shouldAllowProfessorToCreateActivityAndDenyClient() {
        Long organizationId = createOrganization("Activity Security Org");
        Long headquartersId = createHeadquarters(organizationId, "Activity Security HQ");

        given()
                .header("Authorization", bearer(PROFESSOR_TOKEN))
                .contentType("application/json")
                .body(Map.of(
                        "name", "Professor Activity",
                        "description", "Created by professor",
                        "hqId", headquartersId
                ))
                .when()
                .post("/api/activities")
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .body("data.id", notNullValue());

        given()
                .header("Authorization", bearer(CLIENT_TOKEN))
                .contentType("application/json")
                .body(Map.of(
                        "name", "Client Activity",
                        "description", "Should be forbidden",
                        "hqId", headquartersId
                ))
                .when()
                .post("/api/activities")
                .then()
                .statusCode(403)
                .body("success", equalTo(false));
    }

    @Transactional
    void cleanRelational() {
        bookingRepository.deleteAll();
        sessionInstanceRepository.deleteAll();
        activityRepository.deleteAll();
        headquartersRepository.deleteAll();
        organizationRepository.deleteAll();
        entityManager.createNativeQuery("DELETE FROM user_headquarters").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM user_roles").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
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
