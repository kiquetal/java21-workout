package dev.learning.domain.result.books;

import dev.learning.domain.type.books.BookId;

public record BookResult(BookId bookId, String isbn, String title, String author, int publicationYear)
{
}
