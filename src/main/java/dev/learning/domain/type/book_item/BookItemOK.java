package dev.learning.domain.type.book_item;

import dev.learning.domain.result.book_item.BookItemResult;
import dev.learning.domain.type.books.BookId;
import dev.learning.domain.type.books.Isbn;

public record BookItemOK(BookId bookId, Isbn isbn){ }
