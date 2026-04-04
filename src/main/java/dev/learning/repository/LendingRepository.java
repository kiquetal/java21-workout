package dev.learning.repository;


import dev.learning.domain.LendingStatus;
import dev.learning.domain.entity.Book;
import dev.learning.domain.entity.BookLending;
import dev.learning.domain.type.MemberId;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;


@ApplicationScoped
public class LendingRepository implements PanacheRepository<BookLending> {

    public boolean isCurrentlyLent(Book book) {
        return count("book = ?1 and status = ?2", book, LendingStatus.ACTIVE) > 0;
    }

    public List<BookLending> listBookLendingBorrowed(MemberId memberId) {

        return list("member = ?1", memberId);




    }
}
