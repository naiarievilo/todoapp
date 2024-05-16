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

    private final Long id;
    private final String email;
    private final String password;
    private final Set<GrantedAuthority> roles;
    private final Set<GrantedAuthority> permissions;
    private final boolean isLocked;
    private final boolean isEnabled;

    private UserPrincipalImpl(Long id, String email, String password, Set<GrantedAuthority> roles,
        Set<GrantedAuthority> permissions, boolean isLocked, boolean isEnabled) {

        Validate.notNull(id, NOT_NULL.message());
        Validate.notBlank(email, NOT_BLANK.message());
        Validate.notBlank(password, NOT_BLANK.message());
        Validate.noNullElements(roles, NO_NULL_ELEMENTS.message());
        Validate.noNullElements(permissions, NO_NULL_ELEMENTS.message());

        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.permissions = permissions;
        this.isLocked = isLocked;
        this.isEnabled = isEnabled;
    }

    public static UserPrincipalImplBuilder withEmail(String email) {
        Validate.notBlank(email, NOT_BLANK.message());
        return builder().setEmail(email);
    }

    public static UserPrincipalImplBuilder builder() {
        return new UserPrincipalImplBuilder();
    }

    public static UserPrincipal withUser(User user) {
        Validate.notNull(user, NOT_NULL.message());
        return builder()
            .setEmail(user.getEmail())
            .setPassword(user.getPassword())
            .setRoles(UserServiceImpl.getRolesFromUser(user))
            .setPermissions(UserServiceImpl.getPermissionsFromUser(user))
            .setLocked(user.getIsLocked())
            .setEnabled(user.getIsEnabled())
            .build();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
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
    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public static final class UserPrincipalImplBuilder {

        private final Set<GrantedAuthority> roles = new LinkedHashSet<>();
        private final Set<GrantedAuthority> permissions = new LinkedHashSet<>();
        private Long id;
        private String email;
        private String password;
        private boolean isLocked;
        private boolean isEnabled;

        public UserPrincipalImplBuilder setId(Long id) {
            Validate.notNull(id, NOT_NULL.message());
            this.id = id;
            return this;
        }

        public UserPrincipalImplBuilder setEmail(String email) {
            Validate.notBlank(email, NOT_BLANK.message());
            this.email = email;
            return this;
        }

        public UserPrincipalImplBuilder setPassword(String password) {
            Validate.notBlank(password, NOT_BLANK.message());
            this.password = password;
            return this;
        }


        public UserPrincipalImplBuilder setRoles(Roles... roles) {
            Validate.noNullElements(roles, NO_NULL_ELEMENTS.message());

            for (Roles role : roles) {
                this.roles.add(new SimpleGrantedAuthority(role.name()));
            }
            return this;
        }

        public UserPrincipalImplBuilder setRoles(Set<GrantedAuthority> roles) {
            Validate.noNullElements(roles, NO_NULL_ELEMENTS.message());
            this.roles.addAll(roles);
            return this;
        }

        public UserPrincipalImplBuilder setPermissions(Permissions... permissions) {
            Validate.noNullElements(permissions, NO_NULL_ELEMENTS.message());

            for (Permissions permission : permissions) {
                this.permissions.add(new SimpleGrantedAuthority(permission.name()));
            }
            return this;
        }

        public UserPrincipalImplBuilder setPermissions(Set<GrantedAuthority> permissions) {
            Validate.noNullElements(permissions, NO_NULL_ELEMENTS.message());
            this.permissions.addAll(permissions);
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
            return new UserPrincipalImpl(id, email, password, roles, permissions, isLocked, isEnabled);
        }

    }

}
