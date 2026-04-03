package dev.learning.repository;

import dev.learning.domain.entity.Member;
import dev.learning.domain.type.Email;
import dev.learning.domain.type.MemberId;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class MemberRepository implements PanacheRepository<Member> {

    public Optional<Member> findByMemberId(MemberId memberId) {
        return findByIdOptional(memberId.value());
    }

    public boolean existsByEmail(Email email) {
        return count("email", email.value()) > 0;
    }
}
