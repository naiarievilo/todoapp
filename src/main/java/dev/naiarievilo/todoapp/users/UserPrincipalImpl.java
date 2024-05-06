package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.permissions.Permissions;
import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserServiceImpl;
import org.apache.commons.lang3.Validate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

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
        Set<GrantedAuthority> authorities) {
        this(username, password, email, roles, authorities, false, false, true);
    }

    private UserPrincipalImpl(String username, String password, String email, Set<GrantedAuthority> roles,
        Set<GrantedAuthority> authorities, boolean isExpired, boolean isLocked, boolean isEnabled) {

        Validate.notBlank(username, "Username must not be blank");
        Validate.notBlank(password, "Password must not be blank");
        Validate.notBlank(email, "Email must not be blank");
        Validate.notEmpty(roles, "Roles must not be empty");
        Validate.noNullElements(roles, "Roles must not contain null elements");
        Validate.notEmpty(authorities, "Authorities must not be empty");
        Validate.noNullElements(authorities, "Authorities must not contain null elements");

        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
        this.authorities = authorities;
        this.isExpired = isExpired;
        this.isLocked = isLocked;
        this.isEnabled = isEnabled;
    }

    public static UserPrincipalImplBuilder withUsername(String username) {
        Validate.notBlank(username, "Username must not be blank");

        return builder().setUsername(username);
    }

    public static UserPrincipalImplBuilder builder() {
        return new UserPrincipalImplBuilder();
    }

    public static UserPrincipal withUser(User user) {
        Validate.notNull(user, "User must not be null");

        return builder()
            .setUsername(user.getUsername())
            .setPassword(user.getPassword())
            .setEmail(user.getEmail())
            .setRoles(UserServiceImpl.getUserRoles(user))
            .setAuthorities(UserServiceImpl.getUserAuthorities(user))
            .setExpired(user.getIsExpired())
            .setLocked(user.getIsLocked())
            .setEnabled(user.getIsEnabled())
            .build();
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Collection<GrantedAuthority> getRoles() {
        return roles;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
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

    public static final class UserPrincipalImplBuilder {

        private final Set<GrantedAuthority> roles = new LinkedHashSet<>();
        private final Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        private String username;
        private String password;
        private String email;
        private boolean isExpired;
        private boolean isLocked;
        private boolean isEnabled;

        public UserPrincipalImplBuilder setUsername(String username) {
            Validate.notBlank(username, "Username must not be blank");

            this.username = username;
            return this;
        }

        public UserPrincipalImplBuilder setPassword(String password) {
            Validate.notBlank(password, "Password must not be blank");

            this.password = password;
            return this;
        }

        public UserPrincipalImplBuilder setEmail(String email) {
            Validate.notBlank(email, "Email must not be blank");

            this.email = email;
            return this;
        }

        public UserPrincipalImplBuilder setRoles(Roles... roles) {
            Validate.notEmpty(roles, "Must provide at least one role");
            Validate.noNullElements(roles, "Roles must not contain null elements");

            for (Roles role : roles) {
                this.roles.add(new SimpleGrantedAuthority(role.name()));
            }

            return this;
        }

        public UserPrincipalImplBuilder setRoles(Set<GrantedAuthority> roles) {
            Validate.notEmpty(roles, "Must provide at least one role");
            Validate.noNullElements(roles, "Roles must not contain null elements");

            this.roles.addAll(roles);
            return this;
        }

        public UserPrincipalImplBuilder setAuthorities(Permissions... permissions) {
            Validate.notEmpty(permissions, "Must provide at least one permission");
            Validate.noNullElements(permissions, "Permissions must not contain null elements");

            for (Permissions permission : permissions) {
                this.authorities.add(new SimpleGrantedAuthority(permission.name()));
            }

            return this;
        }

        public UserPrincipalImplBuilder setAuthorities(Set<GrantedAuthority> permissions) {
            Validate.notEmpty(permissions, "Must provide at least one permission");
            Validate.noNullElements(permissions, "Permissions must not contain null elements");

            this.authorities.addAll(permissions);
            return this;
        }

        public UserPrincipalImplBuilder setExpired(boolean expired) {
            isExpired = expired;
            return this;
        }

        public UserPrincipalImplBuilder setLocked(boolean locked) {
            isLocked = locked;
            return this;
        }

        public UserPrincipalImplBuilder setEnabled(boolean enabled) {
            isEnabled = enabled;

            return this;
        }

        public UserPrincipal build() {
            return new UserPrincipalImpl(username, password, email, roles, authorities, isExpired, isLocked, isEnabled);
        }

    }

}
