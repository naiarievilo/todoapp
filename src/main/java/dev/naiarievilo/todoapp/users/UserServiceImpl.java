package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.roles.UserRoleRemovalProhibitedException;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import dev.naiarievilo.todoapp.security.UserPrincipalImpl;
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

import java.time.LocalDateTime;
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
    @Transactional
    public UserPrincipal updateEmail(UserPrincipal userPrincipal, String newEmail) {
        if (userExists(newEmail)) {
            throw new EmailAlreadyRegisteredException(newEmail);
        }

        User user = this.getUserByEmail(userPrincipal.getEmail());
        user.setEmail(newEmail);
        userRepository.update(user);
        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal unlockUser(UserPrincipal userPrincipal) {
        if (!userPrincipal.isLocked()) {
            return userPrincipal;
        }
        User user = this.getUserByEmail(userPrincipal.getEmail());

        user.setIsLocked(false);
        userRepository.update(user);
        return UserPrincipalImpl.withUser(user);
    }

    @Override
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public UserPrincipal loadUserPrincipalByEmail(String email) {
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

        userRepository.persist(newUser);
        userInfoService.createUserInfo(userCreationDTO, newUser);

        return UserPrincipalImpl.withUser(newUser);
    }

    @Override
    @Transactional
    public void deleteUser(UserPrincipal userPrincipal) {
        User user = getUserByEmail(userPrincipal.getEmail());
        user.removeAllRoles();

        userInfoService.deleteUserInfo(user.getId());
        userRepository.delete(user);
    }


    @Override
    @Transactional
    public UserPrincipal updatePassword(UserPrincipal userPrincipal, String currentPassword, String newPassword) {
        User user = this.getUserByEmail(userPrincipal.getEmail());
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Incorrect current password");
        }

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

        User user = this.getUserByEmail(userPrincipal.getEmail());
        Role roleToAdd = roleService.getRole(role);
        user.addRole(roleToAdd);
        userRepository.update(user);
        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public UserPrincipal removeRoleFromUser(UserPrincipal userPrincipal, Roles role) {
        if (role.name().equals(ROLE_USER.name())) {
            throw new UserRoleRemovalProhibitedException();
        } else if (!userPrincipal.getAuthorities().contains(new SimpleGrantedAuthority(role.name()))) {
            return userPrincipal;
        }

        User user = getUserByEmail(userPrincipal.getEmail());
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

        User user = this.getUserByEmail(userPrincipal.getEmail());
        user.setIsLocked(true);
        userRepository.update(user);
        return UserPrincipalImpl.withUser(user);
    }


    @Override
    @Transactional
    public UserPrincipal disableUser(UserPrincipal userPrincipal) {
        if (!userPrincipal.isEnabled()) {
            return userPrincipal;
        }
        User user = this.getUserByEmail(userPrincipal.getEmail());
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
        User user = this.getUserByEmail(userPrincipal.getEmail());
        user.setIsEnabled(true);
        userRepository.update(user);
        return UserPrincipalImpl.withUser(user);
    }

    @Override
    @Transactional
    public void addLoginAttempt(User user) {
        user.incrementFailedLoginAttempts();
        user.setFailedLoginTime(LocalDateTime.now());
        userRepository.update(user);
    }

    @Override
    @Transactional
    public void resetLoginAttempts(User user) {
        user.setFailedLoginAttempts(0);
        userRepository.update(user);

    }

}
