package dev.learning.domain.type.book_item;

public record BookId(Long value) {
    public BookId {
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Invalid book ID: " + value);
    }
}
