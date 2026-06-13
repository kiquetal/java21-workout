package dev.learning.domain.command;

import dev.learning.domain.type.book_item.BookItemId;
import dev.learning.domain.type.lending.LendStatus;
import dev.learning.domain.type.member.MemberId;

import java.time.Instant;

public record LendCommand(BookItemId bookItemId, MemberId memberId, Instant startDate, Instant endDate, LendStatus status){}

