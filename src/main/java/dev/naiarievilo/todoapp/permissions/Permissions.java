package dev.naiarievilo.todoapp.permissions;

import java.util.Arrays;
import java.util.List;

public enum Permissions {
    READ("Can access resources"),
    WRITE("Can create or modify resources"),
    DELETE("Can delete resources");

    static final List<String> permissionNames = Arrays.stream(Permissions.values()).map(Permissions::name).toList();
    private final String description;

    Permissions(String description) {
        this.description = description;
    }

    static List<String> permissionNames() {
        return permissionNames;
    }

    static boolean hasPermission(String permission) {
        return permissionNames.contains(permission);
    }

    public String description() {
        return description;
    }
}
