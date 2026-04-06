package dev.learning.domain.type;

import dev.learning.domain.type.books.Isbn;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IsbnTest {

    @Test
    void valid13DigitIsbn() {
        var isbn = new Isbn("9780132350884");
        assertThat(isbn.value()).isEqualTo("9780132350884");
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new Isbn(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid ISBN");
    }

    @Test
    void rejectsTooShort() {
        assertThatThrownBy(() -> new Isbn("978013235"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsLetters() {
        assertThatThrownBy(() -> new Isbn("978013235088X"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
