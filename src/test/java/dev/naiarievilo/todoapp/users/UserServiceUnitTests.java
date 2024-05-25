package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import dev.naiarievilo.todoapp.security.UserPrincipalImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import static dev.naiarievilo.todoapp.roles.Roles.ROLE_ADMIN;
import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTests {

    private static final String EMAIL = "johnDoe@example.com";
    private static final String PASSWORD = "securePassword";
    private static final String CONFIRM_PASSWORD = PASSWORD;
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    private static final String NEW_EMAIL = "newEmail@example.com";
    private static final String NEW_PASSWORD = "newSecurePassword";

    private static final String EMPTY = "";
    private static final String BLANK = "   ";
    private final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserInfoService userInfoService;
    @Mock
    private RoleService roleService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;
    private User user;
    private UserPrincipal userPrincipal;
    private UserCreationDTO userCreationDTO;
    private Role role;

    @BeforeEach
    void setUpUser() {
        role = new Role();
        role.setId(1L);
        role.setName(ROLE_USER.name());
        role.setDescription(ROLE_USER.description());

        user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);
        user.addRole(role);
        user.setIsEnabled(true);
        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);

        userPrincipal = UserPrincipalImpl.withUser(user);

        userCreationDTO = new UserCreationDTO(EMAIL, PASSWORD, CONFIRM_PASSWORD, FIRST_NAME, LAST_NAME);
    }

    @Test
    @DisplayName("userExists: 'NullPointerException' is thrown when email is null")
    void userExists_EmailIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.userExists(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("userExists: 'IllegalArgumentException' is thrown when email is empty")
    void userExists_EmailIsEmpty_IllegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> userService.userExists(EMPTY));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("userExists: 'IllegalArgumentException' is thrown when email is blank")
    void userExists_EmailIsBlank_IllegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> userService.userExists(BLANK));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("userExists: returns false when email isn't registered")
    void userExists_EmailIsNotRegistered_ReturnsFalse() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

        boolean returnedBoolean = userService.userExists(EMAIL);

        assertFalse(returnedBoolean);
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("userExists: returns true when email is registered")
    void userExists_EmailIsRegistered_ReturnsTrue() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));

        boolean returnedBoolean = userService.userExists(EMAIL);

        assertTrue(returnedBoolean);
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("loadUserByEmail: 'NullPointerException' is thrown when email is null")
    void loadUserByEmail_EmailIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.loadUserByEmail(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("loadUserByEmail: 'IllegalArgumentException' is thrown when email is empty")
    void loadUserByEmail_EmailIsEmpty_IllegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> userService.loadUserByEmail(EMPTY));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("loadUserByEmail: 'IllegalArgumentException' is thrown when email is blank")
    void loadUserByEmail_EmailIsBlank_IllegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> userService.loadUserByEmail(BLANK));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("loadUserByEmail: 'UserNotFoundException' is thrown when email isn't registered")
    void loadUserByEmail_EmailIsNotRegistered_UserNotFoundExceptionIsThrown() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.loadUserByEmail(EMAIL));
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("loadUserByEmail: returns 'UserPrincipal' when email is registered")
    void loadUserByEmail_EmailIsRegistered_ReturnsUserPrincipal() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));

        UserPrincipal returnedPrincipal = userService.loadUserByEmail(EMAIL);

        assertEquals(userPrincipal, returnedPrincipal);
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("getUserByEmail: 'NullPointerException' is thrown when email is null")
    void getUserByEmail_EmailIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.getUserByEmail(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserByEmail: 'IllegalArgumentException' is thrown when email is empty")
    void getUserByEmail_EmailIsEmpty_IllegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByEmail(EMPTY));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserByEmail: 'IllegalArgumentException' is thrown when email is blank")
    void getUserByEmail_EmailIsBlank_IllegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByEmail(BLANK));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserByEmail: 'UserNotFoundException' is thrown when email isn't registered")
    void getUserByEmail_EmailIsNotRegistered_UserNotFoundExceptionIsThrown() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(EMAIL));
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("getUserByEmail: returns user when email is registered")
    void getUserByEmail_EmailIsRegistered_ReturnsUser() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));

        User returnedUser = userService.getUserByEmail(EMAIL);

        assertEquals(user, returnedUser);
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("getUserByPrincipal: 'NullPointerException' is thrown when user principal is null")
    void getUserByPrincipal_UserPrincipalIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.getUserByPrincipal(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserByPrincipal: 'UserNotFoundException' is thrown when user principal isn't registered")
    void getUserByPrincipal_UserPrincipalNotRegistered_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByPrincipal(userPrincipal));
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getUserByPrincipal: returns user when user principal is registered")
    void getUserByPrincipal_UserPrincipalIsRegistered_ReturnsUser() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        User returnedUser = userService.getUserByPrincipal(userPrincipal);

        assertEquals(user, returnedUser);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("createUser: 'NullPointerException' is thrown when userCreationDTO is null")
    void createUser_UserCreationDTOIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.createUser(null));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(userInfoService);
    }

    @Test
    @DisplayName("createUser: 'UserAlreadyExistsException' is thrown when user already exists")
    void createUser_UserAlreadyExists_UserAlreadyExistsExceptionIsThrown() {
        String email = userCreationDTO.email();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(userCreationDTO));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
        verifyNoInteractions(roleService);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(userInfoService);
    }

    @Test
    @DisplayName("createUser: creates user when user doesn't exist")
    void createUser_UserDoestNotExist_UserCreated() {
        String email = userCreationDTO.email();
        String password = userCreationDTO.password();
        GrantedAuthority userRole = new SimpleGrantedAuthority(ROLE_USER.name());

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        given(roleService.getRole(ROLE_USER)).willReturn(role);
        given(passwordEncoder.encode(password)).willReturn("encodedPassword");
        given(userRepository.persist(user)).willReturn(user);

        UserPrincipal returnedPrincipal = userService.createUser(userCreationDTO);
        Set<GrantedAuthority> authorities = returnedPrincipal.getAuthorities();

        assertEquals(userPrincipal, returnedPrincipal);
        assertTrue(authorities.size() == 1 && authorities.contains(userRole));

        InOrder invokeInOrder = inOrder(roleService, passwordEncoder, userRepository, userInfoService);
        invokeInOrder.verify(userRepository).findByEmail(email);
        invokeInOrder.verify(roleService).getRole(ROLE_USER);
        invokeInOrder.verify(passwordEncoder).encode(password);
        invokeInOrder.verify(userRepository).persist(user);
        invokeInOrder.verify(userInfoService).createUserInfo(userCreationDTO, user);
    }

    @Test
    @DisplayName("deleteUser: 'NullPointerException' is thrown when user principal is null")
    void deleteUser_UserPrincipalIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.deleteUser(null));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userInfoService);
    }

    @Test
    @DisplayName("deleteUser: 'UserNotFoundException' is thrown when user doesn't exist")
    void deleteUser_UserDoesNotExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userPrincipal));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).delete(any(User.class));
        verifyNoInteractions(userInfoService);
    }

    @Test
    @DisplayName("deleteUser: user is deleted when user exists")
    void deleteUser_UserExists_UserDeleted() {
        given(userRepository.findByEmail(userPrincipal.getEmail())).willReturn(Optional.of(user));

        userService.deleteUser(userPrincipal);

        InOrder invokeInOrder = inOrder(userRepository, userInfoService);
        invokeInOrder.verify(userRepository).findByEmail(userPrincipal.getEmail());
        invokeInOrder.verify(userInfoService).deleteUserInfo(user.getId());
        invokeInOrder.verify(userRepository).delete(userCaptor.capture());
        assertTrue(userCaptor.getValue().getRoles().isEmpty());
    }

    @Test
    @DisplayName("updateEmail: 'NullPointerException' is thrown when user principal is null")
    void updateEmail_UserPrincipalIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.updateEmail(null, NEW_EMAIL));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateEmail: 'NullPointerException' is thrown when new email is null")
    void updateEmail_NewEmailIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.updateEmail(userPrincipal, null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateEmail: 'IllegalArgumentException' is thrown when new email is empty")
    void updateEmail_EmailIsEmpty_IllegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> userService.updateEmail(userPrincipal, EMPTY));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateEmail: 'IllegalArgumentException' is thrown when new email is blank")
    void updateEmail_EmailIsBlank_IllegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> userService.updateEmail(userPrincipal, BLANK));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateEmail: 'EmailAlreadyRegisteredException' is thrown when new email is already registered")
    void updateEmail_NewEmailAlreadyRegistered_EmailAlreadyRegisteredExceptionIsThrown() {
        User otherUser = new User();
        otherUser.setEmail(NEW_EMAIL);

        given(userRepository.findByEmail(NEW_EMAIL)).willReturn(Optional.of(otherUser));
        assertThrows(EmailAlreadyRegisteredException.class, () -> userService.updateEmail(userPrincipal, NEW_EMAIL));
        verify(userRepository).findByEmail(NEW_EMAIL);
        verify(userRepository, never()).findByEmail(userPrincipal.getEmail());
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("updateEmail: email is updated when email isn't already registered")
    void updateEmail_NewEmailIsNotRegistered_EmailUpdated() {

        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(NEW_EMAIL)).willReturn(Optional.empty());
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        UserPrincipal returnedPrincipal = userService.updateEmail(userPrincipal, NEW_EMAIL);

        assertEquals(NEW_EMAIL, returnedPrincipal.getEmail());

        verify(userRepository).findByEmail(NEW_EMAIL);
        verify(userRepository).findByEmail(email);
        verify(userRepository).update(user);

    }

    @Test
    @DisplayName("updatePassword: 'NullPointerException' is thrown when user principal is null")
    void updatePassword_UserPrincipalIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.updatePassword(null, NEW_PASSWORD));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updatePassword: 'NullPointerException' is thrown when new password is null")
    void updatePassword_NewPasswordIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.updatePassword(userPrincipal, null));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updatedPassword: 'IllegalArgumentException' is thrown when new password is empty")
    void updatePassword_NewPasswordIsEmpty_IllegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> userService.updatePassword(userPrincipal, EMPTY));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updatePassword: 'IllegalArgumentException' is thrown when new password is blank")
    void updatePassword_NewPasswordIsBlank_IllegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> userService.updatePassword(userPrincipal, BLANK));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updatePassword: 'UserNotFoundException' is thrown when user doesn't exist")
    void updatePassword_UserDoesNotExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updatePassword(userPrincipal, NEW_EMAIL));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updatePassword: password is updated when user exists")
    void updatePassword_UserExists_PasswordUpdated() {
        String email = userPrincipal.getEmail();
        String newEncodedPassword = "newEncodedPassword";

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.encode(NEW_PASSWORD)).willReturn(newEncodedPassword);

        UserPrincipal returnedPrincipal = userService.updatePassword(userPrincipal, NEW_PASSWORD);

        assertEquals(newEncodedPassword, returnedPrincipal.getPassword());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).encode(NEW_PASSWORD);
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("addRoleToUser: 'NullPointerException' is thrown when user principal is null")
    void addRoleToUser_UserPrincipalIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.addRoleToUser(null, ROLE_ADMIN));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("addRoleToUser: 'NullPointerException' is thrown when role to add is null")
    void addRoleToUser_NewRoleIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.addRoleToUser(userPrincipal, null));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("addRoleToUser: 'UserNotFoundException' is thrown when user doesn't exist")
    void addRoleToUser_UserDoesNotExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.addRoleToUser(userPrincipal, ROLE_ADMIN));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("addRoleToUser: role is not added to user when role is already assigned to user")
    void addRoleToUser_RoleAlreadyAssignedToUser_RoleNotAdded() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(roleService.getRole(ROLE_USER)).willReturn(role);

        UserPrincipal returnedPrincipal = userService.addRoleToUser(userPrincipal, ROLE_USER);
        Set<GrantedAuthority> authorities = returnedPrincipal.getAuthorities();

        assertEquals(1, authorities.size());
        verify(userRepository).findByEmail(email);
        verify(roleService).getRole(ROLE_USER);
        verify(userRepository, never()).update(user);
    }

    @Test
    @DisplayName("addRoleToUser: role is added to user when user exist")
    void addRoleToUser_UserExists_RoleAdded() {
        String email = userPrincipal.getEmail();
        Role roleToAdd = new Role();
        roleToAdd.setName(ROLE_ADMIN.name());
        GrantedAuthority roleAdded = new SimpleGrantedAuthority(roleToAdd.getName());

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(roleService.getRole(ROLE_ADMIN)).willReturn(roleToAdd);

        UserPrincipal returnedPrincipal = userService.addRoleToUser(userPrincipal, ROLE_ADMIN);
        Set<GrantedAuthority> authorities = returnedPrincipal.getAuthorities();

        assertTrue(authorities.contains(roleAdded));
        verify(userRepository).findByEmail(email);
        verify(roleService).getRole(ROLE_ADMIN);
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("removeRoleFromUser: 'NullPointerException' is thrown when user principal is null")
    void removeRoleFromUser_UserPrincipalIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.removeRoleFromUser(null, ROLE_USER));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser: 'NullPointerException' is thrown when role to remove is null")
    void removeRoleFromUser_RoleIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.removeRoleFromUser(userPrincipal, null));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser: 'UserNotFoundException' is thrown when user doesn't exist")
    void removeRoleFromUser_UserDoesNotExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.removeRoleFromUser(userPrincipal, ROLE_USER));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser: 'UserRoleRemovalNotAllowedException' is thrown when trying to remove user role")
    void removeRoleFromUser_RoleIsUserRole_UserRoleRemovalNotAllowedExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(roleService.getRole(ROLE_USER)).willReturn(role);

        assertThrows(UserRoleRemovalNotAllowed.class, () -> userService.removeRoleFromUser(userPrincipal, ROLE_USER));
        verify(userRepository).findByEmail(email);
        verify(roleService).getRole(ROLE_USER);
        verify(userRepository, never()).update(user);
    }

    @Test
    @DisplayName("removeRoleFromUser: role is removed from user when user exists")
    void removeRoleFromUser_UserExists_RoleRemoved() {
        String email = userPrincipal.getEmail();
        Role roleToRemove = new Role();
        roleToRemove.setName(ROLE_ADMIN.name());
        user.addRole(roleToRemove);
        GrantedAuthority roleRemoved = new SimpleGrantedAuthority(roleToRemove.getName());

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(roleService.getRole(ROLE_ADMIN)).willReturn(roleToRemove);

        UserPrincipal returnedPrincipal = userService.removeRoleFromUser(userPrincipal, ROLE_ADMIN);
        Set<GrantedAuthority> authorities = returnedPrincipal.getAuthorities();

        assertFalse(authorities.contains(roleRemoved));

        verify(userRepository).findByEmail(email);
        verify(roleService).getRole(ROLE_ADMIN);
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("lockUser: 'NullPointedException' is thrown when user principal is null")
    void lockUser_UserPrincipalIsNull_NullPointedExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.lockUser(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("lockUser: 'UserNotFoundException' is thrown when user doesn't exist")
    void lockUser_UserDoesntExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.lockUser(userPrincipal));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("lockUser: user isn't updated when user is already locked")
    void lockUser_UserAlreadyLocked_UserNotUpdated() {
        String email = userPrincipal.getEmail();
        user.setIsLocked(true);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.lockUser(userPrincipal);

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("lockUser: user is locked when user isn't locked")
    void lockUser_UserExistsAndIsNotLocked_UserLocked() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.lockUser(userPrincipal);

        verify(userRepository).findByEmail(email);
        verify(userRepository).update(userCaptor.capture());
        assertTrue(userCaptor.getValue().getIsLocked());
    }

    @Test
    @DisplayName("unlockUser: 'NullPointerException' is thrown when user principal is null")
    void unlockUser_UserPrincipalIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.unlockUser(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("unlockUser: 'UserNotFoundException' is thrown when user doesn't exist")
    void unlockUser_UserDoesntExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.unlockUser(userPrincipal));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("unlockUser: user isn't updated when user is already unlocked")
    void unlockUser_UserAlreadyUnlocked_UserNotUpdated() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.unlockUser(userPrincipal);

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("unlockUser: user is unlocked when user is locked")
    void unlockUser_UserIsLocked_UserUnlocked() {
        String email = userPrincipal.getEmail();
        user.setIsLocked(true);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.unlockUser(userPrincipal);

        verify(userRepository).findByEmail(email);
        verify(userRepository).update(userCaptor.capture());
        assertFalse(userCaptor.getValue().getIsLocked());
    }

    @Test
    @DisplayName("disableUser: 'NullPointerException' is thrown when user principal is null")
    void disableUser_UserPrincipalIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.disableUser(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("disableUser: 'UserNotFoundException' is thrown when user doesn't exist")
    void disableUser_UserDoesntExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.disableUser(userPrincipal));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("disableUser: user isn't updated when user is already disabled")
    void disableUser_UserAlreadyDisabled_UserNotUpdated() {
        String email = userPrincipal.getEmail();
        user.setIsEnabled(false);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.disableUser(userPrincipal);
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("disableUser: user is disabled when user is enabled")
    void disableUser_UserIsEnabled_UserDisabled() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.disableUser(userPrincipal);

        verify(userRepository).findByEmail(email);
        verify(userRepository).update(userCaptor.capture());
        assertFalse(userCaptor.getValue().getIsEnabled());
    }

    @Test
    @DisplayName("enableUser: 'NullPointerException' is thrown when user principal is null")
    void enableUser_UserPrincipalIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.enableUser(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("enableUser: 'UserNotFoundException' is thrown when user doesn't exist")
    void enableUser_UserDoesntExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.enableUser(userPrincipal));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("enableUser: user is not updated when user is already enabled")
    void enableUser_UserAlreadyEnabled_UserNotUpdated() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.enableUser(userPrincipal);

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("enableUser: user is enabled when user is not enabled")
    void enableUser_UserIsDisabled_UserEnabled() {
        String email = userPrincipal.getEmail();
        user.setIsEnabled(false);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.enableUser(userPrincipal);

        verify(userRepository).findByEmail(email);
        verify(userRepository).update(userCaptor.capture());
        assertTrue(userCaptor.getValue().getIsEnabled());
    }

    @Test
    @DisplayName("addLoginAttempt: 'NullPointerException' is thrown when user is null")
    void addLoginAttempt_UserIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.addLoginAttempt(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("addLoginAttempt: login attempt updated when user isn't null")
    void addLoginAttempt_UserIsNotNull_LoginAttemptAdded() {
        int oldLoginAttempts = user.getFailedLoginAttempts();
        LocalTime oldLocalTime = LocalTime.now();
        user.setFailedLoginTime(oldLocalTime);

        userService.addLoginAttempt(user);

        verify(userRepository).update(userCaptor.capture());
        User userCaptured = userCaptor.getValue();
        assertNotEquals(oldLoginAttempts, userCaptured.getFailedLoginAttempts());
        assertNotEquals(oldLocalTime, userCaptured.getFailedLoginTime());
    }

    @Test
    @DisplayName("resetLoginAttempt: 'NullPointerException' is thrown when user is null")
    void resetLoginAttempt_UserIsNull_NullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.resetLoginAttempt(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("resetLoginAttempt: login attempts are reset when user isn't null")
    void resetLoginAttempt_UserIsNotNull_LoginAttemptsReset() {
        user.setFailedLoginAttempts(2);

        userService.resetLoginAttempt(user);

        verify(userRepository).update(userCaptor.capture());
        assertEquals(0, userCaptor.getValue().getFailedLoginAttempts());
    }

}
