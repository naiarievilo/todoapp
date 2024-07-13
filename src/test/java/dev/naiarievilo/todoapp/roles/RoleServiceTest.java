package dev.naiarievilo.todoapp.roles;

import dev.naiarievilo.todoapp.roles.exceptions.RoleAlreadyExistsException;
import dev.naiarievilo.todoapp.roles.exceptions.RoleNotFoundException;
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

import java.util.List;
import java.util.Optional;

import static dev.naiarievilo.todoapp.roles.RoleServiceTestCases.*;
import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    RoleRepository roleRepository;
    @InjectMocks
    RoleServiceImpl roleService;
    @Captor
    private ArgumentCaptor<Role> roleCaptor;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName(ROLE_USER.name());
        userRole.setDescription(ROLE_USER.description());

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

        List<Role> returnedRoles = roleService.getAllRoles();
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
