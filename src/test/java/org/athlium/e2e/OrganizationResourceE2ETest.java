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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class OrganizationResourceE2ETest {

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
    }

    @Test
    void shouldUpdateOrganizationName() {
        Long organizationId = createOrganization("Org Original");

        given()
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
                .when()
                .get("/api/organizations/{id}", organizationId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", equalTo(organizationId.intValue()))
                .body("data.name", equalTo("Org Actualizada"));
    }

    @Transactional
    void cleanOrganizations() {
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
}
