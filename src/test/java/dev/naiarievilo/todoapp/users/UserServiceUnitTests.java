package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.UserRoleRemovalProhibitedException;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import dev.naiarievilo.todoapp.security.UserPrincipalImpl;
import dev.naiarievilo.todoapp.users.dtos.CreateUserDTO;
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
    private CreateUserDTO createUserDTO;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUpUser() {
        createUserDTO = new CreateUserDTO(EMAIL_1, PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);

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

        userPrincipal = UserPrincipalImpl.fromUser(user);
    }

    @Test
    @DisplayName("userExists(Long id): " + RETURNS_FALSE_WHEN_USER_DOES_NOT_EXIST)
    void userExistsById_UserDoesNotExist_ReturnsFalse() {
        Long id = userPrincipal.getId();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertFalse(userService.userExists(id));
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("userExists(Long id): " + RETURNS_TRUE_WHEN_USER_EXISTS)
    void userExistsById_UserExists_ReturnsTrue() {
        Long id = userPrincipal.getId();
        given(userRepository.findById(id)).willReturn(Optional.of(user));

        assertTrue(userService.userExists(id));
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("userExists(String email): " + RETURNS_FALSE_WHEN_USER_DOES_NOT_EXIST)
    void userExistsByEmail_UserDoesNotExist_ReturnsFalse() {
        String email = userPrincipal.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertFalse(userService.userExists(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("userExists(String email): " + RETURNS_TRUE_WHEN_USER_EXISTS)
    void userExistsByEmail_UserExists_ReturnsTrue() {
        String email = userPrincipal.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        assertTrue(userService.userExists(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("loadUserPrincipalById(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void loadUserPrincipalById_UserDoesNotExist_ThrowsUserPrincipalNotFoundException() {
        Long id = userPrincipal.getId();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.loadUserPrincipalById(id));
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("loadUserPrincipalById(): " + RETURNS_PRINCIPAL_WHEN_USER_EXISTS)
    void loadUserPrincipalById_UserExists_ReturnsUserPrincipalPrincipal() {
        Long id = userPrincipal.getId();
        given(userRepository.findById(id)).willReturn(Optional.of(user));

        UserPrincipal returnedUserPrincipal = userService.loadUserPrincipalById(id);
        assertEquals(userPrincipal, returnedUserPrincipal);
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("getUserByEmail(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void getUserByEmail_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = userPrincipal.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getUserByEmail(): " + RETURNS_USER_WHEN_USER_EXISTS)
    void getUserByEmail_UserExists_ReturnsUser() {
        String email = userPrincipal.getEmail();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        User returnedUser = userService.getUserByEmail(email);
        assertEquals(user, returnedUser);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getUserById(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void getUserById_UserDoesNotExist_ThrowsUserNotFoundException() {
        Long id = userPrincipal.getId();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(id));
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("getUserById(): " + RETURNS_USER_WHEN_USER_EXISTS)
    void getUserById_UserExists_ReturnsUser() {
        Long id = userPrincipal.getId();
        given(userRepository.findById(id)).willReturn(Optional.of(user));

        User returnedUser = userService.getUserById(id);
        assertEquals(user, returnedUser);
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("createUser(): " + THROWS_USER_ALREADY_EXISTS_WHEN_USER_ALREADY_EXISTS)
    void createUser_UserAlreadyExists_ThrowsUserAlreadyExistException() {
        String email = createUserDTO.email();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(createUserDTO));
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).update(any(User.class));
        verifyNoInteractions(roleService);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(userInfoService);
    }

    @Test
    @DisplayName("createUser(): " + CREATES_USER_WHEN_USER_DOES_NOT_EXIST)
    void createUser_UserDoestNotExist_CreatesUser() {
        String email = createUserDTO.email();
        String password = createUserDTO.password();
        String encodedPassword = "encodedPassword";
        GrantedAuthority userRoleAuthority = new SimpleGrantedAuthority(ROLE_USER.name());

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        given(roleService.getRole(ROLE_USER)).willReturn(userRole);
        given(passwordEncoder.encode(password)).willReturn(encodedPassword);
        given(userRepository.persist(user)).willReturn(user);

        UserPrincipal returnedUserPrincipal = userService.createUser(createUserDTO);
        assertEquals(userPrincipal, returnedUserPrincipal);
        assertEquals(encodedPassword, returnedUserPrincipal.getPassword());

        Collection<GrantedAuthority> authorities = returnedUserPrincipal.getAuthorities();
        assertTrue(authorities.size() == 1 && authorities.contains(userRoleAuthority));

        InOrder invokeInOrder = inOrder(roleService, passwordEncoder, userRepository, userInfoService);
        invokeInOrder.verify(userRepository).findByEmail(email);
        invokeInOrder.verify(roleService).getRole(ROLE_USER);
        invokeInOrder.verify(passwordEncoder).encode(password);
        invokeInOrder.verify(userRepository).persist(userCaptor.capture());
        invokeInOrder.verify(userInfoService).createUserInfo(createUserDTO, user);

        User userCaptured = userCaptor.getValue();
        assertEquals(encodedPassword, userCaptured.getPassword());
        assertTrue(userCaptured.getIsEnabled());
        assertFalse(userCaptured.getIsLocked());
        assertEquals(LocalDate.now(), userCaptured.getCreatedOn());
    }

    @Test
    @DisplayName("deleteUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void deleteUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        Long id = userPrincipal.getId();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userPrincipal));
        verify(userRepository).findById(id);
        verify(userRepository, never()).delete(any(User.class));
        verifyNoInteractions(userInfoService);
    }

    @Test
    @DisplayName("deleteUser(): " + DELETES_USER_WHEN_USER_EXISTS)
    void deleteUser_UserExists_DeletesUser() {
        Long id = userPrincipal.getId();
        given(userRepository.findById(id)).willReturn(Optional.of(user));

        userService.deleteUser(userPrincipal);

        InOrder invokeInOrder = inOrder(userRepository, userInfoService);
        invokeInOrder.verify(userRepository).findById(id);
        invokeInOrder.verify(userInfoService).deleteUserInfo(user.getId());
        invokeInOrder.verify(userRepository).delete(userCaptor.capture());
        assertTrue(userCaptor.getValue().getRoles().isEmpty());
    }

    @Test
    @DisplayName("updateEmail(): " + THROWS_EMAIL_ALREADY_REGISTERED_WHEN_EMAIL_ALREADY_REGISTERED)
    void updateEmail_NewEmailAlreadyRegistered_ThrowsEmailAlreadyRegisteredException() {
        User otherUser = new User();
        otherUser.setEmail(NEW_EMAIL);

        given(userRepository.findByEmail(NEW_EMAIL)).willReturn(Optional.of(otherUser));

        assertThrows(EmailAlreadyRegisteredException.class, () -> userService.updateEmail(userPrincipal, NEW_EMAIL));
        verify(userRepository).findByEmail(NEW_EMAIL);
        verify(userRepository, never()).findByEmail(userPrincipal.getEmail());
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("updateEmail(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void updateEmail_UserDoesNotExist_ThrowsUserNotFoundException() {
        Long id = userPrincipal.getId();
        given(userRepository.findByEmail(NEW_EMAIL)).willReturn(Optional.empty());
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateEmail(userPrincipal, NEW_EMAIL));
        verify(userRepository).findByEmail(NEW_EMAIL);
        verify(userRepository).findById(id);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("updateEmail(): " + DOES_NOT_UPDATE_EMAIL_WHEN_NEW_EMAIL_NOT_NEW)
    void updateEmail_NewEmailAndCurrentEmailEqual_DoesNotUpdateEmail() {
        userService.updateEmail(userPrincipal, userPrincipal.getEmail());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updateEmail(): " + UPDATES_EMAIL_WHEN_NEW_EMAIL_NOT_REGISTERED)
    void updateEmail_NewEmailIsNotRegistered_UpdatesEmail() {
        Long id = userPrincipal.getId();
        given(userRepository.findByEmail(NEW_EMAIL)).willReturn(Optional.empty());
        given(userRepository.findById(id)).willReturn(Optional.of(user));

        UserPrincipal updatedUserPrincipal = userService.updateEmail(userPrincipal, NEW_EMAIL);
        assertEquals(NEW_EMAIL, updatedUserPrincipal.getEmail());
        verify(userRepository).findByEmail(NEW_EMAIL);
        verify(userRepository).findById(id);
        verify(userRepository).update(userCaptor.capture());
        assertEquals(NEW_EMAIL, userCaptor.getValue().getEmail());

    }

    @Test
    @DisplayName("updatePassword(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void updatePassword_UserDoesNotExist_ThrowsUserNotFoundException() {
        Long id = userPrincipal.getId();
        String password = userPrincipal.getPassword();
        given(passwordEncoder.matches(PASSWORD_1, password)).willReturn(true);
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updatePassword(userPrincipal, PASSWORD_1,
            NEW_PASSWORD));
        verify(passwordEncoder).matches(PASSWORD_1, password);
        verify(userRepository).findById(id);
        verify(passwordEncoder, never()).encode(NEW_PASSWORD);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("updatePassword(): " + THROWS_BAD_CREDENTIALS_WHEN_CURRENT_PASSWORD_INCORRECT)
    void updatePassword_IncorrectCurrentPassword_ThrowsBadCredentialsException() {
        Long id = userPrincipal.getId();
        String currentPassword = user.getPassword();
        given(passwordEncoder.matches(PASSWORD_1, currentPassword)).willReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.updatePassword(userPrincipal, PASSWORD_1,
            NEW_PASSWORD));
        verify(passwordEncoder).matches(PASSWORD_1, currentPassword);
        verify(userRepository, never()).findById(id);
        verify(passwordEncoder, never()).encode(NEW_PASSWORD);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("updatePassword(): " + DOES_NOT_UPDATE_PASSWORD_WHEN_NEW_PASSWORD_NOT_NEW)
    void updatePassword_NewPasswordEqualToCurrentPassword_DoesNotUpdatePassword() {
        String currentPassword = userPrincipal.getPassword();
        given(passwordEncoder.matches(currentPassword, currentPassword)).willReturn(true);
        userService.updatePassword(userPrincipal, currentPassword, currentPassword);
        verify(passwordEncoder).matches(currentPassword, PASSWORD_1);
        verify(passwordEncoder, never()).encode(currentPassword);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updatePassword(): " + UPDATES_PASSWORD_WHEN_CURRENT_PASSWORD_CORRECT)
    void updatePassword_UserExistsAndCorrectOldPassword_UpdatesPassword() {
        Long id = userPrincipal.getId();
        String currentPassword = user.getPassword();
        String newEncodedPassword = "newEncodedPassword";

        given(userRepository.findById(id)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(PASSWORD_1, currentPassword)).willReturn(true);
        given(passwordEncoder.encode(NEW_PASSWORD)).willReturn(newEncodedPassword);

        UserPrincipal updatedUserPrincipal = userService.updatePassword(userPrincipal, PASSWORD_1, NEW_PASSWORD);
        assertEquals(newEncodedPassword, updatedUserPrincipal.getPassword());
        verify(userRepository).findById(id);
        verify(passwordEncoder).matches(PASSWORD_1, currentPassword);
        verify(passwordEncoder).encode(NEW_PASSWORD);
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("addRoleToUser(): " + DOES_NOT_ADD_ROLE_WHEN_ROLE_ALREADY_ASSIGNED)
    void addRoleToUser_UserAlreadyHasRole_DoesNotAddRole() {
        userService.addRoleToUser(userPrincipal, ROLE_USER);
        verifyNoInteractions(roleService);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("addRoleToUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void addRoleToUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        Long id = userPrincipal.getId();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.addRoleToUser(userPrincipal, ROLE_ADMIN));
        verify(userRepository).findById(id);
        verify(userRepository, never()).update(any(User.class));
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("addRoleToUser(): " + ADDS_ROLE_TO_USER_WHEN_ROLE_NOT_ASSIGNED)
    void addRoleToUser_UserDoesNotHaveRole_AddsRoleToUser() {
        Long id = userPrincipal.getId();
        GrantedAuthority roleAdded = new SimpleGrantedAuthority(adminRole.getName());

        given(userRepository.findById(id)).willReturn(Optional.of(user));
        given(roleService.getRole(ROLE_ADMIN)).willReturn(adminRole);

        UserPrincipal updatedUserPrincipal = userService.addRoleToUser(userPrincipal, ROLE_ADMIN);
        assertTrue(updatedUserPrincipal.getAuthorities().contains(roleAdded));
        verify(userRepository).findById(id);
        verify(roleService).getRole(ROLE_ADMIN);
        verify(userRepository).update(userCaptor.capture());
        assertTrue(userCaptor.getValue().getRoles().contains(adminRole));
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + DOES_NOT_REMOVE_ROLE_WHEN_ROLE_NOT_ASSIGNED)
    void removeRoleFromUser_UserDoesNotHaveRole_DoesNotRemoveRole() {
        userService.removeRoleFromUser(userPrincipal, ROLE_ADMIN);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_ROLE_REMOVAL_PROHIBITED_WHEN_REMOVING_USER_ROLE)
    void removeRoleFromUser_RoleToRemoveIsUserRole_ThrowsUserRoleRemovalProhibitedException() {
        assertThrows(UserRoleRemovalProhibitedException.class,
            () -> userService.removeRoleFromUser(userPrincipal, ROLE_USER));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void removeRoleFromUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        user.addRole(adminRole);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        Long id = userPrincipal.getId();

        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.removeRoleFromUser(userPrincipal, ROLE_ADMIN));
        verify(userRepository).findById(id);
        verify(userRepository, never()).update(any(User.class));
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + REMOVES_ROLE_WHEN_ROLE_ASSIGNED_AND_REMOVABLE)
    void removeRoleFromUser_RoleIsAssignedAndRemovable_RemovesRole() {
        user.addRole(adminRole);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        Long id = userPrincipal.getId();
        GrantedAuthority roleToRemove = new SimpleGrantedAuthority(adminRole.getName());

        given(userRepository.findById(id)).willReturn(Optional.of(user));
        given(roleService.getRole(ROLE_ADMIN)).willReturn(adminRole);

        UserPrincipal updatedUserPrincipal = userService.removeRoleFromUser(userPrincipal, ROLE_ADMIN);
        assertFalse(updatedUserPrincipal.getAuthorities().contains(roleToRemove));
        verify(userRepository).findById(id);
        verify(roleService).getRole(ROLE_ADMIN);
        verify(userRepository).update(userCaptor.capture());
        assertFalse(userCaptor.getValue().getRoles().contains(adminRole));
    }

    @Test
    @DisplayName("lockUser(): " + DOES_NOT_LOCK_USER_WHEN_USER_ALREADY_LOCKED)
    void lockUser_UserAlreadyLocked_DoesNotLockUser() {
        user.setIsLocked(true);
        userPrincipal = UserPrincipalImpl.fromUser(user);

        userService.lockUser(userPrincipal);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("lockUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void lockUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        Long id = userPrincipal.getId();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.lockUser(userPrincipal));
        verify(userRepository).findById(id);
        verify(userRepository, never()).update(any(User.class));
    }


    @Test
    @DisplayName("lockUser(): " + LOCKS_USER_WHEN_USER_NOT_LOCKED)
    void lockUser_UserNotLocked_LocksUser() {
        Long id = userPrincipal.getId();
        given(userRepository.findById(id)).willReturn(Optional.of(user));

        UserPrincipal updatedUserPrincipal = userService.lockUser(userPrincipal);
        assertTrue(updatedUserPrincipal.isLocked());
        verify(userRepository).findById(id);
        verify(userRepository).update(userCaptor.capture());
        assertTrue(userCaptor.getValue().getIsLocked());
    }

    @Test
    @DisplayName("unlockUser(): " + DOES_NOT_UNLOCK_USER_WHEN_USER_ALREADY_UNLOCKED)
    void unlockUser_UserAlreadyUnlocked_DoesNotUnlockUser() {
        userService.unlockUser(userPrincipal);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("unlockUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void unlockUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        user.setIsLocked(true);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        Long id = userPrincipal.getId();

        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.unlockUser(userPrincipal));
        verify(userRepository).findById(id);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("unlockUser(): " + UNLOCKS_USER_WHEN_USER_LOCKED)
    void unlockUser_UserIsLocked_UnlocksUser() {
        user.setIsLocked(true);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        Long id = userPrincipal.getId();

        given(userRepository.findById(id)).willReturn(Optional.of(user));

        UserPrincipal updatedUserPrincipal = userService.unlockUser(userPrincipal);
        assertFalse(updatedUserPrincipal.isLocked());
        verify(userRepository).findById(id);
        verify(userRepository).update(userCaptor.capture());
        assertFalse(userCaptor.getValue().getIsLocked());
    }

    @Test
    @DisplayName("disableUser(): " + DOES_NOT_DISABLE_USER_WHEN_USER_ALREADY_DISABLED)
    void disableUser_UserAlreadyDisabled_DoesNotDisableUser() {
        user.setIsEnabled(false);
        userPrincipal = UserPrincipalImpl.fromUser(user);

        userService.disableUser(userPrincipal);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("disableUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void disableUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        Long id = userPrincipal.getId();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.disableUser(userPrincipal));
        verify(userRepository).findById(id);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("disableUser(): " + DISABLES_USER_WHEN_USER_ENABLED)
    void disableUser_UserEnabled_DisablesUser() {
        Long id = userPrincipal.getId();
        given(userRepository.findById(id)).willReturn(Optional.of(user));

        UserPrincipal updatedUserPrincipal = userService.disableUser(userPrincipal);
        assertFalse(updatedUserPrincipal.isEnabled());
        verify(userRepository).findById(id);
        verify(userRepository).update(userCaptor.capture());
        assertFalse(userCaptor.getValue().getIsEnabled());
    }

    @Test
    @DisplayName("enableUser(): " + DOES_NOT_ENABLE_USER_WHEN_USER_ALREADY_ENABLED)
    void enableUser_UserAlreadyEnabled_DoesNotEnableUser() {
        userService.enableUser(userPrincipal);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("enableUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void enableUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        user.setIsEnabled(false);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        Long id = userPrincipal.getId();

        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.enableUser(userPrincipal));
        verify(userRepository).findById(id);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("enableUser(): " + ENABLES_USER_WHEN_USER_DISABLED)
    void enableUser_UserDisabled_EnablesUser() {
        user.setIsEnabled(false);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        Long id = userPrincipal.getId();

        given(userRepository.findById(id)).willReturn(Optional.of(user));

        UserPrincipal updatedUserPrincipal = userService.enableUser(userPrincipal);
        assertTrue(updatedUserPrincipal.isEnabled());
        verify(userRepository).findById(id);
        verify(userRepository).update(userCaptor.capture());
        assertTrue(userCaptor.getValue().getIsEnabled());
    }

    @Test
    @DisplayName("addLoginAttempt(): " + ADDS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
    void addLoginAttempt_UserIsNotNull_AddsLoginAttempt() {
        int oldLoginAttempts = user.getFailedLoginAttempts();
        LocalDateTime oldLocalTime = LocalDateTime.now();
        user.setFailedLoginTime(oldLocalTime);

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
