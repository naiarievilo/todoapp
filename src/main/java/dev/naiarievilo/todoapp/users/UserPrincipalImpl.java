package dev.naiarievilo.todoapp.users;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserPrincipalImpl implements UserPrincipal {

    private final String username;
    private final String password;
    private final String email;
    private final Set<GrantedAuthority> authorities;
    private final Set<GrantedAuthority> roles;
    private final boolean isExpired;
    private final boolean isLocked;
    private final boolean isEnabled;

    public UserPrincipalImpl(String username, String password, String email, Set<GrantedAuthority> roles,
        Set<GrantedAuthority> authorities, boolean isExpired, boolean isLocked, boolean isEnabled) {

        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
        this.authorities = authorities;
        this.isExpired = isExpired;
        this.isLocked = isLocked;
        this.isEnabled = isEnabled;
    }

    public UserPrincipalImpl(String username, String password, String email, Set<GrantedAuthority> roles,
        Set<GrantedAuthority> authorities) {
        this(username, password, email, roles, authorities, false, false, true);
    }

    public static UserPrincipalImpl from(User user) {
        Set<GrantedAuthority> roles = user.getRoles().stream()
            .map(Role::getRole)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(Permission::getPermission)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        return new UserPrincipalImpl(
            user.getUsername(),
            user.getPassword(),
            user.getEmail(),
            authorities,
            roles,
            user.getIsExpired(),
            user.getIsLocked(),
            user.getIsEnabled()
        );
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getRoles() {
        return roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !isExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

}
