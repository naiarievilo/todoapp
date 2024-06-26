package dev.naiarievilo.todoapp.security.jwt;

import java.time.Duration;

import static dev.naiarievilo.todoapp.security.jwt.TokenTypes.*;

public enum JwtTokens {
    ACCESS_TOKEN("accessToken", 1800000, USER_ACCESS),
    REFRESH_TOKEN("refreshToken", 604800000, USER_ACCESS),
    VERIFICATION_TOKEN("emailVerificationToken", 900000, USER_VERIFICATION),
    UNLOCK_TOKEN("unlockUserToken", 900000, USER_UNLOCKING),
    ENABLE_TOKEN("enableUserToken", 900000, USER_ENABLING);

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String JWT_NOT_VALID_OR_COULD_NOT_BE_PROCESSED = "JWT is not valid or could not be processed";
    public static final String JWT_REGEX = "^([\\w-]+\\.){2}[\\w-]+$";
    private final String key;
    private final Duration expiration;
    private final TokenTypes tokenType;

    JwtTokens(String key, long expirationInMillis, TokenTypes tokenType) {
        this.key = key;
        this.expiration = Duration.ofMillis(expirationInMillis);
        this.tokenType = tokenType;
    }

    public int type() {
        return tokenType.value();
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
