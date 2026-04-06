package dev.learning.domain.type;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void validEmail() {
        var email = new Email("alice@example.com");
        assertThat(email.value()).isEqualTo("alice@example.com");
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new Email(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMissingAt() {
        assertThatThrownBy(() -> new Email("not-an-email"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
