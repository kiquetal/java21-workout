package dev.learning.service;


import dev.learning.domain.command.LendCommand;
import dev.learning.domain.entity.Member;
import dev.learning.domain.result.lending.LendingResult;
import dev.learning.domain.type.Either;
import dev.learning.repository.BookRepository;
import dev.learning.repository.LendingRepository;
import dev.learning.repository.MemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class LendingService
{

    @Inject
    BookRepository bookRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    LendingRepository lendingRepository;


    private Either<LendingResult, Member> findMember(LendCommand lendCommand)
    {

        Optional<Member> memberOpt = memberRepository.findByMemberId(lendCommand.memberId());
        return memberOpt.<Either<LendingResult, Member>>map(
                Either::right
        ).orElse(new Either.Left<>(new LendingResult.MemberNotFound(lendCommand.memberId())));

    }
}
