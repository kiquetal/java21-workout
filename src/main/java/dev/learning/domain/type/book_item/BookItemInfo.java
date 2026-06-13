package dev.learning.domain.type.book_item;

import dev.learning.domain.type.books.Isbn;

public record BookItemInfo(BookItemId bookId, Isbn isbn, BookItemStatus status,Long bookItemId){ }
