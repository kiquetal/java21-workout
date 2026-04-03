package dev.learning.resource.mapper;

import dev.learning.dto.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.sql.SQLException;
import org.jboss.logging.Logger;

@Provider
public class DatabaseConnectionMapper implements ExceptionMapper<SQLException> {

    private static final Logger LOG = Logger.getLogger(DatabaseConnectionMapper.class);

    @Override
    public Response toResponse(SQLException e) {
        LOG.errorf("Database failure [SQLState=%s]: %s", e.getSQLState(), e.getMessage());
        return Response.status(503)
            .entity(new ErrorResponse("Service temporarily unavailable"))
            .build();
    }
}
