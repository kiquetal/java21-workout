package dev.learning.domain.result;

import dev.learning.domain.result.lending.LendingResult;
import dev.learning.domain.type.book_item.BookId;
import dev.learning.domain.type.lending.LendingDetail;
import dev.learning.domain.type.member.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LendingResultTest {

    @Test
    void exhaustiveSwitchCoversAllVariants() {
        LendingResult result = new LendingResult.MemberNotFound(new MemberId(1L));

        var httpStatus = switch (result) {
            case LendingResult.Success(var b) -> 200;
            case LendingResult.AlreadyLent(var b) -> 409;
            case LendingResult.MemberNotFound(var id) -> 404;
            case LendingResult.BookNotFound(var id) -> 404;
            case LendingResult.MemberHasOverdueBooks(var id, var books) -> 403;
            case LendingResult.MaximumLimitReached(var id) -> 403;
        };

        assertThat(httpStatus).isEqualTo(404);
    }

    @Test
    void successCarriesLendingDetails() {
        var lending = new LendingDetail(
            new BookId(1L),
            new MemberId(2L),
            Instant.parse("2026-05-01T00:00:00Z"),
            Instant.parse("2026-04-01T00:00:00Z")
        );
        LendingResult result = new LendingResult.Success(lending);

        if (result instanceof LendingResult.Success(var book)) {
            assertThat(book.bookId().value()).isEqualTo(1L);
            assertThat(book.memberId().value()).isEqualTo(2L);
        }
    }

    @Test
    void maximumLimitReachedCarriesMemberId() {
        LendingResult result = new LendingResult.MaximumLimitReached(new MemberId(42L));

        if (result instanceof LendingResult.MaximumLimitReached(var id)) {
            assertThat(id.value()).isEqualTo(42L);
        }
    }
}
