package dev.learning.resource;

import dev.learning.domain.command.CreateMemberCommand;
import dev.learning.domain.result.CreateMemberResult.EmailAlreadyExists;
import dev.learning.domain.result.CreateMemberResult.Success;
import dev.learning.domain.type.Email;
import dev.learning.dto.ErrorResponse;
import dev.learning.dto.MemberRequest;
import dev.learning.dto.MemberResponse;
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
    public Response create(@Valid MemberRequest request) {
        LOG.tracev("TRACE — raw request: name={0}, email={1}", request.name(), request.email());
        LOG.debugv("DEBUG — parsing DTO to domain types");
        LOG.infov("INFO — creating member: {0}", request.name());
        LOG.warnv("WARN — demo warning for member: {0}", request.name());
        LOG.errorv("ERROR — demo error for member: {0}", request.name());
        // DTO → Domain (parsing boundary)
        var command = new CreateMemberCommand(
            request.name(),
            new Email(request.email())   // raw string → validated Email
        );

        // Domain → Result
        var result = memberService.create(command);

        // Result → DTO (response)
        return switch (result) {
            case Success(var member) -> Response.status(201)
                .entity(new MemberResponse(member.id, member.name, member.email))
                .build();
            case EmailAlreadyExists(var email) -> Response.status(409)
                .entity(new ErrorResponse("Email %s already registered".formatted(email)))
                .build();
        };
    }
}
