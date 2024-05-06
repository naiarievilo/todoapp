package dev.naiarievilo.todoapp.permissions;

import java.util.List;

public interface PermissionService {

    List<Permission> getAllPermissions();

    Permission getPermission(Permissions permission);

    void createPermission(Permissions permission);

    void deletePermission(Permissions permission);
}
