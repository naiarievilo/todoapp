package dev.naiarievilo.todoapp.permissions;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private static final String PERMISSION_NOT_NULL = "Permission cannot be null";
    private static final String DESCRIPTION_NOT_BLANK = "Description cannot be blank";

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
        Validate.notNull(permission, PERMISSION_NOT_NULL);
        return permissionRepository.findByName(permission.name()).orElseThrow(PermissionNotFoundException::new);
    }

    @Override
    @Transactional
    public void createPermission(Permissions permission) {
        Validate.notNull(permission, PERMISSION_NOT_NULL);

        String description = permission.getDescription();
        Validate.notBlank(description, DESCRIPTION_NOT_BLANK);

        Permission newPermission = new Permission();
        newPermission.setName(permission.name());
        newPermission.setDescription(description);

        permissionRepository.persist(newPermission);
    }

    @Override
    @Transactional
    public void deletePermission(Permissions permission) {
        Validate.notNull(permission, PERMISSION_NOT_NULL);
        permissionRepository.deleteByName(permission.name());
    }
}
