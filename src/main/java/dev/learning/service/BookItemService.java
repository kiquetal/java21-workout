package dev.learning.service;

import dev.learning.domain.entity.BookItem;
import dev.learning.domain.result.book_item.BookItemResult;
import dev.learning.domain.type.book_item.BookId;
import dev.learning.domain.type.book_item.BookItemInfo;
import dev.learning.dto.BookItemRequestUpdate;
import dev.learning.repository.BookItemRepository;
import dev.learning.repository.BookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hibernate.annotations.ColumnTransformers;

import static dev.learning.domain.type.book_item.BookItemStatus.*;
import static jdk.internal.jshell.tool.Selector.FormatAction.ADDED;

@ApplicationScoped
public class
BookItemService
{
    @Inject
    BookItemRepository bookItemRepository;

    @Inject
    BookRepository bookRepository;

    @Transactional
    public BookItemResult createBookItem(BookItemInfo bookItemInfo)
    {
        var book = bookRepository.findById(bookItemInfo.bookId().value());
        if (book == null) {
            return new BookItemResult.BookUnavailable(new BookId(bookItemInfo.bookId().value()),"Book could not be found.");
        }
        BookItem bookItem = new BookItem();
        bookItem.setBook(book);
        bookItem.setStatus(AVAILABLE);
        bookItemRepository.persist(bookItem);
        return new BookItemResult.BookItemAdded(new BookItemInfo(new BookId(bookItem.id), bookItem.getBook().isbn,bookItem.getStatus(),bookItem.id));
    }

    @Transactional
    public BookItemResult updateBookItem(BookItemRequestUpdate bookItem)
    {

        var book = bookItemRepository.findById(bookItem.bookItemId());
        if (book == null) {
            return new BookItemResult.BookUnavailable(new BookId(bookItem.bookItemId()), "BookItem could not be found.");
        }

        var status = bookItem.status() == null ? book.getStatus() : bookItem.status();
        book.setStatus(status);
        return new BookItemResult.BookItemUpdated(new BookItemInfo(new BookId(bookItem.bookItemId()), null, UNAVAILABLE, bookItem.bookItemId()));
    }
}
