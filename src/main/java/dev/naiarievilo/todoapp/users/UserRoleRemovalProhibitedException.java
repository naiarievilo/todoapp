package dev.naiarievilo.todoapp.users;

public class UserRoleRemovalProhibitedException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "User role, the default role, removal not allowed";

    public UserRoleRemovalProhibitedException() {
        super(DEFAULT_MESSAGE);
    }

}
