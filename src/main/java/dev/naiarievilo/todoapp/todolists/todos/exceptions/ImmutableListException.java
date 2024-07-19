package dev.naiarievilo.todoapp.todolists.todos.exceptions;

public class ImmutableListException extends RuntimeException {

    public ImmutableListException(String type) {
        super("List of type '" + type + "' is immutable");
    }
}
