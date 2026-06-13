package dev.learning.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class LendingResourceTest {

    @Test
    void lendWithNonExistentMember_returns404() {
        given()
            .contentType("application/json")
            .body("""
                {"bookId": 1, "memberId": 99999, "dueDate": "2026-07-01"}
                """)
        .when()
            .post("/api/lendings")
        .then()
            .statusCode(404)
            .body("message", containsString("Member not found"));
    }

    @Test
    void lendWithNonExistentBookItem_returns404() {
        given()
            .contentType("application/json")
            .body("""
                {"bookId": 99999, "memberId": 1, "dueDate": "2026-07-01"}
                """)
        .when()
            .post("/api/lendings")
        .then()
            .statusCode(404)
            .body("message", containsString("not found"));
    }

    @Test
    void lendWithMissingFields_returns400() {
        given()
            .contentType("application/json")
            .body("""
                {"bookId": 1}
                """)
        .when()
            .post("/api/lendings")
        .then()
            .statusCode(400);
    }

    @Test
    void lendWithInvalidDateFormat_returns400() {
        given()
            .contentType("application/json")
            .body("""
                {"bookId": 1, "memberId": 1, "dueDate": "not-a-date"}
                """)
        .when()
            .post("/api/lendings")
        .then()
            .statusCode(400);
    }
}
