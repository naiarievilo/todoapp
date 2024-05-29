package dev.naiarievilo.todoapp.users.info;

public class UserInfoAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "User info already exists";

    public UserInfoAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
}
