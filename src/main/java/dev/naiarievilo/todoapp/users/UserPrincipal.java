package dev.naiarievilo.todoapp.users;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public interface UserPrincipal extends UserDetails {

    String getEmail();

    Collection<? extends GrantedAuthority> getRoles();

}
