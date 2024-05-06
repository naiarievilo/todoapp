package dev.naiarievilo.todoapp.roles;

import java.util.Arrays;
import java.util.List;

public enum Roles {
    ROLE_ADMIN("Superuser role"),
    ROLE_USER("Default account role");

    static final List<String> roleNames = Arrays.stream(Roles.values()).map(Roles::name).toList();
    private final String description;

    Roles(String description) {
        this.description = description;
    }

    public static List<String> roleNames() {
        return roleNames;
    }

    public static boolean hasRole(String role) {
        return roleNames.contains(role);
    }

    public String description() {
        return description;
    }

}
