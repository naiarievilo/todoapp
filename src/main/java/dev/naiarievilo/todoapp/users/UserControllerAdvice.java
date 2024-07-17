package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.security.ErrorDetails;
import dev.naiarievilo.todoapp.security.jwt.AccessTokenCreationFailedException;
import dev.naiarievilo.todoapp.users.exceptions.EmailAlreadyRegisteredException;
import dev.naiarievilo.todoapp.users.exceptions.UserAlreadyExistsException;
import dev.naiarievilo.todoapp.users.exceptions.UserNotFoundException;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = UserController.class)
public class UserControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(UserControllerAdvice.class);

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDetails handleUserNotFoundException(UserNotFoundException e) {
        return new ErrorDetails(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({UserAlreadyExistsException.class, EmailAlreadyRegisteredException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDetails handleUserAlreadyExists(RuntimeException e) {
        return new ErrorDetails(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MessagingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDetails handleMessagingException(MessagingException e) {
        logger.warn(e.getMessage());
        return new ErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't send email verification message");
    }

    @ExceptionHandler(AccessTokenCreationFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleAccessTokenCreationFailedException(AccessTokenCreationFailedException e) {
        return new ErrorDetails(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
