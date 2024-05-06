package dev.naiarievilo.todoapp.permissions;

public class PermissionNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Permission not found";

    public PermissionNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

}
