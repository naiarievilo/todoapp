package dev.naiarievilo.todoapp.security;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.naiarievilo.todoapp.roles.Role;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserServiceImpl;
import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static dev.naiarievilo.todoapp.roles.Roles.ROLE_USER;
import static dev.naiarievilo.todoapp.security.JwtConstants.ACCESS_TOKEN;
import static dev.naiarievilo.todoapp.security.JwtConstants.REFRESH_TOKEN;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceUnitTests {

    private static final Integer JWT_EXPIRATION = 30;
    private static final String JWT_ISSUER = "testApp";
    private static final Integer JWT_REFRESH_EXPIRATION = 7;
    private static final String JWT_SECRET = "jwtSecret";

    private final JwtService jwtService;
    private final JWTVerifier jwtVerifier;

    private Authentication authentication;
    private User user;

    JwtServiceUnitTests() {
        jwtService = new JwtService(JWT_SECRET, JWT_EXPIRATION, JWT_REFRESH_EXPIRATION, JWT_ISSUER);

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
        user.setId(ID);
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);
        user.addRole(userRole);
        user.setIsEnabled(true);
        user.setIsLocked(false);

        authentication = EmailPasswordAuthenticationToken.authenticated(user.getEmail(), user.getPassword(),
            UserServiceImpl.getRolesFromUser(user));
    }

    @Test
    @DisplayName("createAccessAndRefreshTokens(): Returns access and refresh tokens when authentication is not null")
    void createAccessAndRefreshTokens_UserPrincipalIsNotNull_CreatesAccessAndRefreshTokens() {
        String email = user.getEmail();
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(authentication);

        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertTrue(tokens.containsKey(ACCESS_TOKEN) && tokens.containsKey(REFRESH_TOKEN));

        String accessToken = tokens.get(ACCESS_TOKEN);
        assertDoesNotThrow(() -> Validate.notBlank(accessToken));
        String refreshToken = tokens.get(REFRESH_TOKEN);
        assertDoesNotThrow(() -> Validate.notBlank(refreshToken));

        assertDoesNotThrow(() -> jwtVerifier.verify(accessToken));
        DecodedJWT decodedAccessToken = jwtVerifier.verify(accessToken);
        assertDoesNotThrow(() -> jwtVerifier.verify(refreshToken));
        DecodedJWT decodedRefreshToken = jwtVerifier.verify(refreshToken);

        assertEquals(JWT_ISSUER, decodedAccessToken.getIssuer());
        assertEquals(JWT_ISSUER, decodedRefreshToken.getIssuer());

        assertEquals(email, decodedAccessToken.getSubject());
        assertEquals(email, decodedRefreshToken.getSubject());

        Instant accessTokenIssuedAt = decodedAccessToken.getIssuedAtAsInstant();
        Instant accessTokenExpiresAt = decodedAccessToken.getExpiresAtAsInstant();

        Instant refreshTokenIssuedAt = decodedRefreshToken.getIssuedAtAsInstant();
        Instant refreshTokenExpiresAt = decodedRefreshToken.getExpiresAtAsInstant();

        assertTrue(accessTokenExpiresAt.isBefore(refreshTokenExpiresAt));
        assertEquals(Duration.ofMinutes(JWT_EXPIRATION), Duration.between(accessTokenIssuedAt, accessTokenExpiresAt));
        assertEquals(Duration.ofDays(JWT_REFRESH_EXPIRATION),
            Duration.between(refreshTokenIssuedAt, refreshTokenExpiresAt));
    }

    @Test
    @DisplayName("createAccessToken(): Creates access token when refresh token is valid")
    void createAccessToken_RefreshTokenIsValid_CreatesAccessToken() {
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(authentication);
        String refreshToken = tokens.get(REFRESH_TOKEN);

        DecodedJWT decodedRefreshToken = jwtVerifier.verify(refreshToken);
        String email = decodedRefreshToken.getSubject();

        String newAccessToken = jwtService.createAccessToken(refreshToken);
        assertDoesNotThrow(() -> Validate.notBlank(newAccessToken));
        assertDoesNotThrow(() -> jwtVerifier.verify(newAccessToken));

        DecodedJWT decodedAccessToken = jwtVerifier.verify(newAccessToken);
        assertEquals(email, decodedAccessToken.getSubject());
        assertEquals(decodedRefreshToken.getIssuer(), decodedAccessToken.getIssuer());

        Instant accessTokenIssuedAt = decodedAccessToken.getIssuedAtAsInstant();
        Instant accessTokenExpiresAt = decodedAccessToken.getExpiresAtAsInstant();
        assertEquals(Duration.ofMinutes(JWT_EXPIRATION), Duration.between(accessTokenIssuedAt, accessTokenExpiresAt));
    }

    @Test
    @DisplayName("verifyToken(): Throws `JWTVerificationException` when token is not valid")
    void verifyToken_TokenIsNotValid_ThrowsJWTVerificationException() {
        assertThrows(JWTVerificationException.class, () -> jwtService.verifyToken("invalidToken"));
    }

    @Test
    @DisplayName("verifyToken(): Returns `DecodedJWT` when token is valid")
    void verifyToken_TokenIsValid_ReturnsDecodedJWT() {
        Map<String, String> tokens = jwtService.createAccessAndRefreshTokens(authentication);
        String accessToken = tokens.get(ACCESS_TOKEN);

        DecodedJWT decodedJwt = jwtService.verifyToken(accessToken);
        assertNotNull(decodedJwt);
    }

}
