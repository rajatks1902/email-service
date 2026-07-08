package com.gemalto.telecom.ota.email;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class EmailResourceTest {

    @Test
    void healthEndpointReturnsOk() {
        RestAssured.given()
                .when().get("/api/email/health")
                .then()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    void sendEndpointReturnsBadRequestWhenToIsMissing() {
        RestAssured.given()
                .multiPart("subject", "Test subject")
                .when().post("/api/email/send")
                .then()
                .statusCode(400)
                .body("success", is(false));
    }
}
