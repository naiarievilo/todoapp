package dev.naiarievilo.todoapp.users;

public class EmailAlreadyRegisteredException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Email is already registered";

    public EmailAlreadyRegisteredException() {
        super(DEFAULT_MESSAGE);
    }

}
