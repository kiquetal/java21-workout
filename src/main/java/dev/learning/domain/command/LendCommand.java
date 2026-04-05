package dev.learning.domain.command;

import dev.learning.domain.type.book_item.BookId;
import dev.learning.domain.type.lending.LendStatus;
import dev.learning.domain.type.member.MemberId;

import java.time.Instant;

public record LendCommand(BookId bookId, MemberId memberId, Instant startDate, Instant endDate, LendStatus status){}

