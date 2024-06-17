package dev.naiarievilo.todoapp.roles;

import dev.naiarievilo.todoapp.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dev.naiarievilo.todoapp.roles.RoleServiceTestCaseMessages.*;
import static dev.naiarievilo.todoapp.roles.Roles.ROLE_ADMIN;
import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceUnitTests {

    @Mock
    RoleRepository roleRepository;
    @InjectMocks
    RoleServiceImpl roleService;
    @Captor
    private ArgumentCaptor<Role> roleCaptor;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName(ROLE_USER.name());
        userRole.setDescription(ROLE_USER.description());

        adminRole = new Role();
        adminRole.setName(ROLE_ADMIN.name());
        adminRole.setDescription(ROLE_ADMIN.description());

        User user = new User();
        user.addRole(userRole);
    }

    @Test
    @DisplayName("roleExists(): " + RETURNS_FALSE_WHEN_ROLE_DOES_NOT_EXIST)
    void roleExists_roleDoesNotExist_ReturnsFalse() {
        String roleName = ROLE_USER.name();
        given(roleRepository.findByName(roleName)).willReturn(Optional.empty());

        assertFalse(roleService.roleExists(ROLE_USER));
        verify(roleRepository).findByName(roleName);
    }

    @Test
    @DisplayName("roleExists(): " + RETURNS_TRUE_WHEN_ROLE_EXISTS)
    void roleExists_roleExists_ReturnsTrue() {
        String roleName = ROLE_USER.name();
        given(roleRepository.findByName(roleName)).willReturn(Optional.of(userRole));

        assertTrue(roleService.roleExists(ROLE_USER));
        verify(roleRepository).findByName(roleName);
    }

    @Test
    @DisplayName("getRole(): " + THROWS_ROLE_NOT_FOUND_WHEN_ROLE_DOES_NOT_EXIST)
    void getRole_RoleDoesNotExist_ThrowsRoleNotFoundException() {
        String roleName = ROLE_USER.name();
        given(roleRepository.findByName(roleName)).willReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> roleService.getRole(ROLE_USER));
        verify(roleRepository).findByName(roleName);
    }

    @Test
    @DisplayName("getRole(): " + RETURNS_ROLE_WHEN_ROLE_EXISTS)
    void getRole_RoleExists_ReturnsRole() {
        String roleName = ROLE_USER.name();
        given(roleRepository.findByName(roleName)).willReturn(Optional.of(userRole));

        Role returnedRole = roleService.getRole(ROLE_USER);
        assertEquals(userRole, returnedRole);
        verify(roleRepository).findByName(roleName);
    }

    @Test
    @DisplayName("getRoles(): " + THROWS_ROLE_NOT_FOUND_WHEN_ONE_ROLE_DOES_NOT_EXIST)
    void getRoles_RolesDoesNotExist_ThrowsRoleNotFoundException() {
        Set<Roles> roles = new LinkedHashSet<>(List.of(ROLE_ADMIN, ROLE_USER));

        given(roleRepository.findByName(ROLE_ADMIN.name())).willReturn(Optional.of(adminRole));
        given(roleRepository.findByName(ROLE_USER.name())).willReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> roleService.getRoles(roles));
        verify(roleRepository, times(2)).findByName(anyString());
    }

    @Test
    @DisplayName("getRoles(): " + RETURNS_ROLES_WHEN_ROLES_EXIST)
    void getRoles_RolesExist_ReturnRoles() {
        Set<Roles> enumRoles = new LinkedHashSet<>(List.of(ROLE_ADMIN, ROLE_USER));
        Set<Role> roles = new LinkedHashSet<>(List.of(userRole, adminRole));

        given(roleRepository.findByName(adminRole.getName())).willReturn(Optional.of(adminRole));
        given(roleRepository.findByName(userRole.getName())).willReturn(Optional.of(userRole));

        Set<Role> returnedRoles = roleService.getRoles(enumRoles);
        assertEquals(roles.size(), returnedRoles.size());
        assertTrue(roles.containsAll(returnedRoles) && returnedRoles.containsAll(roles));
        verify(roleRepository, times(2)).findByName(anyString());
    }

    @Test
    @DisplayName("getAllRoles() : " + RETURNS_ALL_ROLES_IN_DATABASE)
    void getAllRoles_AllRolesInDatabase_ReturnsAllRoles() {
        List<Role> roles = Roles.roles().stream()
            .map(role -> {
                Role toRole = new Role();
                toRole.setName(role.name());
                toRole.setDescription(role.description());
                return toRole;
            })
            .toList();

        given(roleRepository.findAll()).willReturn(roles);

        Set<Role> returnedRoles = roleService.getAllRoles();
        assertEquals(roles.size(), returnedRoles.size());
        assertTrue(roles.containsAll(returnedRoles) && returnedRoles.containsAll(roles));
        verify(roleRepository).findAll();
    }

    @Test
    @DisplayName("createRole(): " + THROWS_ROLE_ALREADY_EXISTS_WHEN_ROLE_EXISTS)
    void createRole_RoleAlreadyExists_DoesNotCreateRole() {
        String roleName = ROLE_USER.name();
        given(roleRepository.findByName(roleName)).willReturn(Optional.of(userRole));

        assertThrows(RoleAlreadyExistsException.class, () -> roleService.createRole(ROLE_USER));
        verify(roleRepository).findByName(roleName);
        verify(roleRepository, never()).persist(any(Role.class));
    }

    @Test
    @DisplayName("createRole(): " + CREATES_ROLE_WHEN_ROLE_DOES_NOT_EXIST)
    void createRole_RoleDoesNotExist_CreatesRole() {
        String roleName = ROLE_USER.name();
        given(roleRepository.findByName(roleName)).willReturn(Optional.empty());

        roleService.createRole(ROLE_USER);
        verify(roleRepository).findByName(roleName);
        verify(roleRepository).persist(roleCaptor.capture());
        Role createdRole = roleCaptor.getValue();
        assertEquals(ROLE_USER.name(), createdRole.getName());
        assertEquals(ROLE_USER.description(), createdRole.getDescription());
    }

    @Test
    @DisplayName("deleteRole(): " + THROWS_ROLE_NOT_FOUND_WHEN_ROLE_DOES_NOT_EXIST)
    void deleteRole_RoleDoesNotExist_ThrowsRoleNotFoundException() {
        String roleName = ROLE_USER.name();
        given(roleRepository.findByName(roleName)).willReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> roleService.deleteRole(ROLE_USER));
        verify(roleRepository).findByName(roleName);
        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    @DisplayName("deleteRole(): " + DELETES_ROLE_WHEN_ROLE_EXISTS)
    void deleteRole_RoleExists_DeletesRole() {
        String roleName = ROLE_USER.name();
        given(roleRepository.findByName(roleName)).willReturn(Optional.of(userRole));

        roleService.deleteRole(ROLE_USER);
        verify(roleRepository).findByName(roleName);
        verify(roleRepository).delete(roleCaptor.capture());
        assertTrue(roleCaptor.getValue().getUsers().isEmpty());
    }
}
