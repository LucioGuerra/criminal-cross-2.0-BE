package org.athlium.e2e;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@QuarkusTest
class UserManagementE2ETest {

    private static final String ADMIN_TOKEN = "users-admin-e2e";
    private static final String ADMIN_UID = "mock-users-admin-e2e";
    private static final String TARGET_UID = "mock-users-target-e2e";

    @Inject
    EntityManager entityManager;

    @BeforeEach
    void setUp() {
        cleanData();
        ensureOrganizationAndHeadquarters();
        ensureUserWithRoles(1001L, ADMIN_UID, "admin@test.com", "ORG_ADMIN");
        ensureUserWithRoles(1002L, TARGET_UID, "target@test.com", "CLIENT");
    }

    @Test
    void shouldUpdateUserEditableFields() {
        given()
                .header("Authorization", bearer(ADMIN_TOKEN))
                .contentType("application/json")
                .body(Map.of(
                        "email", "target-updated@test.com",
                        "name", "Target Updated",
                        "lastName", "User Updated",
                        "active", false))
                .when()
                .put("/api/users/firebase/{uid}", TARGET_UID)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.email", equalTo("target-updated@test.com"))
                .body("data.name", equalTo("Target Updated"))
                .body("data.lastName", equalTo("User Updated"))
                .body("data.active", equalTo(false));
    }

    @Test
    void shouldAssignAndUnassignUserToHeadquarters() {
        given()
                .header("Authorization", bearer(ADMIN_TOKEN))
                .contentType("application/json")
                .when()
                .post("/api/users/firebase/{uid}/headquarters/{headquartersId}", TARGET_UID, 100L)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.headquartersIds", hasItem(100));

        given()
                .header("Authorization", bearer(ADMIN_TOKEN))
                .contentType("application/json")
                .when()
                .delete("/api/users/firebase/{uid}/headquarters/{headquartersId}", TARGET_UID, 100L)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.headquartersIds", not(hasItem(100)));
    }

    @Test
    void shouldFilterUsersByAssignedHeadquarters() {
        given()
                .header("Authorization", bearer(ADMIN_TOKEN))
                .contentType("application/json")
                .when()
                .post("/api/users/firebase/{uid}/headquarters/{headquartersId}", TARGET_UID, 100L)
                .then()
                .statusCode(200);

        given()
                .header("Authorization", bearer(ADMIN_TOKEN))
                .queryParam("headquartersId", 100L)
                .when()
                .get("/api/users")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.totalElements", equalTo(1))
                .body("data.content[0].email", equalTo("target@test.com"))
                .body("data.content[0].headquartersIds", hasItem(100));
    }

    @Transactional
    void cleanData() {
        entityManager.createNativeQuery("DELETE FROM bookings").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM session_instances").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM client_package_credits").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM client_packages").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM activity").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM user_headquarters").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM user_roles").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM headquarters").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM organizations").executeUpdate();
    }

    @Transactional
    void ensureOrganizationAndHeadquarters() {
        entityManager.createNativeQuery("INSERT INTO organizations (id, name) VALUES (10, 'Org Test') ON CONFLICT DO NOTHING")
                .executeUpdate();

        entityManager.createNativeQuery("INSERT INTO headquarters (id, organization_id, name) VALUES (100, 10, 'Sede Centro') ON CONFLICT DO NOTHING")
                .executeUpdate();
    }

    @Transactional
    void ensureUserWithRoles(Long userId, String uid, String email, String... roles) {
        entityManager.createNativeQuery("INSERT INTO users (id, active, email, firebase_uid, last_name, name) VALUES (?, true, ?, ?, 'Last', 'Name') ON CONFLICT DO NOTHING")
                .setParameter(1, userId)
                .setParameter(2, email)
                .setParameter(3, uid)
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
}
