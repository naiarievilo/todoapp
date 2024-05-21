package dev.naiarievilo.todoapp.validation;

public class ValidationErrorMessages {

    public static final String IS_INSTANCE_OF = "Parameter must be an instance of targeted class";
    public static final String NOT_BLANK = "Parameter must not be null, length 0, or contain whitespace only";
    public static final String NOT_EMPTY = "Parameter must not be null or have length or size of 0";
    public static final String NOT_NULL = "Parameter must not be null";
    public static final String NO_NULL_ELEMENTS = "Parameter must not be null or contain null elements";

    private ValidationErrorMessages() {
    }
}
