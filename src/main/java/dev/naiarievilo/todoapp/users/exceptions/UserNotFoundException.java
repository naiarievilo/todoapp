package dev.naiarievilo.todoapp.users.exceptions;

public class UserNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "User not found";

    public UserNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public UserNotFoundException(String email) {
        super("User with email '" + email + "' not found");
    }

    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found");
    }

}
