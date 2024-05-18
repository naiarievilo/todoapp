package dev.naiarievilo.todoapp.users;

public class UserAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "User already exists";

    public UserAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

}
