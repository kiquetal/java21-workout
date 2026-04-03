package dev.learning.service;

import dev.learning.domain.command.LendCommand;
import dev.learning.domain.entity.Book;
import dev.learning.domain.entity.BookLending;
import dev.learning.domain.entity.Member;
import dev.learning.domain.result.LendingResult;
import dev.learning.domain.result.LendingResult.BookNotAvailable;
import dev.learning.domain.result.LendingResult.MemberNotFound;
import dev.learning.domain.result.LendingResult.Success;
import dev.learning.domain.LendingStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDate;

@ApplicationScoped
public class LendingService {

    @Transactional
    public LendingResult lend(LendCommand command) {
        return Member.<Member>findByIdOptional(command.memberId().value())
            .<LendingResult>map(member -> Book.<Book>findByIdOptional(command.bookId().value())
                .filter(book -> !isCurrentlyLent(book))
                .<LendingResult>map(book -> {
                    var lending = new BookLending();
                    lending.book = book;
                    lending.member = member;
                    lending.borrowedAt = LocalDate.now();
                    lending.dueDate = command.dueDate();
                    lending.status = LendingStatus.ACTIVE;
                    lending.persist();
                    return new Success(lending);
                })
                .orElseGet(() -> new BookNotAvailable(command.bookId().value().toString())))
            .orElseGet(() -> new MemberNotFound(command.memberId().value()));
    }

    private boolean isCurrentlyLent(Book book) {
        return BookLending.count("book = ?1 and status = ?2", book, LendingStatus.ACTIVE) > 0;
    }
}
