package dev.naiarievilo.todoapp.security;

import dev.naiarievilo.todoapp.roles.Roles;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserServiceImpl;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.LinkedHashSet;
import java.util.Set;

import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.NOT_BLANK;
import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.NOT_EMPTY;

public class UserPrincipalImpl implements UserPrincipal {

    private final String email;
    private final Long id;
    private final boolean isEnabled;
    private final boolean isLocked;
    private final String password;
    private final Set<GrantedAuthority> roles;

    private UserPrincipalImpl(Long id, String email, String password, Set<GrantedAuthority> roles,
        boolean isLocked, boolean isEnabled) {
        Validate.notBlank(email, NOT_BLANK);
        Validate.notBlank(password, NOT_BLANK);

        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.isLocked = isLocked;
        this.isEnabled = isEnabled;
    }

    public static UserPrincipalImplBuilder withEmail(String email) {
        Validate.notBlank(email, NOT_BLANK);
        return builder().setEmail(email);
    }

    public static UserPrincipalImplBuilder builder() {
        return new UserPrincipalImplBuilder();
    }

    public static UserPrincipal withUser(User user) {
        return builder()
            .setId(user.getId())
            .setEmail(user.getEmail())
            .setPassword(user.getPassword())
            .setRoles(UserServiceImpl.getRolesFromUser(user))
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
    public Set<GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hb = new HashCodeBuilder();
        hb.append(email);
        return hb.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UserPrincipalImpl other)) return false;
        EqualsBuilder eb = new EqualsBuilder();
        eb.append(email, other.email);
        return eb.isEquals();
    }

    public static final class UserPrincipalImplBuilder {

        private final Set<GrantedAuthority> roles = new LinkedHashSet<>();
        private Long id;
        private String email;
        private String password;
        private boolean isLocked;
        private boolean isEnabled;

        public UserPrincipalImplBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public UserPrincipalImplBuilder setEmail(String email) {
            Validate.notBlank(email, NOT_BLANK);
            this.email = email;
            return this;
        }

        public UserPrincipalImplBuilder setPassword(String password) {
            Validate.notBlank(password, NOT_BLANK);
            this.password = password;
            return this;
        }


        public UserPrincipalImplBuilder setRoles(Roles... roles) {
            Validate.notEmpty(roles, NOT_EMPTY);

            for (Roles role : roles) {
                this.roles.add(new SimpleGrantedAuthority(role.name()));
            }
            return this;
        }

        public UserPrincipalImplBuilder setRoles(Set<GrantedAuthority> roles) {
            Validate.notEmpty(roles, NOT_EMPTY);
            this.roles.addAll(roles);
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
            return new UserPrincipalImpl(id, email, password, roles, isLocked, isEnabled);
        }

    }

}
