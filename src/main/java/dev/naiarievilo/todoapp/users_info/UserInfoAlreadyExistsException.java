package dev.naiarievilo.todoapp.users_info;

public class UserInfoAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "User info already exists";

    public UserInfoAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public UserInfoAlreadyExistsException(Long userInfoId) {
        super("User info with id " + userInfoId + " already exists");
    }

    public UserInfoAlreadyExistsException(String message) {
        super(message);
    }
}
