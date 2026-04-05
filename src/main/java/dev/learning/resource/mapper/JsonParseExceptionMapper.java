package dev.learning.resource.mapper;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import dev.learning.dto.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;

@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<InvalidFormatException> {

    @Override
    public Response toResponse(InvalidFormatException e) {
        var field = e.getPath().stream()
            .map(JsonMappingException.Reference::getFieldName)
            .collect(Collectors.joining("."));
        return Response.status(400)
            .entity(new ErrorResponse("Invalid value for '%s': %s".formatted(field, e.getValue())))
            .build();
    }
}
