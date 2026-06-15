package dev.learning.domain.command;

import dev.learning.domain.type.book_item.BookItemId;
import dev.learning.domain.type.member.MemberId;

public record LendCommand(BookItemId bookItemId, MemberId memberId){}

