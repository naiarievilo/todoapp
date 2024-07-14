package dev.naiarievilo.todoapp.roles;

import dev.naiarievilo.todoapp.roles.exceptions.RoleAlreadyExistsException;
import dev.naiarievilo.todoapp.roles.exceptions.RoleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public void createRole(Roles role) {
        if (roleExists(role)) {
            throw new RoleAlreadyExistsException(role.name());
        }

        Role newRole = new Role();
        newRole.setName(role.name());
        newRole.setDescription(role.description());
        roleRepository.persist(newRole);
    }

    public boolean roleExists(Roles role) {
        return roleRepository.findByName(role.name()).isPresent();
    }

    @Transactional
    public void deleteRole(Roles role) {
        Role roleToDelete = getRole(role);
        roleToDelete.unassignUsers();
        roleRepository.delete(roleToDelete);
    }

    public Role getRole(Roles role) {
        return roleRepository.findByName(role.name()).orElseThrow(() -> new RoleNotFoundException(role.name()));
    }

}
