package dev.learning.resource.mapper;

import dev.learning.dto.ErrorResponse;
import io.quarkus.logging.Log;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.exception.ConstraintViolationException;

@Provider
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {

    @Override
    public Response toResponse(PersistenceException e) {
        if (e.getCause() instanceof ConstraintViolationException cve) {
            Log.errorf("Constraint violation: %s — SQL: %s", cve.getConstraintName(), cve.getSQL());
            return Response.status(409)
                .entity(new ErrorResponse("Constraint violation: %s".formatted(cve.getConstraintName())))
                .build();
        }
        Log.error("Unexpected persistence error", e);
        return Response.status(500)
            .entity(new ErrorResponse("Unexpected database error"))
            .build();
    }
}
