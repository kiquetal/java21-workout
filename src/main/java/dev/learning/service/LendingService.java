package dev.learning.service;


import dev.learning.domain.command.LendCommand;
import dev.learning.domain.entity.BookItem;
import dev.learning.domain.entity.Member;
import dev.learning.domain.result.lending.LendingResult;
import dev.learning.domain.type.Either;
import dev.learning.domain.type.book_item.BookItemId;
import dev.learning.domain.type.book_item.BookItemStatus;
import dev.learning.domain.type.lending.LendingDetail;
import dev.learning.domain.type.member.MemberId;
import dev.learning.repository.BookItemRepository;
import dev.learning.repository.BookRepository;
import dev.learning.repository.BookLendingRe;
import dev.learning.repository.MemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
    BookLendingRe lendingRepository;

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


    private record BookItemAndMemberRecord(BookItem bookItem, Member member)
    {
    }
    private Either<LendingResult, BookItemAndMemberRecord> findBookItemAndMember(LendCommand lendCommand,Member member)
    {

        var bookItemOpt = bookItemRepository.findByIdOptional(lendCommand.bookItemId().value());
        return bookItemOpt.<Either<LendingResult, BookItemAndMemberRecord>>map(bookItem -> Either.right(new BookItemAndMemberRecord(bookItem, member))).orElseGet(() -> Either.left(new LendingResult.BookNotFound(lendCommand.bookItemId().value())));


    }
    public LendingResult lend(LendCommand lendCommand){

        return findMember(lendCommand)
                .flatMap(this::checkOverdue)
                .flatMap(m -> findBookItemAndMember(lendCommand,m))
                .fold( err -> err ,
                        bookItem -> {

                            if (bookItem.bookItem.status != BookItemStatus.AVAILABLE) {
                                var lendingDetail = new LendingDetail(new BookItemId(bookItem.bookItem.id), new MemberId(bookItem.member.id), null, null);
                                return new LendingResult.AlreadyLent(lendingDetail);
                            }
                            bookItem.bookItem.status = BookItemStatus.LENT;
                            bookItemRepository.persist(bookItem.bookItem);
                            var lending = lendingRepository.createLending(bookItem.bookItem, bookItem.member, lendCommand.borrowedAt(), lendCommand.dueDate());
                            var lendingDetail = new LendingDetail(new BookItemId(bookItem.bookItem.id), new MemberId(bookItem.member.id), lending.dueDate, lending.borrowedAt);
                            return new LendingResult.Success(lendingDetail);
                        }

                );



    }

}
