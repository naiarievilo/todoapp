package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.UserRoleRemovalProhibitedException;
import dev.naiarievilo.todoapp.security.EmailPasswordAuthenticationToken;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import dev.naiarievilo.todoapp.security.UserPrincipalImpl;
import dev.naiarievilo.todoapp.users_info.UserInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import static dev.naiarievilo.todoapp.roles.Roles.ROLE_ADMIN;
import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static dev.naiarievilo.todoapp.users.UserServiceTestCaseMessages.*;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTests {

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

    @Captor
    private ArgumentCaptor<User> userCaptor;
    private User user;
    private UserPrincipal userPrincipal;
    private UserCreationDTO userCreationDTO;
    private Role userRole;
    private Role adminRole;
    private Authentication authentication;

    @BeforeEach
    void setUpUser() {
        userCreationDTO = new UserCreationDTO(EMAIL, PASSWORD, CONFIRM_PASSWORD, FIRST_NAME, LAST_NAME);

        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(ROLE_USER.name());
        userRole.setDescription(ROLE_USER.description());

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(ROLE_ADMIN.name());
        adminRole.setDescription(ROLE_ADMIN.description());

        user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);
        user.addRole(userRole);

        userPrincipal = UserPrincipalImpl.withUser(user);

        authentication = EmailPasswordAuthenticationToken.authenticated(userPrincipal.getEmail(),
            userPrincipal.getPassword(), userPrincipal.getAuthorities());
    }

    @Test
    @DisplayName("userExists(): " + RETURNS_FALSE_WHEN_USER_DOES_NOT_EXIST)
    void userExists_UserDoesNotExist_ReturnsFalse() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());
        assertFalse(userService.userExists(EMAIL));
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("userExists(): " + RETURNS_TRUE_WHEN_USER_EXISTS)
    void userExists_UserExists_ReturnsTrue() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
        assertTrue(userService.userExists(EMAIL));
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("loadUserPrincipalByEmail(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void loadUserPrincipalByEmail_UserDoesNotExist_ThrowsUserPrincipalNotFoundException() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.loadUserPrincipalByEmail(EMAIL));
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("loadUserPrincipalByEmail(): " + RETURNS_PRINCIPAL_WHEN_USER_EXISTS)
    void loadUserPrincipalByEmail_UserExists_ReturnsUserPrincipalPrincipal() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
        UserPrincipal returnedPrincipal = userService.loadUserPrincipalByEmail(EMAIL);
        assertEquals(userPrincipal, returnedPrincipal);
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("getUserByEmail(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void getUserByEmail_UserDoesNotExist_ThrowsUserNotFoundException() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(EMAIL));
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("getUserByEmail(): " + RETURNS_USER_WHEN_USER_EXISTS)
    void getUserByEmail_UserExists_ReturnsUser() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
        User returnedUser = userService.getUserByEmail(EMAIL);
        assertEquals(user, returnedUser);
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("getUserByPrincipal(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void getUserByPrincipal_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = userPrincipal.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserByPrincipal(userPrincipal));
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getUserByPrincipal(): " + RETURNS_USER_WHEN_USER_EXISTS)
    void getUserByPrincipal_UserExists_ReturnsUser() {
        String email = userPrincipal.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        User returnedUser = userService.getUserByPrincipal(userPrincipal);
        assertEquals(user, returnedUser);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("createUser(): " + THROWS_USER_ALREADY_EXISTS_WHEN_USER_ALREADY_EXISTS)
    void createUser_UserAlreadyExists_ThrowsUserAlreadyExistException() {
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
    @DisplayName("createUser(): " + CREATES_USER_WHEN_USER_DOES_NOT_EXIST)
    void createUser_UserDoestNotExist_CreatesUser() {
        String email = userCreationDTO.email();
        String password = userCreationDTO.password();
        String encodedPassword = "encodedPassword";
        GrantedAuthority userRoleAuthority = new SimpleGrantedAuthority(ROLE_USER.name());

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        given(roleService.getRole(ROLE_USER)).willReturn(userRole);
        given(passwordEncoder.encode(password)).willReturn(encodedPassword);
        given(userRepository.persist(user)).willReturn(user);

        Authentication returnedAuthentication = userService.createUser(userCreationDTO);
        assertEquals(authentication, returnedAuthentication);
        assertEquals(encodedPassword, returnedAuthentication.getCredentials());

        Collection<? extends GrantedAuthority> authorities = returnedAuthentication.getAuthorities();
        assertTrue(authorities.size() == 1 && authorities.contains(userRoleAuthority));

        InOrder invokeInOrder = inOrder(roleService, passwordEncoder, userRepository, userInfoService);
        invokeInOrder.verify(userRepository).findByEmail(email);
        invokeInOrder.verify(roleService).getRole(ROLE_USER);
        invokeInOrder.verify(passwordEncoder).encode(password);
        invokeInOrder.verify(userRepository).persist(userCaptor.capture());
        invokeInOrder.verify(userInfoService).createUserInfo(userCreationDTO, user);

        User userCaptured = userCaptor.getValue();
        assertEquals(encodedPassword, userCaptured.getPassword());
        assertTrue(userCaptured.getIsEnabled());
        assertFalse(userCaptured.getIsLocked());
        assertEquals(LocalDate.now(), userCaptured.getCreatedOn());
    }

    @Test
    @DisplayName("deleteUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void deleteUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = userPrincipal.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(email));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).delete(any(User.class));
        verifyNoInteractions(userInfoService);
    }

    @Test
    @DisplayName("deleteUser(): " + DELETES_USER_WHEN_USER_EXISTS)
    void deleteUser_UserExists_DeletesUser() {
        String email = userPrincipal.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.deleteUser(email);

        InOrder invokeInOrder = inOrder(userRepository, userInfoService);
        invokeInOrder.verify(userRepository).findByEmail(email);
        invokeInOrder.verify(userInfoService).deleteUserInfo(user.getId());
        invokeInOrder.verify(userRepository).delete(userCaptor.capture());
        assertTrue(userCaptor.getValue().getRoles().isEmpty());
    }

    @Test
    @DisplayName("updateEmail(): " + THROWS_EMAIL_ALREADY_REGISTERED_WHEN_EMAIL_ALREADY_REGISTERED)
    void updateEmail_NewEmailAlreadyRegistered_ThrowsEmailAlreadyRegisteredException() {
        String oldEmail = (String) authentication.getPrincipal();
        User otherUser = new User();
        otherUser.setEmail(NEW_EMAIL);

        given(userRepository.findByEmail(NEW_EMAIL)).willReturn(Optional.of(otherUser));

        assertThrows(EmailAlreadyRegisteredException.class, () -> userService.updateEmail(oldEmail, NEW_EMAIL));
        verify(userRepository).findByEmail(NEW_EMAIL);
        verify(userRepository, never()).findByEmail(oldEmail);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("updateEmail(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void updateEmail_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateEmail(email, NEW_EMAIL));
        verify(userRepository, times(2)).findByEmail(anyString());
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("updateEmail(): " + UPDATES_EMAIL_WHEN_NEW_EMAIL_NOT_REGISTERED)
    void updateEmail_NewEmailIsNotRegistered_UpdatesEmail() {
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(NEW_EMAIL)).willReturn(Optional.empty());
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        Authentication returnedAuthentication = userService.updateEmail(email, NEW_EMAIL);

        assertEquals(NEW_EMAIL, returnedAuthentication.getPrincipal());
        verify(userRepository).findByEmail(NEW_EMAIL);
        verify(userRepository).findByEmail(email);
        verify(userRepository).update(user);

    }

    @Test
    @DisplayName("updatePassword(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void updatePassword_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updatePassword(email, NEW_EMAIL));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updatePassword(): " + UPDATES_PASSWORD_WHEN_USER_EXISTS)
    void updatePassword_UserExists_UpdatesPassword() {
        String email = (String) authentication.getPrincipal();
        String newEncodedPassword = "newEncodedPassword";

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.encode(NEW_PASSWORD)).willReturn(newEncodedPassword);

        Authentication returnedAuthentication = userService.updatePassword(email, NEW_PASSWORD);
        assertEquals(newEncodedPassword, returnedAuthentication.getCredentials());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).encode(NEW_PASSWORD);
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("addRoleToUser(): " + DOES_NOT_ADD_ROLE_WHEN_ROLE_ALREADY_ASSIGNED)
    void addRoleToUser_UserAlreadyHasRole_DoesNotAddRole() {
        userService.addRoleToUser(authentication, ROLE_USER);
        verifyNoInteractions(roleService);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("addRoleToUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void addRoleToUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.addRoleToUser(authentication, ROLE_ADMIN));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("addRoleToUser(): " + ADDS_ROLE_TO_USER_WHEN_ROLE_NOT_ASSIGNED)
    void addRoleToUser_UserDoesNotHaveRole_AddsRoleToUser() {
        String email = (String) authentication.getPrincipal();
        Role roleToAdd = new Role();
        roleToAdd.setName(ROLE_ADMIN.name());
        GrantedAuthority roleAdded = new SimpleGrantedAuthority(roleToAdd.getName());

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(roleService.getRole(ROLE_ADMIN)).willReturn(roleToAdd);

        Authentication returnedAuthentication = userService.addRoleToUser(authentication, ROLE_ADMIN);
        Collection<? extends GrantedAuthority> authorities = returnedAuthentication.getAuthorities();

        assertTrue(authorities.contains(roleAdded));
        verify(userRepository).findByEmail(email);
        verify(roleService).getRole(ROLE_ADMIN);
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + DOES_NOT_REMOVE_ROLE_WHEN_ROLE_NOT_ASSIGNED)
    void removeRoleFromUser_UserDoesNotHaveRole_DoesNotRemoveRole() {
        userService.removeRoleFromUser(authentication, ROLE_ADMIN);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_ROLE_REMOVAL_PROHIBITED_WHEN_REMOVING_USER_ROLE)
    void removeRoleFromUser_RoleToRemoveIsUserRole_ThrowsUserRoleRemovalProhibitedException() {
        assertThrows(UserRoleRemovalProhibitedException.class,
            () -> userService.removeRoleFromUser(authentication, ROLE_USER));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void removeRoleFromUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        user.addRole(adminRole);
        authentication = EmailPasswordAuthenticationToken.authenticated(user.getEmail(),
            UserServiceImpl.getRolesFromUser(user));
        String email = (String) authentication.getPrincipal();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.removeRoleFromUser(authentication, ROLE_ADMIN));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + REMOVES_ROLE_WHEN_ROLE_ASSIGNED_AND_REMOVABLE)
    void removeRoleFromUser_RoleIsAssignedAndRemovable_RemovesRole() {
        user.addRole(adminRole);
        GrantedAuthority roleToRemove = new SimpleGrantedAuthority(adminRole.getName());
        authentication = EmailPasswordAuthenticationToken.authenticated(user.getEmail(),
            UserServiceImpl.getRolesFromUser(user));
        String email = (String) authentication.getPrincipal();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(roleService.getRole(ROLE_ADMIN)).willReturn(adminRole);

        Authentication returnedAuthentication = userService.removeRoleFromUser(authentication, ROLE_ADMIN);
        Collection<? extends GrantedAuthority> authorities = returnedAuthentication.getAuthorities();

        assertFalse(authorities.contains(roleToRemove));
        verify(userRepository).findByEmail(email);
        verify(roleService).getRole(ROLE_ADMIN);
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("lockUser(): " + DOES_NOT_LOCK_USER_WHEN_USER_ALREADY_LOCKED)
    void lockUser_UserAlreadyLocked_DoesNotLockUser() {
        user.setIsLocked(true);
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.lockUser(email);
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("lockUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void lockUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.lockUser(email));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }


    @Test
    @DisplayName("lockUser(): " + LOCKS_USER_WHEN_USER_NOT_LOCKED)
    void lockUser_UserNotLocked_LocksUser() {
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.lockUser(email);
        verify(userRepository).findByEmail(email);
        verify(userRepository).update(userCaptor.capture());
        assertTrue(userCaptor.getValue().getIsLocked());
    }

    @Test
    @DisplayName("unlockUser(): " + DOES_NOT_UNLOCK_USER_WHEN_USER_ALREADY_UNLOCKED)
    void unlockUser_UserAlreadyUnlocked_DoesNotUnlockUser() {
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.unlockUser(email);
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("unlockUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void unlockUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        user.setIsLocked(true);
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.unlockUser(email));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("unlockUser(): " + UNLOCKS_USER_WHEN_USER_LOCKED)
    void unlockUser_UserIsLocked_UnlocksUser() {
        user.setIsLocked(true);
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.unlockUser(email);
        verify(userRepository).findByEmail(email);
        verify(userRepository).update(userCaptor.capture());
        assertFalse(userCaptor.getValue().getIsLocked());
    }

    @Test
    @DisplayName("disableUser(): " + DOES_NOT_DISABLE_USER_WHEN_USER_ALREADY_DISABLED)
    void disableUser_UserAlreadyDisabled_DoesNotDisableUser() {
        user.setIsEnabled(false);
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.disableUser(email);
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("disableUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void disableUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.disableUser(email));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("disableUser(): " + DISABLES_USER_WHEN_USER_ENABLED)
    void disableUser_UserEnabled_DisablesUser() {
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.disableUser(email);
        verify(userRepository).findByEmail(email);
        verify(userRepository).update(userCaptor.capture());
        assertFalse(userCaptor.getValue().getIsEnabled());
    }

    @Test
    @DisplayName("enableUser(): " + DOES_NOT_ENABLE_USER_WHEN_USER_ALREADY_ENABLED)
    void enableUser_UserAlreadyEnabled_DoesNotEnableUser() {
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.enableUser(email);
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("enableUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void enableUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        user.setIsEnabled(false);
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.enableUser(email));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("enableUser(): " + ENABLES_USER_WHEN_USER_DISABLED)
    void enableUser_UserDisabled_EnablesUser() {
        user.setIsEnabled(false);
        String email = (String) authentication.getPrincipal();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        userService.enableUser(email);
        verify(userRepository).findByEmail(email);
        verify(userRepository).update(userCaptor.capture());
        assertTrue(userCaptor.getValue().getIsEnabled());
    }

    @Test
    @DisplayName("addLoginAttempt(): " + ADDS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
    void addLoginAttempt_UserIsNotNull_AddsLoginAttempt() {
        LocalDateTime oldLocalTime = LocalDateTime.now();
        user.setFailedLoginTime(oldLocalTime);
        int oldLoginAttempts = user.getFailedLoginAttempts();

        userService.addLoginAttempt(user);
        verify(userRepository).update(userCaptor.capture());
        User userCaptured = userCaptor.getValue();
        assertNotEquals(oldLoginAttempts, userCaptured.getFailedLoginAttempts());
        assertNotEquals(oldLocalTime, userCaptured.getFailedLoginTime());
    }

    @Test
    @DisplayName("resetLoginAttempts(): " + RESETS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
    void resetLoginAttempts_UserIsNotNull_ResetsLoginAttempts() {
        user.setFailedLoginAttempts(2);

        userService.resetLoginAttempts(user);
        verify(userRepository).update(userCaptor.capture());
        assertEquals(0, userCaptor.getValue().getFailedLoginAttempts());
    }

}
