package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.permissions.Permission;
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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_BLANK;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_NULL;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    public static Set<GrantedAuthority> getPermissionsFromUser(User user) {
        Validate.notNull(user, NOT_NULL.message());

        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(Permission::getName)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<GrantedAuthority> getRolesFromUser(User user) {
        Validate.notNull(user, NOT_NULL.message());

        return user.getRoles().stream()
            .map(Role::getName)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public boolean userExists(String email) {
        Validate.notBlank(email, NOT_BLANK.message());
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public UserPrincipal loadUserByEmail(String email) {
        Validate.notBlank(email, NOT_BLANK.message());

        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        return UserPrincipalImpl.withUser(user);
    }

    @Override
    public User getUser(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL.message());
        return userRepository.findByEmail(userPrincipal.getEmail()).orElseThrow(UserNotFoundException::new);
    }

    @Override
    @Transactional
    public void createUser(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL.message());

        Set<Role> roles = getRolesFromUserPrincipal(userPrincipal);

        User newUser = new User();
        newUser.setEmail(userPrincipal.getEmail());
        newUser.setPassword(passwordEncoder.encode(userPrincipal.getPassword()));
        newUser.setRoles(roles);

        userRepository.persist(newUser);
    }

    private Set<Role> getRolesFromUserPrincipal(UserPrincipal userPrincipal) {
        return userPrincipal.getRoles().stream()
            .map(Roles::toRoles)
            .map(roleService::getRole)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public void deleteUser(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL.message());

        User user = getUser(userPrincipal);
        user.removeAllRoles();

        userRepository.deleteByEmail(user.getEmail());
    }

    @Override
    @Transactional
    public UserPrincipal changeEmail(UserPrincipal userPrincipal, String newEmail) {
        Validate.notNull(userPrincipal, NOT_NULL.message());
        Validate.notBlank(newEmail, NOT_BLANK.message());

        User user = getUser(userPrincipal);
        user.setEmail(newEmail);
        userRepository.update(user);

        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal changePassword(UserPrincipal userPrincipal, String newPassword) {
        Validate.notNull(userPrincipal, NOT_NULL.message());
        Validate.notBlank(newPassword, NOT_BLANK.message());

        User user = getUser(userPrincipal);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.update(user);

        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal addRoleToUser(UserPrincipal userPrincipal, Roles role) {
        Validate.notNull(userPrincipal, NOT_NULL.message());
        Validate.notNull(role, NOT_NULL.message());

        User user = getUser(userPrincipal);
        Role roleToAdd = roleService.getRole(role);
        user.addRole(roleToAdd);
        userRepository.update(user);

        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal removeRoleFromUser(UserPrincipal userPrincipal, Roles role) {
        Validate.notNull(userPrincipal, NOT_NULL.message());
        Validate.notNull(role, NOT_NULL.message());

        User user = getUser(userPrincipal);
        Role roleToRemove = roleService.getRole(role);
        user.removeRole(roleToRemove);
        userRepository.update(user);

        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public void lockUser(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL.message());

        User user = getUser(userPrincipal);
        user.setIsLocked(true);
        userRepository.update(user);
    }

    @Override
    @Transactional
    public void unlockUser(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL.message());

        User user = getUser(userPrincipal);
        user.setIsLocked(false);
        userRepository.update(user);
    }

    @Override
    @Transactional
    public void disableUser(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL.message());

        User user = getUser(userPrincipal);
        user.setIsEnabled(false);
        userRepository.update(user);
    }

    @Override
    @Transactional
    public void enableUser(UserPrincipal userPrincipal) {
        Validate.notNull(userPrincipal, NOT_NULL.message());

        User user = getUser(userPrincipal);
        user.setIsEnabled(true);
        userRepository.update(user);
    }
}
