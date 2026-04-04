package dev.learning.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.Set;

@Entity
public class Member extends PanacheEntity {
    @Column(nullable = false)
    public String name;
    @Column(unique = true, nullable = false)
    public String email;
    @Column(name="address")
    public String address;
}
