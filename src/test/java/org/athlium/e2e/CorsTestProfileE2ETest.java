package org.athlium.e2e;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class CorsTestProfileE2ETest {

    private static final String FRONTEND_ORIGIN = "http://localhost:5173";

    @Test
    void shouldReturnCorsHeadersForPreflightRequestsInTestProfile() {
        given()
                .header("Origin", FRONTEND_ORIGIN)
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "authorization,content-type,x-idempotency-key")
                .when()
                .options("/api/auth/health")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(204)))
                .header("Access-Control-Allow-Origin", anyOf(equalTo("*"), equalTo(FRONTEND_ORIGIN)))
                .header("Access-Control-Allow-Methods", containsString("GET"))
                .header("Access-Control-Allow-Headers", anyOf(containsString("authorization"), equalTo("*")));
    }

    @Test
    void shouldReturnCorsHeadersForActualRequestsInTestProfile() {
        given()
                .header("Origin", FRONTEND_ORIGIN)
                .when()
                .get("/api/auth/health")
                .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", notNullValue());
    }

    @Test
    void shouldReturnCorsHeadersForRegisterPreflightRequest() {
        given()
                .header("Origin", FRONTEND_ORIGIN)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type")
                .when()
                .options("/api/auth/register")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(204)))
                .header("Access-Control-Allow-Origin", anyOf(equalTo("*"), equalTo(FRONTEND_ORIGIN)))
                .header("Access-Control-Allow-Methods", containsString("POST"))
                .header("Access-Control-Allow-Headers", anyOf(containsString("content-type"), equalTo("*")));
    }
}
