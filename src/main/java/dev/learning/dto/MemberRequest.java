package dev.learning.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberRequest(@NotBlank String name, @NotBlank String email) {}
