package dev.naiarievilo.todoapp.users;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface UserPrincipal {

    Long getId();

    String getEmail();

    String getPassword();

    Collection<GrantedAuthority> getRoles();

    Collection<GrantedAuthority> getAuthorities();

    boolean isAccountLocked();

    boolean isAccountEnabled();
}
