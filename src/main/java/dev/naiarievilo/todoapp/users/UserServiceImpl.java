package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.roles.RoleService;
import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.roles.UserRoleRemovalProhibitedException;
import dev.naiarievilo.todoapp.security.EmailPasswordAuthenticationToken;
import dev.naiarievilo.todoapp.security.UserPrincipal;
import dev.naiarievilo.todoapp.security.UserPrincipalImpl;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import dev.naiarievilo.todoapp.users.exceptions.EmailAlreadyRegisteredException;
import dev.naiarievilo.todoapp.users.exceptions.UserAlreadyExistsException;
import dev.naiarievilo.todoapp.users.exceptions.UserNotFoundException;
import dev.naiarievilo.todoapp.users_info.UserInfoService;
import org.springframework.security.core.Authentication;
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

    @Override
    @Transactional
    public Authentication updateEmail(String currentEmail, String newEmail) {
        if (userExists(newEmail)) {
            throw new EmailAlreadyRegisteredException(newEmail);
        }

        User user = this.getUserByEmail(currentEmail);
        user.setEmail(newEmail);
        userRepository.update(user);

        return EmailPasswordAuthenticationToken.authenticated(user.getEmail(),
            user.getPassword(), UserServiceImpl.getRolesFromUser(user));
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
    public Authentication createUser(UserCreationDTO userCreationDTO) {
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

        return EmailPasswordAuthenticationToken.authenticated(newUser.getEmail(), newUser.getPassword(),
            Set.of(new SimpleGrantedAuthority(defaultRole.getName()))
        );
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        User user = getUserByEmail(email);
        user.removeAllRoles();

        userInfoService.deleteUserInfo(user.getId());
        userRepository.delete(user);
    }


    @Override
    @Transactional
    public Authentication updatePassword(String email, String newPassword) {
        User user = this.getUserByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.update(user);

        return EmailPasswordAuthenticationToken.authenticated(user.getEmail(),
            user.getPassword(), UserServiceImpl.getRolesFromUser(user));
    }

    @Override
    @Transactional
    public Authentication addRoleToUser(Authentication authentication, Roles role) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority(role.name()))) {
            return authentication;
        }

        User user = this.getUserByEmail((String) authentication.getPrincipal());
        Role roleToAdd = roleService.getRole(role);
        user.addRole(roleToAdd);
        userRepository.update(user);
        return EmailPasswordAuthenticationToken.authenticated(user.getEmail(), UserServiceImpl.getRolesFromUser(user));
    }

    @Override
    @Transactional
    public Authentication removeRoleFromUser(Authentication authentication, Roles role) {
        if (role.name().equals(ROLE_USER.name())) {
            throw new UserRoleRemovalProhibitedException();
        } else if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority(role.name()))) {
            return authentication;
        }

        User user = getUserByEmail((String) authentication.getPrincipal());
        Role roleToRemove = roleService.getRole(role);
        user.removeRole(roleToRemove);
        userRepository.update(user);
        return EmailPasswordAuthenticationToken.authenticated(user.getEmail(), UserServiceImpl.getRolesFromUser(user));
    }

    @Override
    @Transactional
    public void lockUser(String email) {
        User user = this.getUserByEmail(email);
        if (user.getIsLocked()) {
            return;
        }

        user.setIsLocked(true);
        userRepository.update(user);
    }

    @Override
    @Transactional
    public void unlockUser(String email) {
        User user = this.getUserByEmail(email);
        if (!user.getIsLocked()) {
            return;
        }

        user.setIsLocked(false);
        userRepository.update(user);
    }

    @Override
    @Transactional
    public void disableUser(String email) {
        User user = this.getUserByEmail(email);
        if (!user.getIsEnabled()) {
            return;
        }

        user.setIsEnabled(false);
        userRepository.update(user);
    }

    @Override
    @Transactional
    public void enableUser(String email) {
        User user = this.getUserByEmail(email);
        if (user.getIsEnabled()) {
            return;
        }

        user.setIsEnabled(true);
        userRepository.update(user);
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
