package dev.learning.service;

import dev.learning.domain.command.CreateMemberCommand;
import dev.learning.domain.entity.Member;
import dev.learning.domain.result.CreateMemberResult;
import dev.learning.domain.result.CreateMemberResult.EmailAlreadyExists;
import dev.learning.domain.result.CreateMemberResult.Success;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MemberService {

    @Transactional
    public CreateMemberResult create(CreateMemberCommand command) {
        if (Member.count("email", command.email().value()) > 0) {
            return new EmailAlreadyExists(command.email().value());
        }
        var member = new Member();
        member.name = command.name();
        member.email = command.email().value();
        member.persist();
        return new Success(member);
    }
}
