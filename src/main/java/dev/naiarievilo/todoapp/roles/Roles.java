package dev.naiarievilo.todoapp.roles;

import org.apache.commons.lang3.Validate;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_BLANK;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_NULL;

public enum Roles {
    ROLE_ADMIN("Superuser role"),
    ROLE_USER("Default account role");

    static final Set<Roles> rolesSet =
        Arrays.stream(Roles.values()).collect(Collectors.toCollection(LinkedHashSet::new));
    private final String description;

    Roles(String description) {
        this.description = description;
    }

    public static Roles toRoles(GrantedAuthority role) {
        Validate.notNull(role, NOT_NULL.message());

        String roleName = role.getAuthority();
        for (Roles roles : rolesSet) {
            if (roles.name().equals(roleName)) {
                return roles;
            }
        }

        throw new RoleNotFoundException();
    }

    public static Set<Roles> roles() {
        return rolesSet;
    }

    public static boolean hasRole(String role) {
        Validate.notNull(role, NOT_BLANK.message());

        for (Roles roles : rolesSet) {
            if (roles.name().equals(role)) {
                return true;
            }
        }

        return false;
    }

    public String description() {
        return description;
    }

}
