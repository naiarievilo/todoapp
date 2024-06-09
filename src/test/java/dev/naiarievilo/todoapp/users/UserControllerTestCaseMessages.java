package dev.naiarievilo.todoapp.users;

public class UserControllerTestCaseMessages {

    static final String STATUS_200_AUTHENTICATES_USER_WHEN_CREDENTIALS_ARE_VALID =
        "[200 OK] Authenticates user when credentials are valid";
    static final String STATUS_200_RETURNS_NEW_ACCESS_TOKEN_WHEN_REFRESH_TOKEN_VALID =
        "[200 OK] Returns new access token when refresh token is valid";
    static final String STATUS_200_UPDATES_USER_EMAIL_WHEN_NEW_EMAIL_VALID_AND_NOT_REGISTERED =
        "[200 OK] Updates user email to new email when email is valid and not registered";
    static final String STATUS_201_CREATES_USER_WHEN_USER_DOES_NOT_EXIST =
        "[201 Created] Creates new user when user does not exist";
    static final String STATUS_204_DELETES_USER_WHEN_USER_EXISTS =
        "[204 NoContent] Deletes user when user exists";
    static final String STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_DTO_NOT_VALID =
        "[400 Bad Request] Returns error message when updateCredentialsDTO is not valid";
    static final String STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_EMAIL_OR_PASSWORD_INCORRECT =
        "[400 Bad Request] Returns error message when email and/or password are incorrect";
    static final String STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_NEW_EMAIL_NOT_VALID =
        "[400 Bad Request] Returns error message when email is not valid";
    static final String STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_USER_AUTHENTICATION_DTO_NOT_VALID =
        "[400 Bad Request] Returns error message when user-authentication DTO is not valid";
    static final String STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_USER_CREATION_DTO_NOT_VALID =
        "[400 Bad Request] Returns error message when user-creation DTO is not valid";
    static final String STATUS_401_RETURNS_ERROR_MESSAGE_WHEN_REFRESH_TOKEN_NOT_VALID =
        "[401 Unauthorized] Returns error message when refresh token is not valid";
    static final String STATUS_404_RETURNS_ERROR_MESSAGE_WHEN_USER_NOT_FOUND =
        "[404 Not Found] Returns error message when user is not found";
    static final String STATUS_409_RETURNS_ERROR_MESSAGE_WHEN_NEW_EMAIL_ALREADY_REGISTERED =
        "[409 Conflict] Returns error message when email is already registered";
    static final String STATUS_409_RETURNS_ERROR_MESSAGE_WHEN_USER_ALREADY_EXISTS =
        "[409 Conflict] Returns error message when user already exists";
}

