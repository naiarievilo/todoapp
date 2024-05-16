package dev.naiarievilo.todoapp.permissions;

import org.apache.commons.lang3.Validate;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_BLANK;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_NULL;

public enum Permissions {
    READ("Can access resources"),
    WRITE("Can create or modify resources"),
    DELETE("Can delete resources");

    static final Set<Permissions> permissionsSet =
        Arrays.stream(Permissions.values()).collect(Collectors.toCollection(LinkedHashSet::new));
    private final String description;

    Permissions(String description) {
        this.description = description;
    }

    public static Permissions toPermissions(GrantedAuthority permission) {
        Validate.notNull(permission, NOT_NULL.message());

        String permissionName = permission.getAuthority();
        for (Permissions permissionEnum : permissionsSet) {
            if (permissionEnum.name().equals(permissionName)) {
                return permissionEnum;
            }
        }

        throw new PermissionNotFoundException();
    }

    public static Set<Permissions> permissions() {
        return permissionsSet;
    }

    public static boolean hasPermission(String permissionName) {
        Validate.notBlank(permissionName, NOT_BLANK.message());

        for (Permissions permission : permissionsSet) {
            if (permission.name().equals(permissionName)) {
                return true;
            }
        }

        return false;
    }

    public String description() {
        return description;
    }
}
