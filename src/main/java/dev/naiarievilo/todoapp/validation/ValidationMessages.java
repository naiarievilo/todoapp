package dev.naiarievilo.todoapp.validation;

import org.apache.commons.lang3.Validate;

public enum ValidationMessages {
    IS_INSTANCE_OF("%s must be an instance of %s"),
    NOT_BLANK("%s must not be blank"),
    NOT_EMPTY("%s must not be empty"),
    NOT_NULL("%s must not be null"),
    NO_NULL_ELEMENTS("%s must not be null or contain null elements");

    private final String message;

    ValidationMessages(String message) {
        this.message = message;
    }

    public String message(String target, String className) {
        Validate.notBlank(target, NOT_BLANK.message("target"));
        Validate.notBlank(className, NOT_BLANK.message("className"));

        return String.format(message, target, className);
    }

    public String message(String parameter) {
        Validate.notBlank(parameter, NOT_BLANK.message());
        return String.format(message, parameter);
    }

    public String message() {
        return String.format(message, "Parameter");
    }
}
