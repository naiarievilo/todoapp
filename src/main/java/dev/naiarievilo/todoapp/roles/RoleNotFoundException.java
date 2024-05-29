package dev.naiarievilo.todoapp.roles;

public class RoleNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Role not found";

    public RoleNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public RoleNotFoundException(String role) {
        super("Role '" + role + "' not found");
    }

}
