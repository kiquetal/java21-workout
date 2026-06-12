package dev.learning.domain.type.lending;

import dev.learning.domain.type.member.MemberId;
import dev.learning.domain.type.book_item.BookId;

import java.time.Instant;

public record LendingDetail(BookId bookId, MemberId memberId, Instant dueTime, Instant borrowAt) {}
