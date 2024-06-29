package dev.naiarievilo.todoapp.todolists.exceptions;

public class TodoListNotFoundException extends RuntimeException {

    public TodoListNotFoundException(Long id) {
        super("To-do list with id '" + id + "' not found.");
    }
}
