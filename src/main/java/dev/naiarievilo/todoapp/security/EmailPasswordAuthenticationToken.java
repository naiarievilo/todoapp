package dev.naiarievilo.todoapp.security;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class EmailPasswordAuthenticationToken extends AbstractAuthenticationToken {

    private transient Object principal;
    private transient Object credentials;

    public EmailPasswordAuthenticationToken(Object principal, Object credentials,
        Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        super.setAuthenticated(true);

        this.principal = principal;
        this.credentials = credentials;
    }

    public EmailPasswordAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        super.setAuthenticated(true);

        this.principal = principal;
        this.credentials = null;
    }

    public EmailPasswordAuthenticationToken(Object principal, Object credentials) {
        super(null);
        super.setAuthenticated(false);

        this.principal = principal;
        this.credentials = credentials;
    }

    public static EmailPasswordAuthenticationToken unauthenticated(Object principal, Object credentials) {
        return new EmailPasswordAuthenticationToken(principal, credentials);
    }

    public static EmailPasswordAuthenticationToken authenticated(Object principal, Collection<?
        extends GrantedAuthority> authorities) {
        return new EmailPasswordAuthenticationToken(principal, authorities);
    }

    public static EmailPasswordAuthenticationToken authenticated(Object principal, Object credentials,
        Collection<? extends GrantedAuthority> authorities) {
        return new EmailPasswordAuthenticationToken(principal, credentials, authorities);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new IllegalArgumentException("Use `authenticated` static method or constructor with authorities instead");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailPasswordAuthenticationToken token)) return false;

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
