package dev.learning.domain.type.books;

import dev.learning.domain.type.book_item.BookItemId;

public record BookInfo(Isbn isbn, BookItemId bookId) {}
