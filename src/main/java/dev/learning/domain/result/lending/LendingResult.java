package dev.learning.domain.result.lending;

import dev.learning.domain.type.MemberId;
import dev.learning.domain.type.lending.BookLendingResult;

public sealed interface LendingResult {
    record Success(BookLendingResult book) implements LendingResult {}
    record AlreadyLent(BookLendingResult bookLending ) implements LendingResult {}
    record MemberNotFound(MemberId memberId) implements LendingResult{}
    record BookNotFound(String bookId) implements LendingResult{}
    record MemberInDefault(MemberId memberId) implements LendingResult{}
    record MaximumLimitReached(MemberId memberId) implements LendingResult {}
}
