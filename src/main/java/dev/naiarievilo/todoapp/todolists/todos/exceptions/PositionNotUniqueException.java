package dev.naiarievilo.todoapp.todolists.todos.exceptions;

public class PositionNotUniqueException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "To-dos must have unique positions within a list";

    public PositionNotUniqueException() {
        super(DEFAULT_MESSAGE);
    }
}
