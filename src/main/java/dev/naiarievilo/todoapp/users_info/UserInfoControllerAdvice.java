package dev.naiarievilo.todoapp.users_info;

import dev.naiarievilo.todoapp.security.ErrorDetails;
import dev.naiarievilo.todoapp.users_info.exceptions.UserInfoAlreadyExistsException;
import dev.naiarievilo.todoapp.users_info.exceptions.UserInfoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = UserInfoController.class)
public class UserInfoControllerAdvice {

    @ExceptionHandler(UserInfoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDetails handleUserInfoNotFoundException(UserInfoNotFoundException e) {
        return new ErrorDetails(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(UserInfoAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDetails handleUserInfoAlreadyExistsException(UserInfoAlreadyExistsException e) {
        return new ErrorDetails(HttpStatus.CONFLICT, e.getMessage());
    }
}
