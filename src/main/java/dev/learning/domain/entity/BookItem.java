package dev.learning.domain.entity;

import dev.learning.domain.type.book_item.BookItemStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class BookItem extends PanacheEntity
{
    @ManyToOne
    @JoinColumn(name="book_id")
    Book book;;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    BookItemStatus status;
    String notes;

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public Instant getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate)
    {
        this.creationDate = creationDate;
    }

    Instant creationDate;
    Instant updateDate;

    public Instant getUpdateDate()
    {
        return updateDate;
    }

    public void setUpdateDate(Instant updateDate)
    {
        this.updateDate = updateDate;
    }

    public BookItemStatus getStatus()
    {
        return status;
    }

    public void setStatus(BookItemStatus status)
    {
        this.status = status;
    }

    public Book getBook()
    {
        return book;
    }

    public void setBook(Book book)
    {
        this.book = book;
    }
}
