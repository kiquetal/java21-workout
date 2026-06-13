package dev.learning.domain.type.lending;

import dev.learning.domain.type.member.MemberId;
import dev.learning.domain.type.book_item.BookItemId;

import java.time.Instant;

public record LendingDetail(BookItemId bookId, MemberId memberId, Instant dueTime, Instant borrowAt) {}
