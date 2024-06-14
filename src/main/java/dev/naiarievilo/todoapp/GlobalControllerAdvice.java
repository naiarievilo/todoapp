package dev.naiarievilo.todoapp;

import com.auth0.jwt.exceptions.JWTVerificationException;
import dev.naiarievilo.todoapp.security.ErrorDetails;
import dev.naiarievilo.todoapp.validation.ValidationMessages;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Objects;

import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.JWT_NOT_VALID_OR_COULD_NOT_BE_PROCESSED;
import static dev.naiarievilo.todoapp.validation.ValidationMessages.COULD_NOT_BE_VALIDATED;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@ResponseBody
public class GlobalControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult validationResult = e.getBindingResult();
        List<String> validationErrorMessages = validationResult.getAllErrors().stream()
            .map(error -> {
                if (error instanceof FieldError fieldError) {
                    String field = fieldError.getField();
                    String message = fieldError.getDefaultMessage();
                    return ValidationMessages.formatMessage(
                        Objects.requireNonNullElse(message, COULD_NOT_BE_VALIDATED), field);
                }

                return error.getDefaultMessage();
            })
            .toList();

        return new ErrorDetails(HttpStatus.BAD_REQUEST, validationErrorMessages);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleBadCredentialsException(BadCredentialsException e) {
        return new ErrorDetails(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(JWTVerificationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorDetails handleJWTVerificationException() {
        return new ErrorDetails(HttpStatus.UNAUTHORIZED, JWT_NOT_VALID_OR_COULD_NOT_BE_PROCESSED);
    }
}
