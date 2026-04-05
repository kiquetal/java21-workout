package dev.learning.repository;



import dev.learning.domain.entity.BookLending;
import dev.learning.domain.type.MemberId;
import dev.learning.domain.type.lending.LendStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;


@ApplicationScoped
public class LendingRepository implements PanacheRepository<BookLending> {

    public List<BookLending> listBookLendingBorrowed(MemberId memberId) {

        return list("member.id = ?1 and status =?2 ", memberId, LendStatus.LENT) ;
    }

    public boolean hasOverdueBook(MemberId memberId)
    {

        return count("member.id = ?1 and status =?2 and dueDate < ?3", memberId, LendStatus.LENT, Instant.now()) > 0;
    }

}
