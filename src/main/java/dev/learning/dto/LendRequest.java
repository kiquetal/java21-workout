package dev.learning.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record LendRequest(
    @NotNull Long bookId,
    @NotNull Long memberId,
    @NotNull LocalDate dueDate
) {}
