package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.security.ErrorDetails;
import dev.naiarievilo.todoapp.todolists.exceptions.TodoListNotFoundException;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.ImmutableListException;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.PositionExceedsMaxAllowedException;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.PositionNotUniqueException;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.TodoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TodoListControllerAdvice {

    @ExceptionHandler({TodoListNotFoundException.class, TodoNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDetails handleResourceNotFoundExceptions(RuntimeException e) {
        return new ErrorDetails(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({
        PositionExceedsMaxAllowedException.class,
        PositionNotUniqueException.class,
        ImmutableListException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleBadUpdateExceptions(RuntimeException e) {
        return new ErrorDetails(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
