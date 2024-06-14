package dev.naiarievilo.todoapp.persistence;

import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.Roles;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationDataLoader {

    private final RoleService roleService;

    public ApplicationDataLoader(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostConstruct
    public void loadData() {
        loadRoles();
    }

    private void loadRoles() {
        List<Roles> roles = Roles.roles();

        for (Roles role : roles) {
            if (!roleService.roleExists(role)) {
                roleService.createRole(role);
            }
        }
    }
}
