package dev.naiarievilo.todoapp.roles;

import dev.naiarievilo.todoapp.permissions.Permissions;

import java.util.List;
import java.util.Set;

public interface RoleService {

    Role getRole(Roles role);

    Set<Role> getAllRoles();

    void createRole(Roles role, List<Permissions> permissions);

    void deleteRole(Roles role);

    void addPermissionToRole(Roles role, Permissions permission);

    void removePermissionFromRole(Roles role, Permissions permission);

}
