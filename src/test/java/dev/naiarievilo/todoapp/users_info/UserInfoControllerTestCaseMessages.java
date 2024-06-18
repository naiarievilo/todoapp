package dev.naiarievilo.todoapp.users_info;

import static dev.naiarievilo.todoapp.ResponseConstants.*;

public class UserInfoControllerTestCaseMessages {

    static final String STATUS_200_RETURNS_USER_INFO_DTO_WHEN_USER_INFO_EXISTS =
        OK + "Returns `UserInfoDTO` when user info exists";
    static final String STATUS_200_UPDATES_USER_INFO_WHEN_USER_INFO_EXISTS =
        OK + "Updates user info when user info exists";
    static final String STATUS_404_RETURNS_ERROR_MESSAGE_WHEN_USER_INFO_NOT_FOUND =
        NOT_FOUND + RETURNS_ERROR_MESSAGES_WHEN + "user info is not found";
}
