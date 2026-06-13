package dev.learning.resource;


import dev.learning.domain.command.LendCommand;
import dev.learning.domain.result.lending.LendingResult;
import dev.learning.domain.type.book_item.BookItemId;
import dev.learning.domain.type.lending.LendStatus;
import dev.learning.domain.type.member.MemberId;
import dev.learning.dto.ErrorResponse;
import dev.learning.dto.LendRequest;
import dev.learning.service.LendingService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.ZoneOffset;

@Path("/api/lendings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LendingResource
{

    @Inject
    LendingService lendingService;

    @POST
    public Response register(@Valid LendRequest request) {
        var command = new LendCommand(
            new BookItemId(request.bookId()),
            new MemberId(request.memberId()),
            java.time.Instant.now(),
            request.dueDate().atStartOfDay(ZoneOffset.UTC).toInstant(),
            LendStatus.LENT
        );

        var result = lendingService.lend(command);

        return switch (result) {
            case LendingResult.Success(var detail) -> Response.ok(detail).build();
            case LendingResult.AlreadyLent(var detail) -> Response.status(409).entity(new ErrorResponse("Book already lent")).build();
            case LendingResult.MemberNotFound(var id) -> Response.status(404).entity(new ErrorResponse("Member not found: " + id.value())).build();
            case LendingResult.BookNotFound(var id) -> Response.status(404).entity(new ErrorResponse("Book item not found: " + id)).build();
            case LendingResult.MemberHasOverdueBooks(var id, var books) -> Response.status(403).entity(new ErrorResponse("Member has overdue books")).build();
            case LendingResult.MaximumLimitReached(var id) -> Response.status(403).entity(new ErrorResponse("Maximum lending limit reached")).build();
        };
    }
}
