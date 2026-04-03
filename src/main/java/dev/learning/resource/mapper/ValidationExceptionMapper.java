package dev.learning.resource.mapper;

import dev.learning.resource.exceptions.ValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException>
{
    @Override
    public Response toResponse(ValidationException exception)
    {
        return  Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
    }
}
