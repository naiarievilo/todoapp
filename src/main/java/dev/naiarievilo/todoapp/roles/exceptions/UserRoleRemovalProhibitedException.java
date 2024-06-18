package dev.naiarievilo.todoapp.roles.exceptions;

public class UserRoleRemovalProhibitedException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Removal of the default role `ROLE_USER` is prohibited";

    public UserRoleRemovalProhibitedException() {
        super(DEFAULT_MESSAGE);
    }

}
