package dev.naiarievilo.todoapp.permissions;

import java.util.Collection;
import java.util.Set;

public interface PermissionService {

    boolean permissionExists(Permissions permission);

    Set<Permission> getAllPermissions();

    Permission getPermission(Permissions permission);

    Set<Permission> getPermissions(Collection<Permissions> permissions);

    void createPermission(Permissions permission);

    void deletePermission(Permissions permission);
}
