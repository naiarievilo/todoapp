package dev.naiarievilo.todoapp.validation;

import java.util.Arrays;

public class ValidationMessages {

    public static final String CONTAINS_NULL_ELEMENTS = "%s must not contain null elements";
    public static final String COULD_NOT_BE_VALIDATED = "%s could not be validated";
    public static final String DOES_NOT_MATCH = "%s does not match %s";
    public static final String IS_BLANK = "%s is null, length 0, or contains whitespace only";
    public static final String MUST_BE_PROVIDED = "%s must be provided";
    public static final String NOT_VALID = "%s is not valid";
    public static final String ONE_NOT_NULL = "Only one of %s must not be null";

    private ValidationMessages() {
    }

    public static String formatMessage(String message, String value) {
        return String.format(message, value);
    }

    public static String formatFieldNames(String[] fieldNames, String conjunction) {
        int length = fieldNames.length;
        fieldNames[length - 1] = conjunction + fieldNames[length - 1];
        String fieldsFormatted = Arrays.toString(fieldNames).replace("[", "").replace("]", "");

        if (length == 2) {
            return fieldsFormatted.replace(",", "");
        }
        return fieldsFormatted;
    }

    public static String formatMessage(String message, String firstValue, String secondValue) {
        return String.format(message, firstValue, secondValue);
    }
}
