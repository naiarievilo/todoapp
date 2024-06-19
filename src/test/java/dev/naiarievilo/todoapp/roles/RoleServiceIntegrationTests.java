package dev.naiarievilo.todoapp.roles;

import dev.naiarievilo.todoapp.roles.exceptions.RoleAlreadyExistsException;
import dev.naiarievilo.todoapp.roles.exceptions.RoleNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.naiarievilo.todoapp.roles.RoleServiceTestCaseMessages.*;
import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional(readOnly = true)
class RoleServiceIntegrationTests {

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    RoleService roleService;

    @Test
    @Transactional
    @DisplayName("roleExists(): " + RETURNS_FALSE_WHEN_ROLE_DOES_NOT_EXIST)
    void roleExists_roleDoesNotExist_ReturnsFalse() {
        Role targetRole = roleRepository.findByName(ROLE_USER.name()).orElseThrow(RoleNotFoundException::new);
        targetRole.unassignUsers();
        roleRepository.delete(targetRole);
        assertFalse(roleService.roleExists(ROLE_USER));
    }

    @Test
    @DisplayName("roleExists(): " + RETURNS_TRUE_WHEN_ROLE_EXISTS)
    void roleExists_roleExists_ReturnsTrue() {
        assertTrue(roleService.roleExists(ROLE_USER));
    }

    @Test
    @Transactional
    @DisplayName("getRole(): " + THROWS_ROLE_NOT_FOUND_WHEN_ROLE_DOES_NOT_EXIST)
    void getRole_RoleDoesNotExist_ThrowsRoleNotFoundException() {
        Role targetRole = roleRepository.findByName(ROLE_USER.name()).orElseThrow(RoleNotFoundException::new);
        targetRole.unassignUsers();
        roleRepository.delete(targetRole);
        assertThrows(RoleNotFoundException.class, () -> roleService.getRole(ROLE_USER));
    }

    @Test
    @DisplayName("getRole(): " + RETURNS_ROLE_WHEN_ROLE_EXISTS)
    void getRole_RoleExists_ReturnsRole() {
        Role returnedRole = roleService.getRole(ROLE_USER);
        assertEquals(ROLE_USER.name(), returnedRole.getName());
        assertEquals(ROLE_USER.description(), returnedRole.getDescription());
    }

    @Test
    @DisplayName("getAllRoles() : " + RETURNS_ALL_ROLES_IN_DATABASE)
    void getAllRoles_AllRolesInDatabase_ReturnsAllRoles() {
        Set<Role> roles = Roles.roles().stream()
            .map(role -> {
                Role toRole = new Role();
                toRole.setName(role.name());
                toRole.setDescription(role.description());
                return toRole;
            })
            .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Role> returnedRoles = roleService.getAllRoles();
        assertEquals(roles.size(), returnedRoles.size());
        assertTrue(roles.containsAll(returnedRoles));
    }

    @Test
    @DisplayName("createRole(): " + THROWS_ROLE_ALREADY_EXISTS_WHEN_ROLE_EXISTS)
    void createRole_RoleAlreadyExists_DoesNotCreateRole() {
        assertThrows(RoleAlreadyExistsException.class, () -> roleService.createRole(ROLE_USER));
    }

    @Test
    @Transactional
    @DisplayName("createRole(): " + CREATES_ROLE_WHEN_ROLE_DOES_NOT_EXIST)
    void createRole_RoleDoesNotExist_CreatesRole() {
        String targetRoleName = ROLE_USER.name();

        Role targetRole = roleRepository.findByName(targetRoleName).orElseThrow(RoleNotFoundException::new);
        targetRole.unassignUsers();
        roleRepository.delete(targetRole);

        roleService.createRole(ROLE_USER);
        assertTrue(roleRepository.findByName(targetRoleName).isPresent());
    }

    @Test
    @Transactional
    @DisplayName("deleteRole(): " + THROWS_ROLE_NOT_FOUND_WHEN_ROLE_DOES_NOT_EXIST)
    void deleteRole_RoleDoesNotExist_ThrowsRoleNotFoundException() {
        roleService.deleteRole(ROLE_USER);
        assertThrows(RoleNotFoundException.class, () -> roleService.deleteRole(ROLE_USER));
    }

    @Test
    @Transactional
    @DisplayName("deleteRole(): " + DELETES_ROLE_WHEN_ROLE_EXISTS)
    void deleteRole_RoleExists_DeletesRole() {
        roleService.deleteRole(ROLE_USER);
        assertTrue(roleRepository.findByName(ROLE_USER.name()).isEmpty());
    }
}
