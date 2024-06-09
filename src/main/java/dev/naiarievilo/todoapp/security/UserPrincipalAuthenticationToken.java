package dev.naiarievilo.todoapp.security;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class UserPrincipalAuthenticationToken extends AbstractAuthenticationToken {

    private final transient UserPrincipal principal;
    private final transient String credentials;

    public UserPrincipalAuthenticationToken(UserPrincipal principal) {
        super(principal.getAuthorities());
        super.setAuthenticated(true);
        this.credentials = principal.getPassword();
        this.principal = principal;
    }

    @Override
    public String getCredentials() {
        return credentials;
    }

    @Override
    public UserPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new IllegalArgumentException(
            "Authentication status for `UserPrincipalAuthenticationToken` instances cannot be modified" +
                "and should only represent authenticated users.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPrincipalAuthenticationToken token)) return false;

        EqualsBuilder eb = new EqualsBuilder();
        eb.append(principal, token.principal);
        return eb.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hb = new HashCodeBuilder();
        hb.append(principal);
        return hb.toHashCode();
    }
}
