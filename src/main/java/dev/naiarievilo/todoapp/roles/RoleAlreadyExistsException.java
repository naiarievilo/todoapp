package dev.naiarievilo.todoapp.roles;

public class RoleAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Role already exists";

    public RoleAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public RoleAlreadyExistsException(String roleName) {
        super("Role '" + roleName + "' already exists");
    }

}
