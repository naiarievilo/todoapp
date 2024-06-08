package dev.naiarievilo.todoapp.security;

public final class JwtConstants {

    public static final String ACCESS_TOKEN = "accessToken";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String JWT_NOT_VALID_OR_COULD_NOT_BE_PROCESSED =
        "JWT provided is not valid or could not be processed";
    public static final String REFRESH_TOKEN = "refreshToken";

    private JwtConstants() {
    }

}
