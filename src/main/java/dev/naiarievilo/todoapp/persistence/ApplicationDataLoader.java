package dev.naiarievilo.todoapp.persistence;

import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.Roles;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class ApplicationDataLoader {

    private final RoleService roleService;

    public ApplicationDataLoader(RoleService roleService) {
        this.roleService = roleService;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        loadRoles();
    }

    public void loadRoles() {
        List<Roles> roles = Roles.roles();
        for (Roles role : roles) {
            if (!roleService.roleExists(role)) {
                roleService.createRole(role);
            }
        }
    }
}
