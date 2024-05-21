package dev.naiarievilo.todoapp.security;

public final class JwtConstants {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String EMAIL_CLAIM = "email";
    public static final String ROLES_CLAIM = "roles";

    private JwtConstants() {
    }

}
