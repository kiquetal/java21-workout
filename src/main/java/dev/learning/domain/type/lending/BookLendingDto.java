package dev.learning.domain.type.lending;

import dev.learning.domain.LendingStatus;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link dev.learning.domain.entity.BookLending}
 */
public record BookLendingDto(LocalDate borrowedAt, LocalDate returnedAt, LendingStatus status) implements Serializable
{
}
