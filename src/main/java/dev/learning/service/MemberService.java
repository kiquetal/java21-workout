package dev.learning.service;

import dev.learning.domain.command.CreateMemberCommand;
import dev.learning.domain.entity.Member;
import dev.learning.domain.result.member.MemberResult;
import dev.learning.domain.result.member.MemberResult.EmailAlreadyExists;
import dev.learning.domain.result.member.MemberResult.Success;
import dev.learning.repository.MemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MemberService {

    @Inject
    MemberRepository memberRepository;

    @Transactional
    public MemberResult create(CreateMemberCommand command) {
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
