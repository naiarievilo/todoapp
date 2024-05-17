package dev.naiarievilo.todoapp.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface UserPrincipal {

    Long getId();

    String getEmail();

    String getPassword();

    Collection<GrantedAuthority> getRoles();

    Collection<GrantedAuthority> getAuthorities();

    boolean isLocked();

    boolean isEnabled();
}
