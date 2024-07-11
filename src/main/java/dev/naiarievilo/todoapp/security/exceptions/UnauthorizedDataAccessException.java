package dev.naiarievilo.todoapp.security.exceptions;

public class UnauthorizedDataAccessException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "User is not authorized to access this resource";

    public UnauthorizedDataAccessException() {
        super(DEFAULT_MESSAGE);
    }
}
