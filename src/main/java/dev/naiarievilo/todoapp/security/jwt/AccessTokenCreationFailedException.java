package dev.naiarievilo.todoapp.security.jwt;

public class AccessTokenCreationFailedException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Failed to create access token";

    public AccessTokenCreationFailedException() {
        super(DEFAULT_MESSAGE);
    }

    public AccessTokenCreationFailedException(String message) {
        super(message);
    }
}
