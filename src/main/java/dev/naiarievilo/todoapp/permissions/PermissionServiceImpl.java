package dev.naiarievilo.todoapp.permissions;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.DESCRIPTION_NOT_BLANK;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.PERMISSION_NOT_NULL;

@Service
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Override
    public Permission getPermission(Permissions permission) {
        Validate.notNull(permission, PERMISSION_NOT_NULL.message());
        return permissionRepository.findByName(permission.name()).orElseThrow(PermissionNotFoundException::new);
    }

    @Override
    @Transactional
    public void createPermission(Permissions permission) {
        Validate.notNull(permission, PERMISSION_NOT_NULL.message());

        String description = permission.description();
        Validate.notBlank(description, DESCRIPTION_NOT_BLANK.message());

        Permission newPermission = new Permission();
        newPermission.setName(permission.name());
        newPermission.setDescription(description);

        permissionRepository.persist(newPermission);
    }

    @Override
    @Transactional
    public void deletePermission(Permissions permission) {
        Validate.notNull(permission, PERMISSION_NOT_NULL.message());
        permissionRepository.deleteByName(permission.name());
    }
}
