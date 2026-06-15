package dev.learning.resource;

import dev.learning.repository.BookLendingRepository;
import dev.learning.repository.MemberRepository;
import dev.learning.service.LendingService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class LendingResourceTest {


    @InjectMock
    BookLendingRepository bookLendingRepository;
    @Inject
    LendingService lendingService;
    @InjectMock
    MemberRepository memberRepository;

    @Test
    void lendWithNonExistentMember_returns404() {
        given()
            .contentType("application/json")
            .body("""
                {"bookId": 1, "memberId": 99999}
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
                {"bookId": 99999, "memberId": 1}
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
                {"bookId": "not-a-number", "memberId": 1}
                """)
        .when()
            .post("/api/lendings")
        .then()
            .statusCode(400);
    }
    @Test
    void checkMaximunNumberOfLending() {

    //the method to test is lend from BookLendingService

        Mockito.when(memberRepository.findByMemberId(Mockito.any())).thenReturn(java.util.Optional.of(new dev.learning.domain.Member(){{
            id = 1L;
        }}));
        lendingService.lend()
    Mockito.when()
    }


}
