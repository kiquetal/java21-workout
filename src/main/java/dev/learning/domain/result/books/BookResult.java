package dev.learning.domain.result.books;

import dev.learning.domain.type.books.BookInfo;

public sealed interface BookResult
{
    record BookAdded(BookInfo bookOK) implements BookResult {}
    record BookRemoved(BookInfo bookOK) implements BookResult{}
    record BookUnavailable(BookInfo bookId, String reason) implements BookResult {}
}
