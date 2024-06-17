package dev.naiarievilo.todoapp.users;

import static dev.naiarievilo.todoapp.ResponseConstants.*;

class UserControllerTestCaseMessages {

    static final String STATUS_200_ADDS_ROLE_TO_USER_WHEN_AUTHENTICATED_USER_ADMIN =
        OK + "Adds role to user when authenticated user has admin role";
    static final String STATUS_200_AUTHENTICATES_USER_WHEN_CREDENTIALS_CORRECT =
        OK + "Authenticates user when credentials are valid and correct";
    static final String STATUS_200_DELETES_USER_WHEN_USER_EXISTS =
        OK + "Deletes user when user exists";
    static final String STATUS_200_REMOVES_ROLE_FROM_USER_WHEN_AUTHENTICATED_USER_ADMIN =
        OK + "Removes role from user when authenticated user has admin role";
    static final String STATUS_200_RETURNS_NEW_ACCESS_TOKEN_WHEN_REFRESH_TOKEN_VALID =
        OK + "Returns new access token when refresh token is valid";
    static final String STATUS_200_UPDATES_CREDENTIALS_WHEN_NEW_CREDENTIALS_VALID =
        OK + "Updates user credentials when new credentials are valid";
    static final String STATUS_200_UPDATES_EMAIL_WHEN_NEW_EMAIL_VALID_AND_NOT_REGISTERED =
        OK + "Updates user email to new email when email is valid and not registered";
    static final String STATUS_200_UPDATES_PASSWORD_WHEN_NEW_PASSWORD_VALID =
        OK + "Updates user password when new password is valid";
    static final String STATUS_201_CREATES_USER_WHEN_USER_DOES_NOT_EXIST =
        CREATED + "Creates new user when user does not exist";
    static final String STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_CREDENTIALS_INCORRECT =
        BAD_REQUEST + RETURNS_ERROR_MESSAGES_WHEN + "email and/or password are incorrect";
    static final String STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_DTO_NOT_VALID =
        BAD_REQUEST + RETURNS_ERROR_MESSAGES_WHEN + "request payload is not valid";
    static final String STATUS_401_RETURNS_ERROR_MESSAGE_WHEN_REFRESH_TOKEN_NOT_VALID =
        UNAUTHORIZED + RETURNS_ERROR_MESSAGES_WHEN + "refresh token is not valid";
    static final String STATUS_403_RETURNS_FORBIDDEN_WHEN_AUTHENTICATED_USER_NOT_ADMIN =
        FORBIDDEN + "returns forbidden when authenticated user does not have admin role";
    static final String STATUS_404_RETURNS_ERROR_MESSAGE_WHEN_USER_NOT_FOUND =
        NOT_FOUND + RETURNS_ERROR_MESSAGES_WHEN + "user is not found";
    static final String STATUS_409_RETURNS_ERROR_MESSAGE_WHEN_NEW_EMAIL_ALREADY_REGISTERED =
        CONFLICT + RETURNS_ERROR_MESSAGES_WHEN + "email is already registered";
    static final String STATUS_409_RETURNS_ERROR_MESSAGE_WHEN_USER_ALREADY_EXISTS =
        CONFLICT + RETURNS_ERROR_MESSAGES_WHEN + "user already exists";
}

