package dev.naiarievilo.todoapp.users;

import java.util.Arrays;
import java.util.List;

public enum Roles {
    ROLE_ADMIN,
    ROLE_USER;

    static final List<String> roles = Arrays.stream(Roles.values()).map(Roles::name).toList();

    static List<String> getRoles() {
        return roles;
    }

    static boolean hasRole(String role) {
        return roles.contains(role);
    }

}
