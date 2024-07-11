package dev.naiarievilo.todoapp.todolists.exceptions;

public class TodoListNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "List not found";

    public TodoListNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public TodoListNotFoundException(Long id) {
        super("To-do list with id '" + id + "' not found.");
    }
}
