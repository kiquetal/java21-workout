package dev.learning.domain.entity;

import dev.learning.domain.type.book_item.BookItemStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
public class BookItem extends PanacheEntity {
    @ManyToOne
    @JoinColumn(name = "book_id")
    public Book book;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public BookItemStatus status;

    public String notes;

    @Column(updatable = false)
    @CreationTimestamp
    public Instant creationDate;

    @UpdateTimestamp
    public Instant updateDate;
}
