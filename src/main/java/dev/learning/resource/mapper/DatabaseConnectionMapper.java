package dev.learning.resource.mapper;

import dev.learning.dto.ErrorResponse;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.sql.SQLException;

@Provider
public class DatabaseConnectionMapper implements ExceptionMapper<SQLException> {

    @Override
    public Response toResponse(SQLException e) {
        Log.errorf("Database failure [SQLState=%s]: %s", e.getSQLState(), e.getMessage());
        return Response.status(503)
            .entity(new ErrorResponse("Service temporarily unavailable"))
            .build();
    }
}
