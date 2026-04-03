package dev.learning.domain;

import java.time.LocalDate;

public record LendCommand(BookId bookId, MemberId memberId, LocalDate dueDate) {}
