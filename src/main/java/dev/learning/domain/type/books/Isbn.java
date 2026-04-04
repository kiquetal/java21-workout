package dev.learning.domain.type.books;

public record Isbn(String value) {
    public Isbn {
        if (value == null || !value.matches("\\d{13}"))
            throw new IllegalArgumentException("Invalid ISBN: " + value);
    }
}
