package dev.learning.domain.result;

import dev.learning.domain.entity.Member;

public sealed interface CreateMemberResult {
    record Success(Member member) implements CreateMemberResult {}
    record EmailAlreadyExists(String email) implements CreateMemberResult {}
}
