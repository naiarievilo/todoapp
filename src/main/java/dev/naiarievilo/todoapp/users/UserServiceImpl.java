package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import dev.naiarievilo.todoapp.security.UserPrincipalImpl;
import org.apache.commons.lang3.Validate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.NOT_BLANK;
import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.NOT_NULL;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserInfoService userInfoService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserInfoService userInfoService, RoleService roleService,
        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userInfoService = userInfoService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    public static Set<GrantedAuthority> getRolesFromUser(User user) {
        Validate.notNull(user, NOT_NULL);

        return user.getRoles().stream()
            .map(Role::getName)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public boolean userExists(String email) {
        Validate.notBlank(email, NOT_BLANK);
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public UserPrincipal loadUserByEmail(String email) {
        Validate.notBlank(email, NOT_BLANK);

        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        return UserPrincipalImpl.withUser(user);
    }

    @Override
    public User getUserByEmail(String email) {
        Validate.notBlank(email, NOT_BLANK);
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public User getUserByPrincipal(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL);
        return userRepository.findByEmail(userPrincipal.getEmail()).orElseThrow(UserNotFoundException::new);
    }

    @Override
    @Transactional
    public UserPrincipal createUser(UserCreationDTO userCreationDTO) {
        Validate.notNull(userCreationDTO, NOT_NULL);

        if (userExists(userCreationDTO.email())) {
            throw new UserAlreadyExistsException();
        }

        Role defaultRole = roleService.getRole(ROLE_USER);

        User newUser = new User();
        newUser.setEmail(userCreationDTO.email());
        newUser.setPassword(passwordEncoder.encode(userCreationDTO.password()));
        newUser.addRole(defaultRole);
        newUser.setFailedLoginAttempts(0);

        newUser = userRepository.persist(newUser);
        userInfoService.createUserInfo(userCreationDTO, newUser);

        return UserPrincipalImpl.withUser(newUser);
    }

    @Override
    @Transactional
    public void deleteUser(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL);

        User user = getUserByPrincipal(userPrincipal);
        user.removeAllRoles();

        userInfoService.deleteUserInfo(user.getId());
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserPrincipal updateEmail(UserPrincipal userPrincipal, String newEmail) {
        Validate.notNull(userPrincipal, NOT_NULL);
        Validate.notBlank(newEmail, NOT_BLANK);

        if (userExists(newEmail)) {
            throw new EmailAlreadyRegisteredException();
        }

        User user = this.getUserByPrincipal(userPrincipal);
        user.setEmail(newEmail);
        userRepository.update(user);

        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal updatePassword(UserPrincipal userPrincipal, String newPassword) {
        Validate.notNull(userPrincipal, NOT_NULL);
        Validate.notBlank(newPassword, NOT_BLANK);

        User user = getUserByPrincipal(userPrincipal);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.update(user);

        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal addRoleToUser(UserPrincipal userPrincipal, Roles role) {
        Validate.notNull(userPrincipal, NOT_NULL);
        Validate.notNull(role, NOT_NULL);

        User user = getUserByPrincipal(userPrincipal);
        Role roleToAdd = roleService.getRole(role);
        if (user.getRoles().contains(roleToAdd)) {
            return UserPrincipalImpl.withUser(user);
        }

        user.addRole(roleToAdd);
        userRepository.update(user);

        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal removeRoleFromUser(UserPrincipal userPrincipal, Roles role) {
        Validate.notNull(userPrincipal, NOT_NULL);
        Validate.notNull(role, NOT_NULL);

        User user = getUserByPrincipal(userPrincipal);
        Role roleToRemove = roleService.getRole(role);
        if (roleToRemove.getName().equals(ROLE_USER.name())) {
            throw new UserRoleRemovalProhibitedException();
        }

        user.removeRole(roleToRemove);
        userRepository.update(user);

        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public void lockUser(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL);

        User user = getUserByPrincipal(userPrincipal);
        if (user.getIsLocked()) {
            return;
        }

        user.setIsLocked(true);
        userRepository.update(user);
    }

    @Override
    @Transactional
    public void unlockUser(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL);

        User user = getUserByPrincipal(userPrincipal);
        if (!user.getIsLocked()) {
            return;
        }

        user.setIsLocked(false);
        userRepository.update(user);
    }

    @Override
    @Transactional
    public void disableUser(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL);

        User user = getUserByPrincipal(userPrincipal);
        if (!user.getIsEnabled()) {
            return;
        }

        user.setIsEnabled(false);
        userRepository.update(user);
    }

    @Override
    @Transactional
    public void enableUser(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL);

        User user = getUserByPrincipal(userPrincipal);
        if (user.getIsEnabled()) {
            return;
        }

        user.setIsEnabled(true);
        userRepository.update(user);
    }

    @Override
    @Transactional
    public void addLoginAttempt(User user) {
        Validate.notNull(user, NOT_NULL);

        user.incrementFailedLoginAttempts();
        user.setFailedLoginTime(LocalTime.now());

        userRepository.update(user);
    }

    @Override
    @Transactional
    public void resetLoginAttempt(User user) {
        Validate.notNull(user, NOT_NULL);
        user.setFailedLoginAttempts(0);
        userRepository.update(user);

    }


}
