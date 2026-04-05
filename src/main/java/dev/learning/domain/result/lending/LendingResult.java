package dev.learning.domain.result.lending;

import dev.learning.domain.type.member.MemberId;
import dev.learning.domain.type.lending.BookLendingResult;

import java.util.List;

public sealed interface LendingResult {
    record Success(BookLendingResult book) implements LendingResult {}
    record AlreadyLent(BookLendingResult bookLending ) implements LendingResult {}
    record MemberNotFound(MemberId memberId) implements LendingResult{}
    record BookNotFound(String bookId) implements LendingResult{}
    record MemberHasOverdueBooks(MemberId memberId, List<BookLendingResult> books) implements LendingResult{}
    record MaximumLimitReached(MemberId memberId) implements LendingResult {}
}
