package dev.naiarievilo.todoapp.security;

import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserService;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class UserAuthenticationToken extends AbstractAuthenticationToken {

    private final transient User user;
    private final transient String credentials;

    public UserAuthenticationToken(User user) {
        super(UserService.getRolesFromUser(user));
        super.setAuthenticated(true);
        this.credentials = user.getPassword();
        this.user = user;
    }

    @Override
    public String getCredentials() {
        return credentials;
    }

    @Override
    public User getPrincipal() {
        return user;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new IllegalArgumentException(
            "Authentication status for `UserAuthenticationToken` instances cannot be modified" +
                "and should only represent authenticated users.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAuthenticationToken token)) return false;

        EqualsBuilder eb = new EqualsBuilder();
        eb.append(user, token.user);
        return eb.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hb = new HashCodeBuilder();
        hb.append(user);
        return hb.toHashCode();
    }
}
