package dev.naiarievilo.todoapp.roles;

import static dev.naiarievilo.todoapp.ResponseConstants.FORBIDDEN;
import static dev.naiarievilo.todoapp.ResponseConstants.OK;

public class RoleControllerTestCaseMessages {

    static final String STATUS_200_GETS_ALL_ROLES_WHEN_ROLES_PERSISTED_IN_DATABASE =
        OK + "Gets all roles when roles were persisted in database";
    static final String STATUS_403_RETURNS_FORBIDDEN_WHEN_AUTHENTICATED_USER_NOT_ADMIN =
        FORBIDDEN + "Returns forbidden when authenticated user is not admin";
}
