package dev.learning.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.time.Instant;
import java.util.Set;

@Entity
public class Book extends PanacheEntity {
    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public String author;

    @Column(unique = true, nullable = false, length = 13)
    public String isbn;

    public String publisher;

    public Instant publishDate;

    @OneToMany(mappedBy = "book")
    Set<BookItem> bookItems;
}
