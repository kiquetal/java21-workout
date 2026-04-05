package dev.learning.domain.type.lending;

import dev.learning.domain.type.MemberId;
import dev.learning.domain.type.book_item.BookId;

import java.time.Instant;

public record BookLendingResult(BookId bookId, MemberId memberId, Instant dueTime, Instant borrowAt) {}
