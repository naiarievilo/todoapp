package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.security.ErrorDetails;
import dev.naiarievilo.todoapp.todolists.exceptions.TodoListNotFoundException;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.TodoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TodoListControllerAdvice {

    @ExceptionHandler(TodoListNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDetails handleTodoListNotFoundException(TodoListNotFoundException e) {
        return new ErrorDetails(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(TodoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDetails handleTodoNotFoundException(TodoNotFoundException e) {
        return new ErrorDetails(HttpStatus.NOT_FOUND, e.getMessage());
    }

}
