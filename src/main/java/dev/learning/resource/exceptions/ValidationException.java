package dev.learning.resource.exceptions;

public class ValidationException extends RuntimeException
{
    public ValidationException(String message)
    {
        super(message);
    }
}
