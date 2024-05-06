package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.permissions.Permissions;
import dev.naiarievilo.todoapp.roles.Roles;
import org.apache.commons.lang3.Validate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.*;

public class UserPrincipalImpl implements UserPrincipal {

    private final String username;
    private final String password;
    private final String email;
    private final Set<GrantedAuthority> roles;
    private final Set<GrantedAuthority> permissions;
    private final boolean isExpired;
    private final boolean isLocked;
    private final boolean isEnabled;

    private UserPrincipalImpl(String username, String password, String email, Set<GrantedAuthority> roles,
        Set<GrantedAuthority> permissions, boolean isExpired, boolean isLocked, boolean isEnabled) {

        Validate.notBlank(username, USERNAME_NOT_BLANK.message());
        Validate.notBlank(password, PASSWORD_NOT_BLANK.message());
        Validate.notBlank(email, EMAIL_NOT_BLANK.message());
        Validate.noNullElements(roles, ROLES_NOT_NULL_OR_EMPTY.message());
        Validate.noNullElements(permissions, PERMISSIONS_NOT_NULL_OR_EMPTY.message());

        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
        this.permissions = permissions;
        this.isExpired = isExpired;
        this.isLocked = isLocked;
        this.isEnabled = isEnabled;
    }

    public static UserPrincipalImplBuilder withUsername(String username) {
        Validate.notBlank(username, USERNAME_NOT_BLANK.message());

        return builder().setUsername(username);
    }

    public static UserPrincipalImplBuilder builder() {
        return new UserPrincipalImplBuilder();
    }

    public static UserPrincipal withUser(User user) {
        Validate.notNull(user, USER_NOT_NULL.message());

        return builder()
            .setUsername(user.getUsername())
            .setPassword(user.getPassword())
            .setEmail(user.getEmail())
            .setRoles(UserServiceImpl.getUserRoles(user))
            .setPermissions(UserServiceImpl.getUserPermissions(user))
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
        return permissions;
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
        private final Set<GrantedAuthority> permissions = new LinkedHashSet<>();
        private String username;
        private String password;
        private String email;
        private boolean isExpired;
        private boolean isLocked;
        private boolean isEnabled;

        public UserPrincipalImplBuilder setUsername(String username) {
            Validate.notBlank(username, USERNAME_NOT_BLANK.message());

            this.username = username;
            return this;
        }

        public UserPrincipalImplBuilder setPassword(String password) {
            Validate.notBlank(password, PASSWORD_NOT_BLANK.message());

            this.password = password;
            return this;
        }

        public UserPrincipalImplBuilder setEmail(String email) {
            Validate.notBlank(email, EMAIL_NOT_BLANK.message());

            this.email = email;
            return this;
        }

        public UserPrincipalImplBuilder setRoles(Roles... roles) {
            Validate.noNullElements(roles, ROLES_NOT_NULL_OR_EMPTY.message());

            for (Roles role : roles) {
                this.roles.add(new SimpleGrantedAuthority(role.name()));
            }

            return this;
        }

        public UserPrincipalImplBuilder setRoles(Set<GrantedAuthority> roles) {
            Validate.noNullElements(roles, ROLES_NOT_NULL_OR_EMPTY.message());

            this.roles.addAll(roles);
            return this;
        }

        public UserPrincipalImplBuilder setPermissions(Permissions... permissions) {
            Validate.noNullElements(permissions, PERMISSIONS_NOT_NULL_OR_EMPTY.message());

            for (Permissions permission : permissions) {
                this.permissions.add(new SimpleGrantedAuthority(permission.name()));
            }

            return this;
        }

        public UserPrincipalImplBuilder setPermissions(Set<GrantedAuthority> permissions) {
            Validate.noNullElements(permissions, PERMISSIONS_NOT_NULL_OR_EMPTY.message());

            this.permissions.addAll(permissions);
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
            return new UserPrincipalImpl(username, password, email, roles, permissions, isExpired, isLocked, isEnabled);
        }

    }

}