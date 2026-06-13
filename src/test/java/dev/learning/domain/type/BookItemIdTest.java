package dev.learning.domain.type;

import dev.learning.domain.type.book_item.BookItemId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookItemIdTest {

    @Test
    void validId() {
        var id = new BookItemId(1L);
        assertThat(id.value()).isEqualTo(1L);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new BookItemId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid book item ID");
    }

    @Test
    void rejectsZero() {
        assertThatThrownBy(() -> new BookItemId(0L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegative() {
        assertThatThrownBy(() -> new BookItemId(-1L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalityByValue() {
        assertThat(new BookItemId(1L)).isEqualTo(new BookItemId(1L));
        assertThat(new BookItemId(1L)).isNotEqualTo(new BookItemId(2L));
    }
}
