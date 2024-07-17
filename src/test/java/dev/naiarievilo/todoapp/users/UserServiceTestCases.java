package dev.naiarievilo.todoapp.users;

class UserServiceTestCases {

    static final String ADDS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL =
        "adds login attempt when user is not null";
    static final String AUTHENTICATES_USER_WHEN_USER_NOT_AUTHENTICATED =
        "authenticates user when user is not authenticated";
    static final String CREATES_USER_WHEN_USER_DOES_NOT_EXIST =
        "creates `User` when user does not exist";
    static final String DELETES_USER_WHEN_USER_EXISTS =
        "deletes `User` when user exists";
    static final String DISABLES_USER_WHEN_USER_ENABLED =
        "disables user when user is enabled";
    static final String DOES_NOT_AUTHENTICATE_USER_WHEN_USER_ALREADY_AUTHENTICATED =
        "does not authenticate user when user is already authenticated";
    static final String DOES_NOT_DISABLE_USER_WHEN_USER_ALREADY_DISABLED =
        "does not disable user when user already disabled";
    static final String DOES_NOT_ENABLE_USER_WHEN_USER_ALREADY_ENABLED =
        "does not enable user when user is already enabled";
    static final String DOES_NOT_LOCK_USER_WHEN_USER_ALREADY_LOCKED =
        "does not lock user when user is already locked";
    static final String DOES_NOT_UNLOCK_USER_WHEN_USER_ALREADY_UNLOCKED =
        "does not unlock user when user is already unlocked";
    static final String DOES_NOT_UPDATE_EMAIL_WHEN_NEW_EMAIL_NOT_NEW =
        "does not update user's email when new email is equal to current email";
    static final String DOES_NOT_UPDATE_PASSWORD_WHEN_NEW_PASSWORD_NOT_NEW =
        "does not update user's password when new password is equal to current password";
    static final String ENABLES_USER_WHEN_USER_DISABLED =
        "enables user when user is disabled";
    static final String LOCKS_USER_WHEN_USER_NOT_LOCKED =
        "locks user when user is not locked";
    static final String RESETS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL =
        "resets login attempt when user is not null";
    static final String RETURNS_FALSE_WHEN_USER_DOES_NOT_EXIST =
        "returns `false` when user does not exist";
    static final String RETURNS_TRUE_WHEN_USER_EXISTS =
        "returns `true` when user exists";
    static final String RETURNS_USER_WHEN_USER_EXISTS =
        "returns `User` when user exists";
    static final String THROWS_BAD_CREDENTIALS_WHEN_CURRENT_PASSWORD_INCORRECT =
        "throws `BadCredentialsException` when current password is incorrect";
    static final String THROWS_EMAIL_ALREADY_REGISTERED_WHEN_EMAIL_ALREADY_REGISTERED =
        "throws `EmailAlreadyRegisteredException` when new email is registered";
    static final String THROWS_USER_ALREADY_EXISTS_WHEN_USER_ALREADY_EXISTS =
        "throws `UserAlreadyExistsException` when user already exists";
    static final String THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST =
        "throws `UserNotFoundException` when user does not exist";
    static final String UNLOCKS_USER_WHEN_USER_LOCKED =
        "unlocks user when user is locked";
    static final String UPDATES_EMAIL_WHEN_NEW_EMAIL_NOT_REGISTERED =
        "updates user's email when new email is not already registered";
    static final String UPDATES_PASSWORD_WHEN_CURRENT_PASSWORD_CORRECT =
        "updates user's password when current password is correct";
}
