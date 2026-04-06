package dev.learning.resource.mapper;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import dev.learning.dto.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;

@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<MismatchedInputException> {

    @Override
    public Response toResponse(MismatchedInputException e) {
        var field = e.getPath().stream()
            .map(Reference::getFieldName)
            .collect(Collectors.joining("."));
        var value = (e instanceof InvalidFormatException ife) ? ife.getValue() : "invalid";
        return Response.status(400)
            .entity(new ErrorResponse("Invalid value for '%s': %s".formatted(field, value)))
            .build();
    }
}
