package dev.learning.domain.entity;

import dev.learning.domain.LendingStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "book_lending")
public class BookLending extends PanacheEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "book_item_id", nullable = false)
    public BookItem bookItem;

    @ManyToOne(optional = false)

    public Member member;

    @Column(name = "borrowed_at", nullable = false)
    public Instant borrowedAt;

    @Column(name = "due_date", nullable = false)
    public Instant dueDate;

    @Column(name = "returned_at")
    public Instant returnedAt;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    public LendingStatus status;
}
