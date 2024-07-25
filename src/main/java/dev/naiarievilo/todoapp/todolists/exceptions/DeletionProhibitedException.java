package dev.naiarievilo.todoapp.todolists.exceptions;

import dev.naiarievilo.todoapp.todolists.ListTypes;

public class DeletionProhibitedException extends RuntimeException {

    public DeletionProhibitedException(ListTypes listType) {
        super("Deletion of " + listType.getType() + " list is prohibited");
    }
}
