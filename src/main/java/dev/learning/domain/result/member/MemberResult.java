package dev.learning.domain.result.member;

import dev.learning.domain.entity.Member;
import dev.learning.domain.type.member.MemberInfo;

public sealed interface MemberResult
{
    record MemmberCreated(MemberInfo member) implements MemberResult {}
    record EmailAlreadyExists(String email) implements MemberResult {}
    record MemeberDeleted(String email) implements MemberResult { }
}
