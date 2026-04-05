package dev.learning.domain.result.book_item;

import dev.learning.domain.type.book_item.BookItemOK;
import dev.learning.domain.type.books.BookId;

public sealed interface BookItemResult {

    record BookItemAdded(BookItemOK bookItemOK) implements BookItemResult {}
    record BookUnavailable(BookId bookId, String reason) implements BookItemResult {}

}
