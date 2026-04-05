package dev.learning.domain.entity;

import dev.learning.domain.type.member.MemberStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Member extends PanacheEntity {
    @Column(nullable = false)
    public String name;
    @Column(unique = true, nullable = false)
    public String email;
    @Column(name="address")
    public String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MemberStatus status;
}
