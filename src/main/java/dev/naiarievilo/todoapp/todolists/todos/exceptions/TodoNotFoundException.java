package dev.naiarievilo.todoapp.todolists.todos.exceptions;

public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(Long id) {
        super("To-do with id '" + id + "' not found.");
    }
}
