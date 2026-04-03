package dev.learning.domain;

public record BookId(Long value) {
    public BookId {
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Invalid book ID: " + value);
    }
}
