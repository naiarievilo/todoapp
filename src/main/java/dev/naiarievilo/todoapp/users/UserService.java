package dev.naiarievilo.todoapp.users;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.provisioning.GroupManager;
import org.springframework.security.provisioning.UserDetailsManager;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public interface UserService extends UserDetailsManager, GroupManager {

    static Set<GrantedAuthority> getUserAuthorities(User user) {
        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(Permission::getPermission)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    static Set<GrantedAuthority> getUserRoles(User user) {
        return user.getRoles().stream()
            .map(Role::getRole)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    UserPrincipal loadUserByUsername(String username);

    void createUser(UserPrincipal user);

    void updateUser(UserPrincipal user);
}
