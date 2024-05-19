package dev.naiarievilo.todoapp.persistence;

import dev.naiarievilo.todoapp.permissions.PermissionService;
import dev.naiarievilo.todoapp.permissions.Permissions;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.Roles;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataLoader {

    private final RoleService roleService;
    private final PermissionService permissionService;

    public DataLoader(RoleService roleService, PermissionService permissionService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    @PostConstruct
    public void loadData() {
        loadPermissions();
        loadRoles();
    }

    private void loadPermissions() {
        Set<Permissions> permissions = Permissions.permissions();

        for (Permissions permission : permissions) {
            if (!permissionService.permissionExists(permission)) {
                permissionService.createPermission(permission);
            }
        }
    }

    private void loadRoles() {
        Set<Roles> roles = Roles.roles();

        for (Roles role : roles) {
            if (!roleService.roleExists(role)) {
                Set<Permissions> permissions = Permissions.permissions();
                roleService.createRole(role, permissions);
            }
        }
    }
}
