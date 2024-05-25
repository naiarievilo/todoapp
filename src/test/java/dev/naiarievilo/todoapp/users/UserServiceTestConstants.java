package dev.naiarievilo.todoapp.users;

class UserServiceTestConstants {

    // TEST VARIABLES
    static final String EMAIL = "johnDoe@example.com";
    static final String PASSWORD = "securePassword";
    static final String CONFIRM_PASSWORD = PASSWORD;
    static final String FIRST_NAME = "John";
    static final String LAST_NAME = "Doe";

    static final String NEW_EMAIL = "newEmail@example.com";
    static final String NEW_PASSWORD = "newSecurePassword";

    static final String EMPTY = "";
    static final String BLANK = "   ";

    // TEST CASES
    static final String ADDS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL =
        "Adds login attempt when user is not null";
    static final String ADDS_ROLE_WHEN_ROLE_NOT_ASSIGNED =
        "Adds role to user when role is not assigned";
    static final String CREATES_USER_WHEN_USER_NOT_REGISTERED =
        "Creates user when user is not registered";
    static final String DELETES_USER_WHEN_USER_REGISTERED =
        "Deletes user when user is registered";
    static final String DISABLES_USER_WHEN_USER_ENABLED =
        "Disables user when user is enabled";
    static final String DOES_NOT_ADD_ROLE_WHEN_ROLE_ALREADY_ASSIGNED =
        "Does not add role when role is already assigned to user";
    static final String DOES_NOT_DISABLE_USER_WHEN_USER_ALREADY_DISABLED =
        "Does not disable user when user is already disabled";
    static final String DOES_NOT_ENABLE_USER_WHEN_USER_ALREADY_ENABLED =
        "Does not enable user when user is already enabled";
    static final String DOES_NOT_LOCK_USER_WHEN_USER_ALREADY_LOCKED =
        "Does not lock user when user is already locked";
    static final String DOES_NOT_UNLOCK_USER_WHEN_ALREADY_UNLOCKED =
        "Does not unlock user when user is already unlocked";
    static final String ENABLES_USER_WHEN_USER_DISABLED =
        "Enables user when user is disabled";
    static final String LOCKS_USER_WHEN_USER_NOT_LOCKED =
        "Locks user when user is not locked";
    static final String REMOVES_ROLE_WHEN_ROLE_NOT_ASSIGNED =
        "Removes role from user when role is not assigned";
    static final String RESETS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL =
        "Resets login attempt when user is not null";
    static final String RETURNS_FALSE_WHEN_EMAIL_NOT_REGISTERED =
        "Returns false when email is not registered";
    static final String RETURNS_TRUE_WHEN_EMAIL_REGISTERED =
        "Returns true when email is registered";
    static final String RETURNS_PRINCIPAL_WHEN_EMAIL_REGISTERED =
        "Returns user principal when email is registered";
    static final String RETURNS_USER_WHEN_EMAIL_REGISTERED =
        "Returns user when email is registered";
    static final String RETURNS_USER_WHEN_PRINCIPAL_REGISTERED =
        "Returns user when principal is registered";
    static final String THROWS_EMAIL_ALREADY_REGISTERED_WHEN_EMAIL_REGISTERED =
        "'EmailAlreadyRegisteredException' is thrown when email is registered";
    static final String THROWS_ILLEGAL_ARGUMENT_WHEN_EMAIL_BLANK =
        "'IllegalArgumentException' is thrown when email is blank";
    static final String THROWS_ILLEGAL_ARGUMENT_WHEN_EMAIL_EMPTY =
        "'IllegalArgumentException' is thrown when email is empty";
    static final String THROWS_ILLEGAL_ARGUMENT_WHEN_NEW_EMAIL_EMPTY =
        "'IllegalArgumentException' is thrown when new email is empty";
    static final String THROWS_ILLEGAL_ARGUMENT_WHEN_NEW_EMAIL_BLANK =
        "'IllegalArgumentException' is thrown when new email is blank";
    static final String THROWS_ILLEGAL_ARGUMENT_WHEN_NEW_PASSWORD_EMPTY =
        "'IllegalArgumentException' is thrown when new password is empty";
    static final String THROWS_ILLEGAL_ARGUMENT_WHEN_NEW_PASSWORD_BLANK =
        "'IllegalArgumentException' is thrown when new password is blank";
    static final String THROWS_NULL_POINTER_WHEN_EMAIL_NULL =
        "'NullPointerException' is thrown when email is null";
    static final String THROWS_NULL_POINTER_WHEN_NEW_EMAIL_NULL =
        "'NullPointerException' is thrown when new email is null";
    static final String THROWS_NULL_POINTER_WHEN_NEW_PASSWORD_NULL =
        "'NullPointerException' is thrown when new password is null";
    static final String THROWS_NULL_POINTER_WHEN_PRINCIPAL_NULL =
        "'NullPointerException' is thrown when principal is null";
    static final String THROWS_NULL_POINTER_WHEN_ROLE_NULL =
        "'NullPointerException' is thrown when role is null";
    static final String THROWS_NULL_POINTER_WHEN_USER_CREATION_DTO_NULL =
        "'NullPointerException' is thrown when userCreationDTO is null";
    static final String THROWS_NULL_POINTER_WHEN_USER_NULL =
        "'NullPointerException' is thrown when user is null";
    static final String THROWS_USER_ALREADY_EXISTS_WHEN_USER_REGISTERED =
        "'UserAlreadyExistsException' is thrown when user is already registered";
    static final String THROWS_USER_NOT_FOUND_WHEN_PRINCIPAL_NOT_REGISTERED =
        "'UserNotFoundException' is thrown when principal is not registered";
    static final String THROWS_USER_NOT_FOUND_WHEN_USER_NOT_REGISTERED =
        "'UserNotFoundException' is thrown when user is not registered";
    static final String THROWS_USER_NOT_FOUND_WHEN_EMAIL_NOT_REGISTERED =
        "'UserNotFoundException' is thrown when email is not registered";
    static final String THROWS_USER_ROLE_REMOVAL_PROHIBITED_WHEN_USER_ROLE_REMOVED =
        "'UserRoleRemovalNotAllowedException' is thrown when trying to remove user role";
    static final String UNLOCKS_USER_WHEN_USER_LOCKED =
        "Unlocks user when user is locked";
    static final String UPDATES_EMAIL_WHEN_NEW_EMAIL_NOT_REGISTERED =
        "Updates user email when new email is not registered";
    static final String UPDATES_PASSWORD_WHEN_USER_REGISTERED =
        "Updates user password when user is registered";
}
