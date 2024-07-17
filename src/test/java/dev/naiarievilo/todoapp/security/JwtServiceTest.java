package dev.naiarievilo.todoapp.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.security.jwt.AccessTokenCreationFailedException;
import dev.naiarievilo.todoapp.security.jwt.JwtService;
import dev.naiarievilo.todoapp.users.User;
import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static dev.naiarievilo.todoapp.security.jwt.JwtService.TYPE_CLAIM;
import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.ACCESS_TOKEN;
import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.REFRESH_TOKEN;
import static dev.naiarievilo.todoapp.security.jwt.TokenTypes.REFRESH_ACCESS;
import static dev.naiarievilo.todoapp.security.jwt.TokenTypes.USER_ACCESS;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String JWT_ISSUER = "testApp";
    private static final String JWT_SECRET = "jwtSecret";

    private final JwtService jwtService;

    private User user;
    private JWTVerifier jwtVerifier;
    private String expiredAccessToken;

    JwtServiceTest() {
        jwtService = new JwtService(JWT_SECRET, JWT_ISSUER);
    }

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setName(ROLE_USER.name());

        user = new User();
        user.setId(USER_ID_1);
        user.setEmail(EMAIL_1);
        user.setPassword(PASSWORD_1);
        user.addRole(userRole);
        user.setEnabled(true);
        user.setLocked(false);

        Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
        Instant now = Instant.now();
        expiredAccessToken = JWT.create()
            .withSubject(user.getId().toString())
            .withIssuer(JWT_ISSUER)
            .withIssuedAt(now.minusMillis(ACCESS_TOKEN.expirationInMillis()))
            .withClaim(TYPE_CLAIM, ACCESS_TOKEN.type())
            .withExpiresAt(now)
            .sign(algorithm);

        jwtVerifier = JWT.require(algorithm)
            .withIssuer(JWT_ISSUER)
            .withClaimPresence("sub")
            .withClaimPresence("iat")
            .withClaimPresence("exp")
            .withClaimPresence(TYPE_CLAIM)
            .build();
    }

    @Test
    @DisplayName("createAccessAndRefreshTokens(): Returns access and refresh tokens when authentication is not null")
    void createAccessAndRefreshTokens_UserPrincipalIsNotNull_CreatesAccessAndRefreshTokens() {
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);

        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertTrue(tokens.containsKey(ACCESS_TOKEN.key()) && tokens.containsKey(REFRESH_TOKEN.key()));

        String accessToken = tokens.get(ACCESS_TOKEN.key());
        String refreshToken = tokens.get(REFRESH_TOKEN.key());
        assertDoesNotThrow(() -> Validate.notBlank(accessToken));
        assertDoesNotThrow(() -> Validate.notBlank(refreshToken));

        DecodedJWT decodedAccessToken = jwtVerifier.verify(accessToken);
        DecodedJWT decodedRefreshToken = jwtVerifier.verify(refreshToken);

        assertEquals(JWT_ISSUER, decodedAccessToken.getIssuer());
        assertEquals(JWT_ISSUER, decodedRefreshToken.getIssuer());

        String userId = user.getId().toString();
        assertEquals(userId, decodedAccessToken.getSubject());
        assertEquals(userId, decodedRefreshToken.getSubject());

        assertEquals(USER_ACCESS.value(), decodedAccessToken.getClaim(TYPE_CLAIM).asInt());
        assertEquals(REFRESH_ACCESS.value(), decodedRefreshToken.getClaim(TYPE_CLAIM).asInt());

        Instant accessTokenIssuedAt = decodedAccessToken.getIssuedAtAsInstant();
        Instant accessTokenExpiresAt = decodedAccessToken.getExpiresAtAsInstant();

        Instant refreshTokenIssuedAt = decodedRefreshToken.getIssuedAtAsInstant();
        Instant refreshTokenExpiresAt = decodedRefreshToken.getExpiresAtAsInstant();

        assertTrue(accessTokenExpiresAt.isBefore(refreshTokenExpiresAt));
        assertEquals(
            Duration.ofMinutes(ACCESS_TOKEN.expirationInMinutes()),
            Duration.between(accessTokenIssuedAt, accessTokenExpiresAt)
        );
        assertEquals(
            Duration.ofDays(REFRESH_TOKEN.expirationInDays()),
            Duration.between(refreshTokenIssuedAt, refreshTokenExpiresAt)
        );
    }

    @Test
    @DisplayName("createAccessToken(): Throws `AccessTokenCreationFailedException` when access token is not expired")
    void createAccessToken_AccessTokenNotExpired_ThrowsAccessTokenCreationFailedException() {
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);
        String accessToken = tokens.get(ACCESS_TOKEN.key());
        String refreshToken = tokens.get(REFRESH_TOKEN.key());

        assertThrows(AccessTokenCreationFailedException.class, () ->
            jwtService.createAccessToken(accessToken, refreshToken));
    }

    @Test
    @DisplayName("createAccessToken(): Throws `JWTVerificationException` when access token is invalid due to reasons " +
        "other than its expiration")
    void createAccessToken_ExpiredAccessTokenInvalidForOtherReason_ThrowsJWTVerificationException() {
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);
        String refreshToken = tokens.get(REFRESH_TOKEN.key());

        assertThrows(JWTVerificationException.class,
            () -> jwtService.createAccessToken("invalidAccessTokenBesidesBeingExpired", refreshToken));
    }

    @Test
    @DisplayName("createAccessToken(): Creates access token when access token is expired and refresh token is valid")
    void createAccessToken_RefreshTokenIsValid_CreatesAccessToken() {
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);
        String refreshToken = tokens.get(REFRESH_TOKEN.key());
        DecodedJWT decodedRefreshToken = jwtVerifier.verify(refreshToken);

        String newAccessToken = jwtService.createAccessToken(expiredAccessToken, refreshToken);
        assertDoesNotThrow(() -> Validate.notBlank(newAccessToken));

        DecodedJWT decodedAccessToken = jwtVerifier.verify(newAccessToken);
        assertEquals(USER_ACCESS.value(), decodedAccessToken.getClaim(TYPE_CLAIM).asInt());
        assertEquals(decodedRefreshToken.getSubject(), decodedAccessToken.getSubject());
        assertEquals(decodedRefreshToken.getIssuer(), decodedAccessToken.getIssuer());

        Instant accessTokenIssuedAt = decodedAccessToken.getIssuedAtAsInstant();
        Instant accessTokenExpiresAt = decodedAccessToken.getExpiresAtAsInstant();
        assertEquals(
            Duration.ofMinutes(ACCESS_TOKEN.expirationInMinutes()),
            Duration.between(accessTokenIssuedAt, accessTokenExpiresAt)
        );
    }

    @Test
    @DisplayName("verifyToken(): Throws `JWTVerificationException` when token is not valid")
    void verifyToken_TokenIsNotValid_ThrowsJWTVerificationException() {
        assertThrows(JWTVerificationException.class, () -> jwtService.verifyToken("invalidToken", USER_ACCESS));
    }

    @Test
    @DisplayName("verifyToken(): Returns `DecodedJWT` when token is valid")
    void verifyToken_TokenIsValid_ReturnsDecodedJWT() {
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);
        String accessToken = tokens.get(ACCESS_TOKEN.key());

        DecodedJWT decodedJwt = jwtService.verifyToken(accessToken, USER_ACCESS);
        assertNotNull(decodedJwt);
    }

}
