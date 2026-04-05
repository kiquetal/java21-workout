package dev.learning.repository;

import dev.learning.domain.entity.BookItem;
import dev.learning.domain.type.book_item.BookItemStatus;
import dev.learning.domain.type.book_item.BookId;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BookItemRepository implements PanacheRepository<BookItem>
{

    public boolean isAvailable(BookId  bookId) {

        return find("book.id = ?1 and status = ?2", bookId.value(), BookItemStatus.AVAILABLE).firstResultOptional().isPresent();
    }




}
