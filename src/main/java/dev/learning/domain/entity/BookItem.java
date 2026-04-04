package dev.learning.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class BookItem extends PanacheEntity
{
    @ManyToOne
    Book book;

}
