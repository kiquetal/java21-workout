package dev.learning.dto;

import dev.learning.domain.type.book_item.BookItemStatus;

//generate the json for post bookItem
public record BookItemRequest(

        String bookId,
        BookItemStatus status) { }
