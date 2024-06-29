package dev.naiarievilo.todoapp.todolists.todo_groups.exceptions;

public class TodoGroupNotFoundException extends RuntimeException {

    public TodoGroupNotFoundException(Long id) {
        super("To-do group with id '" + id + "' not found.");
    }
}
