package dev.naiarievilo.todoapp.persistence;

import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserService;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.naiarievilo.todoapp.roles.Roles.ROLE_ADMIN;

@Component
@Transactional
public class ApplicationDataLoader {

    private final RoleService roleService;
    private final UserService userService;
    private final String adminEmail;
    private final String adminPassword;

    public ApplicationDataLoader(RoleService roleService, UserService userService,
        @Value("${admin.email}") String adminEmail, @Value("${admin.password}") String adminPassword) {
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.roleService = roleService;
        this.userService = userService;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        loadRoles();
        loadAdminUser();
    }

    public void loadRoles() {
        List<Roles> roles = Roles.roles();
        for (Roles role : roles) {
            if (!roleService.roleExists(role)) {
                roleService.createRole(role);
            }
        }
    }

    public void loadAdminUser() {
        if (userService.userExists(adminEmail)) {
            return;
        }

        var adminDTO = new UserCreationDTO(adminEmail, adminPassword, null, "Server", "Administrator");
        User adminUser = userService.createUser(adminDTO);
        userService.addRoleToUser(adminUser, ROLE_ADMIN);
        userService.authenticateUser(adminUser);
    }
}
