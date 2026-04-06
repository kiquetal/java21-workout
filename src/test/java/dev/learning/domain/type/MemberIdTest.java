package dev.learning.domain.type;

import dev.learning.domain.type.member.MemberId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberIdTest {

    @Test
    void validId() {
        var id = new MemberId(42L);
        assertThat(id.value()).isEqualTo(42L);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new MemberId(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegative() {
        assertThatThrownBy(() -> new MemberId(-5L))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
