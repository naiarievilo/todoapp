package dev.naiarievilo.todoapp.users_info.exceptions;

public class UserInfoNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "User info not found";

    public UserInfoNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public UserInfoNotFoundException(Long userInfoId) {
        super("User info with id '" + userInfoId + "' not found");
    }

    public UserInfoNotFoundException(String message) {
        super(message);
    }
}
