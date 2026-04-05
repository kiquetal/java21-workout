package dev.learning.domain.type.member;

import dev.learning.domain.type.Email;

public record MemberInfo(MemberId id, String name, Email email) {}
