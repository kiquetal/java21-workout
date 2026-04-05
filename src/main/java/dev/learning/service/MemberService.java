package dev.learning.service;

import dev.learning.domain.command.CreateMemberCommand;
import dev.learning.repository.MemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;


@ApplicationScoped
public class MemberService {

    @Inject
    MemberRepository memberRepository;

    @Transactional
    public Response create(CreateMemberCommand command) {

        return Response.ok().build();
    }
}
