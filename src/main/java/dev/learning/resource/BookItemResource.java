package dev.learning.resource;

import dev.learning.dto.BookItemRequestAdd;
import dev.learning.service.BookItemService;
import dev.learning.service.MemberService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/api/book-items")

public class BookItemResource
{

    @Inject
    BookItemService bookItemService;
    @Inject
    MemberService memberService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid BookItemRequestAdd request)

    {

        return Response.ok().build();
    }


}
