package dev.learning.resource.mapper;

import dev.learning.dto.ErrorResponse;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.logging.Logger;

@Provider
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {

    private static final Logger LOG = Logger.getLogger(PersistenceExceptionMapper.class);

    @Override
    public Response toResponse(PersistenceException e) {
        if (e.getCause() instanceof ConstraintViolationException cve) {
            LOG.errorf("Constraint violation: %s — SQL: %s", cve.getConstraintName(), cve.getSQL());
            return Response.status(409)
                .entity(new ErrorResponse("Constraint violation: %s".formatted(cve.getConstraintName())))
                .build();
        }
        LOG.error("Unexpected persistence error", e);
        return Response.status(500)
            .entity(new ErrorResponse("Unexpected database error"))
            .build();
    }
}
