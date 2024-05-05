package dev.naiarievilo.todoapp.users;

import java.util.Arrays;
import java.util.List;

public enum Permissions {
    READ,
    WRITE,
    DELETE;

    static final List<String> permissions = Arrays.stream(Permissions.values()).map(Permissions::name).toList();

    static List<String> getPermissions() {
        return permissions;
    }

    static boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
