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
    private static final int MAX_ATTEMPTS = 10;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public EmailPasswordAuthenticationProvider(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var email = (String) authentication.getPrincipal();
        var password = (String) authentication.getCredentials();

        User user;
        try {
            user = userService.getUserByEmail(email);
        } catch (UserNotFoundException ex) {
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }

        if (user.getIsLocked() || !user.getIsEnabled()) {
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }

        if (user.getFailedLoginAttempts() == MAX_ATTEMPTS) {
            user.setIsLocked(true);
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            userService.resetLoginAttempts(user);
            return new UserAuthenticationToken(user);
        }

        userService.addLoginAttempt(user);
        throw new BadCredentialsException(BAD_CREDENTIALS);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(EmailPasswordAuthenticationToken.class);
    }
}
