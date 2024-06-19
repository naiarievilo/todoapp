package dev.naiarievilo.todoapp.roles;

import dev.naiarievilo.todoapp.roles.dtos.RoleDTO;
import dev.naiarievilo.todoapp.roles.dtos.RolesDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public RolesDTO getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles().stream()
            .map(role -> new RoleDTO(role.getName(), role.getDescription()))
            .toList();
        return new RolesDTO(roles);
    }
}
