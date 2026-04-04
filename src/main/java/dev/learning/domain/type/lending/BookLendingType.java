package dev.learning.domain.type.lending;

import dev.learning.domain.type.MemberId;
import dev.learning.domain.type.books.BookId;

import java.time.Instant;

public record BookLendingType(BookId bookId, MemberId memberId, Instant dueTime, Instant borrowAt) {}
