-- Prevent the same book item from being lent to two people simultaneously
CREATE UNIQUE INDEX idx_book_item_active_lending
    ON book_lending (book_item_id)
    WHERE status = 'LENT';
