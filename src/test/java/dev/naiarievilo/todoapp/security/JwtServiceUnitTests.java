package dev.naiarievilo.todoapp.security;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.security.jwt.JwtService;
import dev.naiarievilo.todoapp.users.User;
import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.ACCESS_TOKEN;
import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.REFRESH_TOKEN;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceUnitTests {

    private static final String JWT_ISSUER = "testApp";
    private static final String JWT_SECRET = "jwtSecret";

    private final JwtService jwtService;
    private final JWTVerifier jwtVerifier;

    private User user;

    JwtServiceUnitTests() {
        jwtService = new JwtService(JWT_SECRET, JWT_ISSUER);

        try {
            Field jwtVerifierField = JwtService.class.getDeclaredField("jwtVerifier");
            jwtVerifierField.setAccessible(true);
            this.jwtVerifier = (JWTVerifier) jwtVerifierField.get(jwtService);
            assertNotNull(jwtVerifier);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setName(ROLE_USER.name());

        user = new User();
        user.setId(ID_1);
        user.setEmail(EMAIL_1);
        user.setPassword(PASSWORD_1);
        user.addRole(userRole);
        user.setEnabled(true);
        user.setLocked(false);
    }

    @Test
    @DisplayName("createAccessAndRefreshTokens(): Returns access and refresh tokens when authentication is not null")
    void createAccessAndRefreshTokens_UserPrincipalIsNotNull_CreatesAccessAndRefreshTokens() {
        String userId = user.getId().toString();
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);

        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertTrue(tokens.containsKey(ACCESS_TOKEN.key()) && tokens.containsKey(REFRESH_TOKEN.key()));

        String accessToken = tokens.get(ACCESS_TOKEN.key());
        assertDoesNotThrow(() -> Validate.notBlank(accessToken));
        String refreshToken = tokens.get(REFRESH_TOKEN.key());
        assertDoesNotThrow(() -> Validate.notBlank(refreshToken));

        assertDoesNotThrow(() -> jwtVerifier.verify(accessToken));
        DecodedJWT decodedAccessToken = jwtVerifier.verify(accessToken);
        assertDoesNotThrow(() -> jwtVerifier.verify(refreshToken));
        DecodedJWT decodedRefreshToken = jwtVerifier.verify(refreshToken);

        assertEquals(JWT_ISSUER, decodedAccessToken.getIssuer());
        assertEquals(JWT_ISSUER, decodedRefreshToken.getIssuer());

        assertEquals(userId, decodedAccessToken.getSubject());
        assertEquals(userId, decodedRefreshToken.getSubject());

        Instant accessTokenIssuedAt = decodedAccessToken.getIssuedAtAsInstant();
        Instant accessTokenExpiresAt = decodedAccessToken.getExpiresAtAsInstant();

        Instant refreshTokenIssuedAt = decodedRefreshToken.getIssuedAtAsInstant();
        Instant refreshTokenExpiresAt = decodedRefreshToken.getExpiresAtAsInstant();

        assertTrue(accessTokenExpiresAt.isBefore(refreshTokenExpiresAt));
        assertEquals(Duration.ofMinutes(ACCESS_TOKEN.expirationInMinutes()),
            Duration.between(accessTokenIssuedAt, accessTokenExpiresAt));
        assertEquals(Duration.ofDays(REFRESH_TOKEN.expirationInDays()),
            Duration.between(refreshTokenIssuedAt, refreshTokenExpiresAt));
    }

    @Test
    @DisplayName("createAccessToken(): Creates access token when refresh token is valid")
    void createAccessToken_RefreshTokenIsValid_CreatesAccessToken() {
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);
        String refreshToken = tokens.get(REFRESH_TOKEN.key());

        DecodedJWT decodedRefreshToken = jwtVerifier.verify(refreshToken);
        String userId = decodedRefreshToken.getSubject();

        String newAccessToken = jwtService.createAccessToken(refreshToken);
        assertDoesNotThrow(() -> Validate.notBlank(newAccessToken));
        assertDoesNotThrow(() -> jwtVerifier.verify(newAccessToken));

        DecodedJWT decodedAccessToken = jwtVerifier.verify(newAccessToken);
        assertEquals(userId, decodedAccessToken.getSubject());
        assertEquals(decodedRefreshToken.getIssuer(), decodedAccessToken.getIssuer());

        Instant accessTokenIssuedAt = decodedAccessToken.getIssuedAtAsInstant();
        Instant accessTokenExpiresAt = decodedAccessToken.getExpiresAtAsInstant();
        assertEquals(Duration.ofMinutes(ACCESS_TOKEN.expirationInMinutes()),
            Duration.between(accessTokenIssuedAt, accessTokenExpiresAt));
    }

    @Test
    @DisplayName("verifyToken(): Throws `JWTVerificationException` when token is not valid")
    void verifyToken_TokenIsNotValid_ThrowsJWTVerificationException() {
        assertThrows(JWTVerificationException.class, () -> jwtService.verifyToken("invalidToken"));
    }

    @Test
    @DisplayName("verifyToken(): Returns `DecodedJWT` when token is valid")
    void verifyToken_TokenIsValid_ReturnsDecodedJWT() {
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(user);
        String accessToken = tokens.get(ACCESS_TOKEN.key());

        DecodedJWT decodedJwt = jwtService.verifyToken(accessToken);
        assertNotNull(decodedJwt);
    }

}
