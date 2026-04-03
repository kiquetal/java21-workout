package dev.learning.domain.result;

import dev.learning.domain.entity.BookLending;

public sealed interface LendingResult {
    record Success(BookLending lending) implements LendingResult {}
    record BookNotAvailable(String isbn) implements LendingResult {}
    record MemberNotFound(Long memberId) implements LendingResult {}
}
