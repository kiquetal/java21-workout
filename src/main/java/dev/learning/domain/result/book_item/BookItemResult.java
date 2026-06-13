package dev.learning.domain.result.book_item;

import dev.learning.domain.type.book_item.BookItemInfo;
import dev.learning.domain.type.book_item.BookItemId;

public sealed interface BookItemResult {
    record BookItemAdded(BookItemInfo bookItemInfo) implements BookItemResult {}
    record BookItemRemoved(BookItemInfo bookItemInfo) implements BookItemResult{}
    record BookItemUpdated(BookItemInfo bookItemInfo) implements BookItemResult {}
    record BookUnavailable(BookItemId bookId, String reason) implements BookItemResult {}

}
