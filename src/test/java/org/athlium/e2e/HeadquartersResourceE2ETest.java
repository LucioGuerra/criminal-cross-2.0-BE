package org.athlium.e2e;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.bookings.infrastructure.repository.BookingPanacheRepository;
import org.athlium.gym.infrastructure.repository.HeadquartersPanacheRepository;
import org.athlium.gym.infrastructure.repository.OrganizationPanacheRepository;
import org.athlium.gym.infrastructure.repository.SessionInstancePanacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        createHeadquarters(organizationId, "HQ Visible To Client");

        given()
                .header("Authorization", bearer(CLIENT_TOKEN))
                .when()
                .get("/api/headquarters")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void shouldAllowClientRoleToGetHeadquarterById() {
        Long organizationId = createOrganization("Org For HQ Detail");
        Long headquartersId = createHeadquarters(organizationId, "HQ Detail For Client");

        given()
                .header("Authorization", bearer(CLIENT_TOKEN))
                .when()
                .get("/api/headquarters/{id}", headquartersId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", equalTo(headquartersId.intValue()))
                .body("data.name", equalTo("HQ Detail For Client"));
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
