package dev.learning.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class BookItem extends PanacheEntity
{
    @ManyToOne
    @JoinColumn(name="book_item_id")
    Book book;
    String isbn;
    @Column(nullable = false)
    BookItemStatus status;

}

}
