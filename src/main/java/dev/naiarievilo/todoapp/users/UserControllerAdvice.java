package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.security.ErrorDetails;
import dev.naiarievilo.todoapp.users.exceptions.EmailAlreadyRegisteredException;
import dev.naiarievilo.todoapp.users.exceptions.UserAlreadyExistsException;
import dev.naiarievilo.todoapp.users.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = UserController.class)
@ResponseBody
public class UserControllerAdvice {

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

}
