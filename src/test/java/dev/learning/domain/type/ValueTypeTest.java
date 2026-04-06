package dev.learning.domain.type;

import dev.learning.domain.type.book_item.BookId;
import dev.learning.domain.type.books.Isbn;
import dev.learning.domain.type.member.MemberId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure domain tests — no Quarkus, no DB, no container.
 * These run in milliseconds. Test the "parse, don't validate" boundary.
 */
class ValueTypeTest {

    @Nested
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

    @Nested
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

    @Nested
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

    @Nested
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

    @Nested
    class TypeSafetyTest {
        /**
         * Demonstrates why value types exist — compiler prevents mixing IDs.
         * This test documents the design intent, not runtime behavior.
         */
        @Test
        void bookIdAndMemberIdAreDistinctTypes() {
            var bookId = new BookId(1L);
            var memberId = new MemberId(1L);

            // Same underlying value, but different types — can't be confused
            assertThat(bookId).isNotEqualTo(memberId);
            assertThat(bookId.value()).isEqualTo(memberId.value());
        }
    }
}
