package dev.naiarievilo.todoapp.users;

public class UserAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "User already exists";

    public UserAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public UserAlreadyExistsException(Long id) {
        super("User with id " + id + " already exists");
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }

}
