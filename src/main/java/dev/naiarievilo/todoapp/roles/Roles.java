package dev.naiarievilo.todoapp.roles;

import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_BLANK;

public enum Roles {
    ROLE_ADMIN("Superuser role"),
    ROLE_USER("Default account role");

    static final Set<Roles> rolesSet =
        Arrays.stream(Roles.values()).collect(Collectors.toCollection(LinkedHashSet::new));
    private final String description;

    Roles(String description) {
        this.description = description;
    }

    public static Optional<Roles> getRole(String role) {
        Validate.notBlank(role, NOT_BLANK.message());

        for (Roles roles : rolesSet) {
            if (roles.name().equals(role)) {
                return Optional.of(roles);
            }
        }

        return Optional.empty();
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
