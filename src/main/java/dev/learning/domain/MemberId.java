package dev.learning.domain;

public record MemberId(Long value) {
    public MemberId {
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Invalid member ID: " + value);
    }
}
