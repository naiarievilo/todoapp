package dev.naiarievilo.todoapp.roles;

import dev.naiarievilo.todoapp.permissions.Permissions;

import java.util.List;

public interface RoleService {

    Role getRole(Roles role);

    List<Role> getRoles();

    void createRole(Roles role, List<Permissions> permissions);

    void deleteRole(Roles role);

    void addPermissionToRole(Roles role, Permissions permission);

    void removePermissionFromRole(Roles role, Permissions permission);

}
