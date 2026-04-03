package dev.learning.service;

import dev.learning.domain.command.CreateMemberCommand;
import dev.learning.domain.entity.Member;
import dev.learning.domain.result.CreateMemberResult;
import dev.learning.domain.result.CreateMemberResult.EmailAlreadyExists;
import dev.learning.domain.result.CreateMemberResult.Success;
import dev.learning.repository.MemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MemberService {

    @Inject
    MemberRepository memberRepository;

    @Transactional
    public CreateMemberResult create(CreateMemberCommand command) {
        if (memberRepository.existsByEmail(command.email())) {
            return new EmailAlreadyExists(command.email().value());
        }
        var member = new Member();
        member.name = command.name();
        member.email = command.email().value();
        memberRepository.persist(member);
        return new Success(member);
    }
}
