package dev.learning.resource;

import dev.learning.dto.MemberRequest;
import dev.learning.service.MemberService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/api/members")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MemberResource {

    private static final Logger LOG = Logger.getLogger(MemberResource.class);

    @Inject
    MemberService memberService;

    @POST
    public Response create(@Valid MemberRequest request)
    {

        return Response.ok().build();
    }
}
