package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.UserRoleRemovalProhibitedException;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import dev.naiarievilo.todoapp.security.UserPrincipalImpl;
import dev.naiarievilo.todoapp.users.exceptions.EmailAlreadyRegisteredException;
import dev.naiarievilo.todoapp.users.exceptions.UserAlreadyExistsException;
import dev.naiarievilo.todoapp.users.info.UserInfoService;
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
import static dev.naiarievilo.todoapp.users.UserServiceTestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTests {

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
    private Role userRole;

    @BeforeEach
    void setUpUser() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(ROLE_USER.name());
        userRole.setDescription(ROLE_USER.description());

        user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);
        user.addRole(userRole);
        user.setIsEnabled(true);
        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);

        userPrincipal = UserPrincipalImpl.withUser(user);

        userCreationDTO = new UserCreationDTO(EMAIL, PASSWORD, CONFIRM_PASSWORD, FIRST_NAME, LAST_NAME);
    }

    @Test
    @DisplayName("userExists(): " + THROWS_NULL_POINTER_WHEN_EMAIL_NULL)
    void userExists_EmailIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.userExists(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("userExists(): " + THROWS_ILLEGAL_ARGUMENT_WHEN_EMAIL_EMPTY)
    void userExists_EmailIsEmpty_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.userExists(EMPTY));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("userExists(): " + THROWS_ILLEGAL_ARGUMENT_WHEN_EMAIL_BLANK)
    void userExists_EmailIsBlank_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.userExists(BLANK));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("userExists(): " + RETURNS_FALSE_WHEN_EMAIL_NOT_REGISTERED)
    void userExists_EmailIsNotRegistered_ReturnsFalse() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

        boolean returnedBoolean = userService.userExists(EMAIL);

        assertFalse(returnedBoolean);
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("userExists(): " + RETURNS_TRUE_WHEN_EMAIL_REGISTERED)
    void userExists_EmailIsRegistered_ReturnsTrue() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));

        boolean returnedBoolean = userService.userExists(EMAIL);

        assertTrue(returnedBoolean);
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("loadUserByEmail(): " + THROWS_NULL_POINTER_WHEN_EMAIL_NULL)
    void loadUserByEmail_EmailIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.loadUserByEmail(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("loadUserByEmail(): " + THROWS_ILLEGAL_ARGUMENT_WHEN_EMAIL_EMPTY)
    void loadUserByEmail_EmailIsEmpty_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.loadUserByEmail(EMPTY));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("loadUserByEmail(): " + THROWS_ILLEGAL_ARGUMENT_WHEN_EMAIL_BLANK)
    void loadUserByEmail_EmailIsBlank_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.loadUserByEmail(BLANK));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("loadUserByEmail(): " + THROWS_USER_NOT_FOUND_WHEN_EMAIL_NOT_REGISTERED)
    void loadUserByEmail_EmailIsNotRegistered_UserNotFoundExceptionIsThrown() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.loadUserByEmail(EMAIL));
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("loadUserByEmail(): " + RETURNS_PRINCIPAL_WHEN_EMAIL_REGISTERED)
    void loadUserByEmail_EmailIsRegistered_ReturnsUserPrincipal() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));

        UserPrincipal returnedPrincipal = userService.loadUserByEmail(EMAIL);

        assertEquals(userPrincipal, returnedPrincipal);
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("getUserByEmail(): " + THROWS_NULL_POINTER_WHEN_EMAIL_NULL)
    void getUserByEmail_EmailIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.getUserByEmail(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserByEmail(): " + THROWS_ILLEGAL_ARGUMENT_WHEN_EMAIL_EMPTY)
    void getUserByEmail_EmailIsEmpty_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByEmail(EMPTY));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserByEmail(): " + THROWS_ILLEGAL_ARGUMENT_WHEN_EMAIL_BLANK)
    void getUserByEmail_EmailIsBlank_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByEmail(BLANK));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserByEmail(): " + THROWS_USER_NOT_FOUND_WHEN_EMAIL_NOT_REGISTERED)
    void getUserByEmail_EmailIsNotRegistered_UserNotFoundExceptionIsThrown() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(EMAIL));
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("getUserByEmail(): " + RETURNS_USER_WHEN_EMAIL_REGISTERED)
    void getUserByEmail_EmailIsRegistered_ReturnsUser() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));

        User returnedUser = userService.getUserByEmail(EMAIL);

        assertEquals(user, returnedUser);
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("getUserByPrincipal(): " + THROWS_NULL_POINTER_WHEN_PRINCIPAL_NULL)
    void getUserByPrincipal_UserPrincipalIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.getUserByPrincipal(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserByPrincipal(): " + THROWS_USER_NOT_FOUND_WHEN_PRINCIPAL_NOT_REGISTERED)
    void getUserByPrincipal_UserPrincipalNotRegistered_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByPrincipal(userPrincipal));
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getUserByPrincipal(): " + RETURNS_USER_WHEN_PRINCIPAL_REGISTERED)
    void getUserByPrincipal_UserPrincipalIsRegistered_ReturnsUser() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        User returnedUser = userService.getUserByPrincipal(userPrincipal);

        assertEquals(user, returnedUser);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("createUser(): " + THROWS_NULL_POINTER_WHEN_USER_CREATION_DTO_NULL)
    void createUser_UserCreationDTOIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.createUser(null));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(userInfoService);
    }

    @Test
    @DisplayName("createUser(): " + THROWS_USER_ALREADY_EXISTS_WHEN_USER_REGISTERED)
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
    @DisplayName("createUser(): " + CREATES_USER_WHEN_USER_NOT_REGISTERED)
    void createUser_UserDoestNotExist_UserCreated() {
        String email = userCreationDTO.email();
        String password = userCreationDTO.password();
        GrantedAuthority userRoleGrantedAuthority = new SimpleGrantedAuthority(ROLE_USER.name());

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        given(roleService.getRole(ROLE_USER)).willReturn(userRole);
        given(passwordEncoder.encode(password)).willReturn("encodedPassword");
        given(userRepository.persist(user)).willReturn(user);

        UserPrincipal returnedPrincipal = userService.createUser(userCreationDTO);
        Set<GrantedAuthority> authorities = returnedPrincipal.getAuthorities();

        assertEquals(userPrincipal, returnedPrincipal);
        assertTrue(authorities.size() == 1 && authorities.contains(userRoleGrantedAuthority));

        InOrder invokeInOrder = inOrder(roleService, passwordEncoder, userRepository, userInfoService);
        invokeInOrder.verify(userRepository).findByEmail(email);
        invokeInOrder.verify(roleService).getRole(ROLE_USER);
        invokeInOrder.verify(passwordEncoder).encode(password);
        invokeInOrder.verify(userRepository).persist(user);
        invokeInOrder.verify(userInfoService).createUserInfo(userCreationDTO, user);
    }

    @Test
    @DisplayName("deleteUser(): " + THROWS_NULL_POINTER_WHEN_PRINCIPAL_NULL)
    void deleteUser_UserPrincipalIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.deleteUser(null));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userInfoService);
    }

    @Test
    @DisplayName("deleteUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_NOT_REGISTERED)
    void deleteUser_UserDoesNotExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userPrincipal));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).delete(any(User.class));
        verifyNoInteractions(userInfoService);
    }

    @Test
    @DisplayName("deleteUser(): " + DELETES_USER_WHEN_USER_REGISTERED)
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
    @DisplayName("updateEmail(): " + THROWS_NULL_POINTER_WHEN_PRINCIPAL_NULL)
    void updateEmail_UserPrincipalIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.updateEmail(null, NEW_EMAIL));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateEmail(): " + THROWS_NULL_POINTER_WHEN_NEW_EMAIL_NULL)
    void updateEmail_NewEmailIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.updateEmail(userPrincipal, null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateEmail(): " + THROWS_ILLEGAL_ARGUMENT_WHEN_NEW_EMAIL_EMPTY)
    void updateEmail_EmailIsEmpty_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.updateEmail(userPrincipal, EMPTY));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateEmail(): " + THROWS_ILLEGAL_ARGUMENT_WHEN_NEW_EMAIL_BLANK)
    void updateEmail_EmailIsBlank_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.updateEmail(userPrincipal, BLANK));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateEmail(): " + THROWS_EMAIL_ALREADY_REGISTERED_WHEN_EMAIL_REGISTERED)
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
    @DisplayName("updateEmail(): " + UPDATES_EMAIL_WHEN_NEW_EMAIL_NOT_REGISTERED)
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
    @DisplayName("updatePassword(): " + THROWS_NULL_POINTER_WHEN_PRINCIPAL_NULL)
    void updatePassword_UserPrincipalIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.updatePassword(null, NEW_PASSWORD));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updatePassword(): " + THROWS_NULL_POINTER_WHEN_NEW_PASSWORD_NULL)
    void updatePassword_NewPasswordIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.updatePassword(userPrincipal, null));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updatePassword(): " + THROWS_ILLEGAL_ARGUMENT_WHEN_NEW_PASSWORD_EMPTY)
    void updatePassword_NewPasswordIsEmpty_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.updatePassword(userPrincipal, EMPTY));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updatePassword(): " + THROWS_ILLEGAL_ARGUMENT_WHEN_NEW_PASSWORD_BLANK)
    void updatePassword_NewPasswordIsBlank_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userService.updatePassword(userPrincipal, BLANK));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updatePassword(): " + THROWS_USER_NOT_FOUND_WHEN_USER_NOT_REGISTERED)
    void updatePassword_UserDoesNotExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updatePassword(userPrincipal, NEW_EMAIL));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updatePassword(): " + UPDATES_PASSWORD_WHEN_USER_REGISTERED)
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
    @DisplayName("addRoleToUser(): " + THROWS_NULL_POINTER_WHEN_PRINCIPAL_NULL)
    void addRoleToUser_UserPrincipalIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.addRoleToUser(null, ROLE_ADMIN));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("addRoleToUser(): " + THROWS_NULL_POINTER_WHEN_ROLE_NULL)
    void addRoleToUser_NewRoleIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.addRoleToUser(userPrincipal, null));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("addRoleToUser(): " + THROWS_USER_NOT_FOUND_WHEN_PRINCIPAL_NOT_REGISTERED)
    void addRoleToUser_UserDoesNotExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.addRoleToUser(userPrincipal, ROLE_ADMIN));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("addRoleToUser(): " + DOES_NOT_ADD_ROLE_WHEN_ROLE_ALREADY_ASSIGNED)
    void addRoleToUser_RoleAlreadyAssignedToUser_RoleNotAdded() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(roleService.getRole(ROLE_USER)).willReturn(userRole);

        UserPrincipal returnedPrincipal = userService.addRoleToUser(userPrincipal, ROLE_USER);
        Set<GrantedAuthority> authorities = returnedPrincipal.getAuthorities();

        assertEquals(1, authorities.size());
        verify(userRepository).findByEmail(email);
        verify(roleService).getRole(ROLE_USER);
        verify(userRepository, never()).update(user);
    }

    @Test
    @DisplayName("addRoleToUser(): " + ADDS_ROLE_WHEN_ROLE_NOT_ASSIGNED)
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
    @DisplayName("removeRoleFromUser(): " + THROWS_NULL_POINTER_WHEN_PRINCIPAL_NULL)
    void removeRoleFromUser_UserPrincipalIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.removeRoleFromUser(null, ROLE_USER));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + THROWS_NULL_POINTER_WHEN_ROLE_NULL)
    void removeRoleFromUser_RoleIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.removeRoleFromUser(userPrincipal, null));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_NOT_FOUND_WHEN_PRINCIPAL_NOT_REGISTERED)
    void removeRoleFromUser_UserDoesNotExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.removeRoleFromUser(userPrincipal, ROLE_USER));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_ROLE_REMOVAL_PROHIBITED_WHEN_USER_ROLE_REMOVED)
    void removeRoleFromUser_RoleIsUserRole_UserRoleRemovalNotAllowedExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(roleService.getRole(ROLE_USER)).willReturn(userRole);

        assertThrows(UserRoleRemovalProhibitedException.class, () -> userService.removeRoleFromUser(userPrincipal,
            ROLE_USER));
        verify(userRepository).findByEmail(email);
        verify(roleService).getRole(ROLE_USER);
        verify(userRepository, never()).update(user);
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + REMOVES_ROLE_WHEN_ROLE_NOT_ASSIGNED)
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
    @DisplayName("lockUser(): " + THROWS_NULL_POINTER_WHEN_PRINCIPAL_NULL)
    void lockUser_UserPrincipalIsNull_NullPointedExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> userService.lockUser(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("lockUser(): " + THROWS_USER_NOT_FOUND_WHEN_PRINCIPAL_NOT_REGISTERED)
    void lockUser_UserDoesNotExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.lockUser(userPrincipal));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("lockUser(): " + DOES_NOT_LOCK_USER_WHEN_USER_ALREADY_LOCKED)
    void lockUser_UserAlreadyLocked_UserNotUpdated() {
        String email = userPrincipal.getEmail();
        user.setIsLocked(true);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.lockUser(userPrincipal);

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("lockUser(): " + LOCKS_USER_WHEN_USER_NOT_LOCKED)
    void lockUser_UserNotLocked_UserLocked() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.lockUser(userPrincipal);

        verify(userRepository).findByEmail(email);
        verify(userRepository).update(userCaptor.capture());
        assertTrue(userCaptor.getValue().getIsLocked());
    }

    @Test
    @DisplayName("unlockUser(): " + THROWS_NULL_POINTER_WHEN_PRINCIPAL_NULL)
    void unlockUser_UserPrincipalIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.unlockUser(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("unlockUser(): " + THROWS_USER_NOT_FOUND_WHEN_PRINCIPAL_NOT_REGISTERED)
    void unlockUser_UserDoesNotExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.unlockUser(userPrincipal));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("unlockUser(): " + DOES_NOT_UNLOCK_USER_WHEN_ALREADY_UNLOCKED)
    void unlockUser_UserAlreadyUnlocked_UserNotUpdated() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.unlockUser(userPrincipal);

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("unlockUser(): " + UNLOCKS_USER_WHEN_USER_LOCKED)
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
    @DisplayName("disableUser(): " + THROWS_NULL_POINTER_WHEN_PRINCIPAL_NULL)
    void disableUser_UserPrincipalIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.disableUser(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("disableUser(): " + THROWS_USER_NOT_FOUND_WHEN_PRINCIPAL_NOT_REGISTERED)
    void disableUser_UserDoesntExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.disableUser(userPrincipal));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("disableUser(): " + DOES_NOT_DISABLE_USER_WHEN_USER_ALREADY_DISABLED)
    void disableUser_UserAlreadyDisabled_UserNotUpdated() {
        String email = userPrincipal.getEmail();
        user.setIsEnabled(false);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.disableUser(userPrincipal);
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("disableUser(): " + DISABLES_USER_WHEN_USER_ENABLED)
    void disableUser_UserIsEnabled_DisablesUser() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.disableUser(userPrincipal);

        verify(userRepository).findByEmail(email);
        verify(userRepository).update(userCaptor.capture());
        assertFalse(userCaptor.getValue().getIsEnabled());
    }

    @Test
    @DisplayName("enableUser(): " + THROWS_NULL_POINTER_WHEN_PRINCIPAL_NULL)
    void enableUser_UserPrincipalIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.enableUser(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("enableUser(): " + THROWS_USER_NOT_FOUND_WHEN_PRINCIPAL_NOT_REGISTERED)
    void enableUser_UserDoesntExist_UserNotFoundExceptionIsThrown() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.enableUser(userPrincipal));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("enableUser(): " + DOES_NOT_ENABLE_USER_WHEN_USER_ALREADY_ENABLED)
    void enableUser_UserAlreadyEnabled_UserNotUpdated() {
        String email = userPrincipal.getEmail();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.enableUser(userPrincipal);

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("enableUser(): " + ENABLES_USER_WHEN_USER_DISABLED)
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
    @DisplayName("addLoginAttempt(): " + THROWS_NULL_POINTER_WHEN_USER_NULL)
    void addLoginAttempt_UserIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.addLoginAttempt(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("addLoginAttempt(): " + ADDS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
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
    @DisplayName("resetLoginAttempt(): " + THROWS_NULL_POINTER_WHEN_USER_NULL)
    void resetLoginAttempt_UserIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> userService.resetLoginAttempt(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("resetLoginAttempt(): " + RESETS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
    void resetLoginAttempt_UserIsNotNull_LoginAttemptsReset() {
        user.setFailedLoginAttempts(2);

        userService.resetLoginAttempt(user);

        verify(userRepository).update(userCaptor.capture());
        assertEquals(0, userCaptor.getValue().getFailedLoginAttempts());
    }

}
