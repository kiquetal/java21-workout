package dev.learning.domain;

public sealed interface LendingResult {
    record Success(BookLending lending) implements LendingResult {}
    record BookNotAvailable(String isbn) implements LendingResult {}
    record MemberNotFound(Long memberId) implements LendingResult {}
}
