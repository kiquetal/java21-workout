package dev.learning.domain.result.lending;

import dev.learning.domain.entity.BookItem;
import dev.learning.domain.type.member.MemberId;
import dev.learning.domain.type.lending.LendingDetail;

import java.util.List;

public sealed interface LendingResult {
    record Success(LendingDetail book) implements LendingResult {}
    record AlreadyLent(LendingDetail bookLending ) implements LendingResult {}
    record MemberNotFound(MemberId memberId) implements LendingResult{}
    record BookNotFound(Long bookItemId) implements LendingResult{}
    record MemberHasOverdueBooks(MemberId memberId, List<LendingDetail> books) implements LendingResult{}
    record MaximumLimitReached(MemberId memberId) implements LendingResult {}
}
