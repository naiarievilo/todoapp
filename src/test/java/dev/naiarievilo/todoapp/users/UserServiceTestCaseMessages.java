package dev.naiarievilo.todoapp.users;

class UserServiceTestCaseMessages {

    static final String ADDS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL =
        "Adds login attempt when user is not null";
    static final String ADDS_ROLE_TO_USER_WHEN_ROLE_NOT_ASSIGNED =
        "Adds role to user when role is not assigned";
    static final String CREATES_USER_WHEN_USER_DOES_NOT_EXIST =
        "Creates `User` when user does not exist";
    static final String DELETES_USER_WHEN_USER_EXISTS =
        "Deletes `User` when user exists";
    static final String DISABLES_USER_WHEN_USER_ENABLED =
        "Disables user when user is enabled";
    static final String DOES_NOT_ADD_ROLE_WHEN_ROLE_ALREADY_ASSIGNED =
        "Doesn't add role to user when role is already assigned";
    static final String DOES_NOT_DISABLE_USER_WHEN_USER_ALREADY_DISABLED =
        "Does not disable user when user already disabled";
    static final String DOES_NOT_ENABLE_USER_WHEN_USER_ALREADY_ENABLED =
        "Does not enable user when user is already enabled";
    static final String DOES_NOT_LOCK_USER_WHEN_USER_ALREADY_LOCKED =
        "Does not lock user when user is already locked";
    static final String DOES_NOT_REMOVE_ROLE_WHEN_ROLE_NOT_ASSIGNED =
        "Does not remove role from user when role is not assigned";
    static final String DOES_NOT_UNLOCK_USER_WHEN_USER_ALREADY_UNLOCKED =
        "Does not unlock user when user is already unlocked";
    static final String ENABLES_USER_WHEN_USER_DISABLED =
        "Enables user when user is disabled";
    static final String LOCKS_USER_WHEN_USER_NOT_LOCKED =
        "Locks user when user is not locked";
    static final String REMOVES_ROLE_WHEN_ROLE_ASSIGNED_AND_REMOVABLE =
        "Removes role from user when role is assigned and removable";
    static final String RESETS_LOGIN_ATTEMPT_WHEN_USER_NOT_NULL =
        "Resets login attempt when user is not null";
    static final String RETURNS_FALSE_WHEN_USER_DOES_NOT_EXIST =
        "Returns `false` when user does not exist";
    static final String RETURNS_PRINCIPAL_WHEN_USER_EXISTS =
        "Returns `UserPrincipal` when user exists";
    static final String RETURNS_TRUE_WHEN_USER_EXISTS =
        "Returns `true` when user exists";
    static final String RETURNS_USER_WHEN_USER_EXISTS =
        "Returns `User` when user exists";
    static final String THROWS_BAD_CREDENTIALS_WHEN_CURRENT_PASSWORD_INCORRECT =
        "Throws `BadCredentialsException` when current password is incorrect";
    static final String THROWS_EMAIL_ALREADY_REGISTERED_WHEN_EMAIL_ALREADY_REGISTERED =
        "Throws `EmailAlreadyRegisteredException` when new email is registered";
    static final String THROWS_USER_ALREADY_EXISTS_WHEN_USER_ALREADY_EXISTS =
        "Throws `UserAlreadyExistsException` when user already exists";
    static final String THROWS_USER_NOT_FOUND_WHEN_USER_DOES_NOT_EXIST =
        "Throws `UserNotFoundException` when user does not exist";
    static final String THROWS_USER_ROLE_REMOVAL_PROHIBITED_WHEN_REMOVING_USER_ROLE =
        "Throws `UserRoleRemovalProhibitedException` when removing `ROLE_USER`";
    static final String UNLOCKS_USER_WHEN_USER_LOCKED =
        "Unlocks user when user is locked";
    static final String UPDATES_EMAIL_WHEN_NEW_EMAIL_NOT_REGISTERED =
        "Updates user's email when new email is not already registered";
    static final String UPDATES_PASSWORD_WHEN_CURRENT_PASSWORD_CORRECT =
        "Updates password when current password is correct";
}
