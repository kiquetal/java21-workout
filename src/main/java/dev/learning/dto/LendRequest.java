package dev.learning.dto;

import jakarta.validation.constraints.NotNull;

public record LendRequest(
    @NotNull Long bookId,
    @NotNull Long memberId
) {}
