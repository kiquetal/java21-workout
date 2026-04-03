package dev.learning.domain.type;

public record MemberId(Long value) {
    public MemberId {
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Invalid member ID: " + value);
    }
}
