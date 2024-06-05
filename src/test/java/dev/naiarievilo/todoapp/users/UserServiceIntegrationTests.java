package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.UserRoleRemovalProhibitedException;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import dev.naiarievilo.todoapp.security.UserPrincipalImpl;
import dev.naiarievilo.todoapp.users_info.UserInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

import static dev.naiarievilo.todoapp.roles.Roles.ROLE_ADMIN;
import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static dev.naiarievilo.todoapp.users.UserServiceTestCaseMessages.*;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional(readOnly = true)
class UserServiceIntegrationTests {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    private User user;
    private UserPrincipal principal;
    private Role adminRole;
    private UserCreationDTO userCreationDTO;

    @BeforeEach
    void setUp() {
        userCreationDTO = new UserCreationDTO(EMAIL, PASSWORD, CONFIRM_PASSWORD, FIRST_NAME, LAST_NAME);

        Role userRole = roleService.getRole(ROLE_USER);
        adminRole = roleService.getRole(ROLE_ADMIN);

        user = new User();
        user.setEmail(userCreationDTO.email());
        user.setPassword(userCreationDTO.password());
        user.addRole(userRole);

        principal = UserPrincipalImpl.withUser(user);
    }

    @Test
    @DisplayName("userExists(): " + RETURNS_FALSE_WHEN_USER_DOES_NOT_EXIST)
    void userExists_UserDoesNotExist_ReturnsFalse() {
        assertFalse(userService.userExists(user.getEmail()));
    }

    @Test
    @Transactional
    @DisplayName("userExists(): " + RETURNS_TRUE_WHEN_USER_EXISTS)
    void userExists_UserExists_ReturnsTrue() {
        userRepository.persist(user);
        String userEmail = user.getEmail();
        assertTrue(userService.userExists(userEmail));
        assertTrue(userRepository.findByEmail(userEmail).isPresent());
    }

    @Test
    @DisplayName("loadUserByEmail(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void loadUserByEmail_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = user.getEmail();
        assertThrows(UserNotFoundException.class, () -> userService.loadUserByEmail(email));
    }

    @Test
    @Transactional
    @DisplayName("loadUserByEmail(): " + RETURNS_PRINCIPAL_WHEN_USER_EXISTS)
    void loadUserByEmail_UserExists_ReturnsUserPrincipal() {
        userRepository.persist(user);
        String userEmail = user.getEmail();

        UserPrincipal returnedPrincipal = userService.loadUserByEmail(userEmail);
        assertEquals(user.getId(), returnedPrincipal.getId());
        assertEquals(user.getEmail(), returnedPrincipal.getEmail());
    }

    @Test
    @DisplayName("getUserByEmail(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void getUserByEmail_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = user.getEmail();
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(email));
    }

    @Test
    @Transactional
    @DisplayName("getUserByEmail(): " + RETURNS_USER_WHEN_USER_EXISTS)
    void getUserByEmail_UserExists_ReturnsUser() {
        userRepository.persist(user);
        String email = user.getEmail();

        User returnedUser = userService.getUserByEmail(email);
        assertEquals(user, returnedUser);
    }

    @Test
    @DisplayName("getUserByPrincipal(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void getUserByPrincipal_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserByPrincipal(principal));
    }

    @Test
    @Transactional
    @DisplayName("getUserByPrincipal(): " + RETURNS_USER_WHEN_USER_EXISTS)
    void getUserByPrincipal_UserExists_ReturnsUser() {
        userRepository.persist(user);
        principal = UserPrincipalImpl.withUser(user);

        User returnedUser = userService.getUserByPrincipal(principal);
        assertNotNull(returnedUser);
        assertEquals(user, returnedUser);
    }

    @Test
    @Transactional
    @DisplayName("createUser(): " + THROWS_USER_ALREADY_EXISTS_WHEN_USER_ALREADY_EXISTS)
    void createUser_UserAlreadyExists_ThrowsUserAlreadyExistsException() {
        userRepository.persist(user);
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(userCreationDTO));
    }

    @Test
    @Transactional
    @DisplayName("createUser(): " + CREATES_USER_WHEN_USER_DOES_NOT_EXIST)
    void createUser_UserDoesNotExist_CreatesUser() {
        userService.createUser(userCreationDTO);

        User createdUser = userRepository.findByEmail(userCreationDTO.email()).orElseThrow(UserNotFoundException::new);
        assertTrue(passwordEncoder.matches(userCreationDTO.password(), createdUser.getPassword()));
        assertTrue(userInfoService.userInfoExists(createdUser.getId()));
    }

    @Test
    @DisplayName("deleteUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void deleteUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = principal.getEmail();
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(email));
    }

    @Test
    @Transactional
    @DisplayName("deleteUser(): " + DELETES_USER_WHEN_USER_EXISTS)
    void deleteUser_UserExists_DeletesUser() {
        userRepository.persist(user);
        userInfoService.createUserInfo(userCreationDTO, user);
        principal = UserPrincipalImpl.withUser(user);
        String email = principal.getEmail();

        userService.deleteUser(email);
        assertTrue(userRepository.findByEmail(principal.getEmail()).isEmpty());
        assertFalse(userInfoService.userInfoExists(principal.getId()));
    }

    @Test
    @DisplayName("updateEmail(): " + THROWS_EMAIL_ALREADY_REGISTERED_WHEN_EMAIL_ALREADY_REGISTERED)
    void updateEmail_NewEmailAlreadyRegistered_ThrowsEmailAlreadyRegisteredException() {
        userRepository.persist(user);
        principal = UserPrincipalImpl.withUser(user);

        User otherUser = new User();
        otherUser.setEmail(NEW_EMAIL);
        otherUser.setPassword(passwordEncoder.encode(NEW_PASSWORD));
        userRepository.persist(otherUser);

        assertThrows(EmailAlreadyRegisteredException.class, () -> userService.updateEmail(principal, NEW_EMAIL));
    }

    @Test
    @DisplayName("updateEmail():" + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void updateEmail_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.updateEmail(principal, NEW_EMAIL));
    }

    @Test
    @Transactional
    @DisplayName("updateEmail(): " + UPDATES_EMAIL_WHEN_NEW_EMAIL_NOT_REGISTERED)
    void updateEmail_NewEmailNotRegistered_UpdatesEmail() {
        userRepository.persist(user);
        principal = UserPrincipalImpl.withUser(user);

        UserPrincipal returnedPrincipal = userService.updateEmail(principal, NEW_EMAIL);
        assertEquals(NEW_EMAIL, returnedPrincipal.getEmail());
        assertTrue(userRepository.findByEmail(NEW_EMAIL).isPresent());
    }

    @Test
    @DisplayName("updatePassword(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void updatePassword_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.updatePassword(principal, NEW_PASSWORD));
    }

    @Test
    @Transactional
    @DisplayName("updatePassword(): " + UPDATES_PASSWORD_WHEN_USER_EXISTS)
    void updatePassword_UserExists_UpdatesPassword() {
        userRepository.persist(user);
        principal = UserPrincipalImpl.withUser(user);

        UserPrincipal updatedPrincipal = userService.updatePassword(principal, NEW_PASSWORD);
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, updatedPrincipal.getPassword()));
        User updatedUser = userRepository.findByEmail(principal.getEmail()).orElseThrow(UserNotFoundException::new);
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, updatedUser.getPassword()));
    }

    @Test
    @DisplayName("addRoleToUser(): " + DOES_NOT_ADD_ROLE_WHEN_ROLE_ALREADY_ASSIGNED)
    void addRoleToUser_UserAlreadyHasRole_DoesNotAddRole() {
        assertDoesNotThrow(() -> userService.addRoleToUser(principal, ROLE_USER));
    }

    @Test
    @DisplayName("addRoleToUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void addRoleToUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.addRoleToUser(principal, ROLE_ADMIN));
    }

    @Test
    @Transactional
    @DisplayName("addRoleToUser() : " + ADDS_ROLE_TO_USER_WHEN_ROLE_NOT_ASSIGNED)
    void addRoleToUser_UserDoesNotHaveRole_AddsRoleToUser() {
        userRepository.persist(user);
        GrantedAuthority adminGrantedAuthority = new SimpleGrantedAuthority(adminRole.getName());

        UserPrincipal updatedPrincipal = userService.addRoleToUser(principal, ROLE_ADMIN);

        assertTrue(updatedPrincipal.getAuthorities().contains(adminGrantedAuthority));
        User updatedUser = userRepository.findByEmail(principal.getEmail()).orElseThrow(UserNotFoundException::new);
        assertTrue(updatedUser.getRoles().contains(adminRole));
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + DOES_NOT_REMOVE_ROLE_WHEN_ROLE_NOT_ASSIGNED)
    void removeRoleFromUser_UserDoesNotHaveRole_DoesNotRemoveRole() {
        assertDoesNotThrow(() -> userService.removeRoleFromUser(principal, ROLE_ADMIN));
    }

    @Test
    @Transactional
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_ROLE_REMOVAL_PROHIBITED_WHEN_REMOVING_USER_ROLE)
    void removeRoleFromUser_RoleToRemoveIsUserRole_ThrowsUserRoleRemovalProhibitedException() {
        userRepository.persist(user);
        assertThrows(UserRoleRemovalProhibitedException.class, () ->
            userService.removeRoleFromUser(principal, ROLE_USER));
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void removeRoleFromUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        user.addRole(adminRole);
        principal = UserPrincipalImpl.withUser(user);

        assertThrows(UserNotFoundException.class, () -> userService.removeRoleFromUser(principal, ROLE_ADMIN));
    }

    @Test
    @Transactional
    @DisplayName("removeRoleFromUser(): " + REMOVES_ROLE_WHEN_ROLE_ASSIGNED_AND_REMOVABLE)
    void removeRoleFromUser_RoleIsAssignedAndRemovable_RemoveRole() {
        user.addRole(adminRole);
        userRepository.persist(user);
        principal = UserPrincipalImpl.withUser(user);
        GrantedAuthority adminAuthority = new SimpleGrantedAuthority(adminRole.getName());

        UserPrincipal returnedPrincipal = userService.removeRoleFromUser(principal, ROLE_ADMIN);

        assertFalse(returnedPrincipal.getAuthorities().contains(adminAuthority));
        User updatedUser = userRepository.findByEmail(user.getEmail()).orElseThrow(UserNotFoundException::new);
        assertFalse(updatedUser.getRoles().contains(adminRole));
    }

    @Test
    @DisplayName("lockUser(): " + DOES_NOT_LOCK_USER_WHEN_USER_ALREADY_LOCKED)
    void lockUser_UserAlreadyLocked_DoesNotLockUser() {
        user.setIsLocked(true);
        principal = UserPrincipalImpl.withUser(user);

        assertDoesNotThrow(() -> userService.lockUser(principal));
    }

    @Test
    @DisplayName("lockUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void lockUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.lockUser(principal));
    }

    @Test
    @Transactional
    @DisplayName("lockUser(): " + LOCKS_USER_WHEN_USER_NOT_LOCKED)
    void lockUser_UserNotLocked_LocksUser() {
        userRepository.persist(user);
        principal = UserPrincipalImpl.withUser(user);

        UserPrincipal returnedPrincipal = userService.lockUser(principal);

        assertTrue(returnedPrincipal.isLocked());
        User updatedUser = userRepository.findById(returnedPrincipal.getId()).orElseThrow(UserNotFoundException::new);
        assertTrue(updatedUser.getIsLocked());
    }

    @Test
    @DisplayName("unlockUser(): " + DOES_NOT_UNLOCK_USER_WHEN_USER_ALREADY_UNLOCKED)
    void unlockUser_UserAlreadyUnlocked_DoesNotUnlockUser() {
        assertDoesNotThrow(() -> userService.unlockUser(principal));
    }

    @Test
    @DisplayName("unlockUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void unlockUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        user.setIsLocked(true);
        principal = UserPrincipalImpl.withUser(user);
        assertThrows(UserNotFoundException.class, () -> userService.unlockUser(principal));
        assertTrue(userRepository.findByEmail(principal.getEmail()).isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("unlockUser(): " + UNLOCKS_USER_WHEN_USER_LOCKED)
    void unlockUser_UserLocked_UnlocksUser() {
        user.setIsLocked(true);
        userRepository.persist(user);
        principal = UserPrincipalImpl.withUser(user);

        UserPrincipal returnedPrincipal = userService.unlockUser(principal);

        assertFalse(returnedPrincipal.isLocked());
        User updatedUser = userRepository.findById(returnedPrincipal.getId()).orElseThrow(UserNotFoundException::new);
        assertFalse(updatedUser.getIsLocked());
    }

    @Test
    @DisplayName("disableUser(): " + DOES_NOT_DISABLE_USER_WHEN_USER_ALREADY_DISABLED)
    void disableUser_UserAlreadyDisabled_DoesNotDisableUser() {
        user.setIsEnabled(false);
        principal = UserPrincipalImpl.withUser(user);
        assertDoesNotThrow(() -> userService.disableUser(principal));
    }

    @Test
    @DisplayName("disableUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void disableUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.disableUser(principal));
    }

    @Test
    @Transactional
    @DisplayName("disabledUser(): " + DISABLES_USER_WHEN_USER_ENABLED)
    void disabledUser_UserEnabled_DisabledUser() {
        userRepository.persist(user);
        principal = UserPrincipalImpl.withUser(user);

        UserPrincipal returnedPrincipal = userService.disableUser(principal);

        assertFalse(returnedPrincipal.isEnabled());
        User updatedUser = userRepository.findById(returnedPrincipal.getId()).orElseThrow(UserNotFoundException::new);
        assertFalse(updatedUser.getIsEnabled());
    }

    @Test
    @DisplayName("enableUser(): " + DOES_NOT_ENABLE_USER_WHEN_USER_ALREADY_ENABLED)
    void enableUser_UserAlreadyEnabled_DoesNotEnableUser() {
        assertDoesNotThrow(() -> userService.enableUser(principal));
    }

    @Test
    @DisplayName("enableUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void enableUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        user.setIsEnabled(false);
        principal = UserPrincipalImpl.withUser(user);
        assertThrows(UserNotFoundException.class, () -> userService.enableUser(principal));
    }

    @Test
    @Transactional
    @DisplayName("enableUser(): " + ENABLES_USER_WHEN_USER_DISABLED)
    void enableUser_UserDisabled_EnablesUser() {
        user.setIsEnabled(false);
        userRepository.persist(user);
        principal = UserPrincipalImpl.withUser(user);

        UserPrincipal returnedPrincipal = userService.enableUser(principal);

        assertTrue(returnedPrincipal.isEnabled());
        User updatedUser = userRepository.findById(returnedPrincipal.getId()).orElseThrow(UserNotFoundException::new);
        assertTrue(updatedUser.getIsEnabled());
    }

    @Test
    @Transactional
    @DisplayName("addLoginAttempt(): " + ADDS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
    void addLoginAttempt_UserNotNull_AddsLoginAttempt() {
        user.setFailedLoginTime(LocalTime.now());
        userRepository.persist(user);
        int oldLoginAttempts = user.getFailedLoginAttempts();
        LocalTime oldFailedLoginTime = user.getFailedLoginTime();

        userService.addLoginAttempt(user);

        User updatedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertEquals(updatedUser.getFailedLoginAttempts(), oldLoginAttempts + 1);
        assertTrue(oldFailedLoginTime.isBefore(updatedUser.getFailedLoginTime()));
    }

    @Test
    @Transactional
    @DisplayName("resetLoginAttempts(): " + RESETS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
    void resetLoginAttempts_UserNotNull_ResetsLoginAttempts() {
        user.incrementFailedLoginAttempts();
        user.incrementFailedLoginAttempts();
        userRepository.persist(user);

        userService.resetLoginAttempts(user);

        User updatedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertEquals(0, updatedUser.getFailedLoginAttempts());
    }

}
