package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.roles.UserRoleRemovalProhibitedException;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import dev.naiarievilo.todoapp.security.UserPrincipalImpl;
import dev.naiarievilo.todoapp.users.exceptions.EmailAlreadyRegisteredException;
import dev.naiarievilo.todoapp.users.exceptions.UserAlreadyExistsException;
import dev.naiarievilo.todoapp.users.info.UserInfoService;
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

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

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
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public UserPrincipal loadUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
        return UserPrincipalImpl.withUser(user);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    public User getUserByPrincipal(UserPrincipal userPrincipal) {
        String email = userPrincipal.getEmail();
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    @Transactional
    public UserPrincipal createUser(UserCreationDTO userCreationDTO) {
        String email = userCreationDTO.email();
        if (userExists(email))
            throw new UserAlreadyExistsException(email);


        Role defaultRole = roleService.getRole(ROLE_USER);

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(userCreationDTO.password()));
        newUser.addRole(defaultRole);

        newUser = userRepository.persist(newUser);
        userInfoService.createUserInfo(userCreationDTO, newUser);

        return UserPrincipalImpl.withUser(newUser);
    }

    @Override
    @Transactional
    public void deleteUser(UserPrincipal userPrincipal) {
        User user = getUserByPrincipal(userPrincipal);
        user.removeAllRoles();

        userInfoService.deleteUserInfo(user.getId());
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserPrincipal updateEmail(UserPrincipal userPrincipal, String newEmail) {
        if (userExists(newEmail)) {
            throw new EmailAlreadyRegisteredException(newEmail);
        }

        User user = this.getUserByPrincipal(userPrincipal);
        user.setEmail(newEmail);
        userRepository.update(user);

        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal updatePassword(UserPrincipal userPrincipal, String newPassword) {
        User user = getUserByPrincipal(userPrincipal);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.update(user);

        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal addRoleToUser(UserPrincipal userPrincipal, Roles role) {
        if (userPrincipal.getAuthorities().contains(new SimpleGrantedAuthority(role.name()))) {
            return userPrincipal;
        }

        User user = getUserByPrincipal(userPrincipal);
        Role roleToAdd = roleService.getRole(role);
        user.addRole(roleToAdd);
        userRepository.update(user);
        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal removeRoleFromUser(UserPrincipal userPrincipal, Roles role) {
        if (!userPrincipal.getAuthorities().contains(new SimpleGrantedAuthority(role.name()))) {
            return userPrincipal;
        }

        if (role.name().equals(ROLE_USER.name())) {
            throw new UserRoleRemovalProhibitedException();
        }

        User user = getUserByPrincipal(userPrincipal);
        Role roleToRemove = roleService.getRole(role);
        user.removeRole(roleToRemove);
        userRepository.update(user);
        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal lockUser(UserPrincipal userPrincipal) {
        if (userPrincipal.isLocked()) {
            return userPrincipal;
        }

        User user = getUserByPrincipal(userPrincipal);
        user.setIsLocked(true);
        userRepository.update(user);
        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal unlockUser(UserPrincipal userPrincipal) {
        if (!userPrincipal.isLocked()) {
            return userPrincipal;
        }

        User user = getUserByPrincipal(userPrincipal);
        user.setIsLocked(false);
        userRepository.update(user);
        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal disableUser(UserPrincipal userPrincipal) {
        if (!userPrincipal.isEnabled()) {
            return userPrincipal;
        }

        User user = getUserByPrincipal(userPrincipal);
        user.setIsEnabled(false);
        userRepository.update(user);
        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal enableUser(UserPrincipal userPrincipal) {
        if (userPrincipal.isEnabled()) {
            return userPrincipal;
        }

        User user = getUserByPrincipal(userPrincipal);
        user.setIsEnabled(true);
        userRepository.update(user);
        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public void addLoginAttempt(User user) {
        user.incrementFailedLoginAttempts();
        user.setFailedLoginTime(LocalTime.now());
        userRepository.update(user);
    }

    @Override
    @Transactional
    public void resetLoginAttempts(User user) {
        user.setFailedLoginAttempts(0);
        userRepository.update(user);

    }

}
