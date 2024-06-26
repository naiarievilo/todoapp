package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.exceptions.UserRoleRemovalProhibitedException;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users.exceptions.EmailAlreadyRegisteredException;
import dev.naiarievilo.todoapp.users.exceptions.UserAlreadyExistsException;
import dev.naiarievilo.todoapp.users.exceptions.UserNotFoundException;
import dev.naiarievilo.todoapp.users_info.UserInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
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
    private Role adminRole;
    private UserCreationDTO userCreationDTO;

    @BeforeEach
    void setUp() {
        userCreationDTO = new UserCreationDTO(EMAIL_1, PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);

        Role userRole = roleService.getRole(ROLE_USER);
        adminRole = roleService.getRole(ROLE_ADMIN);

        user = new User();
        user.setEmail(userCreationDTO.email());
        user.setPassword(userCreationDTO.password());
        user.addRole(userRole);
    }

    @Test
    @DisplayName("userExists(Long id): " + RETURNS_FALSE_WHEN_USER_DOES_NOT_EXIST)
    void userExistsById_UserDoesNotExist_ReturnsFalse() {
        assertFalse(userService.userExists(ID_2));
    }

    @Test
    @Transactional
    @DisplayName("authenticateUser(): " + AUTHENTICATES_USER_WHEN_USER_NOT_AUTHENTICATED)
    void authenticateUser_UserNotAuthenticated_AuthenticatesUser() {
        userRepository.persist(user);
        userService.authenticateUser(user);
        User updatedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertTrue(updatedUser.isAuthenticated());
    }

    @Test
    @Transactional
    @DisplayName("userExists(Long id): " + RETURNS_TRUE_WHEN_USER_EXISTS)
    void userExistsById_UserExists_ReturnsTrue() {
        userRepository.persist(user);
        assertTrue(userService.userExists(user.getId()));
    }

    @Test
    @DisplayName("userExists(String email): " + RETURNS_FALSE_WHEN_USER_DOES_NOT_EXIST)
    void userExistsByEmail_UserDoesNotExist_ReturnsFalse() {
        assertFalse(userService.userExists(user.getEmail()));
    }

    @Test
    @Transactional
    @DisplayName("userExists(String email): " + RETURNS_TRUE_WHEN_USER_EXISTS)
    void userExistsByEmail_UserExists_ReturnsTrue() {
        userRepository.persist(user);
        assertTrue(userService.userExists(user.getEmail()));
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
        User returnedUser = userService.getUserByEmail(user.getEmail());
        assertEquals(user, returnedUser);
    }

    @Test
    @DisplayName("getUserById(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void getUserById_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(ID_2));
    }

    @Test
    @Transactional
    @DisplayName("getUserById(): " + RETURNS_USER_WHEN_USER_EXISTS)
    void getUserById_UserExists_ReturnsUser() {
        userRepository.persist(user);
        User returnedUser = userService.getUserById(user.getId());
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
        User returnedUser = userService.createUser(userCreationDTO);
        User createdUser = userRepository.findByEmail(userCreationDTO.email()).orElseThrow(UserNotFoundException::new);
        assertEquals(createdUser.getId(), returnedUser.getId());
        assertEquals(createdUser.getEmail(), returnedUser.getEmail());
        assertTrue(passwordEncoder.matches(userCreationDTO.password(), createdUser.getPassword()));
        assertTrue(userInfoService.userInfoExists(createdUser.getId()));
    }

    @Test
    @Transactional
    @DisplayName("deleteUser(): " + DELETES_USER_WHEN_USER_EXISTS)
    void deleteUser_UserExists_DeletesUser() {
        userRepository.persist(user);
        userInfoService.createUserInfo(userCreationDTO, user);
        Long id = user.getId();

        userService.deleteUser(id);
        assertFalse(userRepository.findById(id).isPresent());
        assertFalse(userInfoService.userInfoExists(id));
    }

    @Test
    @DisplayName("updateEmail(): " + THROWS_EMAIL_ALREADY_REGISTERED_WHEN_EMAIL_ALREADY_REGISTERED)
    void updateEmail_NewEmailAlreadyRegistered_ThrowsEmailAlreadyRegisteredException() {
        User otherUser = new User();
        otherUser.setEmail(NEW_EMAIL);
        otherUser.setPassword(NEW_PASSWORD);

        userRepository.persist(otherUser);
        userRepository.persist(user);

        assertThrows(EmailAlreadyRegisteredException.class, () -> userService.updateEmail(user, NEW_EMAIL));
    }

    @Test
    @Transactional
    @DisplayName("updateEmail(): " + UPDATES_EMAIL_WHEN_NEW_EMAIL_NOT_REGISTERED)
    void updateEmail_NewEmailNotRegistered_UpdatesEmail() {
        userRepository.persist(user);

        User updatedUser = userService.updateEmail(user, NEW_EMAIL);
        assertEquals(NEW_EMAIL, updatedUser.getEmail());
        assertTrue(userRepository.findByEmail(NEW_EMAIL).isPresent());
    }

    @Test
    @Transactional
    @DisplayName("updatePassword(): " + THROWS_BAD_CREDENTIALS_WHEN_CURRENT_PASSWORD_INCORRECT)
    void updatePassword_IncorrectOldPassword_ThrowsBadCredentialsException() {
        user = userService.createUser(userCreationDTO);
        String notCurrentPassword = "notCurrentPassword";
        assertThrows(BadCredentialsException.class, () -> userService.updatePassword(user, notCurrentPassword,
            NEW_PASSWORD));
    }

    @Test
    @Transactional
    @DisplayName("updatePassword(): " + UPDATES_PASSWORD_WHEN_CURRENT_PASSWORD_CORRECT)
    void updatePassword_UserExistsAndCorrectOldPassword_UpdatesPassword() {
        user = userService.createUser(userCreationDTO);

        User updatedUser = userService.updatePassword(user, PASSWORD_1, NEW_PASSWORD);
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, updatedUser.getPassword()));

        User retrievedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, retrievedUser.getPassword()));
    }

    @Test
    @Transactional
    @DisplayName("addRoleToUser() : " + ADDS_ROLE_TO_USER_WHEN_ROLE_NOT_ASSIGNED)
    void addRoleToUser_UserDoesNotHaveRole_AddsRoleToUser() {
        userRepository.persist(user);

        User updatedUser = userService.addRoleToUser(user, ROLE_ADMIN);
        assertTrue(updatedUser.getRoles().contains(adminRole));

        User retrievedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertTrue(retrievedUser.getRoles().contains(adminRole));
    }

    @Test
    @Transactional
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_ROLE_REMOVAL_PROHIBITED_WHEN_REMOVING_USER_ROLE)
    void removeRoleFromUser_RoleToRemoveIsUserRole_ThrowsUserRoleRemovalProhibitedException() {
        userRepository.persist(user);
        assertThrows(UserRoleRemovalProhibitedException.class, () -> userService.removeRoleFromUser(user, ROLE_USER));
    }

    @Test
    @Transactional
    @DisplayName("removeRoleFromUser(): " + REMOVES_ROLE_WHEN_ROLE_ASSIGNED_AND_REMOVABLE)
    void removeRoleFromUser_RoleIsAssignedAndRemovable_RemoveRole() {
        user.addRole(adminRole);
        userRepository.persist(user);

        User updatedUser = userService.removeRoleFromUser(user, ROLE_ADMIN);
        assertFalse(updatedUser.getRoles().contains(adminRole));

        User retrievedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertFalse(retrievedUser.getRoles().contains(adminRole));
    }

    @Test
    @Transactional
    @DisplayName("lockUser(): " + LOCKS_USER_WHEN_USER_NOT_LOCKED)
    void lockUser_UserNotLocked_LocksUser() {
        userRepository.persist(user);

        User updatedUser = userService.lockUser(user);
        assertTrue(updatedUser.isLocked());
        User retrievedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertTrue(retrievedUser.isLocked());
    }

    @Test
    @Transactional
    @DisplayName("unlockUser(): " + UNLOCKS_USER_WHEN_USER_LOCKED)
    void unlockUser_UserLocked_UnlocksUser() {
        user.setLocked(true);
        userRepository.persist(user);

        User updatedUser = userService.unlockUser(user);
        assertFalse(updatedUser.isLocked());
        User retrievedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertFalse(retrievedUser.isLocked());
    }

    @Test
    @Transactional
    @DisplayName("disabledUser(): " + DISABLES_USER_WHEN_USER_ENABLED)
    void disabledUser_UserEnabled_DisabledUser() {
        userRepository.persist(user);

        User updatedUser = userService.disableUser(user);
        assertFalse(updatedUser.isEnabled());

        User retrievedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertFalse(retrievedUser.isEnabled());
    }

    @Test
    @Transactional
    @DisplayName("enableUser(): " + ENABLES_USER_WHEN_USER_DISABLED)
    void enableUser_UserDisabled_EnablesUser() {
        user.setEnabled(false);
        userRepository.persist(user);

        User updatedUser = userService.enableUser(user);
        assertTrue(updatedUser.isEnabled());
        User retrievedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertTrue(retrievedUser.isEnabled());
    }

    @Test
    @Transactional
    @DisplayName("addLoginAttempt(): " + ADDS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
    void addLoginAttempt_UserNotNull_AddsLoginAttempt() {
        user.setLastLoginAttempt(LocalDateTime.now());
        userRepository.persist(user);
        LocalDateTime lastLoginAttempt = user.getLastLoginAttempt();
        int oldLoginAttempts = user.getLoginAttempts();

        userService.addLoginAttempt(user);
        User updatedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertEquals(updatedUser.getLoginAttempts(), oldLoginAttempts + 1);
        assertTrue(lastLoginAttempt.isBefore(updatedUser.getLastLoginAttempt()));
    }

    @Test
    @Transactional
    @DisplayName("resetLoginAttempts(): " + RESETS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
    void resetLoginAttempts_UserNotNull_ResetsLoginAttempts() {
        user.setLoginAttempts((byte) 7);
        userRepository.persist(user);

        userService.resetLoginAttempts(user);
        User updatedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertEquals(0, updatedUser.getLoginAttempts());
    }

}
