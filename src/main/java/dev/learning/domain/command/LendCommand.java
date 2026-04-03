package dev.learning.domain.command;

import dev.learning.domain.type.BookId;
import dev.learning.domain.type.MemberId;
import java.time.LocalDate;

public record LendCommand(BookId bookId, MemberId memberId, LocalDate dueDate) {}
