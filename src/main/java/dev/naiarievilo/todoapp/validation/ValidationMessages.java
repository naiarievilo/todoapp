package dev.naiarievilo.todoapp.validation;

public enum ValidationMessages {
    DESCRIPTION_NOT_BLANK("Description must not be blank"),
    EMAIL_NOT_BLANK("Email must not be blank"),
    PASSWORD_NOT_BLANK("Password must not be blank"),
    PERMISSION_NOT_NULL("Permission must not be null"),
    PERMISSIONS_NOT_NULL_OR_EMPTY("Permissions must not be null, contain null elements, or be empty"),
    ROLE_NOT_NULL("Role must not be null"),
    ROLES_NOT_NULL_OR_EMPTY("Roles must not be null, contain null elements, or be empty"),
    USER_NOT_NULL("User must not be null"),
    USERNAME_NOT_BLANK("Username must not be blank");

    private final String message;

    ValidationMessages(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
