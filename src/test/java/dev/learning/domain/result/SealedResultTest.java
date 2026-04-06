package dev.learning.domain.result;

import dev.learning.domain.result.book_item.BookItemResult;
import dev.learning.domain.result.lending.LendingResult;
import dev.learning.domain.type.book_item.BookId;
import dev.learning.domain.type.book_item.BookItemInfo;
import dev.learning.domain.type.book_item.BookItemStatus;
import dev.learning.domain.type.books.Isbn;
import dev.learning.domain.type.lending.BookLendingResult;
import dev.learning.domain.type.member.MemberId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests sealed result types — exhaustive pattern matching,
 * correct data carried by each variant.
 * Pure domain, no Quarkus.
 */
class SealedResultTest {

    @Nested
    class BookItemResultTest {

        @Test
        void successCarriesBookItemInfo() {
            var info = new BookItemInfo(new BookId(1L), new Isbn("9780132350884"), BookItemStatus.AVAILABLE, 1L);
            BookItemResult result = new BookItemResult.BookItemAdded(info);

            // Exhaustive switch — compiler ensures all variants handled
            var message = switch (result) {
                case BookItemResult.BookItemAdded(var i) -> "added: " + i.bookId().value();
                case BookItemResult.BookItemRemoved(var i) -> "removed: " + i.bookId().value();
                case BookItemResult.BookItemUpdated(var i) -> "updated: " + i.bookId().value();
                case BookItemResult.BookUnavailable(var id, var reason) -> "unavailable: " + reason;
            };

            assertThat(message).isEqualTo("added: 1");
        }

        @Test
        void unavailableCarriesReason() {
            BookItemResult result = new BookItemResult.BookUnavailable(new BookId(99L), "Book could not be found.");

            var reason = switch (result) {
                case BookItemResult.BookUnavailable(var id, var r) -> r;
                default -> "unexpected";
            };

            assertThat(reason).isEqualTo("Book could not be found.");
        }
    }

    @Nested
    class LendingResultTest {

        @Test
        void exhaustiveSwitchCoversAllVariants() {
            var memberId = new MemberId(1L);
            LendingResult result = new LendingResult.MemberNotFound(memberId);

            // This switch must cover ALL variants — no default needed
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
            var lending = new BookLendingResult(
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
            var memberId = new MemberId(42L);
            LendingResult result = new LendingResult.MaximumLimitReached(memberId);

            assertThat(result).isInstanceOf(LendingResult.MaximumLimitReached.class);
            if (result instanceof LendingResult.MaximumLimitReached(var id)) {
                assertThat(id.value()).isEqualTo(42L);
            }
        }
    }
}
