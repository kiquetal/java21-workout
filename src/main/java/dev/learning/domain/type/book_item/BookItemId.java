package dev.learning.domain.type.book_item;

public record BookItemId(Long value) {
    public BookItemId {
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Invalid book item ID: " + value);
    }
}
