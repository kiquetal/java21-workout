package dev.learning.resource;

import dev.learning.service.BookItemService;
import dev.learning.service.MemberService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

@ApplicationScoped
@Path("/api/book-items")
public class BookItemResource
{

    @Inject
    private BookItemService bookItemService;
    @Inject
    private MemberService memberService;



}
