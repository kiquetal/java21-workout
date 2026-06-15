package dev.learning.service;


import dev.learning.domain.command.LendCommand;
import dev.learning.domain.entity.BookItem;
import dev.learning.domain.entity.BookLending;
import dev.learning.domain.entity.Member;
import dev.learning.domain.result.lending.LendingResult;
import dev.learning.domain.type.Either;
import dev.learning.domain.type.book_item.BookItemId;
import dev.learning.domain.type.book_item.BookItemStatus;
import dev.learning.domain.type.lending.LendingDetail;
import dev.learning.domain.type.member.MemberId;
import dev.learning.repository.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
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
    BookLendingRepository bookLendingRepository;

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
        var hasOverdue = bookLendingRepository.hasOverdueBook(memberId);
        if (hasOverdue) {
            var overdueBooks = bookLendingRepository.listBookLendingBorrowed(memberId);
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
    @Transactional
    public LendingResult lend(LendCommand lendCommand){

        return findMember(lendCommand)
                .flatMap(this::checkOverdue)
                .flatMap(m -> findBookItemAndMember(lendCommand,m))
                .flatMap(this::checkIfAlreadyLent)
                .fold( err -> err ,
                        bookItem -> persistAndReturnResult(bookItem,lendCommand)

                );



    }

    private LendingResult persistAndReturnResult(BookItemAndMemberRecord bookItem, LendCommand lendCommand)
    {        var lending = new BookLending();
        lending.bookItem = bookItem.bookItem;
        lending.member = bookItem.member;
        lending.borrowedAt = lendCommand.borrowedAt();
        lending.dueDate = lendCommand.dueDate();
        bookLendingRepository.persist(lending);
        bookItem.bookItem.status = BookItemStatus.LENT;
        return new LendingResult.Success(new LendingDetail(new BookItemId(bookItem.bookItem.id), new MemberId(bookItem.member.id), lending.dueDate, lending.borrowedAt));


    }

    private Either<LendingResult, BookItemAndMemberRecord> checkIfAlreadyLent(BookItemAndMemberRecord record)
    {
        if (record.bookItem.status != BookItemStatus.AVAILABLE) {
            var lendingDetail = new LendingDetail(new BookItemId(record.bookItem.id), new MemberId(record.member.id), null, null);
            return Either.left(new LendingResult.AlreadyLent(lendingDetail));
        }
        return Either.right(record);
    }

}
