package dev.learning.dto;

import dev.learning.domain.type.book_item.BookItemStatus;
import jakarta.validation.constraints.NotNull;

public record BookItemRequestUpdate(Long bookItemId,
                                    @NotNull BookItemStatus status) {}
