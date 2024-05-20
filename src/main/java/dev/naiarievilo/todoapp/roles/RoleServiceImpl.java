package dev.naiarievilo.todoapp.roles;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_BLANK;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_NULL;

@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public boolean roleExists(Roles role) {
        Validate.notNull(role, NOT_NULL.message());
        return roleRepository.findByName(role.name()).isPresent();
    }

    @Override
    public Role getRole(Roles role) {
        Validate.notNull(role, NOT_NULL.message());
        return roleRepository.findByName(role.name()).orElseThrow(RoleNotFoundException::new);
    }

    @Override
    public Set<Role> getRoles(Collection<Roles> roles) {
        Validate.noNullElements(roles, NOT_NULL.message());

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
        Validate.notNull(role, NOT_NULL.message());

        String roleName = role.name();
        String description = role.description();

        Validate.notBlank(description, NOT_BLANK.message());

        Role newRole = new Role();
        newRole.setName(roleName);
        newRole.setDescription(description);

        roleRepository.persist(newRole);
    }

    @Override
    @Transactional
    public void deleteRole(Roles role) {
        Validate.notNull(role, NOT_NULL.message());
        roleRepository.deleteByName(role.name());
    }

}
