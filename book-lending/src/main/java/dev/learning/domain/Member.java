package dev.learning.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Member extends PanacheEntity {
    @Column(nullable = false)
    public String name;

    @Column(unique = true, nullable = false)
    public String email;
}
