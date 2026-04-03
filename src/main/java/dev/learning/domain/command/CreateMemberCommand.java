package dev.learning.domain.command;

import dev.learning.domain.type.Email;

public record CreateMemberCommand(String name, Email email) {}
