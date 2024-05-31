package dev.naiarievilo.todoapp.users_info;

public class UserInfoTestCaseMessages {

    static final String CREATES_USER_INFO_WHEN_USER_INFO_DOES_NOT_EXIST =
        "Creates `UserInfo` when user info does not exist";
    static final String DELETES_USER_INFO_WHEN_USER_INFO_EXISTS =
        "Deletes `UserInfo` when user info does not exist";
    static final String RETURNS_FALSE_WHEN_USER_INFO_DOES_NOT_EXIST =
        "Returns `false` when user info does not exist";
    static final String RETURNS_TRUE_WHEN_USER_INFO_EXISTS =
        "Returns `true` when user info exists";
    static final String RETURNS_USER_INFO_WHEN_INFO_EXISTS =
        "Returns `UserInfo` when user info exists";
    static final String THROWS_USER_INFO_ALREADY_EXISTS_WHEN_INFO_ALREADY_EXISTS =
        "Throws `UserInfoAlreadyExistsException` when user info already exists";
    static final String THROWS_USER_INFO_NOT_FOUND_WHEN_INFO_DOES_NOT_EXIST =
        "Throws `UserInfoNotFoundException` when user info does not exist";
    static final String UPDATES_AVATAR_URL_WHEN_USER_INFO_EXISTS =
        "Updates `avatarUrl` when user info exists";
    static final String UPDATES_FIRST_NAME_WHEN_USER_INFO_EXISTS =
        "Updates `firstName` when user info exists";
    static final String UPDATES_LAST_NAME_WHEN_USER_INFO_EXISTS =
        "Updates `lastName` when user info exists";
}
