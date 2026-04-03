package dev.learning.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "book_lending")
public class BookLending extends PanacheEntity {
    @ManyToOne(optional = false)
    public Book book;

    @ManyToOne(optional = false)
    public Member member;

    @Column(name = "borrowed_at", nullable = false)
    public LocalDate borrowedAt;

    @Column(name = "due_date", nullable = false)
    public LocalDate dueDate;

    @Column(name = "returned_at")
    public LocalDate returnedAt;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    public LendingStatus status;
}
