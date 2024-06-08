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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    private UserPrincipal userPrincipal;
    private Role adminRole;
    private UserCreationDTO userCreationDTO;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userCreationDTO = new UserCreationDTO(EMAIL, PASSWORD, CONFIRM_PASSWORD, FIRST_NAME, LAST_NAME);

        Role userRole = roleService.getRole(ROLE_USER);
        adminRole = roleService.getRole(ROLE_ADMIN);

        user = new User();
        user.setEmail(userCreationDTO.email());
        user.setPassword(userCreationDTO.password());
        user.addRole(userRole);

        userPrincipal = UserPrincipalImpl.withUser(user);

        authentication =
            EmailPasswordAuthenticationToken.authenticated(userPrincipal.getEmail(), userPrincipal.getAuthorities());
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
    }

    @Test
    @DisplayName("loadUserPrincipalByEmail(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void loadUserPrincipalByEmail_UserDoesNotExist_ThrowsUserPrincipalNotFoundException() {
        String email = user.getEmail();
        assertThrows(UserNotFoundException.class, () -> userService.loadUserPrincipalByEmail(email));
    }

    @Test
    @Transactional
    @DisplayName("loadUserPrincipalByEmail(): " + RETURNS_PRINCIPAL_WHEN_USER_EXISTS)
    void loadUserPrincipalByEmail_UserExists_ReturnsUserPrincipalPrincipal() {
        userRepository.persist(user);
        String email = user.getEmail();

        UserPrincipal returnedPrincipal = userService.loadUserPrincipalByEmail(email);
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
        assertThrows(UserNotFoundException.class, () -> userService.getUserByPrincipal(userPrincipal));
    }

    @Test
    @Transactional
    @DisplayName("getUserByPrincipal(): " + RETURNS_USER_WHEN_USER_EXISTS)
    void getUserByPrincipal_UserExists_ReturnsUser() {
        userRepository.persist(user);
        userPrincipal = UserPrincipalImpl.withUser(user);

        User returnedUser = userService.getUserByPrincipal(userPrincipal);
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
        String email = userPrincipal.getEmail();
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(email));
    }

    @Test
    @Transactional
    @DisplayName("deleteUser(): " + DELETES_USER_WHEN_USER_EXISTS)
    void deleteUser_UserExists_DeletesUser() {
        userRepository.persist(user);
        userInfoService.createUserInfo(userCreationDTO, user);
        userPrincipal = UserPrincipalImpl.withUser(user);
        String email = user.getEmail();

        userService.deleteUser(email);
        assertFalse(userRepository.findByEmail(email).isPresent());
        assertFalse(userInfoService.userInfoExists(userPrincipal.getId()));
    }

    @Test
    @DisplayName("updateEmail(): " + THROWS_EMAIL_ALREADY_REGISTERED_WHEN_EMAIL_ALREADY_REGISTERED)
    void updateEmail_NewEmailAlreadyRegistered_ThrowsEmailAlreadyRegisteredException() {
        userRepository.persist(user);

        User otherUser = new User();
        otherUser.setEmail(NEW_EMAIL);
        otherUser.setPassword(NEW_PASSWORD);
        userRepository.persist(otherUser);

        String email = (String) authentication.getPrincipal();
        assertThrows(EmailAlreadyRegisteredException.class, () -> userService.updateEmail(email, NEW_EMAIL));
    }

    @Test
    @DisplayName("updateEmail():" + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void updateEmail_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = (String) authentication.getPrincipal();
        assertThrows(UserNotFoundException.class, () -> userService.updateEmail(email, NEW_EMAIL));
    }

    @Test
    @Transactional
    @DisplayName("updateEmail(): " + UPDATES_EMAIL_WHEN_NEW_EMAIL_NOT_REGISTERED)
    void updateEmail_NewEmailNotRegistered_UpdatesEmail() {
        userRepository.persist(user);
        String email = (String) authentication.getPrincipal();

        Authentication returnedAuthentication = userService.updateEmail(email, NEW_EMAIL);
        assertEquals(NEW_EMAIL, returnedAuthentication.getPrincipal());
        assertTrue(userRepository.findByEmail(NEW_EMAIL).isPresent());
    }

    @Test
    @DisplayName("updatePassword(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void updatePassword_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = (String) authentication.getPrincipal();
        assertThrows(UserNotFoundException.class, () -> userService.updatePassword(email, NEW_PASSWORD));
    }

    @Test
    @Transactional
    @DisplayName("updatePassword(): " + UPDATES_PASSWORD_WHEN_USER_EXISTS)
    void updatePassword_UserExists_UpdatesPassword() {
        userRepository.persist(user);
        String email = (String) authentication.getPrincipal();

        Authentication returnedAuthentication = userService.updatePassword(email, NEW_PASSWORD);
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, (String) returnedAuthentication.getCredentials()));

        User updatedUser = userRepository.findByEmail(userPrincipal.getEmail()).orElseThrow(UserNotFoundException::new);
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, updatedUser.getPassword()));
    }

    @Test
    @DisplayName("addRoleToUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void addRoleToUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.addRoleToUser(authentication, ROLE_ADMIN));
    }

    @Test
    @Transactional
    @DisplayName("addRoleToUser() : " + ADDS_ROLE_TO_USER_WHEN_ROLE_NOT_ASSIGNED)
    void addRoleToUser_UserDoesNotHaveRole_AddsRoleToUser() {
        userRepository.persist(user);
        GrantedAuthority adminGrantedAuthority = new SimpleGrantedAuthority(adminRole.getName());

        Authentication returnedAuthentication = userService.addRoleToUser(authentication, ROLE_ADMIN);
        assertTrue(returnedAuthentication.getAuthorities().contains(adminGrantedAuthority));

        User updatedUser = userRepository.findByEmail(userPrincipal.getEmail()).orElseThrow(UserNotFoundException::new);
        assertTrue(updatedUser.getRoles().contains(adminRole));
    }

    @Test
    @Transactional
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_ROLE_REMOVAL_PROHIBITED_WHEN_REMOVING_USER_ROLE)
    void removeRoleFromUser_RoleToRemoveIsUserRole_ThrowsUserRoleRemovalProhibitedException() {
        userRepository.persist(user);
        assertThrows(UserRoleRemovalProhibitedException.class, () ->
            userService.removeRoleFromUser(authentication, ROLE_USER));
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void removeRoleFromUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        user.addRole(adminRole);
        authentication =
            EmailPasswordAuthenticationToken.authenticated(user.getEmail(), UserServiceImpl.getRolesFromUser(user));
        assertThrows(UserNotFoundException.class, () -> userService.removeRoleFromUser(authentication, ROLE_ADMIN));
    }

    @Test
    @Transactional
    @DisplayName("removeRoleFromUser(): " + REMOVES_ROLE_WHEN_ROLE_ASSIGNED_AND_REMOVABLE)
    void removeRoleFromUser_RoleIsAssignedAndRemovable_RemoveRole() {
        user.addRole(adminRole);
        userRepository.persist(user);
        authentication =
            EmailPasswordAuthenticationToken.authenticated(user.getEmail(), UserServiceImpl.getRolesFromUser(user));
        GrantedAuthority adminAuthority = new SimpleGrantedAuthority(adminRole.getName());

        Authentication returnedAuthentication = userService.removeRoleFromUser(authentication, ROLE_ADMIN);
        assertFalse(returnedAuthentication.getAuthorities().contains(adminAuthority));

        User updatedUser = userRepository.findByEmail(user.getEmail()).orElseThrow(UserNotFoundException::new);
        assertFalse(updatedUser.getRoles().contains(adminRole));
    }

    @Test
    @DisplayName("lockUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void lockUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = (String) authentication.getPrincipal();
        assertThrows(UserNotFoundException.class, () -> userService.lockUser(email));
    }

    @Test
    @Transactional
    @DisplayName("lockUser(): " + LOCKS_USER_WHEN_USER_NOT_LOCKED)
    void lockUser_UserNotLocked_LocksUser() {
        userRepository.persist(user);
        String email = (String) authentication.getPrincipal();

        userService.lockUser(email);
        User updatedUser = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        assertTrue(updatedUser.getIsLocked());
    }

    @Test
    @DisplayName("unlockUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void unlockUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        user.setIsLocked(true);
        String email = (String) authentication.getPrincipal();
        assertThrows(UserNotFoundException.class, () -> userService.unlockUser(email));
    }

    @Test
    @Transactional
    @DisplayName("unlockUser(): " + UNLOCKS_USER_WHEN_USER_LOCKED)
    void unlockUser_UserLocked_UnlocksUser() {
        user.setIsLocked(true);
        userRepository.persist(user);
        String email = (String) authentication.getPrincipal();

        userService.unlockUser(email);
        User updatedUser = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        assertFalse(updatedUser.getIsLocked());
    }

    @Test
    @DisplayName("disableUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void disableUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = (String) authentication.getPrincipal();
        assertThrows(UserNotFoundException.class, () -> userService.disableUser(email));
    }

    @Test
    @Transactional
    @DisplayName("disabledUser(): " + DISABLES_USER_WHEN_USER_ENABLED)
    void disabledUser_UserEnabled_DisabledUser() {
        userRepository.persist(user);
        String email = (String) authentication.getPrincipal();

        userService.disableUser(email);
        User updatedUser = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        assertFalse(updatedUser.getIsEnabled());
    }

    @Test
    @DisplayName("enableUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void enableUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        user.setIsEnabled(false);
        String email = (String) authentication.getPrincipal();
        assertThrows(UserNotFoundException.class, () -> userService.enableUser(email));
    }

    @Test
    @Transactional
    @DisplayName("enableUser(): " + ENABLES_USER_WHEN_USER_DISABLED)
    void enableUser_UserDisabled_EnablesUser() {
        user.setIsEnabled(false);
        userRepository.persist(user);
        String email = (String) authentication.getPrincipal();

        userService.enableUser(email);
        User updatedUser = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        assertTrue(updatedUser.getIsEnabled());
    }

    @Test
    @Transactional
    @DisplayName("addLoginAttempt(): " + ADDS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
    void addLoginAttempt_UserNotNull_AddsLoginAttempt() {
        user.setFailedLoginTime(LocalDateTime.now());
        userRepository.persist(user);
        int oldLoginAttempts = user.getFailedLoginAttempts();
        LocalDateTime oldFailedLoginTime = user.getFailedLoginTime();

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
