package dev.naiarievilo.todoapp.security;

import org.apache.commons.lang3.Validate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.NOT_BLANK;

public class EmailPasswordAuthenticationToken implements Authentication {

    private static final String METHOD_NOT_SUPPORTED = "%s is not supported for the purpose of the present token";

    private final String principal;
    private final String credentials;
    private Boolean isAuthenticated;

    public EmailPasswordAuthenticationToken(String email, String password) {
        Validate.notBlank(email, NOT_BLANK.message("email"));
        Validate.notBlank(password, NOT_BLANK.message("password"));

        this.principal = email;
        this.credentials = password;
        this.isAuthenticated = false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        throw new UnsupportedOperationException(String.format(METHOD_NOT_SUPPORTED, "getAuthorities()"));
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getDetails() {
        throw new UnsupportedOperationException(String.format(METHOD_NOT_SUPPORTED, "getDetails()"));
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException(String.format(METHOD_NOT_SUPPORTED, "getName()"));
    }
}
