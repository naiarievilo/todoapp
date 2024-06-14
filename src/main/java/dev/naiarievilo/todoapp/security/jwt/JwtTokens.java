package dev.naiarievilo.todoapp.security.jwt;

import java.time.Duration;

public enum JwtTokens {
    ACCESS_TOKEN("accessToken", 1800000),
    REFRESH_TOKEN("refreshToken", 604800000);


    public static final String BEARER_PREFIX = "Bearer ";
    public static final String JWT_NOT_VALID_OR_COULD_NOT_BE_PROCESSED = "JWT is not valid or could not be processed";
    public static final String JWT_REGEX = "^([\\w-]+\\.){2}[\\w-]+$";
    private final String key;
    private final Duration expiration;

    JwtTokens(String key, long expirationInMillis) {
        this.key = key;
        this.expiration = Duration.ofMillis(expirationInMillis);
    }

    public String key() {
        return key;
    }

    public Long expirationInMillis() {
        return expiration.toMillis();
    }

    public Long expirationInMinutes() {
        return expiration.toMinutes();
    }

    public Long expirationInDays() {
        return expiration.toDays();
    }
}
