package dev.learning.domain.result;

import dev.learning.domain.result.book_item.BookItemResult;
import dev.learning.domain.type.book_item.BookItemId;
import dev.learning.domain.type.book_item.BookItemInfo;
import dev.learning.domain.type.book_item.BookItemStatus;
import dev.learning.domain.type.books.Isbn;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookItemResultTest {

    @Test
    void successCarriesBookItemInfo() {
        var info = new BookItemInfo(new BookItemId(1L), new Isbn("9780132350884"), BookItemStatus.AVAILABLE, 1L);
        BookItemResult result = new BookItemResult.BookItemAdded(info);

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
        BookItemResult result = new BookItemResult.BookUnavailable(new BookItemId(99L), "Book could not be found.");

        var reason = switch (result) {
            case BookItemResult.BookUnavailable(var id, var r) -> r;
            default -> "unexpected";
        };

        assertThat(reason).isEqualTo("Book could not be found.");
    }
}
