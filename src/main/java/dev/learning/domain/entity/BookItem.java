package dev.learning.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
public class BookItem extends PanacheEntity
{
    @ManyToOne
    @JoinColumn(name="book_item_id")
    Book book;
    String isbn;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    BookItemStatus status;

}
