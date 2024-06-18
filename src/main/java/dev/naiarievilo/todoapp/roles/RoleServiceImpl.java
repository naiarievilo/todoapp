package dev.naiarievilo.todoapp.roles;

import dev.naiarievilo.todoapp.roles.exceptions.RoleAlreadyExistsException;
import dev.naiarievilo.todoapp.roles.exceptions.RoleNotFoundException;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public boolean roleExists(Roles role) {
        return roleRepository.findByName(role.name()).isPresent();
    }

    @Override
    public Role getRole(Roles role) {
        return roleRepository.findByName(role.name()).orElseThrow(() -> new RoleNotFoundException(role.name()));
    }

    @Override
    public Set<Role> getRoles(Collection<Roles> roles) {
        Validate.notEmpty(roles);

        Set<Role> rolesSet = new LinkedHashSet<>();
        for (Roles role : roles) {
            rolesSet.add(getRole(role));
        }
        return rolesSet;
    }

    @Override
    public Set<Role> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return new LinkedHashSet<>(roles);
    }

    @Override
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

    @Override
    @Transactional
    public void deleteRole(Roles role) {
        Role roleToDelete = getRole(role);
        roleToDelete.unassignUsers();
        roleRepository.delete(roleToDelete);
    }

}
