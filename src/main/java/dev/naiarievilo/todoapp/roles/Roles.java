package dev.naiarievilo.todoapp.roles;

import org.apache.commons.lang3.Validate;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.List;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.IS_BLANK;

public enum Roles {
    ROLE_ADMIN("Superuser role"),
    ROLE_USER("Default account role");

    static final List<Roles> rolesList = Arrays.stream(Roles.values()).toList();
    private final String description;

    Roles(String description) {
        this.description = description;
    }

    public static Roles toRole(GrantedAuthority role) {
        String roleName = role.getAuthority();
        for (Roles roles : rolesList) {
            if (roles.name().equals(roleName)) {
                return roles;
            }
        }

        throw new RoleNotFoundException(roleName);
    }

    public static List<Roles> roles() {
        return rolesList;
    }

    public static boolean hasRole(String roleName) {
        Validate.notBlank(roleName, IS_BLANK, "roleName");

        for (Roles role : rolesList) {
            if (role.name().equals(roleName)) {
                return true;
            }
        }
        return false;
    }

    public static Roles getRole(String roleName) {
        Validate.notBlank(roleName, IS_BLANK, "roleName");

        for (Roles role : rolesList) {
            if (role.name().equals(roleName)) {
                return role;
            }
        }

        throw new RoleNotFoundException(roleName);
    }

    public String description() {
        return description;
    }
}
