package dev.learning.domain.command;

import dev.learning.domain.type.BookId;
import dev.learning.domain.type.LendStatus;
import dev.learning.domain.type.MemberId;

import java.time.Instant;

public record LendCommand(BookId bookId, MemberId memberId, Instant startDate, Instant endDate, LendStatus status){}

