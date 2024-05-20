package dev.naiarievilo.todoapp.security;

import dev.naiarievilo.todoapp.users.UserService;
import org.apache.commons.lang3.Validate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.IS_INSTANCE_OF;

public class EmailPasswordAuthenticationProvider implements AuthenticationProvider {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public EmailPasswordAuthenticationProvider(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Validate.isInstanceOf(EmailPasswordAuthenticationToken.class, authentication, IS_INSTANCE_OF.message(
            "authentication", EmailPasswordAuthenticationToken.class.getName()));

        String email = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        UserPrincipal user = userService.loadUserByEmail(email);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Incorrect email and/or password");
        }

        authentication.setAuthenticated(true);
        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(EmailPasswordAuthenticationToken.class);
    }
}
