package dev.naiarievilo.todoapp.roles;

public class RoleServiceTestCaseMessages {

    static final String CREATES_ROLE_WHEN_ROLE_DOES_NOT_EXIST =
        "creates role when role does not exist";
    static final String DELETES_ROLE_WHEN_ROLE_EXISTS =
        "deletes role when role exists";
    static final String RETURNS_ALL_ROLES_IN_DATABASE =
        "returns collection of `Role` stored in the database";
    static final String RETURNS_FALSE_WHEN_ROLE_DOES_NOT_EXIST =
        "returns `false` when role does not exist";
    static final String RETURNS_ROLES_WHEN_ROLES_EXIST =
        "returns collection of `Role` when roles exist";
    static final String RETURNS_ROLE_WHEN_ROLE_EXISTS =
        "returns `Role` when role exists";
    static final String RETURNS_TRUE_WHEN_ROLE_EXISTS =
        "returns `true` when role exists";
    static final String THROWS_ROLE_ALREADY_EXISTS_WHEN_ROLE_EXISTS =
        "throws `RoleAlreadyExistsException` when role already exists";
    static final String THROWS_ROLE_NOT_FOUND_WHEN_ONE_ROLE_DOES_NOT_EXIST =
        "throws `RoleNotFoundException` when one role does not exist";
    static final String THROWS_ROLE_NOT_FOUND_WHEN_ROLE_DOES_NOT_EXIST =
        "throws `RoleNotFoundException` when role does not exist";
}
