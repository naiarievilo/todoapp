package dev.naiarievilo.todoapp.users;

import com.auth0.jwt.exceptions.JWTVerificationException;
import dev.naiarievilo.todoapp.security.ErrorDetails;
import dev.naiarievilo.todoapp.users.exceptions.EmailAlreadyRegisteredException;
import dev.naiarievilo.todoapp.users.exceptions.UserAlreadyExistsException;
import dev.naiarievilo.todoapp.users.exceptions.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

import static dev.naiarievilo.todoapp.security.JwtConstants.JWT_NOT_VALID_OR_COULD_NOT_BE_PROCESSED;

@RestControllerAdvice(basePackageClasses = UserController.class)
public class UserControllerAdvice {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDetails> handleConstraintViolationException(ConstraintViolationException e) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> validationErrorMessages = e.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .toList();
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST, validationErrorMessages);

        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleBadCredentialsException(BadCredentialsException e) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleUserNotFoundException(UserNotFoundException e) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleUserAlreadyExists(UserAlreadyExistsException e) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.CONFLICT, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ErrorDetails> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException e) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.CONFLICT, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<ErrorDetails> handleJWTVerificationException(JWTVerificationException e) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.UNAUTHORIZED, JWT_NOT_VALID_OR_COULD_NOT_BE_PROCESSED);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
    }

}
