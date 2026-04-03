package dev.learning.resource;

import dev.learning.domain.BookId;
import dev.learning.domain.BookLending;
import dev.learning.domain.LendCommand;
import dev.learning.domain.LendingResult.BookNotAvailable;
import dev.learning.domain.LendingResult.MemberNotFound;
import dev.learning.domain.LendingResult.Success;
import dev.learning.domain.MemberId;
import dev.learning.dto.ErrorResponse;
import dev.learning.dto.LendRequest;
import dev.learning.dto.LendingResponse;
import dev.learning.service.LendingService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/lendings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LendingResource {

    @Inject
    LendingService lendingService;

    @POST
    public Response lend(@Valid LendRequest request) {
        var command = new LendCommand(
            new BookId(request.bookId()),
            new MemberId(request.memberId()),
            request.dueDate()
        );
        var result = lendingService.lend(command);
        return switch (result) {
            case Success(var lending) -> Response.ok(toResponse(lending)).build();
            case BookNotAvailable(var isbn) -> Response.status(409).entity(new ErrorResponse("Book %s not available".formatted(isbn))).build();
            case MemberNotFound(var id) -> Response.status(404).entity(new ErrorResponse("Member %d not found".formatted(id))).build();
        };
    }

    private LendingResponse toResponse(BookLending lending) {
        return new LendingResponse(lending.id, lending.book.title, lending.member.name, lending.dueDate);
    }
}
