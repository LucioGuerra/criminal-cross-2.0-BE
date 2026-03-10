package org.athlium.e2e;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.infrastructure.repository.OrganizationPanacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class OrganizationResourceE2ETest {

    private static final String ADMIN_TOKEN = "org-admin-e2e";
    private static final String CLIENT_TOKEN = "org-client-e2e";

    @Inject
    OrganizationPanacheRepository organizationRepository;

    @Inject
    org.athlium.gym.infrastructure.repository.HeadquartersPanacheRepository headquartersRepository;

    @Inject
    jakarta.persistence.EntityManager entityManager;

    @Inject
    org.athlium.bookings.infrastructure.repository.BookingPanacheRepository bookingRepository;

    @Inject
    org.athlium.gym.infrastructure.repository.SessionInstancePanacheRepository sessionInstanceRepository;

    @BeforeEach
    void setUp() {
        cleanOrganizations();
        ensureUserWithRoles(1L, ADMIN_TOKEN, "ORG_ADMIN");
        ensureUserWithRoles(2L, CLIENT_TOKEN, "CLIENT");
    }

    @Test
    void shouldUpdateOrganizationName() {
        Long organizationId = createOrganization("Org Original");

        given()
                .header("Authorization", bearer(ADMIN_TOKEN))
                .contentType("application/json")
                .body(Map.of("name", "Org Actualizada"))
                .when()
                .put("/api/organizations/{id}", organizationId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", equalTo(organizationId.intValue()))
                .body("data.name", equalTo("Org Actualizada"));

        given()
                .header("Authorization", bearer(ADMIN_TOKEN))
                .when()
                .get("/api/organizations/{id}", organizationId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", equalTo(organizationId.intValue()))
                .body("data.name", equalTo("Org Actualizada"));
    }

    @Test
    void shouldRequireAuthenticationForOrganizationCreation() {
        given()
                .contentType("application/json")
                .body(Map.of("name", "Org Sin Auth"))
                .when()
                .post("/api/organizations")
                .then()
                .statusCode(401)
                .body("success", equalTo(false));
    }

    @Test
    void shouldRejectClientRoleForOrganizationCreation() {
        given()
                .header("Authorization", bearer(CLIENT_TOKEN))
                .contentType("application/json")
                .body(Map.of("name", "Org Cliente"))
                .when()
                .post("/api/organizations")
                .then()
                .statusCode(403)
                .body("success", equalTo(false));
    }

    @Test
    void shouldAllowClientRoleToListOrganizations() {
        createOrganization("Org Visible To Client");

        given()
                .header("Authorization", bearer(CLIENT_TOKEN))
                .when()
                .get("/api/organizations")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.size()", greaterThanOrEqualTo(1));
    }

    @Transactional
    void cleanOrganizations() {
        bookingRepository.deleteAll();
        sessionInstanceRepository.deleteAll();
        entityManager.createNativeQuery("DELETE FROM activity").executeUpdate();
        headquartersRepository.deleteAll();
        organizationRepository.deleteAll();
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
