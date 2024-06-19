package dev.naiarievilo.todoapp.roles;

import java.util.List;

public interface RoleService {

    boolean roleExists(Roles role);

    Role getRole(Roles role);

    List<Role> getAllRoles();

    void createRole(Roles roles);

    void deleteRole(Roles role);

}
