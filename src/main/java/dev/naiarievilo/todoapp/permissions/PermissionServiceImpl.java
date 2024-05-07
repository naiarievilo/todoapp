package dev.naiarievilo.todoapp.permissions;

import dev.naiarievilo.todoapp.roles.Role;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.*;

@Service
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public Set<Permission> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return new LinkedHashSet<>(permissions);
    }

    @Override
    public Permission getPermission(Permissions permission) {
        Validate.notNull(permission, NOT_NULL.message());
        return permissionRepository.findByName(permission.name()).orElseThrow(PermissionNotFoundException::new);
    }

    @Override
    public Set<Permission> getPermissions(Collection<Permissions> permissions) {
        Validate.noNullElements(permissions, NO_NULL_ELEMENTS.message());
        return permissions.stream()
            .map(this::getPermission)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public void createPermission(Permissions permission) {
        Validate.notNull(permission, NOT_NULL.message());

        String description = permission.description();
        Validate.notBlank(description, NOT_BLANK.message());

        Permission newPermission = new Permission();
        newPermission.setName(permission.name());
        newPermission.setDescription(description);

        permissionRepository.persist(newPermission);
    }

    @Override
    @Transactional
    public void deletePermission(Permissions permission) {
        Validate.notNull(permission, NOT_NULL.message());

        Permission permissionToDelete = getPermission(permission);
        for (Role role : permissionToDelete.getRoles()) {
            role.removePermission(permissionToDelete);
        }

        permissionRepository.delete(permissionToDelete);
    }
}
