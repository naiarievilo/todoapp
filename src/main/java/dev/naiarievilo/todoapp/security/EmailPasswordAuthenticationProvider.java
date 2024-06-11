package dev.naiarievilo.todoapp.security;

import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserService;
import dev.naiarievilo.todoapp.users.exceptions.UserNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class EmailPasswordAuthenticationProvider implements AuthenticationProvider {

    public static final String BAD_CREDENTIALS = "Incorrect email and/or password";
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public EmailPasswordAuthenticationProvider(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        User user;
        try {
            user = userService.getUserByEmail(email);
        } catch (UserNotFoundException ex) {
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            userService.addLoginAttempt(user);
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }

        userService.resetLoginAttempts(user);
        return new UserPrincipalAuthenticationToken(UserPrincipalImpl.fromUser(user));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(EmailPasswordAuthenticationToken.class);
    }
}
