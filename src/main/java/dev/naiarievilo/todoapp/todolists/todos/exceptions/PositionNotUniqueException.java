package dev.naiarievilo.todoapp.todolists.todos.exceptions;

public class PositionNotUniqueException extends RuntimeException {

    public PositionNotUniqueException(Long todoId) {
        super("New position for to-do with id '" + todoId + "' is not unique in the list");
    }
}
