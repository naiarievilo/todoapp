package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.UserRoleRemovalProhibitedException;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import dev.naiarievilo.todoapp.security.UserPrincipalImpl;
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
    private UserPrincipal staleUserPrincipal;
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

        userPrincipal = UserPrincipalImpl.fromUser(user);
        staleUserPrincipal = UserPrincipalImpl.withUser(user)
            .setId(ID_1).build();
    }

    @Test
    @DisplayName("userExists(Long id): " + RETURNS_FALSE_WHEN_USER_DOES_NOT_EXIST)
    void userExistsById_UserDoesNotExist_ReturnsFalse() {
        assertFalse(userService.userExists(ID_1));
    }

    @Test
    @Transactional
    @DisplayName("userExists(Long id): " + RETURNS_TRUE_WHEN_USER_EXISTS)
    void userExistsById_UserExists_ReturnsTrue() {
        userRepository.persist(user);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        assertTrue(userService.userExists(userPrincipal.getId()));
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
        assertTrue(userService.userExists(userPrincipal.getEmail()));
    }

    @Test
    @DisplayName("loadUserPrincipalById(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void loadUserPrincipalById_UserDoesNotExist_ThrowsUserPrincipalNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.loadUserPrincipalById(ID_1));
    }

    @Test
    @Transactional
    @DisplayName("loadUserPrincipalById(): " + RETURNS_PRINCIPAL_WHEN_USER_EXISTS)
    void loadUserPrincipalById_UserExists_ReturnsUserPrincipalPrincipal() {
        userRepository.persist(user);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        Long id = userPrincipal.getId();

        UserPrincipal returnedPrincipal = userService.loadUserPrincipalById(id);
        assertEquals(user.getId(), returnedPrincipal.getId());
        assertEquals(user.getEmail(), returnedPrincipal.getEmail());
    }

    @Test
    @DisplayName("getUserByEmail(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void getUserByEmail_UserDoesNotExist_ThrowsUserNotFoundException() {
        String email = userPrincipal.getEmail();
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(email));
    }

    @Test
    @Transactional
    @DisplayName("getUserByEmail(): " + RETURNS_USER_WHEN_USER_EXISTS)
    void getUserByEmail_UserExists_ReturnsUser() {
        userRepository.persist(user);
        userPrincipal = UserPrincipalImpl.fromUser(user);

        User returnedUser = userService.getUserByEmail(userPrincipal.getEmail());
        assertEquals(user, returnedUser);
    }

    @Test
    @DisplayName("getUserById(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void getUserById_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(ID_1));
    }

    @Test
    @Transactional
    @DisplayName("getUserById(): " + RETURNS_USER_WHEN_USER_EXISTS)
    void getUserById_UserExists_ReturnsUser() {
        userRepository.persist(user);
        userPrincipal = UserPrincipalImpl.fromUser(user);

        User returnedUser = userService.getUserById(userPrincipal.getId());
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
        UserPrincipal returnedUserPrincipal = userService.createUser(userCreationDTO);
        User createdUser = userRepository.findByEmail(userCreationDTO.email()).orElseThrow(UserNotFoundException::new);
        assertEquals(createdUser.getId(), returnedUserPrincipal.getId());
        assertEquals(createdUser.getEmail(), returnedUserPrincipal.getEmail());
        assertTrue(passwordEncoder.matches(userCreationDTO.password(), createdUser.getPassword()));
        assertTrue(userInfoService.userInfoExists(createdUser.getId()));
    }

    @Test
    @DisplayName("deleteUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void deleteUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(staleUserPrincipal));
    }

    @Test
    @Transactional
    @DisplayName("deleteUser(): " + DELETES_USER_WHEN_USER_EXISTS)
    void deleteUser_UserExists_DeletesUser() {
        userRepository.persist(user);
        userInfoService.createUserInfo(userCreationDTO, user);
        userPrincipal = UserPrincipalImpl.fromUser(user);

        userService.deleteUser(userPrincipal);
        assertFalse(userRepository.findByEmail(userPrincipal.getEmail()).isPresent());
        assertFalse(userInfoService.userInfoExists(userPrincipal.getId()));
    }

    @Test
    @DisplayName("updateEmail(): " + THROWS_EMAIL_ALREADY_REGISTERED_WHEN_EMAIL_ALREADY_REGISTERED)
    void updateEmail_NewEmailAlreadyRegistered_ThrowsEmailAlreadyRegisteredException() {
        User otherUser = new User();
        otherUser.setEmail(NEW_EMAIL);
        otherUser.setPassword(NEW_PASSWORD);
        userRepository.persist(otherUser);

        userRepository.persist(user);
        userPrincipal = UserPrincipalImpl.fromUser(user);

        assertThrows(EmailAlreadyRegisteredException.class, () -> userService.updateEmail(userPrincipal, NEW_EMAIL));
    }

    @Test
    @DisplayName("updateEmail():" + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void updateEmail_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.updateEmail(staleUserPrincipal, NEW_EMAIL));
    }

    @Test
    @Transactional
    @DisplayName("updateEmail(): " + UPDATES_EMAIL_WHEN_NEW_EMAIL_NOT_REGISTERED)
    void updateEmail_NewEmailNotRegistered_UpdatesEmail() {
        userRepository.persist(user);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        UserPrincipal updatedUserPrincipal = userService.updateEmail(userPrincipal, NEW_EMAIL);
        assertEquals(NEW_EMAIL, updatedUserPrincipal.getEmail());
        assertTrue(userRepository.findByEmail(NEW_EMAIL).isPresent());
    }

    @Test
    @DisplayName("updatePassword(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void updatePassword_UserDoesNotExist_ThrowsUserNotFoundException() {
        staleUserPrincipal = UserPrincipalImpl.withUser(user)
            .setId(ID_1).setPassword(passwordEncoder.encode(user.getPassword())).build();
        assertThrows(UserNotFoundException.class, () -> userService.updatePassword(staleUserPrincipal, PASSWORD_1,
            NEW_PASSWORD));
    }

    @Test
    @Transactional
    @DisplayName("updatePassword(): " + THROWS_BAD_CREDENTIALS_WHEN_CURRENT_PASSWORD_INCORRECT)
    void updatePassword_IncorrectOldPassword_ThrowsBadCredentialsException() {
        UserPrincipal newUserPrincipal = userService.createUser(userCreationDTO);
        String notCurrentPassword = "notCurrentPassword";
        assertThrows(BadCredentialsException.class, () -> userService.updatePassword(newUserPrincipal,
            notCurrentPassword,
            NEW_PASSWORD));
    }

    @Test
    @Transactional
    @DisplayName("updatePassword(): " + UPDATES_PASSWORD_WHEN_CURRENT_PASSWORD_CORRECT)
    void updatePassword_UserExistsAndCorrectOldPassword_UpdatesPassword() {
        UserPrincipal newUserPrincipal = userService.createUser(userCreationDTO);

        UserPrincipal updatedUserPrincipal = userService.updatePassword(newUserPrincipal, PASSWORD_1, NEW_PASSWORD);
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, updatedUserPrincipal.getPassword()));

        User updatedUser = userRepository.findByEmail(userPrincipal.getEmail()).orElseThrow(UserNotFoundException::new);
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, updatedUser.getPassword()));
    }

    @Test
    @DisplayName("addRoleToUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void addRoleToUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.addRoleToUser(staleUserPrincipal, ROLE_ADMIN));
    }

    @Test
    @Transactional
    @DisplayName("addRoleToUser() : " + ADDS_ROLE_TO_USER_WHEN_ROLE_NOT_ASSIGNED)
    void addRoleToUser_UserDoesNotHaveRole_AddsRoleToUser() {
        userRepository.persist(user);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        GrantedAuthority roleAdded = new SimpleGrantedAuthority(adminRole.getName());

        UserPrincipal updatedUserPrincipal = userService.addRoleToUser(userPrincipal, ROLE_ADMIN);
        assertTrue(updatedUserPrincipal.getAuthorities().contains(roleAdded));

        User updatedUser = userRepository.findByEmail(userPrincipal.getEmail()).orElseThrow(UserNotFoundException::new);
        assertTrue(updatedUser.getRoles().contains(adminRole));
    }

    @Test
    @Transactional
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_ROLE_REMOVAL_PROHIBITED_WHEN_REMOVING_USER_ROLE)
    void removeRoleFromUser_RoleToRemoveIsUserRole_ThrowsUserRoleRemovalProhibitedException() {
        userRepository.persist(user);
        assertThrows(UserRoleRemovalProhibitedException.class, () -> userService.removeRoleFromUser(userPrincipal,
            ROLE_USER));
    }

    @Test
    @DisplayName("removeRoleFromUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void removeRoleFromUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        user.addRole(adminRole);
        staleUserPrincipal = UserPrincipalImpl.withUser(user).setId(ID_1).build();
        assertThrows(UserNotFoundException.class, () -> userService.removeRoleFromUser(staleUserPrincipal, ROLE_ADMIN));
    }

    @Test
    @Transactional
    @DisplayName("removeRoleFromUser(): " + REMOVES_ROLE_WHEN_ROLE_ASSIGNED_AND_REMOVABLE)
    void removeRoleFromUser_RoleIsAssignedAndRemovable_RemoveRole() {
        user.addRole(adminRole);
        userRepository.persist(user);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        GrantedAuthority roleRemoved = new SimpleGrantedAuthority(adminRole.getName());

        UserPrincipal updatedUserPrincipal = userService.removeRoleFromUser(userPrincipal, ROLE_ADMIN);
        assertFalse(updatedUserPrincipal.getAuthorities().contains(roleRemoved));

        User updatedUser = userRepository.findByEmail(user.getEmail()).orElseThrow(UserNotFoundException::new);
        assertFalse(updatedUser.getRoles().contains(adminRole));
    }

    @Test
    @DisplayName("lockUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void lockUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.lockUser(staleUserPrincipal));
    }

    @Test
    @Transactional
    @DisplayName("lockUser(): " + LOCKS_USER_WHEN_USER_NOT_LOCKED)
    void lockUser_UserNotLocked_LocksUser() {
        userRepository.persist(user);
        userPrincipal = UserPrincipalImpl.fromUser(user);

        UserPrincipal updatedUserPrincipal = userService.lockUser(userPrincipal);
        assertTrue(updatedUserPrincipal.isLocked());
        User updatedUser = userRepository.findByEmail(userPrincipal.getEmail()).orElseThrow(UserNotFoundException::new);
        assertTrue(updatedUser.getIsLocked());
    }

    @Test
    @DisplayName("unlockUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void unlockUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        staleUserPrincipal = UserPrincipalImpl.withUser(user).setId(ID_1).setLocked(true).build();
        assertThrows(UserNotFoundException.class, () -> userService.unlockUser(staleUserPrincipal));
    }

    @Test
    @Transactional
    @DisplayName("unlockUser(): " + UNLOCKS_USER_WHEN_USER_LOCKED)
    void unlockUser_UserLocked_UnlocksUser() {
        user.setIsLocked(true);
        userRepository.persist(user);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        String email = userPrincipal.getEmail();

        UserPrincipal updatedUserPrincipal = userService.unlockUser(userPrincipal);
        assertFalse(updatedUserPrincipal.isLocked());
        User updatedUser = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        assertFalse(updatedUser.getIsLocked());
    }

    @Test
    @DisplayName("disableUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void disableUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.disableUser(staleUserPrincipal));
    }

    @Test
    @Transactional
    @DisplayName("disabledUser(): " + DISABLES_USER_WHEN_USER_ENABLED)
    void disabledUser_UserEnabled_DisabledUser() {
        userRepository.persist(user);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        String email = userPrincipal.getEmail();

        userService.disableUser(userPrincipal);
        User updatedUser = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        assertFalse(updatedUser.getIsEnabled());
    }

    @Test
    @DisplayName("enableUser(): " + THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST)
    void enableUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        staleUserPrincipal = UserPrincipalImpl.withUser(user).setId(ID_1).setEnabled(false).build();
        assertThrows(UserNotFoundException.class, () -> userService.enableUser(staleUserPrincipal));
    }

    @Test
    @Transactional
    @DisplayName("enableUser(): " + ENABLES_USER_WHEN_USER_DISABLED)
    void enableUser_UserDisabled_EnablesUser() {
        user.setIsEnabled(false);
        userRepository.persist(user);
        userPrincipal = UserPrincipalImpl.fromUser(user);
        String email = userPrincipal.getEmail();

        UserPrincipal updatedUserPrincipal = userService.enableUser(userPrincipal);
        assertTrue(updatedUserPrincipal.isEnabled());
        User updatedUser = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        assertTrue(updatedUser.getIsEnabled());
    }

    @Test
    @Transactional
    @DisplayName("addLoginAttempt(): " + ADDS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
    void addLoginAttempt_UserNotNull_AddsLoginAttempt() {
        user.setFailedLoginTime(LocalDateTime.now());
        userRepository.persist(user);
        LocalDateTime oldFailedLoginTime = user.getFailedLoginTime();
        int oldLoginAttempts = user.getFailedLoginAttempts();

        userService.addLoginAttempt(user);
        User updatedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertEquals(updatedUser.getFailedLoginAttempts(), oldLoginAttempts + 1);
        assertTrue(oldFailedLoginTime.isBefore(updatedUser.getFailedLoginTime()));
    }

    @Test
    @Transactional
    @DisplayName("resetLoginAttempts(): " + RESETS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL)
    void resetLoginAttempts_UserNotNull_ResetsLoginAttempts() {
        user.setFailedLoginAttempts(7);
        userRepository.persist(user);

        userService.resetLoginAttempts(user);
        User updatedUser = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
        assertEquals(0, updatedUser.getFailedLoginAttempts());
    }

}
