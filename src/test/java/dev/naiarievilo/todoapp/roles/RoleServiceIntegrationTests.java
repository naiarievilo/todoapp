package dev.naiarievilo.todoapp.roles;

import org.junit.jupiter.api.BeforeEach;
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
import static dev.naiarievilo.todoapp.roles.Roles.ROLE_ADMIN;
import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional(readOnly = true)
class RoleServiceIntegrationTests {

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    RoleService roleService;

    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        adminRole = new Role();
        adminRole.setName(ROLE_ADMIN.name());
        adminRole.setDescription(ROLE_ADMIN.description());

        userRole = new Role();
        userRole.setName(ROLE_USER.name());
        userRole.setDescription(ROLE_USER.description());
    }

    @Test
    @Transactional
    @DisplayName("roleExists(): " + RETURNS_FALSE_WHEN_ROLE_DOES_NOT_EXIST)
    void roleExists_roleDoesNotExist_ReturnsFalse() {
        roleRepository.deleteByName(ROLE_USER.name());
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
        roleRepository.deleteByName(ROLE_USER.name());
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
    @Transactional
    @DisplayName("getRoles(): " + THROWS_ROLE_NOT_FOUND_WHEN_ONE_ROLE_DOES_NOT_EXIST)
    void getRoles_RolesDoesNotExist_ThrowsRoleNotFoundException() {
        roleRepository.deleteByName(ROLE_USER.name());
        Set<Roles> roles = new LinkedHashSet<>(List.of(ROLE_ADMIN, ROLE_USER));
        assertThrows(RoleNotFoundException.class, () -> roleService.getRoles(roles));
    }

    @Test
    @DisplayName("getRoles(): " + RETURNS_ROLES_WHEN_ROLES_EXIST)
    void getRoles_RolesExist_ReturnRoles() {
        Set<Roles> roles = new LinkedHashSet<>(List.of(ROLE_ADMIN, ROLE_USER));
        Set<Role> entityRoles = new LinkedHashSet<>(List.of(adminRole, userRole));

        Set<Role> returnedRoles = roleService.getRoles(roles);
        assertEquals(roles.size(), returnedRoles.size());
        assertTrue(returnedRoles.containsAll(entityRoles));
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

        Set<Role> returnedRoles = roleService.getAllRoles();
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
        String roleName = ROLE_USER.name();
        roleRepository.deleteByName(roleName);

        roleService.createRole(ROLE_USER);
        assertTrue(roleRepository.findByName(roleName).isPresent());
    }

    @Test
    @Transactional
    @DisplayName("deleteRole(): " + THROWS_ROLE_NOT_FOUND_WHEN_ROLE_DOES_NOT_EXIST)
    void deleteRole_RoleDoesNotExist_ThrowsRoleNotFoundException() {
        roleRepository.deleteByName(ROLE_USER.name());
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
