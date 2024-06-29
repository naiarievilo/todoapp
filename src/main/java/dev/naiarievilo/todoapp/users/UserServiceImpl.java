package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.roles.exceptions.UserRoleRemovalProhibitedException;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users.exceptions.EmailAlreadyRegisteredException;
import dev.naiarievilo.todoapp.users.exceptions.UserAlreadyExistsException;
import dev.naiarievilo.todoapp.users.exceptions.UserNotFoundException;
import dev.naiarievilo.todoapp.users_info.UserInfoService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    public static final byte EMAIL_CONFIRMATION_PERIOD = 7;

    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final UserInfoService userInfoService;
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository, UserInfoService userInfoService, RoleService roleService,
        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userInfoService = userInfoService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    public static Set<GrantedAuthority> getRolesFromUser(User user) {
        return user.getRoles().stream()
            .map(Role::getName)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public boolean isUserInactive(User user) {
        return user.isLocked() || !user.isEnabled();
    }

    @Override
    public boolean isUserExpired(User user) {
        var emailAuthenticationPeriod = user.getCreationDate().plusDays(EMAIL_CONFIRMATION_PERIOD);
        return !user.isAuthenticated() && LocalDateTime.now().isAfter(emailAuthenticationPeriod);
    }

    @Override
    public boolean userExists(Long id) {
        return userRepository.findById(id).isPresent();
    }

    @Override
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    @Transactional
    public void authenticateUser(User user) {
        if (user.isAuthenticated()) {
            return;
        }

        user.setAuthenticated(true);
        userRepository.update(user);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    public User getUserByEmailEagerly(String email) {
        return userRepository.findByEmailEagerly(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User getUserByIdEagerly(Long id) {
        return userRepository.findByIdEagerly(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional
    public User createUser(UserCreationDTO userCreationDTO) {
        String email = userCreationDTO.email();
        if (userExists(email)) {
            throw new UserAlreadyExistsException(email);
        }

        Role defaultRole = roleService.getRole(ROLE_USER);
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(userCreationDTO.password()));
        newUser.addRole(defaultRole);

        userRepository.persist(newUser);
        userInfoService.createUserInfo(userCreationDTO, newUser);

        return newUser;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // Entity must be managed to disassociate a user from their assigned roles.
        User user = getUserByIdEagerly(id);
        user.removeAllRoles();
        userInfoService.deleteUserInfo(user.getId());
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public User updateEmail(User user, String newEmail) {
        if (newEmail.equals(user.getEmail())) {
            return user;
        } else if (userExists(newEmail)) {
            throw new EmailAlreadyRegisteredException(newEmail);
        }

        user.setEmail(newEmail);
        userRepository.update(user);
        return user;
    }

    @Override
    @Transactional
    public User updatePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        } else if (newPassword.equals(currentPassword)) {
            return user;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.update(user);
        return user;
    }

    @Override
    @Transactional
    public User addRoleToUser(User user, Roles role) {
        Set<Role> userAssignedRoles = user.getRoles();
        for (Role currentRole : userAssignedRoles) {
            if (currentRole.getName().equals(role.name())) {
                return user;
            }
        }

        Role roleToAdd = roleService.getRole(role);
        user.addRole(roleToAdd);
        userRepository.update(user);
        return user;
    }

    @Override
    @Transactional
    public User removeRoleFromUser(User user, Roles role) {
        if (role.name().equals(ROLE_USER.name())) {
            throw new UserRoleRemovalProhibitedException();
        }

        Set<Role> userAssignedRoles = user.getRoles();
        boolean roleIsAssigned = false;
        for (Role currentRole : userAssignedRoles) {
            if (currentRole.getName().equals(role.name())) {
                roleIsAssigned = true;
                break;
            }
        }

        if (!roleIsAssigned) {
            return user;
        }

        Role roleToRemove = roleService.getRole(role);
        user.removeRole(roleToRemove);
        userRepository.update(user);
        return user;
    }

    @Override
    @Transactional
    public User lockUser(User user) {
        if (user.isLocked()) {
            return user;
        }

        user.setLocked(true);
        userRepository.update(user);
        return user;
    }

    @Override
    @Transactional
    public User unlockUser(User user) {
        if (!user.isLocked()) {
            return user;
        }

        user.setLocked(false);
        userRepository.update(user);
        return user;
    }

    @Override
    @Transactional
    public User disableUser(User user) {
        if (!user.isEnabled()) {
            return user;
        }
        user.setEnabled(false);
        userRepository.update(user);
        return user;
    }

    @Override
    @Transactional
    public User enableUser(User user) {
        if (user.isEnabled()) {
            return user;
        }
        user.setEnabled(true);
        userRepository.update(user);
        return user;
    }

    @Override
    @Transactional
    public void addLoginAttempt(User user) {
        user.addLoginAttempt();
        user.setLastLoginAttempt(LocalDateTime.now());
        // Invoking `update` instead of `merge` throws `PersistentObjectException`
        userRepository.merge(user);
    }

    @Override
    @Transactional
    public void resetLoginAttempts(User user) {
        user.setLoginAttempts((byte) 0);
        user.setLastLogin(LocalDate.now());
        // Invoking `update` instead of `merge` throws `PersistentObjectException`
        userRepository.merge(user);
    }

}
