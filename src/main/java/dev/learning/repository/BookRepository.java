package dev.learning.repository;

import dev.learning.domain.entity.Book;
import dev.learning.domain.type.books.BookId;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class BookRepository implements PanacheRepository<Book> {

    public Optional<Book> findByBookId(BookId bookId) {
        return findByIdOptional(bookId.value());
    }
}
