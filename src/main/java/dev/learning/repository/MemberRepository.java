package dev.learning.repository;

import dev.learning.domain.entity.Member;
import dev.learning.domain.type.Email;
import dev.learning.domain.type.member.MemberId;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;

import java.util.Optional;

@ApplicationScoped
public class MemberRepository implements PanacheRepository<Member> {

    public Optional<Member> findByMemberId(MemberId memberId)
    {
        return find("id", memberId.value())
                .firstResultOptional();
    }

    public Optional<Member> findByMemberIdForUpdate(MemberId memberId)
    {
        return find("id", memberId.value())
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .firstResultOptional();
    }

    public boolean existsByEmail(Email email) {
        return count("email", email.value()) > 0;
    }
}
