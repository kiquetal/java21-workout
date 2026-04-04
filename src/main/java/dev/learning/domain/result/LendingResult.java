package dev.learning.domain.result;

import dev.learning.domain.type.MemberId;
import dev.learning.domain.type.lending.BookLendingType;


public sealed interface LendingResult {
    record Success(BookLendingType book) implements LendingResult {}
    record AlreadyLent(BookLendingType bookLending ) implements LendingResult {}
    record MemberNotFound(MemberId memberId) implements LendingResult{}
    record BookNotFound(String bookId) implements LendingResult{}
    record MemberInDefault(MemberId memberId) implements LendingResult{}
}
