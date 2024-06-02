package dev.naiarievilo.todoapp.roles;

public class RoleServiceTestCaseMessages {

    static final String CREATES_ROLE_WHEN_ROLE_DOES_NOT_EXIST =
        "Creates role when role does not exist";
    static final String DELETES_ROLE_WHEN_ROLE_EXISTS =
        "Deletes role when role exists";
    static final String RETURNS_ALL_ROLES_IN_DATABASE =
        "Returns collection of `Role` stored in the database";
    static final String RETURNS_FALSE_WHEN_ROLE_DOES_NOT_EXIST =
        "Returns `false` when role does not exist";
    static final String RETURNS_ROLES_WHEN_ROLES_EXIST =
        "Returns collection of `Role` when roles exist";
    static final String RETURNS_ROLE_WHEN_ROLE_EXISTS =
        "Returns `Role` when role exists";
    static final String RETURNS_TRUE_WHEN_ROLE_EXISTS =
        "Returns `true` when role exists";
    static final String THROWS_ROLE_ALREADY_EXISTS_WHEN_ROLE_EXISTS =
        "Throws `RoleAlreadyExistsException` when role already exists";
    static final String THROWS_ROLE_NOT_FOUND_WHEN_ONE_ROLE_DOES_NOT_EXIST =
        "Throws `RoleNotFoundException` when one role does not exist";
    static final String THROWS_ROLE_NOT_FOUND_WHEN_ROLE_DOES_NOT_EXIST =
        "Throws `RoleNotFoundException` when role does not exist";
}
