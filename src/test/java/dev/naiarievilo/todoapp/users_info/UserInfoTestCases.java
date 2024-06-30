package dev.naiarievilo.todoapp.users_info;

public class UserInfoTestCases {

    static final String CREATES_USER_INFO_WHEN_USER_INFO_DOES_NOT_EXIST =
        "creates `UserInfo` when user info does not exist";
    static final String DELETES_USER_INFO_WHEN_USER_INFO_EXISTS =
        "deletes `UserInfo` when user info does not exist";
    static final String RETURNS_FALSE_WHEN_USER_INFO_DOES_NOT_EXIST =
        "returns `false` when user info does not exist";
    static final String RETURNS_TRUE_WHEN_USER_INFO_EXISTS =
        "returns `true` when user info exists";
    static final String RETURNS_USER_INFO_WHEN_INFO_EXISTS =
        "returns `UserInfo` when user info exists";
    static final String THROWS_USER_INFO_ALREADY_EXISTS_WHEN_INFO_ALREADY_EXISTS =
        "throws `UserInfoAlreadyExistsException` when user info already exists";
    static final String THROWS_USER_INFO_NOT_FOUND_WHEN_INFO_DOES_NOT_EXIST =
        "throws `UserInfoNotFoundException` when user info does not exist";
    static final String UPDATES_USER_INFO_WHEN_USER_INFO_EXISTS =
        "Updates `UserInfo` when user info exists";
}
