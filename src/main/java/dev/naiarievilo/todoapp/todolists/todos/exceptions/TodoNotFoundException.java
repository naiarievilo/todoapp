package dev.naiarievilo.todoapp.todolists.todos.exceptions;

public class TodoNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "To-do not found";

    public TodoNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public TodoNotFoundException(Long id) {
        super("To-do with id '" + id + "' not found.");
    }
}
