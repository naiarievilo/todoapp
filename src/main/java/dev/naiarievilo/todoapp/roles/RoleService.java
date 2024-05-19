package dev.naiarievilo.todoapp.roles;

import dev.naiarievilo.todoapp.permissions.Permissions;

import java.util.Collection;
import java.util.Set;

public interface RoleService {

    boolean roleExists(Roles role);

    Role getRole(Roles role);

    Set<Role> getRoles(Collection<Roles> roles);

    Set<Role> getAllRoles();

    void createRole(Roles role, Collection<Permissions> permissions);

    void deleteRole(Roles role);

    void addPermissionToRole(Roles role, Permissions permission);

    void removePermissionFromRole(Roles role, Permissions permission);

}
