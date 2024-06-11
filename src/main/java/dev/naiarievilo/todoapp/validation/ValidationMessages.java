package dev.naiarievilo.todoapp.validation;

public class ValidationMessages {

    public static final String COULD_NOT_BE_VALIDATED = "%s could not be validated";
    public static final String DOES_NOT_MATCH = "%s confirmation does not match %s";
    public static final String IS_BLANK = "%s is null, length 0, or contains whitespace only";
    public static final String IS_EMPTY = "%s is null or has length or size of 0";
    public static final String MUST_BE_PROVIDED = "%s must be provided";
    public static final String NOT_INSTANCE_OF = "%s is not instance of %s";
    public static final String NOT_VALID = "%s is not valid";

    private ValidationMessages() {
    }

    public static String formatMessage(String message, String variable) {
        return String.format(message, formatVariableName(variable));
    }

    // Format DTO parameter [record] and field [class] names from `camelCase` to `Camel case`.
    public static String formatVariableName(String variableName) {
        String[] words = variableName.split("(?=[A-Z])");
        String formattedVariableName = String.join(" ", words);
        return formattedVariableName.substring(0, 1).toUpperCase().concat(formattedVariableName.substring(1));
    }

    public static String formatMessage(String message, String firstVariable, String secondVariable) {
        return String.format(
            message, formatVariableName(firstVariable), formatVariableName(secondVariable).toLowerCase());
    }
}
