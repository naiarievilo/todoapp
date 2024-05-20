package dev.naiarievilo.todoapp.roles;

import java.util.Collection;
import java.util.Set;

public interface RoleService {

    boolean roleExists(Roles role);

    Role getRole(Roles role);

    Set<Role> getRoles(Collection<Roles> roles);

    Set<Role> getAllRoles();

    void createRole(Roles roles);

    void deleteRole(Roles role);

}
