package dev.naiarievilo.todoapp.roles;

import dev.naiarievilo.todoapp.permissions.Permission;
import dev.naiarievilo.todoapp.permissions.PermissionService;
import dev.naiarievilo.todoapp.permissions.Permissions;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.*;

@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionService permissionService;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionService permissionService) {
        this.roleRepository = roleRepository;
        this.permissionService = permissionService;
    }

    @Override
    public Role getRole(Roles role) {
        Validate.notNull(role, ROLE_NOT_NULL.message());
        return roleRepository.findByName(role.name()).orElseThrow(RoleNotFoundException::new);
    }

    @Override
    public Set<Role> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return new LinkedHashSet<>(roles);
    }

    @Override
    @Transactional
    public void createRole(Roles role, List<Permissions> permissions) {
        Validate.notNull(role, ROLE_NOT_NULL.message());
        Validate.noNullElements(permissions, PERMISSIONS_NOT_NULL_OR_EMPTY.message());

        String roleName = role.name();
        String description = role.description();

        Validate.notBlank(description, DESCRIPTION_NOT_BLANK.message());

        Set<Permission> permissionSet = permissions.stream()
            .map(permissionService::getPermission)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Role newRole = new Role();
        newRole.setName(roleName);
        newRole.setDescription(description);
        newRole.setPermissions(permissionSet);

        roleRepository.persist(newRole);
    }

    @Override
    @Transactional
    public void deleteRole(Roles role) {
        Validate.notNull(role, ROLE_NOT_NULL.message());
        roleRepository.deleteByName(role.name());
    }

    @Override
    @Transactional
    public void addPermissionToRole(Roles role, Permissions permission) {
        Validate.notNull(role, ROLE_NOT_NULL.message());
        Validate.notNull(permission, PERMISSIONS_NOT_NULL_OR_EMPTY.message());

        Role targetRole = roleRepository.findByName(role.name()).orElseThrow(RoleNotFoundException::new);
        Permission permissionToAdd = permissionService.getPermission(permission);

        targetRole.addPermission(permissionToAdd);
    }

    @Override
    @Transactional
    public void removePermissionFromRole(Roles role, Permissions permission) {
        Validate.notNull(role, ROLE_NOT_NULL.message());
        Validate.notNull(permission, PERMISSIONS_NOT_NULL_OR_EMPTY.message());

        Role targetRole = roleRepository.findByName(role.name()).orElseThrow(RoleNotFoundException::new);
        Permission permissionToRemove = permissionService.getPermission(permission);

        targetRole.removePermission(permissionToRemove);
    }

}
