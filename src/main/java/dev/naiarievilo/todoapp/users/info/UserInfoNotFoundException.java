package dev.naiarievilo.todoapp.users.info;

public class UserInfoNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "User info not found";

    public UserInfoNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
}
