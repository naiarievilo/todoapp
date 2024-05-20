package dev.naiarievilo.todoapp.persistence;

import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.Roles;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataLoader {

    private final RoleService roleService;

    public DataLoader(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostConstruct
    public void loadData() {
        loadRoles();
    }

    private void loadRoles() {
        Set<Roles> roles = Roles.roles();

        for (Roles role : roles) {
            if (!roleService.roleExists(role)) {
                roleService.createRole(role);
            }
        }
    }
}
