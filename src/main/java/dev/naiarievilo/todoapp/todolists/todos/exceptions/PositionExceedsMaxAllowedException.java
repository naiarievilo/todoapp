package dev.naiarievilo.todoapp.todolists.todos.exceptions;

public class PositionExceedsMaxAllowedException extends RuntimeException {

    public PositionExceedsMaxAllowedException(Long todoId) {
        super("New position for to-do with id '" + todoId + "' exceeds maximum allowed");
    }

}
