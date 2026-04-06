package dev.learning.domain.type;

import dev.learning.domain.type.book_item.BookId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookIdTest {

    @Test
    void validId() {
        var id = new BookId(1L);
        assertThat(id.value()).isEqualTo(1L);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new BookId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid book ID");
    }

    @Test
    void rejectsZero() {
        assertThatThrownBy(() -> new BookId(0L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegative() {
        assertThatThrownBy(() -> new BookId(-1L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalityByValue() {
        assertThat(new BookId(1L)).isEqualTo(new BookId(1L));
        assertThat(new BookId(1L)).isNotEqualTo(new BookId(2L));
    }
}
