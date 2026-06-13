package dev.learning.service;


import dev.learning.domain.command.LendCommand;
import dev.learning.domain.entity.BookItem;
import dev.learning.domain.entity.Member;
import dev.learning.domain.result.lending.LendingResult;
import dev.learning.domain.type.Either;
import dev.learning.domain.type.book_item.BookItemId;
import dev.learning.domain.type.lending.LendingDetail;
import dev.learning.domain.type.member.MemberId;
import dev.learning.repository.BookItemRepository;
import dev.learning.repository.BookRepository;
import dev.learning.repository.LendingRepository;
import dev.learning.repository.MemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.stream;

@ApplicationScoped
public class LendingService
{

    @Inject
    BookRepository bookRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    LendingRepository lendingRepository;

    @Inject
    BookItemRepository bookItemRepository;
    private Either<LendingResult, Member> findMember(LendCommand lendCommand)
    {

        Optional<Member> memberOpt = memberRepository.findByMemberId(lendCommand.memberId());
        return memberOpt.<Either<LendingResult, Member>>map(
                Either::right
        ).orElse(Either.left(new LendingResult.MemberNotFound(lendCommand.memberId())));

    }

    private Either<LendingResult,Member> checkOverdue(Member member)
    {
        var memberId = new MemberId(member.id);
        var hasOverdue = lendingRepository.hasOverdueBook(memberId);
        if (hasOverdue) {
            var overdueBooks = lendingRepository.listBookLendingBorrowed(memberId);
            //convert overdueBooks to bookdetails
            List<LendingDetail> lendingDetails = overdueBooks.stream().map(
                     b ->  new LendingDetail(new BookItemId(b.bookItem.id), memberId, b.dueDate,b.borrowedAt)
            ).toList();

            return Either.left(new LendingResult.MemberHasOverdueBooks(memberId, lendingDetails));
        }
        return Either.right(member);
    }


    private Either<LendingResult, BookItem> findBookItem(LendCommand lendCommand)
    {
        var bookItemOpt = bookItemRepository.findByIdOptional(lendCommand.bookItemId().value());
        return bookItemOpt.<Either<LendingResult, BookItem>>map(
                Either::right
        ).orElse(Either.left(new LendingResult.BookNotFound(lendCommand.bookItemId().value())));
    }
    public LendingResult lend(LendCommand lendCommand){

        return findMember(lendCommand)
                .flatMap(this::checkOverdue)
                .flatMap(m -> findBookItem(lendCommand))
                .fold(
                         e -> e,
                          bookItem ->
                                  new LendingResult.Success(new LendingDetail(new BookItemId(bookItem.id), new MemberId(bookItem.book.id), Instant.now().plus(Duration.ofDays(8)), Instant.now()))
                );




    }

}
