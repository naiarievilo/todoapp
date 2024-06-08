package dev.naiarievilo.todoapp.users.exceptions;

public class EmailAlreadyRegisteredException extends UserAlreadyExistsException {

    private static final String DEFAULT_MESSAGE = "Email is already registered";

    public EmailAlreadyRegisteredException() {
        super(DEFAULT_MESSAGE);
    }

    public EmailAlreadyRegisteredException(String email) {
        super("Email '" + email + "' is already registered under another user");
    }

}
