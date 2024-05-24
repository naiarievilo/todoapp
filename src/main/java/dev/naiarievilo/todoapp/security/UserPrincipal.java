package dev.naiarievilo.todoapp.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

public interface UserPrincipal {

    Long getId();

    String getEmail();

    String getPassword();

    Set<GrantedAuthority> getAuthorities();

    boolean isLocked();

    boolean isEnabled();
}
