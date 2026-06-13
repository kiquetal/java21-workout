package dev.learning.service;

import dev.learning.domain.entity.BookItem;
import dev.learning.domain.result.book_item.BookItemResult;
import dev.learning.domain.type.book_item.BookItemId;
import dev.learning.domain.type.book_item.BookItemInfo;
import dev.learning.domain.type.books.Isbn;
import dev.learning.dto.BookItemRequestUpdate;
import dev.learning.repository.BookItemRepository;
import dev.learning.repository.BookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import static dev.learning.domain.type.book_item.BookItemStatus.*;

@ApplicationScoped
public class BookItemService {
    @Inject
    BookItemRepository bookItemRepository;

    @Inject
    BookRepository bookRepository;

    @Transactional
    public BookItemResult createBookItem(BookItemInfo bookItemInfo) {
        var book = bookRepository.findById(bookItemInfo.bookId().value());
        if (book == null) {
            return new BookItemResult.BookUnavailable(bookItemInfo.bookId(), "Book could not be found.");
        }
        var bookItem = new BookItem();
        bookItem.book = book;
        bookItem.status = AVAILABLE;
        bookItemRepository.persist(bookItem);
        return new BookItemResult.BookItemAdded(new BookItemInfo(new BookItemId(bookItem.id), new Isbn(bookItem.book.isbn), bookItem.status, bookItem.id));
    }

    @Transactional
    public BookItemResult updateBookItem(BookItemRequestUpdate bookItem) {
        var item = bookItemRepository.findById(bookItem.bookItemId());
        if (item == null) {
            return new BookItemResult.BookUnavailable(new BookItemId(bookItem.bookItemId()), "BookItem could not be found.");
        }
        item.status = bookItem.status() == null ? item.status : bookItem.status();
        return new BookItemResult.BookItemUpdated(new BookItemInfo(new BookItemId(bookItem.bookItemId()), null, item.status, bookItem.bookItemId()));
    }
}
