package dev.learning.dto;

import java.time.LocalDate;

public record LendingResponse(Long id, String bookTitle, String memberName, LocalDate dueDate) {}
