package dev.naiarievilo.todoapp.todolists.todos.exceptions;

public class PositionExceedsMaxAllowedException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "To-do updating position exceeds maximum allowed";

    public PositionExceedsMaxAllowedException() {
        super(DEFAULT_MESSAGE);
    }

}
