package dev.learning.domain.result.book_item;

import dev.learning.domain.type.book_item.BookItemOK;
import dev.learning.domain.type.book_item.BookId;

public sealed interface BookItemResult {

    record BookItemAdded(BookItemOK bookItemOK) implements BookItemResult {}
    record BookItemRemoved(BookItemOK bookItemOK) implements BookItemResult{}
    record BookUnavailable(BookId bookId, String reason) implements BookItemResult {}

}
