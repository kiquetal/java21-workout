package dev.learning.domain.type;

import dev.learning.domain.type.book_item.BookId;
import dev.learning.domain.type.member.MemberId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypeSafetyTest {

    @Test
    void bookIdAndMemberIdAreDistinctTypes() {
        var bookId = new BookId(1L);
        var memberId = new MemberId(1L);

        assertThat(bookId).isNotEqualTo(memberId);
        assertThat(bookId.value()).isEqualTo(memberId.value());
    }
}
