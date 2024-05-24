package dev.naiarievilo.todoapp.validation;

public class ValidationErrorMessages {

    // Method validation error messages
    public static final String IS_INSTANCE_OF = "Parameter must be an instance of targeted class";
    public static final String NOT_BLANK = "Parameter must not be null, length 0, or contain whitespace only";
    public static final String NOT_EMPTY = "Parameter must not be null or have length or size of 0";
    public static final String NOT_NULL = "Parameter must not be null";
    public static final String NO_NULL_ELEMENTS = "Parameter must not be null or contain null elements";

    // DTO validation error messages
    public static final String EMAIL_MUST_BE_VALID = "Email provided must be valid";
    public static final String EMAIL_MUST_BE_PROVIDED = "Email must be provided";
    public static final String PASSWORD_MUST_BE_PROVIDED = "Password must be provided";
    public static final String PASSWORD_CONFIRMATION_MUST_BE_PROVIDED = "Password confirmation must be provided";
    public static final String FIRST_NAME_MUST_BE_PROVIDED = "First name must be provided";
    public static final String LAST_NAME_MUST_BE_PROVIDED = "Last name must be provided";

    private ValidationErrorMessages() {
    }
}
