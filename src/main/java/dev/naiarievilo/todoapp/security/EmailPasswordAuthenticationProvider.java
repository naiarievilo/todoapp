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
    private static final BadCredentialsException BAD_CREDENTIALS_EXCEPTION =
        new BadCredentialsException(BAD_CREDENTIALS);
    private static final int MAX_LOGIN_ATTEMPTS_ALLOWED = 10;

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
            throw BAD_CREDENTIALS_EXCEPTION;
        }

        if (userService.isUserExpired(user)) {
            userService.deleteUser(user);
            throw BAD_CREDENTIALS_EXCEPTION;

        } else if (userService.isUserInactive(user)) {
            throw BAD_CREDENTIALS_EXCEPTION;

        } else if (user.getLoginAttempts() >= MAX_LOGIN_ATTEMPTS_ALLOWED) {
            userService.lockUser(user);
            throw BAD_CREDENTIALS_EXCEPTION;

        } else if (!passwordEncoder.matches(password, user.getPassword())) {
            userService.addLoginAttempt(user);
            throw BAD_CREDENTIALS_EXCEPTION;
        }

        userService.resetLoginAttempts(user);
        return new UserAuthenticationToken(user);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(EmailPasswordAuthenticationToken.class);
    }
}
