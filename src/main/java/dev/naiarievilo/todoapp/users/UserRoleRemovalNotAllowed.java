package dev.naiarievilo.todoapp.users;

public class UserRoleRemovalNotAllowed extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "User role, the default role, removal not allowed";

    public UserRoleRemovalNotAllowed() {
        super(DEFAULT_MESSAGE);
    }

}
