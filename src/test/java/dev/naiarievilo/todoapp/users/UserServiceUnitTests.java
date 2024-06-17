package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.UserRoleRemovalProhibitedException;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users.exceptions.EmailAlreadyRegisteredException;
import dev.naiarievilo.todoapp.users.exceptions.UserAlreadyExistsException;
import dev.naiarievilo.todoapp.users.exceptions.UserNotFoundException;
import dev.naiarievilo.todoapp.users_info.UserInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static dev.naiarievilo.todoapp.roles.Roles.ROLE_ADMIN;
import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static dev.naiarievilo.todoapp.users.UserServiceImpl.EMAIL_CONFIRMATION_PERIOD;
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
    private UserCreationDTO userCreationDTO;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUpUser() {
        userCreationDTO = new UserCreationDTO(EMAIL_1, PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);

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
        user.setEmail(EMAIL_1);
        user.setPassword(PASSWORD_1);
        user.addRole(userRole);
    }

    @Test
    @DisplayName("isUserExpired(): returns `false` when user is not expired")
    void isUserExpired_UserNotExpired_ReturnsFalse() {
        assertFalse(userService.isUserExpired(user));
    }

    @Test
    @DisplayName("isUserExpired(): returns `true` when user is expired")
    void isUserExpired_UserExpired_ReturnsTrue() {
        user.setCreationDate(LocalDateTime.now().minusDays(EMAIL_CONFIRMATION_PERIOD));
        assertTrue(userService.isUserExpired(user));
    }

    @Test
    @DisplayName("isUserInactive(): returns `false` when user is active")
    void isUserInactive_UserActive_ReturnsFalse() {
        assertFalse(userService.isUserInactive(user));
    }

    @Test
    @DisplayName("isUserInactive(): returns `true` when user is inactive")
    void isUserInactive_UserInactive_ReturnsTrue() {
        user.setLocked(true);
        user.setEnabled(false);
        assertTrue(userService.isUserInactive(user));
    }

    @Test
    @DisplayName("authenticateUser(): " + DOES_NOT_AUTHENTICATE_USER_WHEN_USER_ALREADY_AUTHENTICATED)
    void authenticateUser_UserAlreadyAuthenticated_DoesNotAuthenticateUser() {
        user.setAuthenticated(true);
        userService.authenticateUser(user);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("authenticateUser(): " + AUTHENTICATES_USER_WHEN_USER_NOT_AUTHENTICATED)
    void authenticateUser_UserNotAuthenticated_AuthenticatesUser() {
        userService.authenticateUser(user);
        verify(userRepository).update(userCaptor.capture());
        assertTrue(userCaptor.getValue().isAuthenticated());
    }

    @Test
    @DisplayName("userExists(Long id): " + RETURNS_FALSE_WHEN_USER_DOES_NOT_EXIST)
    void userExistsById_UserDoesNotExist_ReturnsFalse() {
        Long id = user.getId();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertFalse(userService.userExists(id));
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("userExists(Long id): " + RETURNS_TRUE_WHEN_USER_EXISTS)
    void userExistsById_UserExists_ReturnsTrue() {
        Long id = user.getId();
        given(userRepository.findById(id)).willReturn(Optional.of(user));

        assertTrue(userService.userExists(id));
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("userExists(String email): " + RETURNS_FALSE_WHEN_USER_DOES_NOT_EXIST)
    void userExistsByEmail_UserDoesNotExist_ReturnsFalse() {
        String email = user.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertFalse(userService.userExists(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("userExists(String email): " + RETURNS_TRUE_WHEN_USER_EXISTS)
    void userExistsByEmail_UserExists_ReturnsTrue() {
        String email = user.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        assertTrue(userService.userExists(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getUserByEmail(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void getUserByEmail_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = user.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getUserByEmail(): " + RETURNS_USER_WHEN_USER_EXISTS)
    void getUserByEmail_UserExists_ReturnsUser() {
        String email = user.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        User returnedUser = userService.getUserByEmail(email);
        assertEquals(user, returnedUser);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getUserById(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void getUserById_UserDoesNotExist_ThrowsUserNotFoundException() {
        Long id = user.getId();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(id));
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("getUserById(): " + RETURNS_USER_WHEN_USER_EXISTS)
    void getUserById_UserExists_ReturnsUser() {
        Long id = user.getId();
        given(userRepository.findById(id)).willReturn(Optional.of(user));

        User returnedUser = userService.getUserById(id);
        assertEquals(user, returnedUser);
        verify(userRepository).findById(id);
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

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        given(roleService.getRole(ROLE_USER)).willReturn(userRole);
        given(passwordEncoder.encode(password)).willReturn(encodedPassword);
        given(userRepository.persist(user)).willReturn(user);

        User returnedUser = userService.createUser(userCreationDTO);
        assertEquals(user, returnedUser);
        assertEquals(encodedPassword, returnedUser.getPassword());

        Set<Role> roles = returnedUser.getRoles();
        assertTrue(roles.size() == 1 && roles.contains(userRole));

        InOrder invokeInOrder = inOrder(roleService, passwordEncoder, userRepository, userInfoService);
        invokeInOrder.verify(userRepository).findByEmail(email);
        invokeInOrder.verify(roleService).getRole(ROLE_USER);
        invokeInOrder.verify(passwordEncoder).encode(password);
        invokeInOrder.verify(userRepository).persist(userCaptor.capture());
        invokeInOrder.verify(userInfoService).createUserInfo(userCreationDTO, user);

        User userCaptured = userCaptor.getValue();
        assertEquals(encodedPassword, userCaptured.getPassword());
        assertTrue(userCaptured.isEnabled());
        assertFalse(userCaptured.isLocked());
    }

    @Test
    @DisplayName("deleteUser(): " + DELETES_USER_WHEN_USER_EXISTS)
    void deleteUser_UserExists_DeletesUser() {
        Long id = user.getId();
        given(userRepository.findById(id)).willReturn(Optional.of(user));
        userService.deleteUser(user);

        InOrder invokeInOrder = inOrder(userRepository, userInfoService);
        invokeInOrder.verify(userRepository).findById(id);
        invokeInOrder.verify(userInfoService).deleteUserInfo(id);
        invokeInOrder.verify(userRepository).delete(userCaptor.capture());
        assertTrue(userCaptor.getValue().getRoles().isEmpty());
    }

    @Test
    @DisplayName("updateEmail(): " + THROWS_EMAIL_ALREADY_REGISTERED_WHEN_EMAIL_ALREADY_REGISTERED)
    void updateEmail_NewEmailAlreadyRegistered_ThrowsEmailAlreadyRegisteredException() {
        User otherUser = new User();
        otherUser.setEmail(NEW_EMAIL);
        given(userRepository.findByEmail(NEW_EMAIL)).willReturn(Optional.of(otherUser));

        assertThrows(EmailAlreadyRegisteredException.class, () -> userService.updateEmail(user, NEW_EMAIL));
        verify(userRepository).findByEmail(NEW_EMAIL);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("updateEmail(): " + DOES_NOT_UPDATE_EMAIL_WHEN_NEW_EMAIL_NOT_NEW)
    void updateEmail_NewEmailAndCurrentEmailEqual_DoesNotUpdateEmail() {
        userService.updateEmail(user, user.getEmail());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updateEmail(): " + UPDATES_EMAIL_WHEN_NEW_EMAIL_NOT_REGISTERED)
    void updateEmail_NewEmailIsNotRegistered_UpdatesEmail() {
        given(userRepository.findByEmail(NEW_EMAIL)).willReturn(Optional.empty());

        User updatedUser = userService.updateEmail(user, NEW_EMAIL);
        assertEquals(NEW_EMAIL, updatedUser.getEmail());
        verify(userRepository).findByEmail(NEW_EMAIL);
        verify(userRepository).update(userCaptor.capture());
        assertEquals(NEW_EMAIL, userCaptor.getValue().getEmail());

    }

    @Test
    @DisplayName("updatePassword(): " + THROWS_BAD_CREDENTIALS_WHEN_CURRENT_PASSWORD_INCORRECT)
    void updatePassword_IncorrectCurrentPassword_ThrowsBadCredentialsException() {
        String currentPassword = user.getPassword();
        given(passwordEncoder.matches(PASSWORD_1, currentPassword)).willReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.updatePassword(user, PASSWORD_1, NEW_PASSWORD));
        verify(passwordEncoder).matches(PASSWORD_1, currentPassword);
        verify(passwordEncoder, never()).encode(NEW_PASSWORD);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("updatePassword(): " + DOES_NOT_UPDATE_PASSWORD_WHEN_NEW_PASSWORD_NOT_NEW)
    void updatePassword_NewPasswordEqualToCurrentPassword_DoesNotUpdatePassword() {
        String currentPassword = user.getPassword();
        given(passwordEncoder.matches(currentPassword, currentPassword)).willReturn(true);

        userService.updatePassword(user, currentPassword, currentPassword);
        verify(passwordEncoder).matches(currentPassword, PASSWORD_1);
        verify(passwordEncoder, never()).encode(currentPassword);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updatePassword(): " + UPDATES_PASSWORD_WHEN_CURRENT_PASSWORD_CORRECT)
    void updatePassword_UserExistsAndCorrectOldPassword_UpdatesPassword() {
        String currentPassword = user.getPassword();
        String newEncodedPassword = "newEncodedPassword";

        given(passwordEncoder.matches(PASSWORD_1, currentPassword)).willReturn(true);
        given(passwordEncoder.encode(NEW_PASSWORD)).willReturn(newEncodedPassword);

        User updatedUser = userService.updatePassword(user, PASSWORD_1, NEW_PASSWORD);
        assertEquals(newEncodedPassword, updatedUser.getPassword());
        verify(passwordEncoder).matches(PASSWORD_1, currentPassword);
        verify(passwordEncoder).encode(NEW_PASSWORD);
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("addRoleToUser(): " + DOES_NOT_ADD_ROLE_WHEN_ROLE_ALREADY_ASSIGNED)
    void addRoleToUser_UserAlreadyHasRole_DoesNotAddRole() {
        userService.addRoleToUser(user, ROLE_USER);
        verifyNoInteractions(roleService);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("addRoleToUser(): " + ADDS_ROLE_TO_USER_WHEN_ROLE_NOT_ASSIGNED)
    void addRoleToUser_UserDoesNotHaveRole_AddsRoleToUser() {
        given(roleService.getRole(ROLE_ADMIN)).willReturn(adminRole);

        User updatedUser = userService.addRoleToUser(user, ROLE_ADMIN);
        assertTrue(updatedUser.getRoles().contains(adminRole));
        verify(roleService).getRole(ROLE_ADMIN);
        verify(userRepository).update(userCaptor.capture());
        assertTrue(userCaptor.getValue().getRoles().contains(adminRole));
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + DOES_NOT_REMOVE_ROLE_WHEN_ROLE_NOT_ASSIGNED)
    void removeRoleFromUser_UserDoesNotHaveRole_DoesNotRemoveRole() {
        userService.removeRoleFromUser(user, ROLE_ADMIN);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_ROLE_REMOVAL_PROHIBITED_WHEN_REMOVING_USER_ROLE)
    void removeRoleFromUser_RoleToRemoveIsUserRole_ThrowsUserRoleRemovalProhibitedException() {
        assertThrows(UserRoleRemovalProhibitedException.class, () -> userService.removeRoleFromUser(user, ROLE_USER));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + REMOVES_ROLE_WHEN_ROLE_ASSIGNED_AND_REMOVABLE)
    void removeRoleFromUser_RoleIsAssignedAndRemovable_RemovesRole() {
        user.addRole(adminRole);
        given(roleService.getRole(ROLE_ADMIN)).willReturn(adminRole);

        User updatedUser = userService.removeRoleFromUser(user, ROLE_ADMIN);
        assertFalse(updatedUser.getRoles().contains(adminRole));
        verify(roleService).getRole(ROLE_ADMIN);
        verify(userRepository).update(userCaptor.capture());
        assertFalse(userCaptor.getValue().getRoles().contains(adminRole));
    }

    @Test
    @DisplayName("lockUser(): " + DOES_NOT_LOCK_USER_WHEN_USER_ALREADY_LOCKED)
    void lockUser_UserAlreadyLocked_DoesNotLockUser() {
        user.setLocked(true);
        userService.lockUser(user);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("lockUser(): " + LOCKS_USER_WHEN_USER_NOT_LOCKED)
    void lockUser_UserNotLocked_LocksUser() {
        User updatedUser = userService.lockUser(user);
        assertTrue(updatedUser.isLocked());
        verify(userRepository).update(userCaptor.capture());
        assertTrue(userCaptor.getValue().isLocked());
    }

    @Test
    @DisplayName("unlockUser(): " + DOES_NOT_UNLOCK_USER_WHEN_USER_ALREADY_UNLOCKED)
    void unlockUser_UserAlreadyUnlocked_DoesNotUnlockUser() {
        userService.unlockUser(user);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("unlockUser(): " + UNLOCKS_USER_WHEN_USER_LOCKED)
    void unlockUser_UserIsLocked_UnlocksUser() {
        user.setLocked(true);

        User updatedUser = userService.unlockUser(user);
        assertFalse(updatedUser.isLocked());
        verify(userRepository).update(userCaptor.capture());
        assertFalse(userCaptor.getValue().isLocked());
    }

    @Test
    @DisplayName("disableUser(): " + DOES_NOT_DISABLE_USER_WHEN_USER_ALREADY_DISABLED)
    void disableUser_UserAlreadyDisabled_DoesNotDisableUser() {
        user.setEnabled(false);
        userService.disableUser(user);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("disableUser(): " + DISABLES_USER_WHEN_USER_ENABLED)
    void disableUser_UserEnabled_DisablesUser() {
        User updatedUser = userService.disableUser(user);
        assertFalse(updatedUser.isEnabled());
        verify(userRepository).update(userCaptor.capture());
        assertFalse(userCaptor.getValue().isEnabled());
    }

    @Test
    @DisplayName("enableUser(): " + DOES_NOT_ENABLE_USER_WHEN_USER_ALREADY_ENABLED)
    void enableUser_UserAlreadyEnabled_DoesNotEnableUser() {
        userService.enableUser(user);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("enableUser(): " + ENABLES_USER_WHEN_USER_DISABLED)
    void enableUser_UserDisabled_EnablesUser() {
        user.setEnabled(false);

        User updatedUser = userService.enableUser(user);
        assertTrue(updatedUser.isEnabled());
        verify(userRepository).update(userCaptor.capture());
        assertTrue(userCaptor.getValue().isEnabled());
    }

    @Test
    @DisplayName("addLoginAttempt(): " + ADDS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
    void addLoginAttempt_UserIsNotNull_AddsLoginAttempt() {
        int oldLoginAttempts = user.getLoginAttempts();
        LocalDateTime oldLocalTime = LocalDateTime.now();
        user.setLastLoginAttempt(oldLocalTime);

        userService.addLoginAttempt(user);
        verify(userRepository).update(userCaptor.capture());
        User userCaptured = userCaptor.getValue();
        assertNotEquals(oldLoginAttempts, userCaptured.getLoginAttempts());
        assertNotEquals(oldLocalTime, userCaptured.getLastLoginAttempt());
    }

    @Test
    @DisplayName("resetLoginAttempts(): " + RESETS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
    void resetLoginAttempts_UserIsNotNull_ResetsLoginAttempts() {
        user.setLoginAttempts((byte) 2);

        userService.resetLoginAttempts(user);
        verify(userRepository).update(userCaptor.capture());
        assertEquals(0, userCaptor.getValue().getLoginAttempts());
    }

}
