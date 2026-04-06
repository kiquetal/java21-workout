package dev.learning.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class BookItemResourceTest {

    @Test
    void createWithInvalidJsonType_returns400WithFieldName() {
        given()
            .contentType("application/json")
            .body("""
                {"bookId": "not-a-number", "notes": "test"}
                """)
        .when()
            .post("/chiron/api/book-items")
        .then()
            .statusCode(400)
            .body("message", containsString("bookId"));
    }

    @Test
    void createWithMissingRequiredField_returns400() {
        given()
            .contentType("application/json")
            .body("""
                {"notes": "test"}
                """)
        .when()
            .post("/chiron/api/book-items")
        .then()
            .statusCode(400);
    }

    @Test
    void createWithNonExistentBook_returns404or409() {
        given()
            .contentType("application/json")
            .body("""
                {"bookId": 99999, "notes": "test"}
                """)
        .when()
            .post("/chiron/api/book-items")
        .then()
            .statusCode(anyOf(is(404), is(409)));
    }

    @Test
    void updateWithInvalidStatus_returns400() {
        given()
            .contentType("application/json")
            .body("""
                {"bookItemId": 1, "status": "INVALID_STATUS"}
                """)
        .when()
            .put("/chiron/api/book-items")
        .then()
            .statusCode(400)
            .body("message", containsString("status"));
    }
}
