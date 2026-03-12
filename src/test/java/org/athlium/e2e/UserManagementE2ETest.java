package org.athlium.e2e;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@QuarkusTest
@TestProfile(UserManagementE2ETest.NoBypassAuthProfile.class)
class UserManagementE2ETest {

    private static final String ADMIN_TOKEN = "users-admin-e2e";
    private static final String ADMIN_UID = "mock-users-admin-e2e";
    private static final String OWNER_TOKEN = "users-owner-e2e";
    private static final String OWNER_UID = "mock-users-owner-e2e";
    private static final String TARGET_UID = "mock-users-target-e2e";
    private static final String OTHER_CLIENT_TOKEN = "other-client-e2e";
    private static final String OTHER_CLIENT_UID = "mock-other-client-e2e";

    @Inject
    EntityManager entityManager;

    @BeforeEach
    void setUp() {
        cleanData();
        ensureOrganizationAndHeadquarters();
        ensureUserWithRoles(1001L, ADMIN_UID, "admin@test.com", "ORG_ADMIN");
        ensureUserWithRoles(1004L, OWNER_UID, "owner@test.com", "ORG_OWNER");
        ensureUserWithRoles(1002L, TARGET_UID, "target@test.com", "CLIENT");
        ensureUserWithRoles(1003L, OTHER_CLIENT_UID, "other-client@test.com", "CLIENT");
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
    void shouldAllowOrgAdminToAssignAndUnassignOtherUserInSameOrganization() {
        ensureUserHeadquartersMembership(1001L, 100L);

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
    void shouldAllowSelfAssignForRegularClient() {
        given()
                .header("Authorization", bearer("users-target-e2e"))
                .contentType("application/json")
                .when()
                .post("/api/users/firebase/{uid}/headquarters/{headquartersId}", TARGET_UID, 100L)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.headquartersIds", hasItem(100));
    }

    @Test
    void shouldForbidAssigningAnotherUserForRegularClient() {
        given()
                .header("Authorization", bearer(OTHER_CLIENT_TOKEN))
                .contentType("application/json")
                .when()
                .post("/api/users/firebase/{uid}/headquarters/{headquartersId}", TARGET_UID, 100L)
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Only headquarters admins, owners, or SUPERADMIN can update other users"));
    }

    @Test
    void shouldAllowSelfRemoveForRegularClient() {
        ensureUserHeadquartersMembership(1002L, 100L);

        given()
                .header("Authorization", bearer("users-target-e2e"))
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
        ensureUserHeadquartersMembership(1001L, 100L);

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
                .body("data.totalElements", equalTo(2))
                .body("data.content.email", hasItem("target@test.com"));
    }

    @Test
    void shouldAllowOrgOwnerToListUsers() {
        given()
                .header("Authorization", bearer(OWNER_TOKEN))
                .when()
                .get("/api/users")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    void shouldForbidRegularClientFromListingUsers() {
        given()
                .header("Authorization", bearer(OTHER_CLIENT_TOKEN))
                .when()
                .get("/api/users")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Insufficient permissions"));
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

    @Transactional
    void ensureUserHeadquartersMembership(Long userId, Long headquartersId) {
        entityManager.createNativeQuery("INSERT INTO user_headquarters (user_id, headquarters_id) VALUES (?, ?) ON CONFLICT DO NOTHING")
                .setParameter(1, userId)
                .setParameter(2, headquartersId)
                .executeUpdate();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    public static class NoBypassAuthProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Collections.singletonMap("auth.dev-bypass.enabled", "false");
        }
    }
}
