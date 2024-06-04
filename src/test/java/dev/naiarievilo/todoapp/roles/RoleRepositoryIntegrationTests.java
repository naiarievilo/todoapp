package dev.naiarievilo.todoapp.roles;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional(readOnly = true)
class RoleRepositoryIntegrationTests {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("findByName(): Returns empty `Optional<Role>` when role is not found")
    void findByName_RoleDoesNotExist_ReturnsEmptyOptional() {
        assertTrue(roleRepository.findByName("ROLE_GUEST").isEmpty());
    }

    @Test
    @DisplayName("findByName(): Returns populated `Optional<Role>` when role is found")
    void findByName_RoleExists_ReturnsPopulatedOptional() {
        assertTrue(roleRepository.findByName(ROLE_USER.name()).isPresent());
    }

    @Test
    @DisplayName("findAll(): Returns all roles in the database")
    void findAll_RolesPresentInDatabase_ReturnsAllRolesInDatabase() {
        Set<Role> roles = Roles.roles().stream()
            .map(role -> {
                Role toRole = new Role();
                toRole.setName(role.name());
                toRole.setDescription(role.description());
                return toRole;
            })
            .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Role> returnedRoles = roleRepository.findAll();
        assertEquals(roles.size(), returnedRoles.size());
        assertTrue(roles.containsAll(returnedRoles) && returnedRoles.containsAll(roles));
    }

    @Test
    @Transactional
    @DisplayName("deleteByName(): Deletes role when role exists")
    void deleteByName_RoleExists_DeletesRole() {
        assertTrue(roleRepository.findByName(ROLE_USER.name()).isPresent());
        roleRepository.deleteByName(ROLE_USER.name());
        assertFalse(roleRepository.findByName(ROLE_USER.name()).isPresent());
    }

    @Test
    @Transactional
    @DisplayName("deleteAll(): Deletes all roles in the database")
    void deleteAll_RolesPresentInDatabase_DeletesAllRolesInDatabase() {
        int rolesCount = Roles.roles().size();
        assertEquals(rolesCount, roleRepository.findAll().size());
        roleRepository.deleteAll();
        assertTrue(roleRepository.findAll().isEmpty());
    }
}
