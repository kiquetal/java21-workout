package dev.learning.service;

import dev.learning.domain.entity.BookItem;
import dev.learning.domain.result.book_item.BookItemResult;
import dev.learning.domain.type.book_item.BookId;
import dev.learning.domain.type.book_item.BookItemInfo;
import dev.learning.repository.BookItemRepository;
import dev.learning.repository.BookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hibernate.annotations.ColumnTransformers;

import static dev.learning.domain.type.book_item.BookItemStatus.AVAILABLE;
import static dev.learning.domain.type.book_item.BookItemStatus.UNAVAILABLE;
import static jdk.internal.jshell.tool.Selector.FormatAction.ADDED;

@ApplicationScoped
public class BookItemService
{
    @Inject
    BookItemRepository bookItemRepository;

    @Inject
    BookRepository bookRepository;

    @Transactional
    public BookItemResult createBookItem(BookItemInfo bookItem)
    {
        var book = bookRepository.findById(bookItem.bookId().value());
        if (book == null) {
            return new BookItemResult.BookUnavailable(new BookId(bookItem.bookId().value()),"Book could not be found.");
        }
        BookItem bookIem = new BookItem();
        bookIem.setBook(book);
        bookIem.setStatus(AVAILABLE);

        bookItemRepository.persist(bookIem);
        return new BookItemResult.BookItemAdded(new BookItemInfo(new BookId(bookIem.id), bookItem.isbn()));

    }

    public BookItemResult updateBookItem(BookItemInfo bookItem)
    {

        var bookItem = bookItemRepository.findById(bookItem.bookItemId().value());



        return new BookItemResult.BookItemUpdated(bookItem);
    }
}
