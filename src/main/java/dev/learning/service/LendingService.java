package dev.learning.service;

import dev.learning.domain.LendingStatus;
import dev.learning.domain.command.LendCommand;
import dev.learning.domain.entity.BookLending;
import dev.learning.domain.result.LendingResult;
import dev.learning.domain.result.LendingResult.BookNotAvailable;
import dev.learning.domain.result.LendingResult.MemberNotFound;
import dev.learning.domain.result.LendingResult.Success;
import dev.learning.repository.BookRepository;
import dev.learning.repository.LendingRepository;
import dev.learning.repository.MemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;

@ApplicationScoped
public class LendingService {

    @Inject
    BookRepository bookRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    LendingRepository lendingRepository;

    @Transactional
    public LendingResult lend(LendCommand command) {
        return memberRepository.findByMemberId(command.memberId())
            .<LendingResult>map(member -> bookRepository.findByBookId(command.bookId())
                .filter(book -> !lendingRepository.isCurrentlyLent(book))
                .<LendingResult>map(book -> {
                    var lending = new BookLending();
                    lending.book = book;
                    lending.member = member;
                    lending.borrowedAt = LocalDate.now();
                    lending.dueDate = command.dueDate();
                    lending.status = LendingStatus.ACTIVE;
                    lendingRepository.persist(lending);
                    return new Success(lending);
                })
                .orElseGet(() -> new BookNotAvailable(command.bookId().value().toString())))
            .orElseGet(() -> new MemberNotFound(command.memberId().value()));
    }
}
