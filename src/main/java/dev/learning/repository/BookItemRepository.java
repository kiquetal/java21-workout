package dev.learning.repository;

import dev.learning.domain.entity.BookItem;
import dev.learning.domain.type.book_item.BookItemInfo;
import dev.learning.domain.type.book_item.BookItemStatus;
import dev.learning.domain.type.book_item.BookItemId;
import dev.learning.domain.type.books.BookInfo;
import dev.learning.domain.type.books.Isbn;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class BookItemRepository implements PanacheRepository<BookItem>
{

    public boolean isAvailable(Isbn isbn) {
        return find("book.isbn = ?1 and status = ?2", isbn.value(), BookItemStatus.AVAILABLE).firstResultOptional().isPresent();
    }
    public List<BookItem> listAllBookItemByIsbn(Isbn isbn) {

        return list("book.isbn = ?1", isbn.value());
    }


}
