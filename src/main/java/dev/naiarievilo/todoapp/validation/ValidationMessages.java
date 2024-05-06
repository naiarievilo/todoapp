package dev.naiarievilo.todoapp.validation;

public enum ValidationMessages {
    NOT_BLANK("Parameter must not be blank"),
    NOT_EMPTY("Parameter must not be empty"),
    NOT_NULL("Parameter must not be null"),
    NO_NULL_ELEMENTS("Parameter must not be null or contain null elements");

    private final String message;

    ValidationMessages(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
